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

class GitCreatorChangedDefaultNames {

    static TestRepoCreator initGitRepo(File dir, String buildFileContent)  {
        TestRepoCreator creator = new TestRepoCreator(dir)
        if(! buildFileContent.isEmpty()) {
            //add build files
            creator.addBuildGroovyFile(buildFileContent)
        }

        creator.renameBranch("trunk")

        def cMaster = creator.createCommits("trunk", 2)
        creator.createBranch("prerelease", cMaster)
        def cDevelop = creator.createCommits("prerelease", 2)

        creator.setBranch("trunk")
        creator.createBranch("bugfix/JIRA-1", cMaster)
        creator.createCommits("jira1", 2)

        creator.setBranch("prerelease")
        creator.createBranch("story/JIRA-2", cDevelop)
        creator.createCommits("jira2", 3)

        return creator
    }

    static TestRepoCreator initTest1(File dir, String buildFileContent) {
        TestRepoCreator creator = initGitRepo(dir, buildFileContent)

        creator.merge("trunk", "bugfix/JIRA-1", "story JIRA-1 merge")
        creator.setBranch("prerelease")
        def cDevelop = creator.createCommits("prerelease1", 2)
        creator.createBranch("story/JIRA-3", cDevelop)
        creator.createCommits("jira3", 2)

        creator.merge("prerelease", "trunk", "sync after JIRA 1")

        return creator
    }

    static TestRepoCreator initTest2(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest1(dir, buildFileContent)

        creator.setBranch("story/JIRA-2")
        creator.createCommits("jira21", 1)
        def cFeature2 = creator.merge("prerelease", "story/JIRA-2", "story JIRA-2 merge")

        creator.setBranch("prerelease")
        creator.createBranch("prepared/4.0", cFeature2)

        return creator
    }

    static TestRepoCreator initTest3(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest2(dir, buildFileContent)

        creator.setBranch("prerelease")
        creator.createCommits("prerelease2", 1)

        creator.setBranch("prepared/4.0")
        def cRelease = creator.createCommits("prepared200", 2)

        creator.createBranch("bugfix/JIRA-4", cRelease)
        creator.setBranch("bugfix/JIRA-4")
        creator.createCommits("jira4bugfix", 2)

        return creator
    }

    static TestRepoCreator initTest4(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest3(dir, buildFileContent)

        creator.setBranch("prerelease")
        def cDevelop2 = creator.createCommits("prerelease3", 1)
        creator.createBranch("story/JIRA-5", cDevelop2)
        creator.setBranch("story/JIRA-5")
        creator.createCommits("jira5", 2)

        creator.merge("prepared/4.0", "bugfix/JIRA-4", "merge bugfix to prepared 711")

        return creator
    }

    static TestRepoCreator initTest5(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest4(dir, buildFileContent)

        creator.merge("prerelease", "prepared/4.0", "merge prepared 20 to prerelease")
        def cRelease711M = creator.merge("trunk", "prepared/4.0", "merge prepared 20 to trunk")
        creator.createTag("version/4.0.0", "create prepared tag", cRelease711M)

        return creator
    }

    static TestRepoCreator initTest6(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest5(dir, buildFileContent)

        creator.removeBranch("prepared/4.0")

        return creator
    }

    static TestRepoCreator initTest7(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest6(dir, buildFileContent)

        creator.setBranch("trunk")
        creator.createBranch("bugfix/JIRA-6", Constants.HEAD)

        creator.setBranch("bugfix/JIRA-6")
        creator.createCommits("jira6", 2)

        creator.setBranch("prerelease")
        creator.createCommits("prerelease4", 2)
        creator.setBranch("story/JIRA-5")
        creator.createCommits("jira51", 2)

        return creator
    }

    static TestRepoCreator initTest8(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest7(dir, buildFileContent)

        creator.setBranch("story/JIRA-3")
        creator.createCommits("jira32", 2)
        def cFeature3 = creator.merge("prerelease", "story/JIRA-3", "story JIRA-3 merge")

        creator.createBranch("prepared/4.1", cFeature3)
        creator.setBranch("prepared/4.1")
        creator.createCommits("prepared2", 1)

        creator.setBranch("bugfix/JIRA-6")
        creator.createCommits("jira61", 2)

        return creator
    }

