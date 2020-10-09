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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Constants.R_HEADS
import org.eclipse.jgit.lib.Constants.R_TAGS
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevWalk
import java.io.File
import java.io.IOException
import java.util.stream.Collectors


open class VersionRunner {

    lateinit var client: Git

    constructor(dirpath: String) {
        client = Git.open(File(dirpath))
    }

    constructor(dir: File) {
        client = Git.open(dir)
    }

    val repository: Repository by lazy {
        client.repository
    }

    fun run() {

            val branch = repository.branch

            val tag = getTag()

            System.out.println("Branch: " + branch)
            System.out.println("Tag: " + tag)
            getTagMap()
            //branchCommits()

    }

    fun getTag(): String {
        var rvTagName = ""
        val rw = RevWalk(repository)

        repository.refDatabase.getRefsByPrefix(Constants.R_TAGS).forEach { ref: Ref ->
            if(rw.parseCommit(ref.objectId).id.equals(getObjectId(repository, Constants.HEAD))) {
                rvTagName = ref.name.substring(Constants.R_TAGS.length)
            }
        }
        rw.dispose()

        return rvTagName
    }

    fun getTagMap() {

        val tagList: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(R_TAGS)
        val tags: MutableMap<ObjectId, List<Ref>> = tagList.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))

        val branchList: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(R_HEADS)
        val branches: MutableMap<ObjectId, List<Ref>> = branchList.stream().collect(Collectors.groupingBy(this::getObjectIdFromRef))


        val walk = RevWalk(repository)
        val startCommit = walk.parseCommit(repository.resolve(Constants.HEAD))
        walk.markStart(startCommit)

        var commit = walk.next()
        while( commit != null) {

            var hash = walk.parseCommit(commit)
            var tagRefs = tags[commit]

            if(tagRefs != null) {
                tagRefs.forEach { ref ->
                    println ("tag: " + hash.name + " " + ref.name.substring(R_TAGS.length))
                }
            }

            var branchRefs = branches[commit]

            if(branchRefs != null) {
                branchRefs.forEach { ref ->
                    println("branch: " + hash.name + " " + ref.name.substring(R_HEADS.length))
                }
            }

            commit = walk.next()
        }

        walk.dispose()
    }

    fun branchCommits() {
        val branchList: Collection<Ref> = repository.getRefDatabase().getRefsByPrefix(R_HEADS)
        val walk = RevWalk(repository)

        branchList.forEach {
            walk.reset()
            walk.markStart(walk.parseCommit(it.objectId))

            var c = walk.next()
            while(c != null) {
                println(" branch " + it + " commit ... " + c)
                c = walk.next()
            }


            /**
            var commit = walk.next()
            while( commit != null) {
                var hash = walk.parseCommit(commit)
                println("branch: " + it.name + " " + hash)
            }**/
        }
    }

    fun getObjectId(repository: Repository, rev: String): RevObject {
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