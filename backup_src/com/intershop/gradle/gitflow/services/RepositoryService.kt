/*
 * Copyright 2020 Intershop Communications AG.
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
 *  limitations under the License.
 */
package com.intershop.gradle.gitflow.services

import com.intershop.gradle.gitflow.extension.ScmExtension
import com.intershop.gradle.gitflow.utils.BranchObject
import com.intershop.gradle.gitflow.utils.BranchType
import com.intershop.gradle.gitflow.version.AbstractBranchFilter
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.TransportCommand
import org.eclipse.jgit.api.errors.InvalidRemoteException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.SshTransport
import org.eclipse.jgit.transport.TagOpt
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * This is the container of all information from an existing
 * working copy without any access to the remote location of the project.
 */
open class RepositoryService(val projectDir: File, val extension: ScmExtension) {

    companion object {
        @JvmStatic
        private val log: Logger = LoggerFactory.getLogger(this::class.java.name)

        /**
         * Check of a GIT remote url.
         *
         * @returns true if the protocol is SSH.
         */
        fun isSSHGit(remoteUrl: String): Boolean {
            return (remoteUrl.startsWith("git@") || remoteUrl.startsWith("ssh://git@"))
        }
    }

    /**
     * Returns the GIT repository
     * implementation for the local repository.
     */
    val repository: Repository by lazy {
        RepositoryBuilder().readEnvironment().findGitDir(projectDir).build()
    }

    /**
     * Returns the GIT client implementation
     * for the local repository.
     */
    val client: Git by lazy {
        Git(repository)
    }

    /**
     * The base (stabilization) branch name of the current working copy.
     *
     * @property branchName
     */
    val branchName: String by lazy {
        repository.branch
    }

    /**
     * The base branch type of the current working copy.
     */
    val branchType: BranchType by lazy {
        when (branchName) {
            "master" -> BranchType.MASTER
            extension.prefixes.developBranch -> BranchType.DEVELOP
            else -> if(Regex(extension.prefixes.releaseBranchPattern).matches(branchName)) {
                        BranchType.RELEASE
                    } else {
                        BranchType.BRANCH
                    }
        }
    }

    /**
     * This is true, if the local working copy changed.
     *
     * @property changed
     */
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

    /**
     * This is true, if the feature branch contains a version.
     *
     * @property branchWithVersion
     */
    var branchWithVersion: Boolean = false

    /**
     * It returns the remote url, calculated from the properties of the working copy (read only).
     *
     * @property remoteUrl remote url
     */
    val remoteUrl: String by lazy {
        repository.config.getString("remote", "origin", "url") ?: ""
    }

    /**
     * The revision id from the working copy (read only).
     *
     * @property revID revision id
     */
    val revID: String by lazy {
        val id = getObjectId(Constants.HEAD)
        if(id != null) { id.name } else { "" }
    }

    /**
     * Returns a tag name from the current repo (read only).
     */
    val tagName: String by lazy {
        var rvTagName = ""
        val rw = RevWalk(repository)

        repository.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { ref: Ref ->
            if(ObjectId.toString(rw.parseCommit(ref.objectId).id) == revID) {
                rvTagName = ref.name.substring(Constants.R_TAGS.length)
            }
        }
        rw.dispose()
        if(Regex(extension.prefixes.tagPattern).matches(rvTagName)) { rvTagName } else { "" }
    }

    /**
     * Calculates the rev object from revision id string.
     *
     * @param rev
     * @return rev object from the Git repository
     */
    fun getObjectId( rev: String): RevObject {
        val id = repository.resolve(rev)
        val rw = RevWalk(client.repository)
        return rw.parseAny(id)
    }

    /**
     * Map with rev ids and assigned tag names.
     */
    fun getTagMap(branchFilter: AbstractBranchFilter): Map<String, BranchObject>  {
        //specify return value
        val rv = mutableMapOf<String, BranchObject>()

        // fetch all tags from repo
        if(remoteConfigAvailable) {
            fetchTagsCmd()
        }

        //specify walk
        val walk = RevWalk(repository)

        //check tags and calculate
        repository.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { ref: Ref ->
            val tagName = ref.name.substring(Constants.R_TAGS.length)
            val version = branchFilter.getVersionStr(tagName)
            if(! version.isNullOrBlank()) {
                val rc = walk.parseCommit(ref.objectId)
                rv[ObjectId.toString(rc)] = BranchObject(ObjectId.toString(rc), version, tagName)
            }
        }

        walk.dispose()
        return rv
    }

    private val credentials: CredentialsProvider? by lazy {
        if(remoteUrl.startsWith("http") &&
                extension.user.nameIsAvailable &&
                extension.user.passwordIsAvailable) {
            log.debug("User name {} and password is used.", extension.user.name)
            UsernamePasswordCredentialsProvider(extension.user.name, extension.user.password)
        } else {
            null
        }
    }

    private val sshConnector: SSHConnector? by lazy {
        if (isSSHGit(remoteUrl) && extension.key.fileIsAvailable) {
            log.debug("ssh connector is used with key {}.", extension.key.file!!.absolutePath)
            SSHConnector(extension.key)
        } else {
            null
        }
    }

    /**
     * This variable returns true if the
     * remote configuration is available.
     *
     * @property remoteConfigAvailable
     */
    val remoteConfigAvailable: Boolean by lazy {
        remoteUrl.isNotEmpty() && (credentials != null || sshConnector != null)
    }

    /**
     * Fetch all tag information from remote branch
     * branch if necessary.
     */
    fun fetchTagsCmd() {
        try {
            // fetch tags
            val cmd = client.fetch()
            cmd.remote = "origin"
            cmd.setTagOpt(TagOpt.FETCH_TAGS)
            cmd.refSpecs = listOf(Transport.REFSPEC_TAGS)
            addCredentialsToCmd(cmd)
            cmd.call()
        } catch( nrex: InvalidRemoteException) {
            log.warn("No remote repository is available! {}", nrex.message)
        } catch( tex: TransportException) {
            tex.printStackTrace()
            log.warn("It was not possible to fetch all tags. Please check your credential configuration.", tex)
        }
    }

    /**
     * add credentials to the git command.
     *
     * @param cmd git command
     */
    fun addCredentialsToCmd( cmd: TransportCommand<*, *>) {
        // add credentials to command
        if (credentials != null) {
            cmd.setCredentialsProvider(credentials)
        } else if (sshConnector != null) {
            cmd.setTransportConfigCallback {
                (it as SshTransport).sshSessionFactory = sshConnector
            }
        }
    }
}
