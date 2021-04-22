/*
 * Copyright 2015 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.gitflow.tasks

import com.intershop.gradle.gitflow.extension.VersionExtension
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.filter.TreeFilter
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import com.intershop.release.version.Version
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.provideDelegate
import java.io.BufferedOutputStream
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * This is an helper task to show the create the changelog.
 */
open class CreateChangeLog @Inject constructor(objectFactory: ObjectFactory,
                                               projectLayout: ProjectLayout,): DefaultTask() {

    companion object {
        private const val HASHLENGTH = 7
    }

    private val changelogFileProperty: RegularFileProperty = objectFactory.fileProperty()
    private val prevVersionProperty: Property<String> = project.objects.property(String::class.java)

    init {
        description = "Creates a changelog based on Git information in Markdow format"

        prevVersionProperty.convention("")
        changelogFileProperty.convention(projectLayout.buildDirectory.file("changelog/changelog.md"))
    }

    private val extension = project.extensions.getByType(VersionExtension::class.java)
    private val repo = extension.versionService.repository

    /**
     * This is the provider for the targetVersion.
     */
    fun providePrevVersion(prevVersion: Provider<String>) = prevVersionProperty.set(prevVersion)

    /**
     * This is the property with command line option to specify
     * the start version of the change log calculation.
     *
     * @property preVersion
     */
    @get:Optional
    @set:Option(option= "prevVersion", description="Specifies a special version for the changelog creation.")
    @get:Input
    var preVersion: String
        get() = prevVersionProperty.get()
        set(value) = prevVersionProperty.set(value)

    /**
     * This method calculates the end version of the
     * report. It returns an JGit object.
     *
     * @property endRevObject
     */
    @get:Input
    val endRevObject: RevObject by lazy {
        val objectID = repo.resolve(Constants.HEAD)
        val rw = RevWalk(repo)
        rw.parseAny(objectID)
    }

    /**
     * Provider for output file property.
     */
    fun provideChangelogFile(changelogFile: Provider<RegularFile>) {
        changelogFileProperty.set(changelogFile)
    }

    /**
     * Input file property for output.
     *
     * @property changelogFile
     */
    @get:OutputFile
    var changelogFile: File
        get() = changelogFileProperty.get().asFile
        set(value) = changelogFileProperty.set(value)

    private fun calculateStart() : RevCommit {
        val headId = repo.resolve(extension.versionService.revID)

        val walk = RevWalk(repo)
        walk.sort(RevSort.TOPO)
        walk.markStart(walk.parseCommit(headId))

        var commit: RevCommit? = walk.next()
        var preCommit: RevCommit?
        do {
            preCommit = commit
            commit = walk.next()
        } while (commit != null)

        return walk.parseCommit(preCommit)
    }

    private fun getHeader(sourceVersion: String, targetVersion: String): String {
        return """
        # Change Log for $targetVersion

        This list contains changes since ${sourceVersion}.
        
        Created: ${LocalDateTime.now()}

        | Change | Type |
        | :--------------- | :----- |
        
        """.trimIndent()
    }

    private fun addFilesInCommit(changelogFile: File, commit: RevCommit) {
        val diffFmt = DiffFormatter( BufferedOutputStream(System.out) )
        diffFmt.setRepository(repo)
        diffFmt.pathFilter = TreeFilter.ANY_DIFF
        diffFmt.isDetectRenames = true

        val rw = RevWalk(repo)
        rw.parseHeaders(commit.getParent(0))

        val a = commit.getParent(0).tree
        val b = commit.tree

        diffFmt.scan(a, b).forEach {  e: DiffEntry ->
            changelogFile.appendText( processDiffEntry(e) )
        }
    }

    private fun processDiffEntry(e: DiffEntry): String {
        return when (e.changeType) {
            DiffEntry.ChangeType.ADD -> getFileLine(e.newPath, "A")
            DiffEntry.ChangeType.DELETE -> getFileLine(e.oldPath, "D")
            DiffEntry.ChangeType.MODIFY -> getFileLine(e.newPath, "M")
            DiffEntry.ChangeType.COPY -> getFileLine("${e.oldPath} ->\n${e.newPath} (${e.score})", "C")
            DiffEntry.ChangeType.RENAME -> getFileLine("${e.oldPath} ->\n${e.newPath} (${e.score})", "R")
            else -> "unknown change"
        }
    }

    private fun getFileLine(path: String, changeType: String): String {
        return "| +- `$path` | $changeType | \n"
    }

    private fun getMessageLine(message: String, rev: String): String {
        return "| **$message** | (${rev}) | \n"
    }

    /**
     * Main function of this task.
     */
    @TaskAction
    fun run() {
        changelogFile.parentFile.mkdirs()
        if (changelogFile.exists()) {
            changelogFile.delete()
        }

        changelogFile.createNewFile()

        val preVersStr: String? = prevVersionProperty.orNull

        if(! preVersStr.isNullOrBlank()) {
            changelogFile.appendText(getHeader(preVersStr, project.version.toString()))
        } else {
            changelogFile.appendText(getHeader("beginning", project.version.toString()))
        }

        val startRevObject = if(! preVersStr.isNullOrBlank()) {
            extension.versionService.getRevObjectFrom(Version.forString(preVersStr)) ?: calculateStart()
        } else {
            calculateStart()
        }

        val client = extension.versionService.client
        val refs = client.log().addRange(startRevObject,endRevObject).call()

        refs.forEach { rc ->
            changelogFile.appendText(getMessageLine(rc.fullMessage, rc.name.substring(0, HASHLENGTH)))
            addFilesInCommit(changelogFile, rc)
        }
        project.logger.info("Change log was written to {}", changelogFile.absolutePath)
    }
}
