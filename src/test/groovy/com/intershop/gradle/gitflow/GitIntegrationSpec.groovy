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
import groovy.util.logging.Slf4j
import org.eclipse.jgit.lib.Constants

@Slf4j
class GitIntegrationSpec extends AbstractIntegrationGroovySpec {

    TestRepoCreator createTest1(File dir)  {
        TestRepoCreator creator = new TestRepoCreator(dir)
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

    def addTest2(TestRepoCreator creator) {
        creator.merge("master", "hotfix/JIRA-1", "feature JIRA-1 merge")
        creator.setBranch("develop")
        def cDevelop = creator.createCommits("develop1", 2)
        creator.createBranch("feature/JIRA-3", cDevelop)
        creator.createCommits("jira3", 2)

        creator.merge("develop", "master", "sync after JIRA 1")
    }

    def addTest3(TestRepoCreator creator) {
        creator.setBranch("feature/JIRA-2")
        creator.createCommits("jira21", 1)
        def cFeature2 = creator.merge("develop", "feature/JIRA-2", "feature JIRA-2 merge")

        creator.setBranch("develop")
        creator.createBranch("release/7.11.0", cFeature2)
    }

    def addTest4(TestRepoCreator creator) {
        creator.setBranch("develop")
        creator.createCommits("develop2", 1)

        creator.setBranch("release/7.11.0")
        def cRelease = creator.createCommits("release7110", 2)

        creator.createBranch("hotfix/JIRA-4", cRelease)
        creator.setBranch("hotfix/JIRA-4")
        creator.createCommits("jira4hotfix", 2)
    }

    def addTest5(TestRepoCreator creator) {
        creator.setBranch("develop")
        def cDevelop2 = creator.createCommits("develop3", 1)
        creator.createBranch("feature/JIRA-5", cDevelop2)
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira5", 2)

        creator.merge("release/7.11.0", "hotfix/JIRA-4", "merge hotfix to release 711")
    }

    def addTest6(TestRepoCreator creator) {
        creator.merge("develop", "release/7.11.0", "merge release 711 to develop")
        def cRelease711M = creator.merge("master", "release/7.11.0", "merge release 711 to master")
        creator.createTag("version/7.11.0.0", "create release tag", cRelease711M)
    }

    def addTest61(TestRepoCreator creator) {
        creator.removeBranch("release/7.11.0")
    }

    def addTest7(TestRepoCreator creator) {
        creator.setBranch("master")
        creator.createBranch("hotfix/JIRA-6", Constants.HEAD)

        creator.setBranch("hotfix/JIRA-6")
        creator.createCommits("jira6", 2)

        creator.setBranch("develop")
        creator.createCommits("develop4", 2)
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira51", 2)
    }

    def addTest8(TestRepoCreator creator) {
        creator.setBranch("feature/JIRA-3")
        creator.createCommits("jira32", 2)
        def cFeature3 = creator.merge("develop", "feature/JIRA-3", "feature JIRA-3 merge")

        creator.createBranch("release/7.11.1", cFeature3)
        creator.setBranch("release/7.11.1")
        creator.createCommits("release2", 1)

        creator.setBranch("hotfix/JIRA-6")
        creator.createCommits("jira61", 2)
    }

    def addTest9(TestRepoCreator creator) {
        def cHotfix6 = creator.merge("master", "hotfix/JIRA-6",  "merge hotfix JIRA6 to master")
        creator.createTag("version/7.11.0.1", "create release tag", cHotfix6)
    }

    def addTest10(TestRepoCreator creator) {
        creator.merge("develop", "master",  "merge release 711 hotfix 6 to develop")
        creator.merge("release/7.11.1", "master",  "merge release 711 hotfix 6 to develop")
    }

    def addTest11(TestRepoCreator creator) {
        creator.setBranch("develop")
        creator.createCommits("develop5", 2)

        creator.setBranch("release/7.11.1")
        creator.createCommits("release3", 2)

        creator.setBranch("develop")
        creator.createCommits("release2", 1)

        creator.merge("master", "release/7.11.1", "merge release 711 to master")

    }

    def addTest12(TestRepoCreator creator) {
        creator.merge("develop", "release/7.11.1",  "merge release 711 to develop")
        creator.removeBranch("release/7.11.1")

        creator.setBranch("master")
        creator.createTag("version/7.11.1.0", "create release tag", Constants.HEAD)
    }

    def addTest13(TestRepoCreator creator) {
        creator.setBranch("feature/JIRA-5")
        creator.createCommits("jira52", 2)
        def cFeature5 = creator.merge("develop", "feature/JIRA-5",  "merge feature JIRA5 to develop")

        creator.setBranch("master")
        creator.createBranch("release/7.11", Constants.HEAD)

        creator.setBranch("develop")
        creator.createBranch("release/7.12.0", cFeature5)
        creator.createCommits("release712", 2)
    }

    def addTest14(TestRepoCreator creator) {
        creator.setBranch("release/7.11")
        def cRelease1 = creator.createCommits("release4", 2)

        creator.createBranch("hotfix/JIRA-7", cRelease1)
        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira7", 2)

        creator.setBranch("release/7.12.0")
        creator.createCommits("release7121", 2)
    }

    def addTest15(TestRepoCreator creator) {
        def cRelease3 = creator.merge("master", "release/7.12.0", "merge release 711 to master")
        creator.merge("develop", "release/7.12.0",  "merge release 711 to develop")

        creator.removeBranch("release/7.12.0")

        creator.setBranch("master")
        creator.createTag("version/7.12.0.0", "create release tag", cRelease3)
    }

    def addTest16(TestRepoCreator creator) {
        creator.setBranch("hotfix/JIRA-7")
        creator.createCommits("jira71", 2)

        creator.merge("release/7.11", "hotfix/JIRA-7",  "merge hotfix to release")
    }

    def addTest17(TestRepoCreator creator) {
        creator.merge("master", "release/7.11", "merge release 711 to master")
        creator.merge("develop", "release/7.11",  "merge release 711 to develop")

        creator.setBranch("release/7.11")
        def cRelease4 = creator.createCommits("release7111", 1)
        creator.createTag("version/7.11.1.1", "create release tag", cRelease4)
    }

    def addTest18(TestRepoCreator creator) {
        creator.setBranch("release/7.11")
        creator.createCommits("release7112", 2)
    }

    def 'test 01 - no releases available'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 1.0.0.0-SNAPSHOT (default version)'
        version == "1.0.0.0-SNAPSHOT"
        println(" - master -> 1.0.0.0-SNAPSHOT")

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on hotfix branch hotfix/JIRA-1'
        creator.setBranch("hotfix/JIRA-1")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is hotfix-<branch name>'
        version == "hotfix-JIRA-1-SNAPSHOT"
        println(" - hotfix/JIRA-1 -> hotfix-JIRA-1-SNAPSHOT")

        when: 'on feature branch feature/JIRA-2'
        creator.setBranch("feature/JIRA-2")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-2-SNAPSHOT"
        println(" - feature/JIRA-2 -> feature-JIRA-2-SNAPSHOT")
    }

