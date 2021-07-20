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

import com.intershop.gradle.gitflow.utils.GitCreatorChangedDefaultNames
import com.intershop.gradle.gitflow.utils.GitVersionService
import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import com.intershop.release.version.Version
import com.intershop.release.version.VersionType
import groovy.util.logging.Slf4j

@Slf4j
class GitIntegrationChangedDefaultNamesSpec extends AbstractIntegrationGroovySpec {

    def getConfGitVersionService(File dir) {
        GitVersionService gvs = new GitVersionService(dir, VersionType.threeDigits)
        gvs.defaultVersion = Version.forString("2.0.0", VersionType.threeDigits)
        gvs.developBranch = "prerelease"
        gvs.mainBranch = "trunk"
        gvs.featurePrefix = "story"
        gvs.hotfixPrefix = "bugfix"
        gvs.releasePrefix = "prepared"
        return gvs
    }

    def 'test init - no releases available'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initGitRepo(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 2.0.0-SNAPSHOT (default version)'
        version == "2.0.0-SNAPSHOT"
        println(" - trunk -> 2.0.0-SNAPSHOT")

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on bugfix branch bugfix/JIRA-1'
        creator.setBranch("bugfix/JIRA-1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is bugfix-<branch name>'
        version == "JIRA-1-SNAPSHOT"
        println(" - bugfix/JIRA-1 -> bugfix-JIRA-1-SNAPSHOT")

        when: 'on story branch story/JIRA-2'
        creator.setBranch("story/JIRA-2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-2-SNAPSHOT"
        println(" - story/JIRA-2 -> story-JIRA-2-SNAPSHOT")
    }

    def 'test 01 - no prepared - bugfix 1 merge'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest1(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 2.0.0-SNAPSHOT (default version)'
        version == "2.0.0-SNAPSHOT"
        println(' - trunk -> 2.0.0-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(' - prerelease -> dev-SNAPSHOT')

        when: 'on story branch story/JIRA-2'
        creator.setBranch("story/JIRA-2")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-2-SNAPSHOT"
        println(" - story/JIRA-2 -> JIRA-2-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")
    }

    def 'test 02 - no prepared - prepared  branch with bugfix'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest2(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 2.0.0-SNAPSHOT (default version)'
        version == "2.0.0-SNAPSHOT"
        println(' - trunk -> 2.0.0-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.0'
        creator.setBranch("prepared/4.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.0.0-SNAPSHOT'
        version == "4.0.0-SNAPSHOT"
        println(" - prepared/4.0 -> 4.0.0-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")
    }

    def 'test 03 - no prepared - prepared  branch merged bugfix'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest3(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 2.0.0-SNAPSHOT (default version)'
        version == "2.0.0-SNAPSHOT"
        println(' - trunk -> 2.0.0-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.0'
        creator.setBranch("prepared/4.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.0.0-SNAPSHOT'
        version == "4.0.0-SNAPSHOT"
        println(" - prepared/4.0 -> 4.0.0-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")

        when: 'on bugfix branch bugfix/JIRA-4'
        creator.setBranch("bugfix/JIRA-4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then:
        version == "JIRA-4-SNAPSHOT"
        println(" - bugfix/JIRA-4 -> JIRA-4-SNAPSHOT")
    }

    def 'test 04 - prepared - prepared branch ready'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest4(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 2.0.0-SNAPSHOT (default version)'
        version == "2.0.0-SNAPSHOT"
        println(' - trunk -> 2.0.0-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.0'
        creator.setBranch("prepared/4.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.0.0-SNAPSHOT'
        version == "4.0.0-SNAPSHOT"
        println(" - prepared/4.0 -> 4.0.0-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 05 - prepared - prepared branch merged and taged, prepared branch still available'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest5(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.0'
        version == "4.0.0"
        println(' - trunk -> 4.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.0'
        creator.setBranch("prepared/4.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.0.0-SNAPSHOT'
        version == "4.0.0-SNAPSHOT"
        println(" - prepared/4.0 -> 4.0.0-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 06 - prepared and storys, prepared branch removed'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest6(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.0'
        version == "4.0.0"
        println(' - trunk -> 4.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 07 - prepared, bugfix and storys'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest7(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.0'
        version == "4.0.0"
        println(' - trunk -> 4.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on story branch bugfix/JIRA-6'
        creator.setBranch("bugfix/JIRA-6")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is bugfix-<branch name>'
        version == "JIRA-6-SNAPSHOT"
        println(" - bugfix/JIRA-6 -> JIRA-6-SNAPSHOT")

        when: 'on story branch story/JIRA-3'
        creator.setBranch("story/JIRA-3")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-3-SNAPSHOT"
        println(" - story/JIRA-3 -> JIRA-3-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 08 - prepared, story merged'(){
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest8(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.0'
        version == "4.0.0"
        println(' - trunk -> 4.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.1'
        creator.setBranch("prepared/4.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.1.0-SNAPSHOT'
        version == "4.1.0-SNAPSHOT"
        println(" - prepared/4.1 -> 4.1.0-SNAPSHOT")

        when: 'on story branch bugfix/JIRA-6'
        creator.setBranch("bugfix/JIRA-6")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is bugfix-<branch name>'
        version == "JIRA-6-SNAPSHOT"
        println(" - bugfix/JIRA-6 -> JIRA-6-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 09 - prepareds, prepared branch and story'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest9(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.1'
        version == "4.0.1"
        println(' - trunk -> 4.0.1')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.1'
        creator.setBranch("prepared/4.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.1.0-SNAPSHOT'
        version == "4.1.0-SNAPSHOT"
        println(" - prepared/4.1 -> 4.1.0-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 10 - new prepared branch ready'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest10(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.0.1'
        version == "4.0.1"
        println(' - trunk -> 4.0.1')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared branch prepared/4.1'
        creator.setBranch("prepared/4.1")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is 4.1.0-SNAPSHOT'
        version == "4.1.0-SNAPSHOT"
        println(" - prepared/4.1 -> 4.1.0-SNAPSHOT")

        when: 'on story branch story/JIRA-5'
        creator.setBranch("story/JIRA-5")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is story-<branch name>'
        version == "JIRA-5-SNAPSHOT"
        println(" - story/JIRA-5 -> JIRA-5-SNAPSHOT")
    }

    def 'test 11 - prepared branch merged'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest11(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.1.0-SNAPSHOT'
        version == "4.1.0-SNAPSHOT"
        println(' - trunk -> 4.1.0-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")
    }

    def 'test 12 - tagged und story merged'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest12(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.1.0'
        version == "4.1.0"
        println(' - trunk -> 4.1.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")
    }

    def 'test 13 - two prepared branches'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest13(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.1.0'
        version == "4.1.0"
        println(' - trunk -> 4.1.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.0'
        version == "4.1.0"
        println(' - on prepared/4 -> 4.1.0')

        when: 'on prepared/5.0'
        creator.setBranch("prepared/5.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 5.0.0-SNAPSHOT'
        version == "5.0.0-SNAPSHOT"
        println(' - trunk -> 5.0.0-SNAPSHOT')
    }

    def 'test 14 - two prepared branches and one bugfix branch'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest14(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 4.1.0'
        version == "4.1.0"
        println(' - trunk -> 4.1.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.1-SNAPSHOT'
        version == "4.1.1-SNAPSHOT"
        println(' - on prepared/4 -> 4.1.1-SNAPSHOT')

        when: 'on prepared/5'
        creator.setBranch("prepared/5.0")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 5.0.0-SNAPSHOT'
        version == "5.0.0-SNAPSHOT"
        println(' - trunk -> 5.0.0-SNAPSHOT')

        when: 'on story branch bugfix/JIRA-7'
        creator.setBranch("bugfix/JIRA-7")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is bugfix-<branch name>'
        version == "JIRA-7-SNAPSHOT"
        println(" - bugfix/JIRA-7 -> JIRA-7-SNAPSHOT")
    }

    def 'test 15 - new prepared branch merged'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest15(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 5.0.0'
        version == "5.0.0"
        println(' - trunk -> 5.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.1-SNAPSHOT'
        version == "4.1.1-SNAPSHOT"
        println(' - on prepared/4 -> 4.1.1-SNAPSHOT')

        when: 'on story branch bugfix/JIRA-7'
        creator.setBranch("bugfix/JIRA-7")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is bugfix-<branch name>'
        version == "JIRA-7-SNAPSHOT"
        println(" - bugfix/JIRA-7 -> bugfix-JIRA-7-SNAPSHOT")
    }

    def 'test 16 - old prepared taged'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest16(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 5.0.0'
        version == "5.0.0"
        println(' - trunk -> 5.0.0')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.1-SNAPSHOT'
        version == "4.1.1-SNAPSHOT"
        println(' - on prepared/4 -> 4.1.1-SNAPSHOT')
    }

    def 'test 17 - two "trunk branches"'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest17(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'Version is 5.0.1-SNAPSHOT'
        version == "5.0.1-SNAPSHOT"
        println(' - trunk -> 5.0.1-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.1'
        version == "4.1.1"
        println(' - on prepared/4 -> 4.1.1')
    }

    def 'test 18 - two "trunk branches" - bugfix merged in 2'() {
        given:
        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest18(testProjectDir, "")

        when: 'on trunk'
        creator.setBranch("trunk")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version

        then: 'version is 5.0.1-SNAPSHOT'
        version == "5.0.1-SNAPSHOT"
        println(' - on trunk -> 5.0.1-SNAPSHOT')

        when: 'on prerelease branch'
        creator.setBranch("prerelease")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'version is dev-SNAPSHOT'
        version == "dev-SNAPSHOT"
        println(" - prerelease -> dev-SNAPSHOT")

        when: 'on prepared/4'
        creator.setBranch("prepared/4")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version

        then: 'Version is 4.1.2-SNAPSHOT'
        version == "4.1.2-SNAPSHOT"
        println(' - on prepared/4 -> 4.1.2-SNAPSHOT')
    }
}
