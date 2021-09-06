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

import com.intershop.gradle.gitflow.utils.GitCreatorThreeNumbers
import com.intershop.gradle.gitflow.utils.GitVersionService
import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import com.intershop.release.version.VersionType
import groovy.util.logging.Slf4j

@Slf4j
class GitIntegrationThreeNumbersSpec extends AbstractIntegrationGroovySpec {

    def getConfGitVersionService(File dir) {
        GitVersionService gvs = new GitVersionService(dir, VersionType.threeDigits)
        gvs.fullbranch = true
        return gvs
    }

    def 'test init - no releases available'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initGitRepo(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(" - master -> 1.0.0-SNAPSHOT")
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on hotfix branch hotfix/JIRA-1'
        creator.setBranch("hotfix/JIRA-1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is hotfix-<branch name>'
        version == "JIRA-1-SNAPSHOT"
        println(" - hotfix/JIRA-1 -> JIRA-1-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')

        when: 'on feature branch feature/JIRA-2'
        creator.setBranch("feature/JIRA-2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-2-SNAPSHOT"
        println(" - feature/JIRA-2 -> JIRA-2-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 01 - no release - hotfix 1 merge'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest1(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(' - master -> 1.0.0-SNAPSHOT')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(' - develop -> dev-SNAPSHOT')
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on feature branch feature/JIRA-2'
        creator.setBranch("feature/JIRA-2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-2-SNAPSHOT"
        println(" - feature/JIRA-2 -> JIRA-2-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 03 - no release - release  branch with hotfix'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest2(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(' - master -> 1.0.0-SNAPSHOT')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.0'
        creator.setBranch("release/2.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.0.0-SNAPSHOT'
        version == "2.0.0-SNAPSHOT"
        println(" - release/2.0 -> 2.0.0-SNAPSHOT")
        preVersion == null
        println(' - release -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 04 - no release - release  branch merged hotfix'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest3(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(' - master -> 1.0.0-SNAPSHOT')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.0'
        creator.setBranch("release/2.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.0.0-SNAPSHOT'
        version == "2.0.0-SNAPSHOT"
        println(" - release/2.0 -> 2.0.0-SNAPSHOT")
        preVersion == null
        println(' - release -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on hotfix branch hotfix/JIRA-4'
        creator.setBranch("hotfix/JIRA-4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then:
        version == "JIRA-4-SNAPSHOT"
        println(" - hotfix/JIRA-4 -> JIRA-4-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')
    }

    def 'test 04 - release - release branch ready'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest4(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(' - master -> 1.0.0-SNAPSHOT')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.0'
        creator.setBranch("release/2.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.0.0-SNAPSHOT'
        version == "2.0.0-SNAPSHOT"
        println(" - release/2.0 -> 2.0.0-SNAPSHOT")
        preVersion == null
        println(' - release -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 06 - release - release branch merged and taged, release branch still available'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest5(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.0'
        version == "2.0.0"
        println(' - master -> 2.0.0')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.0'
        creator.setBranch("release/2.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.0.0-SNAPSHOT'
        version == "2.0.0-SNAPSHOT"
        println(" - release/2.0 -> 2.0.0-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 07 - release and features, release branch removed'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest6(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.0'
        version == "2.0.0"
        println(' - master -> 2.0.0')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 07 - release, hotfix and features'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest7(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.0'
        version == "2.0.0"
        println(' - master -> 2.0.0')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on feature branch hotfix/JIRA-6'
        creator.setBranch("hotfix/JIRA-6")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is hotfix-<branch name>'
        version == "JIRA-6-SNAPSHOT"
        println(" - hotfix/JIRA-6 -> JIRA-6-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')

        when: 'on feature branch feature/JIRA-3'
        creator.setBranch("feature/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - feature/JIRA-3 -> JIRA-3-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 08 - release, feature merged'(){
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest8(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.0'
        version == "2.0.0"
        println(' - master -> 2.0.0')
        preVersion == null
        println(' - master -> pre version is null')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.1'
        creator.setBranch("release/2.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.1.0-SNAPSHOT'
        version == "2.1.0-SNAPSHOT"
        println(" - release/2.1 -> 2.1.0-SNAPSHOT")
        preVersion == null
        println(' - release -> pre version is null')

        when: 'on feature branch hotfix/JIRA-6'
        creator.setBranch("hotfix/JIRA-6")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is hotfix-<branch name>'
        version == "JIRA-6-SNAPSHOT"
        println(" - hotfix/JIRA-6 -> JIRA-6-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 09 - releases, release branch and feature'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest9(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.1'
        version == "2.0.1"
        println(' - master -> 2.0.1')
        preVersion == "2.0.0"
        println(' - master -> pre version is 2.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.1'
        creator.setBranch("release/2.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.1.0-SNAPSHOT'
        version == "2.1.0-SNAPSHOT"
        println(" - release/2.1 -> 2.1.0-SNAPSHOT")
        preVersion == null
        println(' - release -> pre version is null')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 10 - new release branch ready'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest10(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.0.1'
        version == "2.0.1"
        println(' - master -> 2.0.1')
        preVersion == "2.0.0"
        println(' - master -> pre version is 2.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release branch release/2.1'
        creator.setBranch("release/2.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is 2.1.0-SNAPSHOT'
        version == "2.1.0-SNAPSHOT"
        println(" - release/2.1 -> 2.1.0-SNAPSHOT")
        preVersion == "2.0.1"
        println(' - release -> pre version is 2.0.1')

        when: 'on feature branch feature/JIRA-5'
        creator.setBranch("feature/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is feature-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - feature/JIRA-5 -> JIRA-5-SNAPSHOT")
        preVersion == null
        println(' - feature -> pre version is null')
    }

    def 'test 11 - release branch merged'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest11(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.1.0-SNAPSHOT'
        version == "2.1.0-SNAPSHOT"
        println(' - master -> 2.1.0-SNAPSHOT')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
    }

    def 'test 12 - tagged und feature merged'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest12(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.1.0'
        version == "2.1.0"
        println(' - master -> 2.1.0')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')
    }

    def 'test 13 - two release branches'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest13(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.1.0'
        version == "2.1.0"
        println(' - master -> 2.1.0')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.0'
        version == "2.1.0"
        println(' - on release/2 -> 2.1.0')
        preVersion == "2.0.1"
        println(' - release -> pre version is 2.0.1')

        when: 'on release/3.0'
        creator.setBranch("release/3.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 3.0.0-SNAPSHOT'
        version == "3.0.0-SNAPSHOT"
        println(' - release -> 3.0.0-SNAPSHOT')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')
    }

    def 'test 14 - two release branches and one hotfix branch'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest14(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 2.1.0'
        version == "2.1.0"
        println(' - master -> 2.1.0')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.1-SNAPSHOT'
        version == "2.1.1-SNAPSHOT"
        println(' - on release/2 -> 2.1.1-SNAPSHOT')
        preVersion == "2.1.0"
        println(' - release -> pre version is 2.1.0')

        when: 'on release/3'
        creator.setBranch("release/3.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 3.0.0-SNAPSHOT'
        version == "3.0.0-SNAPSHOT"
        println(' - release -> 3.0.0-SNAPSHOT')
        preVersion == "2.0.1"
        println(' - master -> pre version is 2.0.1')

        when: 'on feature branch hotfix/JIRA-7'
        creator.setBranch("hotfix/JIRA-7")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is hotfix-<branch name>'
        version == "JIRA-7-SNAPSHOT"
        println(" - hotfix/JIRA-7 -> JIRA-7-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')
    }

    def 'test 15 - new release branch merged'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest15(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 3.0.0'
        version == "3.0.0"
        println(' - master -> 3.0.0')
        preVersion == "2.1.0"
        println(' - master -> pre version is 2.1.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.1-SNAPSHOT'
        version == "2.1.1-SNAPSHOT"
        println(' - on release/2 -> 2.1.1-SNAPSHOT')
        preVersion == "2.1.0"
        println(' - release -> pre version is 2.1.0')

        when: 'on feature branch hotfix/JIRA-7'
        creator.setBranch("hotfix/JIRA-7")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is hotfix-<branch name>'
        version == "JIRA-7-SNAPSHOT"
        println(" - hotfix/JIRA-7 -> JIRA-7-SNAPSHOT")
        preVersion == null
        println(' - hotfix -> pre version is null')
    }

    def 'test 16 - old release taged'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest16(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 3.0.0'
        version == "3.0.0"
        println(' - master -> 3.0.0')
        preVersion == "2.1.0"
        println(' - master -> pre version is 2.1.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.1-SNAPSHOT'
        version == "2.1.1-SNAPSHOT"
        println(' - on release/2 -> 2.1.1-SNAPSHOT')
        preVersion == "2.1.0"
        println(' - release -> pre version is 2.1.0')
    }

    def 'test 17 - two "master branches"'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest17(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 3.0.1-SNAPSHOT'
        version == "3.0.1-SNAPSHOT"
        println(' - master -> 3.0.1-SNAPSHOT')
        preVersion == "3.0.0"
        println(' - master -> pre version is 3.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.1'
        version == "2.1.1"
        println(' - on release/2 -> 2.1.1')
        preVersion == "2.1.0"
        println(' - release -> pre version is 2.1.0')
    }

    def 'test 18 - two "master branches" - hotfix merged in 2'() {
        given:
        TestRepoCreator creator = GitCreatorThreeNumbers.initTest18(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then:
        version == "3.0.1-SNAPSHOT"
        println(' - master -> 3.0.1-SNAPSHOT')
        preVersion == "3.0.0"
        println(' - master -> pre version is 3.0.0')

        when: 'on develop branch'
        creator.setBranch("develop")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - develop -> dev-SNAPSHOT")
        preVersion == null
        println(' - develop -> pre version is null')

        when: 'on release/2'
        creator.setBranch("release/2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = gvs.previousVersion

        then: 'Version is 2.1.2-SNAPSHOT'
        version == "2.1.2-SNAPSHOT"
        println(' - release/2 -> 2.1.2-SNAPSHOT')
        preVersion == "2.1.1"
        println(' - release -> pre version is 2.1.1')
    }
}
