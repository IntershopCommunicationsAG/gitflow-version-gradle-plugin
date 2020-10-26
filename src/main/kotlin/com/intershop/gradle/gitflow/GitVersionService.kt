/*
 * Copyright 2020 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intershop.gradle.gitflow

import com.intershop.release.version.Version
import com.intershop.release.version.VersionType
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.stream.Collectors

class GitVersionService(val directory: File) {

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    var client: Git

    init {
        client = Git.open(directory)
    }

    val repository: Repository by lazy {
        client.repository
    }

    val branch: String by lazy {
        repository.branch
    }

    var mainBranch: String = "master"

    var developBranch: String = "develop"

    var hotfixPrefix: String = "hotfix"

    var featurePrefix: String = "feature"

    var releasePrefix: String = "release"

    var versionPrefix: String = "version"

    var separator: String = "/"

    var defaultVersion: Version = Version.Builder(VersionType.fourDigits).build()

    val changed: Boolean by lazy {
        val status = client.status().call()
        val rv = status.untracked.size > 0 || status.uncommittedChanges.size > 0 ||
                status.removed.size > 0 || status.added.size > 0 ||
                status.changed.size > 0 || status.modified.size > 0

        if(log.isInfoEnabled && rv) {
            log.info("There are local changes on the repository.")
            if(status.untracked.size > 0) {
                status.untracked.forEach {
                    log.info("GIT: This file is not indexed {}", it)
                }
                status.removed.forEach {
                    log.info("GIT: This file is deleted {}", it)
                }
                status.added.forEach {
                    log.info("GIT: This file is added {}", it)
                }
                status.changed.forEach {
                    log.info("GIT: This file is changed {}", it)
                }
                status.modified.forEach {
                    log.info("GIT: This file is modified {}", it)
                }
                status.uncommittedChanges.forEach {
                    log.info("GIT: This file is uncommitted {}", it)
                }
            }
        }
        rv
    }

    val version: String by lazy {

        val tag = getVersionTagFrom(Constants.HEAD)
        var rv = ""
            when {
                tag.isNotBlank() -> {
                    rv = getVersionForLocalChanges(tag, "${tag}-local-SNAPSHOT")
                }
                branch == mainBranch -> {
                    val vm = getLatestVersion()
                    val addMetaData = getVersionForLocalChanges("", "local")

                    rv = when {
                        vm == defaultVersion && addMetaData.isEmpty() -> vm.setBranchMetadata("SNAPSHOT").toString()
                        addMetaData.isNotBlank() -> vm.setBranchMetadata("${addMetaData}-SNAPSHOT").toString()
                        else -> vm.toString()
                    }
                }
                branch == developBranch -> {
                    rv = "${getVersionForLocalChanges("dev", "local-dev")}-SNAPSHOT"
                }
                branch.startsWith("${hotfixPrefix}${separator}") -> {
                    // version = hotfix/local-'branchname'-SNAPSHOT
                    rv = "${getVersionForLocalChanges(hotfixPrefix, "local-${hotfixPrefix}")}-${getBranchNameForVersion(hotfixPrefix, branch)}-SNAPSHOT"
                }
                branch.startsWith("${featurePrefix}${separator}") -> {
                    // version = feature/local-'branchname'-SNAPSHOT
                    rv = "${getVersionForLocalChanges(featurePrefix, "local-${featurePrefix}")}-${getBranchNameForVersion(featurePrefix, branch)}-SNAPSHOT"
                }
                branch.startsWith("${releasePrefix}${separator}") -> {
                    // version = 'branchname'-SNAPSHOT or increased version from tag ...
                    val vb = getLatestVersion()
                    rv = if(vb == defaultVersion) {
                        val branchName = getBranchNameForVersion(releasePrefix, branch)
                        val vrb = Version.forString(branchName, VersionType.fourDigits)
                        getVersionForLocalChanges("${vrb}-SNAPSHOT", "${vrb}-local-SNAPSHOT")
                    } else {
                        val addMetaData = getVersionForLocalChanges("", "local")
                        if(addMetaData.isNotBlank()) {
                            vb.setBranchMetadata("${addMetaData}-SNAPSHOT").toString()
                        } else {
                            vb.toString()
                        }
                    }
                }
            }
        rv
    }

    private fun getBranchNameForVersion(prefix: String, branchName: String): String {
        return branchName.substring("${prefix}${separator}".length)
    }

    private fun getVersionForLocalChanges(version: String, alt: String): String {
        return if(changed) {
            alt
        } else {
            version
        }
    }

    private fun getVersionTagFrom(latestRef: String): String {
        var rvTagName = ""
        val rw = RevWalk(repository)

        repository.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { ref: Ref ->
            if(rw.parseCommit(ref.objectId).id.equals(getObjectId(repository, latestRef))) {
                rvTagName = ref.name.substring(Constants.R_TAGS.length)
            }
        }
        rw.dispose()

        return if(rvTagName.startsWith("${versionPrefix}${separator}")) {
            rvTagName.substring("${versionPrefix}${separator}".length)
        } else {
            ""
        }
    }

    private fun getLatestVersion(): Version {
        val branchesVersion  = getLatestVersionFromBranches()
        val tagVersion = getLatestVersionFromTags()

        if(tagVersion == null && branchesVersion != null) {
            return branchesVersion.setBranchMetadata("SNAPSHOT")
        }

        if(tagVersion != null && branchesVersion != null && tagVersion < branchesVersion) {
            return branchesVersion.setBranchMetadata("SNAPSHOT")
        }

        if(tagVersion != null) {
            return tagVersion.setBranchMetadata("SNAPSHOT")
        }

        return defaultVersion
    }

    private fun getLatestVersionFromBranches(): Version? {
        val branchList: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(Constants.R_HEADS)
        val branches: MutableMap<ObjectId, List<Ref>> = branchList.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))

        val walk = RevWalk(repository)
        val startCommit = walk.parseCommit(repository.resolve(Constants.HEAD))
        walk.markStart(startCommit)

        var commit = walk.next()
        while( commit != null) {
            var branchRefs = branches[commit]

            if(branchRefs != null) {
                branchRefs.filter {
                    it.name.substring(Constants.R_HEADS.length).startsWith("${releasePrefix}${separator}")
                }.forEach { ref ->
                    val vStr = ref.name.substring(Constants.R_HEADS.length).substring("${releasePrefix}${separator}".length)
                    return Version.forString(vStr, VersionType.fourDigits)
                }
            }

            commit = walk.next()
        }

        return null
    }

    private fun getLatestVersionFromTags(): Version? {
        val tagList: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(Constants.R_TAGS)
        val tags: MutableMap<ObjectId, List<Ref>> = tagList.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))

        val walk = RevWalk(repository)
        val startCommit = walk.parseCommit(repository.resolve(Constants.HEAD))
        walk.markStart(startCommit)

        var commit = walk.next()
        while( commit != null) {
            var tagRefs = tags[commit]

            if(tagRefs != null) {
                tagRefs.forEach { ref ->
                    val vStr = ref.name.substring(Constants.R_TAGS.length).substring("${versionPrefix}${separator}".length)
                    return Version.forString(vStr, VersionType.fourDigits).incrementHotfixVersion()
                }
            }

            commit = walk.next()
        }

        return null
    }

    private fun getObjectId(repository: Repository, rev: String): RevObject {
        val id = repository.resolve(rev)
        val rw = RevWalk(repository)
        return rw.parseAny(id)
    }

    @Throws(JGitInternalException::class)
    private fun getObjectIdFromRef(r: Ref): ObjectId {
        return try {
            var key: ObjectId? = repository.getRefDatabase().peel(r).getPeeledObjectId()
            if (key == null) {
                r.objectId
            } else {
                key
            }
        } catch (e: IOException) {
            throw JGitInternalException(e.message, e)
        }
    }
}