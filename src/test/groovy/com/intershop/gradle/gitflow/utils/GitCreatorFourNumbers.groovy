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

import org.eclipse.jgit.lib.Constants

class GitCreatorFourNumbers {

    static TestRepoCreator initGitRepo(File dir, String buildFileContent)  {
        TestRepoCreator creator = new TestRepoCreator(dir)
        if(! buildFileContent.isEmpty()) {
            //add build files
            creator.addBuildGroovyFile(buildFileContent)
        }

        def cMaster = creator.createCommits("master", 2)
        creator.createBranch("develop", cMaster)
        def cDevelop = creator.createCommits("develop", 2)

        creator.setBranch("master")
        creator.createBranch("hotfix/JIRA-1", cMaster)
        creator.createCommits("jira1", 2)

        creator.setBranch("develop")
        creator.createBranch("feature/JIRA-2", cDevelop)
        creator.createCommits("jira2", 3)

        return creator
    }

    static TestRepoCreator initTest1(File dir, String buildFileContent) {
        TestRepoCreator creator = initGitRepo(dir, buildFileContent)

        creator.merge("master", "hotfix/JIRA-1", "feature JIRA-1 merge")
        creator.setBranch("develop")
        def cDevelop = creator.createCommits("develop1", 2)
        creator.createBranch("feature/JIRA-3", cDevelop)
        creator.createCommits("jira3", 2)

        creator.merge("develop", "master", "sync after JIRA 1")

        return creator
    }

    static TestRepoCreator initTest2(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest1(dir, buildFileContent)

        creator.setBranch("feature/JIRA-2")
        creator.createCommits("jira21", 1)
        def cFeature2 = creator.merge("develop", "feature/JIRA-2", "feature JIRA-2 merge")

        creator.setBranch("develop")
        creator.createBranch("release/7.11.0", cFeature2)

        return creator
    }

    static TestRepoCreator initTest3(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest2(dir, buildFileContent)

        creator.setBranch("develop")
        creator.createCommits("develop2", 1)

        creator.setBranch("release/7.11.0")
        def cRelease = creator.createCommits("release7110", 2)

        creator.createBranch("hotfix/JIRA-4", cRelease)
        creator.setBranch("hotfix/JIRA-4")
        creator.createCommits("jira4hotfix", 2)

        return creator
    }

    static TestRepoCreator initTest4(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest3(dir, buildFileContent)

        creator.setBranch("develop")
        def cDevelop2 = creator.createCommits("develop3", 1)
        creator.createBranch("feature/JIRA-5", cDevelop2)
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira5", 2)

        creator.merge("release/7.11.0", "hotfix/JIRA-4", "merge hotfix to release 711")

        return creator
    }

    static TestRepoCreator initTest5(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest4(dir, buildFileContent)

        creator.merge("develop", "release/7.11.0", "merge release 711 to develop")
        def cRelease711M = creator.merge("master", "release/7.11.0", "merge release 711 to master")
        creator.createTag("version/7.11.0.0", "create release tag", cRelease711M)

        return creator
    }

    static TestRepoCreator initTest6(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest5(dir, buildFileContent)

        creator.removeBranch("release/7.11.0")

        return creator
    }

    static TestRepoCreator initTest7(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest6(dir, buildFileContent)

        creator.setBranch("master")
        creator.createBranch("hotfix/JIRA-6", Constants.HEAD)

        creator.setBranch("hotfix/JIRA-6")
        creator.createCommits("jira6", 2)

        creator.setBranch("develop")
        creator.createCommits("develop4", 2)
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira51", 2)

        return creator
    }

    static TestRepoCreator initTest8(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest7(dir, buildFileContent)

        creator.setBranch("feature/JIRA-3")
        creator.createCommits("jira32", 2)
        def cFeature3 = creator.merge("develop", "feature/JIRA-3", "feature JIRA-3 merge")

        creator.createBranch("release/7.11.1", cFeature3)
        creator.setBranch("release/7.11.1")
        creator.createCommits("release2", 1)

        creator.setBranch("hotfix/JIRA-6")
        creator.createCommits("jira61", 2)

        return creator
    }

    static TestRepoCreator initTest9(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest8(dir, buildFileContent)

        def cHotfix6 = creator.merge("master", "hotfix/JIRA-6",  "merge hotfix JIRA6 to master")
        creator.merge("develop", "hotfix/JIRA-6",  "merge hotfix JIRA6 to master")
        creator.setBranch("master")
        creator.createTag("version/7.11.0.1", "create release tag", cHotfix6)

        return creator
    }

    static TestRepoCreator initTest10(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest9(dir, buildFileContent)

        creator.merge("develop", "master",  "merge release 711 hotfix 6 to develop")
        creator.merge("release/7.11.1", "master",  "merge release 711 hotfix 6 to develop")

        return creator
    }

    static TestRepoCreator initTest11(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest10(dir, buildFileContent)

        creator.setBranch("develop")
        creator.createCommits("develop5", 2)

        creator.setBranch("release/7.11.1")
        creator.createCommits("release3", 2)

        creator.setBranch("develop")
        creator.createCommits("release2", 1)

        creator.merge("master", "release/7.11.1", "merge release 711 to master")

        return creator
    }

    static TestRepoCreator initTest12(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest11(dir, buildFileContent)

        creator.merge("develop", "release/7.11.1",  "merge release 711 to develop")
        creator.removeBranch("release/7.11.1")

        creator.setBranch("master")
        creator.createTag("version/7.11.1.0", "create release tag", Constants.HEAD)

        return creator
    }

    static TestRepoCreator initTest13(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest12(dir, buildFileContent)

        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira52", 2)
        def cFeature5 = creator.merge("develop", "feature/JIRA-5",  "merge feature JIRA5 to develop")

        creator.setBranch("master")
        creator.createBranch("release/7.11", Constants.HEAD)

        creator.setBranch("develop")
        creator.createBranch("release/7.12.0", cFeature5)
        creator.createCommits("release712", 2)

        return creator
    }

    static TestRepoCreator initTest14(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest13(dir, buildFileContent)

        creator.setBranch("release/7.11")
        def cRelease1 = creator.createCommits("release4", 2)

        creator.createBranch("hotfix/JIRA-7", cRelease1)
        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira7", 2)

        creator.setBranch("release/7.12.0")
        creator.createCommits("release7121", 2)

        return creator
    }

    static TestRepoCreator initTest15(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest14(dir, buildFileContent)

        def cRelease3 = creator.merge("master", "release/7.12.0", "merge release 711 to master")
        creator.merge("develop", "release/7.12.0",  "merge release 711 to develop")

        creator.removeBranch("release/7.12.0")

        creator.setBranch("master")
        creator.createTag("version/7.12.0.0", "create release tag", cRelease3)

        return creator
    }

    static TestRepoCreator initTest16(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest15(dir, buildFileContent)

        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira71", 2)

        creator.merge("release/7.11", "hotfix/JIRA-7",  "merge hotfix to release")

        return creator
    }

    static TestRepoCreator initTest17(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest16(dir, buildFileContent)

        creator.merge("master", "release/7.11", "merge release 711 to master")
        creator.merge("develop", "release/7.11",  "merge release 711 to develop")

        creator.setBranch("release/7.11")
        def cRelease4 = creator.createCommits("release7111", 1)
        creator.createTag("version/7.11.1.1", "create release tag", cRelease4)

        return creator
    }

    static TestRepoCreator initTest18(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest17(dir, buildFileContent)

        creator.setBranch("release/7.11")
        creator.createCommits("release7112", 2)

        return creator
    }
}
