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
package com.intershop.gradle.gitflow.utils

import com.intershop.release.version.Version
import com.intershop.release.version.VersionType
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.stream.Collectors

/**
 * Calculate the version from a Git Repository.
 * The repository uses GitFlow for the development.
 * The start parameter is the local source directory.
 * @constructor creates the service from a directory
 * @param directory local source directory
 *
 * @param versionType default value com.intershop.release.version.VersionType.fourDigits.
*/

class GitVersionService @JvmOverloads constructor(val directory: File,
                                                  val versionType: VersionType = VersionType.fourDigits) {

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    private var client: Git

    init {
        client = Git.open(directory)
    }

    private val repository: Repository by lazy {
        client.repository
    }

    /**
     * This is the current branch name.
     */
    val branch: String by lazy {
        repository.branch
    }

    // GitFlow configuration

    /**
     * Name of the main branch.
     * Default value is master.
     */
    var mainBranch: String = "master"

    /**
     * Name of the develop branch.
     * Default value is develop.
     */
    var developBranch: String = "develop"

    /**
     * Prefix of a hotfix branch.
     * Default value is hotfix.
     */
    var hotfixPrefix: String = "hotfix"

    /**
     * Prefix of a feature branch.
     * Default value is feature.
     */
    var featurePrefix: String = "feature"

    /**
     * Prefix of a release branch.
     * Default value is release.
     */
    var releasePrefix: String = "release"

    /**
     * Prefix of a version tag.
     * Default value is version.
     */
    var versionPrefix: String = "version"

    /**
     * Separator for prefix and branch name.
     * Default value is /.
     */
    var separator: String = "/"

    /**
     * Version can be set for development to local only.
     */
    var localOnly: Boolean = false

    /**
     * Default version.
     * Default value is Version.Builder(versionType).build().
     */
    var defaultVersion: Version = Version.Builder(versionType).build()

    private val changed: Boolean by lazy {
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

    /**
     * This is the calculated version of the Git repository.
     */
    val version: String by lazy {

        val tag = getVersionTagFrom(Constants.HEAD)
        var rv = ""

        if(! localOnly) {
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
                    rv = "${
                        getVersionForLocalChanges(
                            hotfixPrefix,
                            "local-${hotfixPrefix}"
                        )
                    }-${getBranchNameForVersion(hotfixPrefix, branch)}-SNAPSHOT"
                }
                branch.startsWith("${featurePrefix}${separator}") -> {
                    // version = feature/local-'branchname'-SNAPSHOT
                    rv = "${
                        getVersionForLocalChanges(
                            featurePrefix,
                            "local-${featurePrefix}"
                        )
                    }-${getBranchNameForVersion(featurePrefix, branch)}-SNAPSHOT"
                }
                branch.startsWith("${releasePrefix}${separator}") -> {
                    // version = 'branchname'-SNAPSHOT or increased version from tag ...
                    val vb = getLatestVersion()
                    rv = if (vb == defaultVersion) {
                        val branchName = getBranchNameForVersion(releasePrefix, branch)
                        val vrb = Version.forString(branchName, versionType)
                        getVersionForLocalChanges("${vrb}-SNAPSHOT", "${vrb}-local-SNAPSHOT")
                    } else {
                        val addMetaData = getVersionForLocalChanges("", "local")
                        if (addMetaData.isNotBlank()) {
                            vb.setBranchMetadata("${addMetaData}-SNAPSHOT").toString()
                        } else {
                            vb.toString()
                        }
                    }
                }
            }
        } else {
            rv = "local"
        }
        rv
    }

    /**
     * This is the calculated previous version from the Git repository.
     */
    val previousVersion: String ?by lazy {
        var pvstr: String? = null
        val pv = getPreviousVersionFromTags()
        if( pv != null) {
            pvstr = pv.toString()
        }
        pvstr
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

    private fun getMapFrom(type: String): MutableMap<ObjectId, List<Ref>> {
        val list: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(type)
        val branchTags: MutableMap<ObjectId, List<Ref>> =
            list.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))

        return branchTags
    }

    private fun getLastCommit(walk: RevWalk): RevCommit? {
        val startCommit = walk.parseCommit(repository.resolve(Constants.HEAD))
        walk.markStart(startCommit)
        val commit = walk.next()

        return commit;
    }

    private fun getVersionFromRef(ref: Ref, prefix: String, type: String): Version {
        val vStr = ref.name.substring(type.length).substring("${prefix}${separator}".length)
        return Version.forString(vStr, versionType)
    }

    private fun getLatestVersionFromBranches(): Version? {
        val branches: MutableMap<ObjectId, List<Ref>> = getMapFrom(Constants.R_HEADS)
        val walk = RevWalk(repository)
        var commit: RevCommit? = getLastCommit(walk)
        while( commit != null) {
            var branchRefs = branches[commit]

            if(branchRefs != null) {
                branchRefs.filter {
                    it.name.substring(Constants.R_HEADS.length).startsWith("${releasePrefix}${separator}")
                }.forEach { ref ->
                    return getVersionFromRef(ref, releasePrefix, Constants.R_HEADS)
                }
            }

            commit = walk.next()
        }

        return null
    }

    private fun getLatestVersionFromTags(): Version? {
        val tags: MutableMap<ObjectId, List<Ref>> = getMapFrom(Constants.R_TAGS)
        val walk = RevWalk(repository)
        var commit = getLastCommit(walk)

        while( commit != null) {
            var tagRefs = tags[commit]

            if(tagRefs != null) {
                tagRefs.forEach { ref ->
                    val branchVersion = getVersionFromRef(ref, versionPrefix, Constants.R_TAGS)
                    return if(versionType == VersionType.fourDigits) {
                        branchVersion.incrementHotfixVersion()
                    } else {
                        branchVersion.incrementPatchVersion()
                    }
                }
            }

            commit = walk.next()
        }

        return null
    }

    private fun getPreviousVersionFromTags(): Version? {
        var previousVersion: Version? = null

        if(branch == mainBranch || branch.startsWith("${releasePrefix}${separator}")) {

            val tags: MutableMap<ObjectId, List<Ref>> = getMapFrom(Constants.R_TAGS)
            val walk = RevWalk(repository)
            var commit = getLastCommit(walk)
            var found = 0
            var commitNo = 0


            while( commit != null && found < 2) {
                var tagRefs = tags[commit]
                if(tagRefs != null) {
                    tagRefs.forEach { ref ->
                        previousVersion = getVersionFromRef(ref, versionPrefix, Constants.R_TAGS)
                        found = if(commitNo > 0) { 2 } else { found +1 }
                    }
                }
                if(tagRefs == null && found == 0) {
                    ++commitNo
                }
                commit = walk.next()
            }

            if(found == 1) {
                previousVersion = null
            }

        } else {
            log.info("It is not possible to calculate a previous version from branch '{}'", branch)
        }

        return previousVersion
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