    def 'test 02 - no release - hotfix 1 merge'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 1.0.0.0-SNAPSHOT (default version)'
        version == "1.0.0.0-SNAPSHOT"
        println(' - master -> 1.0.0.0-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(' - develop -> dev-SNAPSHOT')

        when: 'on feature branch feature/JIRA-2'
        creator.setBranch("feature/JIRA-2")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-2-SNAPSHOT"
        println(" - feature/JIRA-2 -> feature-JIRA-2-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")
    }

    def 'test 03 - no release - release  branch with hotfix'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 1.0.0.0-SNAPSHOT (default version)'
        version == "1.0.0.0-SNAPSHOT"
        println(' - master -> 1.0.0.0-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.0'
        creator.setBranch("release/7.11.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.0.0-SNAPSHOT'
        version == "7.11.0.0-SNAPSHOT"
        println(" - release/7.11.0 -> 7.11.0.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")
    }

    def 'test 04 - no release - release  branch merged hotfix'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 1.0.0.0-SNAPSHOT (default version)'
        version == "1.0.0.0-SNAPSHOT"
        println(' - master -> 1.0.0.0-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.0'
        creator.setBranch("release/7.11.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.0.0-SNAPSHOT'
        version == "7.11.0.0-SNAPSHOT"
        println(" - release/7.11.0 -> 7.11.0.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")

        when: 'on hotfix branch hotfix/JIRA-4'
        creator.setBranch("hotfix/JIRA-4")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then:
        version == "hotfix-JIRA-4-SNAPSHOT"
        println(" - hotfix/JIRA-4 -> hotfix-JIRA-4-SNAPSHOT")
    }

    def 'test 05 - release - release branch ready'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 1.0.0.0-SNAPSHOT (default version)'
        version == "1.0.0.0-SNAPSHOT"
        println(' - master -> 1.0.0.0-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.0'
        creator.setBranch("release/7.11.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.0.0-SNAPSHOT'
        version == "7.11.0.0-SNAPSHOT"
        println(" - release/7.11.0 -> 7.11.0.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 06 - release - release branch merged and taged, release branch still available'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.0'
        version == "7.11.0.0"
        println(' - master -> 7.11.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.0'
        creator.setBranch("release/7.11.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.0.0-SNAPSHOT'
        version == "7.11.0.0-SNAPSHOT"
        println(" - release/7.11.0 -> 7.11.0.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 06 - 1 - release and features, release branch removed'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest61(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.0'
        version == "7.11.0.0"
        println(' - master -> 7.11.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 07 - release, hotfix and features'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest61(creator)
        addTest7(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.0'
        version == "7.11.0.0"
        println(' - master -> 7.11.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on feature branch hotfix/JIRA-6'
        creator.setBranch("hotfix/JIRA-6")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is hotfix-<branch name>'
        version == "hotfix-JIRA-6-SNAPSHOT"
        println(" - hotfix/JIRA-6 -> hotfix-JIRA-6-SNAPSHOT")

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> feature-JIRA-3-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 08 - release, feature merged'(){
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.0'
        version == "7.11.0.0"
        println(' - master -> 7.11.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.1'
        creator.setBranch("release/7.11.1")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.1.0-SNAPSHOT'
        version == "7.11.1.0-SNAPSHOT"
        println(" - release/7.11.1 -> 7.11.1.0-SNAPSHOT")

        when: 'on feature branch hotfix/JIRA-6'
        creator.setBranch("hotfix/JIRA-6")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is hotfix-<branch name>'
        version == "hotfix-JIRA-6-SNAPSHOT"
        println(" - hotfix/JIRA-6 -> hotfix-JIRA-6-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 09 - releases, release branch and feature'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.1'
        version == "7.11.0.1"
        println(' - master -> 7.11.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.1'
        creator.setBranch("release/7.11.1")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.1.0-SNAPSHOT'
        version == "7.11.1.0-SNAPSHOT"
        println(" - release/7.11.1 -> 7.11.1.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 10 - new release branch ready'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.0.1'
        version == "7.11.0.1"
        println(' - master -> 7.11.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release branch release/7.11.1'
        creator.setBranch("release/7.11.1")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 7.11.1.0-SNAPSHOT'
        version == "7.11.1.0-SNAPSHOT"
        println(" - release/7.11.1 -> 7.11.1.0-SNAPSHOT")

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is feature-<branch name>'
        version == "feature-JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> feature-JIRA-5-SNAPSHOT")
    }

    def 'test 11 - release branch merged'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.1.0-SNAPSHOT'
        version == "7.11.1.0-SNAPSHOT"
        println(' - master -> 7.11.1.0-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
    }

    def 'test 12 - tagged und feature merged'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.1.0'
        version == "7.11.1.0"
        println(' - master -> 7.11.1.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
    }

    def 'test 13 - two release branches'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.1.0'
        version == "7.11.1.0"
        println(' - master -> 7.11.1.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.0'
        version == "7.11.1.0"
        println(' - on release/7.11 -> 7.11.1.0')

        when: 'on release/7.12'
        creator.setBranch("release/7.12.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.12.0.0-SNAPSHOT'
        version == "7.12.0.0-SNAPSHOT"
        println(' - master -> 7.12.0.0-SNAPSHOT')
    }

    def 'test 14 - two release branches and one hotfix branch'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)
        addTest14(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.11.1.0'
        version == "7.11.1.0"
        println(' - master -> 7.11.1.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.1-SNAPSHOT'
        version == "7.11.1.1-SNAPSHOT"
        println(' - on release/7.11 -> 7.11.1.1-SNAPSHOT')

        when: 'on release/7.12'
        creator.setBranch("release/7.12.0")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.12.0.0-SNAPSHOT'
        version == "7.12.0.0-SNAPSHOT"
        println(' - master -> 7.12.0.0-SNAPSHOT')

        when: 'on feature branch hotfix/JIRA-7'
        creator.setBranch("hotfix/JIRA-7")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is hotfix-<branch name>'
        version == "hotfix-JIRA-7-SNAPSHOT"
        println(" - hotfix/JIRA-7 -> hotfix-JIRA-7-SNAPSHOT")
    }

    def 'test 15 - new release branch merged'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)
        addTest14(creator)
        addTest15(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.12.0.0'
        version == "7.12.0.0"
        println(' - master -> 7.12.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.1-SNAPSHOT'
        version == "7.11.1.1-SNAPSHOT"
        println(' - on release/7.11 -> 7.11.1.1-SNAPSHOT')

        when: 'on feature branch hotfix/JIRA-7'
        creator.setBranch("hotfix/JIRA-7")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is hotfix-<branch name>'
        version == "hotfix-JIRA-7-SNAPSHOT"
        println(" - hotfix/JIRA-7 -> hotfix-JIRA-7-SNAPSHOT")
    }

    def 'test 16 - old release taged'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)
        addTest14(creator)
        addTest15(creator)
        addTest16(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.12.0.0'
        version == "7.12.0.0"
        println(' - master -> 7.12.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.1-SNAPSHOT'
        version == "7.11.1.1-SNAPSHOT"
        println(' - on release/7.11 -> 7.11.1.1-SNAPSHOT')
    }

    def 'test 17 - two "master branches"'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)
        addTest14(creator)
        addTest15(creator)
        addTest16(creator)
        addTest17(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 7.12.0.1-SNAPSHOT'
        version == "7.12.0.1-SNAPSHOT"
        println(' - master -> 7.12.0.1-SNAPSHOT')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.1'
        version == "7.11.1.1"
        println(' - on release/7.11 -> 7.11.1.1')
    }

    def 'test 18 - two "master branches" - hotfix merged in 7.11'() {
        given:
        TestRepoCreator creator = createTest1(testProjectDir)
        addTest2(creator)
        addTest3(creator)
        addTest4(creator)
        addTest5(creator)
        addTest6(creator)
        addTest7(creator)
        addTest8(creator)
        addTest9(creator)
        addTest10(creator)
        addTest11(creator)
        addTest12(creator)
        addTest13(creator)
        addTest14(creator)
        addTest15(creator)
        addTest16(creator)
        addTest17(creator)
        addTest18(creator)

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = new GitVersionService(creator.directory)
        String version = gvs.version

        then:
        version == "7.12.0.1-SNAPSHOT"

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")

        when: 'on release/7.11'
        creator.setBranch("release/7.11")
        gvs = new GitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 7.11.1.2-SNAPSHOT'
        version == "7.11.1.2-SNAPSHOT"
        println(' - on release/7.11 -> 7.11.1.2-SNAPSHOT')
    }
}
