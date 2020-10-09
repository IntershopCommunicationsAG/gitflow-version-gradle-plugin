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
package com.intershop.gradle.gitflow.utils

import groovy.util.logging.Slf4j
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.api.TagCommand
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.lib.Constants

@Slf4j
class TestRepoCreator {

    private File repoDir
    private Git git

    TestRepoCreator(File directory) {
        repoDir = directory
        git = Git.init().setDirectory(repoDir).call()
        log.info("created repo: " + git.getRepository().getDirectory())
        git.add().addFilepattern(".").call()
        git.commit().setMessage("Initial commit").call()
        log.info("Committed repository " + git.getRepository().getDirectory());
    }

    String createCommits(String prefix, int count) {
        String rv = ""
        for(int i = 0; i < count; ++i) {
            String fileName = prefix + "_testfile" + i + ".test"

            File file = new File(git.getRepository().getDirectory().getParent(), fileName)
            if (! file.createNewFile()) {
                throw new IOException("Could not create file " + fileName)
            }
            git.add().addFilepattern(fileName).call();
            RevCommit commit = git.commit().setMessage("Commit file " + fileName).call()
            rv = commit.getName()
            log.info("Committed file " + fileName + " to repository at " + git.getRepository().getDirectory())
        }
        return rv
    }

    void createBranch(String branchName, String refName) {
        CreateBranchCommand cmd = git.branchCreate().setName(branchName).setStartPoint(refName).setForce(true)
        cmd.call()
        git.checkout().setName(branchName).call()
    }

    void setBranch(String branchName) {
        git.checkout().setName(branchName).call()
    }

    String merge(String targetBranch, String srcBranch, String msg) {
        String rv = ""

        setBranch(targetBranch)
        ObjectId mergeBase = git.repository.resolve(srcBranch)

        MergeResult merge = git.merge().include(mergeBase).setCommit(true).
                        setFastForward(MergeCommand.FastForwardMode.NO_FF).
                        setMessage("Merge " + msg).call()

        rv = merge.newHead.getName()
        log.info("Merge-Results for id: " + mergeBase + ": " + merge)
        return rv
    }

    String removeBranch(String branchName) {
        git.branchDelete().setBranchNames(Constants.R_HEADS + branchName).call();
    }

    void createTag(String tagname, String msg, String rev) {
        TagCommand cmd = git.tag()
        cmd.setName(tagname)

        ObjectId id = git.getRepository().resolve(rev)
        RevWalk rw = new RevWalk(git.getRepository())

        cmd.setObjectId(rw.parseAny(id))
        cmd.setMessage(msg)
        cmd.setAnnotated(true)
        cmd.setForceUpdate(false)

        Ref ref = cmd.call()
        log.info("Tag " + tagname + " created by jgit: " + ref)
    }
}
