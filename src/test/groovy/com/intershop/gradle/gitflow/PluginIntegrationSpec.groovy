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
package com.intershop.gradle.gitflow

import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import org.eclipse.jgit.lib.Constants
import spock.lang.Shared


class GitIntegrationSpec extends AbstractIntegrationGroovySpec {
    @Shared
    TestRepoCreator creator = new TestRepoCreator(testProjectDir)

    def 'test 1 - no release'() {
        given:

        def cMaster = creator.createCommits("master", 2)
        creator.createBranch("develop", cMaster)
        def cDevelop = creator.createCommits("develop", 2)

        creator.setBranch("master")
        creator.createBranch("hotfix/JIRA-1", cDevelop)
        creator.createCommits("jira1", 2)

        creator.setBranch("develop")
        creator.createBranch("feature/JIRA-2", cDevelop)
        creator.createCommits("jira2", 3)

        when:
        creator.setBranch("master")
        VersionRunner vr = new VersionRunner(testProjectDir.absolutePath)
        vr.run()

        then:
        true

        when:
        creator.setBranch("develop")
        vr = new VersionRunner(testProjectDir.absolutePath)
        vr.run()

        then:
        true

        when:
        creator.setBranch("hotfix/JIRA-1")
        vr = new VersionRunner(testProjectDir.absolutePath)
        vr.run()

        then:
        true

        when:
        creator.setBranch("feature/JIRA-2")
        vr = new VersionRunner(testProjectDir.absolutePath)
        vr.run()

        then:
        true
    }

    def createPrimaryGitRepo() {
        TestRepoCreator creator = new TestRepoCreator(testProjectDir)

        def cMaster = creator.createCommits("master", 2)
        creator.createBranch("develop", cMaster)
        def cDevelop = creator.createCommits("develop", 2)

        creator.setBranch("master")
        creator.createBranch("hotfix/JIRA-1", cDevelop)
        creator.createCommits("jira1", 2)

        creator.setBranch("develop")
        creator.createBranch("feature/JIRA-2", cDevelop)
        creator.createCommits("jira2", 3)

        creator.merge("master", "hotfix/JIRA-1", "feature JIRA-1 merge")
        creator.setBranch("develop")
        creator.createCommits("develop1", 2)
        creator.createBranch("feature/JIRA-3", cDevelop)
        creator.createCommits("jira3", 2)

        creator.merge("develop", "master", "sync after JIRA 1")

        creator.setBranch("feature/JIRA-2")
        creator.createCommits("jira21", 1)
        def cFeature2 = creator.merge("develop", "feature/JIRA-2", "feature JIRA-2 merge")

        creator.setBranch("develop")
        creator.createBranch("release/7.11", cFeature2)
        creator.createCommits("develop2", 1)

        creator.setBranch("release/7.11")
        def cRelease = creator.createCommits("release711", 2)

        creator.createBranch("hotfix/JIRA-4", cRelease)
        creator.setBranch("hotfix/JIRA-4")
        creator.createCommits("jira4hotfix", 2)

        creator.setBranch("develop")
        def cDevelop2 = creator.createCommits("develop3", 1)
        creator.createBranch("feature/JIRA-5", cDevelop2)
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira5", 2)

        creator.merge("release/7.11", "hotfix/JIRA-4", "merge hotfix to release 711")
        def cRelease711M = creator.merge("master", "release/7.11", "merge release 711 to master")

        creator.setBranch("master")
        creator.createTag("version/7.11.0.0", "create release tag", cRelease711M)
        creator.createBranch("hotifx/JIRA-6", Constants.HEAD)

        creator.setBranch("hotifx/JIRA-6")
        creator.createCommits("jira6", 2)

        creator.setBranch("develop")
        def cDevelop1 = creator.createCommits("develop4", 2)
        creator.createBranch("feature/JIRA-5", cDevelop1)
        creator.createCommits("develop5", 1)

        creator.setBranch("master")
        creator.merge("develop", "release/7.11",  "merge release 711 to develop")
        creator.removeBranch("release/7.11")

        creator.setBranch("feature/JIRA-3")
        creator.createCommits("jira32", 2)
        def cFeature3 = creator.merge("develop", "feature/JIRA-3", "feature JIRA-3 merge")
        creator.createBranch("release/7.11", cFeature3)
        creator.createCommits("release2", 1)

        creator.setBranch("hotifx/JIRA-6")
        creator.createCommits("jira61", 2)
        def cHotfix6 = creator.merge("master", "hotifx/JIRA-6",  "merge hotfix JIRA6 to master")
        creator.createTag("version/7.11.0.1", "create release tag", cHotfix6)

        creator.merge("develop", "master",  "merge release 711 hotfix 6 to develop")
        creator.merge("release/7.11", "master",  "merge release 711 hotfix 6 to develop")

        creator.setBranch("develop")
        creator.createCommits("develop5", 2)

        creator.setBranch("release/7.11")
        creator.createCommits("release3", 2)

        creator.setBranch("develop")
        creator.createCommits("release2", 1)

        def cRelease2 = creator.merge("master", "release/7.11", "merge release 711 to master")
        creator.createTag("version/7.11.1.0", "create release tag", cRelease2)

        creator.merge("develop", "release/7.11",  "merge release 711 to develop")
        creator.removeBranch("release/7.11")

        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira51", 2)
        def cFeature5 = creator.merge("develop", "feature/JIRA-5",  "merge feature JIRA5 to develop")

        creator.createBranch("release/7.11", cRelease2)
        creator.createBranch("hotfix/JIRA-7", cRelease2)

        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira7", 2)

        creator.createBranch("release/7.12", cFeature5)
        creator.createCommits("release712", 2)


        def cRelease3 = creator.merge("master", "release/7.12", "merge release 711 to master")
        creator.merge("develop", "release/7.12",  "merge release 711 to develop")

        creator.removeBranch("release/7.12")

        creator.setBranch("master")
        creator.createTag("version/7.12.0.0", "create release tag", cRelease3)

        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira71", 2)

        def cRelease4 = creator.merge("release/7.11", "hotfix/JIRA-7",  "merge hotfix to release")
        creator.merge("master", "hotfix/JIRA-7", "merge release 711 to master")
        creator.merge("develop", "hotfix/JIRA-7",  "merge release 711 to develop")

        creator.setBranch("release/7.11")
        creator.createTag("version/7.11.1.1", "create release tag", cRelease4)

    }

    def 'test git util creation'() {
        given:
            createPrimaryGitRepo(testProjectDir)
        when:
            VersionRunner vr = new VersionRunner(testProjectDir.absolutePath)
            vr.run()

        then:
            true
    }

    def 'test git with file dir'() {

        when:
        VersionRunner vr = new VersionRunner(testProjectDir.absolutePath)
        vr.run()

        then:
        true
    }
}
