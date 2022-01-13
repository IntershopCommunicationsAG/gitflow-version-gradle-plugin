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
import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
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

class GitVersionService @JvmOverloads constructor(
    private val directory: File,
    private val versionType: VersionType = VersionType.fourDigits) {

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(this::class.java.name)

        /**
         * Git prefix for heads.
         */
        const val headsPrefix = "refs/heads/"
    }

    /**
     * Git client for the existing working copy.
     */
    val client: Git = Git.open(directory)

    /**
     * Repository for the existing working copy.
     */
    val repository: Repository by lazy {
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
     * Version is shortened for branches and hotfixes
     * with an hash for the string if shortened is true.
     */
    var fullbranch: Boolean = false

    /**
     * Default version.
     * Default value is Version.Builder(versionType).build().
     */
    var defaultVersion: Version = Version.Builder(versionType).build()

    /**
     * Source Branch Name
     * Default value is an empty string.
     */
    var sourceBranch: String = ""

    /**
     * Pull Request ID
     * Default value is an empty string. Used to identify a merge build
     */
    var pullRequestID: String = ""

    /**
     * Build ID for Pull Requests and unique container version
     * Default value is an empty string. Used to identify a merge build
     */
    var buildID: String = ""

    /**
     * This must be set to true, if the version should be calculated for a pull request.
     * Therefore the following environment variables must be set: sourceBranch, pullRequestID, buildID
     */
    var isMergeBuild: Boolean = false

    /**
     * This must be set to true, if the Container version must be always unique.
     * Therefore the following environment variables must be set: uniqueContainer, buildID
     */
    var isUniqueVersion: Boolean = false

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

    private fun versionFromMainBranch(isContainer: Boolean) : String {
        val vm = getLatestVersion()
        val addMetaData = versionForLocalChanges("", "local")

        val uniqueID = if(isUniqueVersion) "id${buildID}-" else ""

        return when {
            vm == defaultVersion && addMetaData.isEmpty() && !isContainer ->
                vm.setBuildMetadata("${uniqueID}SNAPSHOT").toString()
            vm == defaultVersion && addMetaData.isEmpty() && isContainer ->
                vm.setBuildMetadata("${uniqueID}latest").toString()
            addMetaData.isNotBlank() && !isContainer ->
                vm.setBranchMetadata(addMetaData).setBuildMetadata("${uniqueID}SNAPSHOT").toString()
            addMetaData.isNotBlank() && isContainer ->
                vm.setBranchMetadata(addMetaData).setBuildMetadata("${uniqueID}latest").toString()

            else -> vm.toString()
        }
    }

    private fun versionForMergeBranch(isContainer: Boolean): String {
        if (sourceBranch.isEmpty() && pullRequestID.isEmpty()) {
            log.error("The source branch or the pull request ID is not specified," +
                       "but necessary because this should be a version for a pull request.")
            throw GradleException("Missing environment variables for pull request - source branch and ID")
        }
        if (buildID.isEmpty()) {
            log.warn("The build ID is not specified for the version calculation for a pull request")
        }
        val mBranchName = if (sourceBranch.startsWith(headsPrefix)) {
            sourceBranch.substring(headsPrefix.length)
        } else {
            sourceBranch
        }

        val bBranchName = when {
            mBranchName.startsWith(featurePrefix) -> {
                getBranchNameForVersion(featurePrefix, mBranchName)
            }
            mBranchName.startsWith(hotfixPrefix) -> {
                getBranchNameForVersion(hotfixPrefix, mBranchName)
            }
            mBranchName.startsWith(releasePrefix) -> {
                getBranchNameForVersion(releasePrefix, mBranchName)
            }
            mBranchName.startsWith(developBranch) -> {
                developBranch
            }
            else -> {
                "mergeversion"
            }
        }

        return if(isContainer) {
                    "${bBranchName}-pr${pullRequestID}-id${buildID}-latest"
                } else {
                    "${bBranchName}-pr${pullRequestID}-id${buildID}-SNAPSHOT"
                }
    }

    private fun versionFromDevBranch(isContainer: Boolean) : String {
        val uniqueID = if(isUniqueVersion) "-id${buildID}" else ""
        return if(isContainer) {
            "${versionForLocalChanges("dev", "local-dev")}${uniqueID}-latest"
        } else {
            "${versionForLocalChanges("dev", "local-dev")}${uniqueID}-SNAPSHOT"
        }
    }

    private fun versionFromHotfix(isContainer: Boolean) : String {
        val v = "${ versionForLocalChanges( "", "local-" 
                            )}${getBranchNameForVersion(hotfixPrefix, branch)}"
        return if(isContainer) { "${ v }-latest" } else { "${ v }-SNAPSHOT" }
    }

    private fun versionFromFeature(isContainer: Boolean) : String {
        val v = "${ versionForLocalChanges( "", "local-"
                            )}${getBranchNameForVersion(featurePrefix, branch)}"

        return if(isContainer) { "${ v }-latest" } else { "${ v }-SNAPSHOT" }
    }

    private fun versionFromRelease(isContainer: Boolean) : String {
        // version = 'branchname'-SNAPSHOT or increased version from tag ...
        val vb = getLatestVersion()
        return if (vb == defaultVersion) {
            val branchName = getBranchNameForVersion(releasePrefix, branch)
            val vrb = Version.forString(branchName, versionType)
            val v = versionForLocalChanges("${vrb}", "${vrb}-local")
            if(isContainer) { "${ v }-latest" } else { "${ v }-SNAPSHOT" }
        } else {
            val addMetaData = versionForLocalChanges("", "local")
            if (addMetaData.isNotBlank()) {
                if(isContainer) {
                    vb.setBranchMetadata("${addMetaData}-latest").toString()
                } else {
                    vb.setBranchMetadata("${addMetaData}-SNAPSHOT").toString()
                }
            } else {
                vb.toString()
            }
        }
    }

    /**
     * This is the calculated version of the Git repository.
     */
    val version: String by lazy {

        val tag = getVersionTagFrom(Constants.HEAD)
        var rv = ""

        if(! localOnly) {
            when {
                isMergeBuild -> {
                    rv = versionForMergeBranch(false)
                }
                tag.isNotBlank() -> {
                    rv = versionForLocalChanges(tag, "${tag}-local-SNAPSHOT")
                }
                branch == mainBranch -> {
                    rv = versionFromMainBranch(false)
                }
                branch == developBranch -> {
                    rv = versionFromDevBranch(false)
                }
                branch.startsWith("${hotfixPrefix}${separator}") -> {
                    rv = versionFromHotfix(false)
                }
                branch.startsWith("${featurePrefix}${separator}") -> {
                    // version = feature/local-'branchname'-SNAPSHOT
                    rv = versionFromFeature(false)
                }
                branch.startsWith("${releasePrefix}${separator}") -> {
                    rv = versionFromRelease(false)
                }
                else -> {
                    val branches = getBranchListForRef()
                    rv = when {
                        branches.contains(mainBranch) -> versionFromMainBranch(false)
                        branches.contains(developBranch) -> versionFromDevBranch(false)
                        branches.contains(releasePrefix) -> {
                            val bn = branches.first { it.startsWith("${releasePrefix}${separator}") }
                            getBranchNameForVersion(releasePrefix, bn)
                        }
                        else -> "version-SNAPSHOT"
                    }
                }
            }
        } else {
            rv = "LOCAL"
        }
        rv
    }

    /**
     * This is the calculated containerVersion of the Git repository.
     */
    val containerVersion: String by lazy {
        val tag = getVersionTagFrom(Constants.HEAD)
        var rv = ""

        if(! localOnly) {
            when {
                isMergeBuild -> {
                    rv = versionForMergeBranch(true)
                }
                tag.isNotBlank() -> {
                    rv = versionForLocalChanges(tag, "${tag}-local")
                }
                branch == mainBranch -> {
                    rv = versionFromMainBranch(true)
                }
                branch == developBranch -> {
                    rv = versionFromDevBranch(true)
                }
                branch.startsWith("${hotfixPrefix}${separator}") -> {
                    rv = versionFromHotfix(true)
                }
                branch.startsWith("${featurePrefix}${separator}") -> {
                    // version = feature/local-'branchname'-SNAPSHOT
                    rv = versionFromFeature(true)
                }
                branch.startsWith("${releasePrefix}${separator}") -> {
                    rv = versionFromRelease(true)
                }
                else -> {
                    val branches = getBranchListForRef()
                    rv = when {
                        branches.contains(mainBranch) -> versionFromMainBranch(true)
                        branches.contains(developBranch) -> versionFromDevBranch(true)
                        branches.contains(releasePrefix) -> {
                            val bn = branches.first { it.startsWith("${releasePrefix}${separator}") }
                            getBranchNameForVersion(releasePrefix, bn)
                        }
                        else -> "latest"
                    }
                }
            }
        }

        rv
    }

    /**
     * The revision id from the working copy (read only).
     *
     * @property revID revision id
     */
    val revID: String
        get() {
            val id = repository.resolve(Constants.HEAD)
            return if(id != null) { id.name } else { "" }
        }

    private fun getBranchListForRef() : List<String> {
        val reflist = repository.refDatabase.refs
        val branches = mutableListOf<String>()

        reflist.forEach {
            if(it.name.startsWith("refs/heads/")) {
                branches.add(it.name.substring(headsPrefix.length))
            }
        }
        return branches
    }

    /**
     * This is the calculated previous version from the Git repository.
     */
    val previousVersion: String? by lazy {
        var pvstr: String? = null
        val pv = getPreviousVersionFromTags()
        if( pv != null) {
            pvstr = pv.toString()
        }
        pvstr
    }

    /**
     * Calculates the commit for a specified version.
     *
     * @param version is the a version
     */
    fun getRevObjectFrom(version: Version) : RevCommit? {
        val tags: MutableMap<ObjectId, List<Ref>> = getMapFrom(Constants.R_TAGS)
        val walk = RevWalk(repository)
        var commit = getLastCommit(walk)
        var found = false

        var result: RevCommit? = null

        while( commit != null && ! found ) {
            val tagRefs = tags[commit]
            tagRefs?.filter {
                it.name.substring(Constants.R_TAGS.length).startsWith("${versionPrefix}${separator}")
            }?.forEach { ref ->
                if(version == getVersionFromRef(ref, versionPrefix, Constants.R_TAGS)) {
                    result = commit
                    found = true
                }
            }
            commit = walk.next()
        }

        return result
    }

    private fun getBranchNameForVersion(prefix: String, branchName: String): String {
        val bname = branchName.substring("${prefix}${separator}".length)
        val sname = bname.split("/".toRegex(), 2)
        val fname = if(sname.size > 1) {
                        sname[1].replace("/", "_").shortened()
                    } else {
                        sname[0].shortened()
                    }
        return fname
    }

    private fun String.shortened(): String {
        return if(! fullbranch) {
            val finder = "^#?\\d+".toRegex()
            val founded = finder.find(this)
            val number = if(founded != null) { founded.value.replace("#", "") } else { "" }

            return if(number.isNotEmpty() && this.length > number.length) {
                number + "." + this.substring(number.length + 1).sha().
                                replace("[a-z]".toRegex(), "").substring(0,10)
            } else {
                this.sha().replace("[a-z]".toRegex(), "").substring(0,10)
            }
        } else {
            this
        }
    }

    private fun String.sha(): String {
        val sha = MessageDigest.getInstance("SHA-1")
        return BigInteger(1, sha.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    private fun versionForLocalChanges(version: String, alt: String): String {
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
            return branchesVersion.setBuildMetadata("SNAPSHOT")
        }

        if(tagVersion != null && branchesVersion != null && tagVersion < branchesVersion) {
            return branchesVersion.setBuildMetadata("SNAPSHOT")
        }

        if(tagVersion != null) {
            return tagVersion.setBuildMetadata("SNAPSHOT")
        }

        return defaultVersion
    }

    private fun getMapFrom(type: String): MutableMap<ObjectId, List<Ref>> {
        val list: Collection<Ref> = repository.refDatabase.getRefsByPrefix(type)
        return list.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))
    }

    private fun getLastCommit(walk: RevWalk): RevCommit? {
        val startCommit = walk.parseCommit(repository.resolve(Constants.HEAD))
        walk.markStart(startCommit)
        return walk.next()
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
            val branchRefs = branches[commit]

            branchRefs?.filter {
                it.name.substring(Constants.R_HEADS.length).startsWith("${releasePrefix}${separator}")
            }?.forEach { ref ->
                return getVersionFromRef(ref, releasePrefix, Constants.R_HEADS)
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
            tags[commit]?.filter {
                it.name.substring(Constants.R_TAGS.length).startsWith("${versionPrefix}${separator}")
            }?.forEach { ref ->
                val branchVersion = getVersionFromRef(ref, versionPrefix, Constants.R_TAGS)
                return if (versionType == VersionType.fourDigits) {
                    branchVersion.incrementHotfixVersion()
                } else {
                    branchVersion.incrementPatchVersion()
                }
            }

            commit = walk.next()
        }

        return null
    }

    private fun getPreviousVersionFromTags(): Version? {
        var previousVersion: Version? = null

        val calcPrevVer = if(! branch.startsWith(featurePrefix) &&
            ! branch.startsWith(hotfixPrefix) &&
            ! branch.startsWith(releasePrefix) &&
            branch != developBranch &&  branch != mainBranch) {
            val list = getBranchListForRef()
            (list.contains(mainBranch) || list.filter { it.startsWith(releasePrefix) }.isNotEmpty())
        } else {
            (branch == mainBranch || branch.startsWith("${releasePrefix}${separator}"))
        }

        if(calcPrevVer) {

            val tags: MutableMap<ObjectId, List<Ref>> = getMapFrom(Constants.R_TAGS)
            val walk = RevWalk(repository)
            var commit = getLastCommit(walk)
            var found = 0
            var commitNo = 0


            while( commit != null && found < 2) {
                val tagRefs = tags[commit]
                tagRefs?.filter {
                    it.name.substring(Constants.R_TAGS.length).startsWith("${versionPrefix}${separator}")
                }?.forEach { ref ->
                        previousVersion = getVersionFromRef(ref, versionPrefix, Constants.R_TAGS)
                    found = if(commitNo > 0) { 2 } else { found +1 }
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
            repository.refDatabase.peel(r).peeledObjectId ?: r.objectId
        } catch (e: IOException) {
            throw JGitInternalException(e.message, e)
        }
    }
}