    static TestRepoCreator initTest9(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest8(dir, buildFileContent)

        def cHotfix6 = creator.merge("trunk", "bugfix/JIRA-6",  "merge bugfix JIRA6 to trunk")
        creator.merge("prerelease", "bugfix/JIRA-6",  "merge bugfix JIRA6 to trunk")
        creator.setBranch("trunk")
        creator.createTag("version/4.0.1", "create prepared tag", cHotfix6)

        return creator
    }

    static TestRepoCreator initTest10(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest9(dir, buildFileContent)

        creator.merge("prerelease", "trunk",  "merge prepared 21 bugfix 6 to prerelease")
        creator.merge("prepared/4.1", "trunk",  "merge prepared 21 bugfix 6 to prerelease")

        return creator
    }

    static TestRepoCreator initTest11(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest10(dir, buildFileContent)

        creator.setBranch("prerelease")
        creator.createCommits("prerelease5", 2)

        creator.setBranch("prepared/4.1")
        creator.createCommits("prepared21", 2)

        creator.setBranch("prerelease")
        creator.createCommits("prepared22", 1)

        creator.merge("trunk", "prepared/4.1", "merge prepared 21 to trunk")

        return creator
    }

    static TestRepoCreator initTest12(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest11(dir, buildFileContent)

        creator.merge("prerelease", "prepared/4.1",  "merge prepared 21 to prerelease")
        creator.removeBranch("prepared/4.1")

        creator.setBranch("trunk")
        creator.createTag("version/4.1.0", "create prepared tag", Constants.HEAD)

        return creator
    }

    static TestRepoCreator initTest13(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest12(dir, buildFileContent)

        creator.setBranch("story/JIRA-5")
        creator.createCommits("jira52", 2)
        def cFeature5 = creator.merge("prerelease", "story/JIRA-5",  "merge story JIRA5 to prerelease")

        creator.setBranch("trunk")
        creator.createBranch("prepared/4", Constants.HEAD)

        creator.setBranch("prerelease")
        creator.createBranch("prepared/5.0", cFeature5)
        creator.createCommits("prepared30", 2)

        return creator
    }

    static TestRepoCreator initTest14(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest13(dir, buildFileContent)

        creator.setBranch("prepared/4")
        def cRelease1 = creator.createCommits("prepared4", 2)

        creator.createBranch("bugfix/JIRA-7", cRelease1)
        creator.setBranch("bugfix/JIRA-7")
        creator.createCommits("jira7", 2)

        creator.setBranch("prepared/5.0")
        creator.createCommits("prepared31", 2)

        return creator
    }

    static TestRepoCreator initTest15(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest14(dir, buildFileContent)

        def cRelease3 = creator.merge("trunk", "prepared/5.0", "merge prepared 3 to trunk")
        creator.merge("prerelease", "prepared/5.0",  "merge prepared 3 to prerelease")

        creator.removeBranch("prepared/5.0")

        creator.setBranch("trunk")
        creator.createTag("version/5.0.0", "create prepared tag", cRelease3)

        return creator
    }

    static TestRepoCreator initTest16(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest15(dir, buildFileContent)

        creator.setBranch("bugfix/JIRA-7")
        creator.createCommits("jira71", 2)

        creator.merge("prepared/4", "bugfix/JIRA-7",  "merge bugfix to prepared")

        return creator
    }

    static TestRepoCreator initTest17(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest16(dir, buildFileContent)

        creator.merge("trunk", "prepared/4", "merge prepared 2 to trunk")
        creator.merge("prerelease", "prepared/4",  "merge prepared 2 to prerelease")

        creator.setBranch("prepared/4")
        def cRelease4 = creator.createCommits("prepared210", 1)
        creator.createTag("version/4.1.1", "create prepared tag", cRelease4)

        return creator
    }

    static TestRepoCreator initTest18(File dir, String buildFileContent) {
        TestRepoCreator creator = initTest17(dir, buildFileContent)

        creator.setBranch("prepared/4")
        creator.createCommits("prepared212", 2)

        return creator
    }
}
