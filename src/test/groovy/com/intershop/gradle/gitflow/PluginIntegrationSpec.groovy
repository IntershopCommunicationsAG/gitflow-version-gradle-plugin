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

import com.intershop.gradle.gitflow.utils.GitCreatorChangedDefaultNames
import com.intershop.gradle.gitflow.utils.GitCreatorSpecialPath
import com.intershop.gradle.gitflow.utils.GitCreatorThreeNumbers
import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import org.gradle.testkit.runner.TaskOutcome

class PluginIntegrationSpec extends AbstractIntegrationGroovySpec {

    TestRepoCreator createTest1(File dir, String buildFileContent)  {
        TestRepoCreator creator = new TestRepoCreator(dir)
        //add build files
        creator.addBuildGroovyFile(buildFileContent)

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

    def 'initial plugin integration test'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorThreeNumbers.initGitRepo(testProjectDir, buildFileContent)
        String cid = creator.setBranch("master")
        creator.gitidCheckout(cid)

        when:
        List<String> args = [':showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.output.contains("1.0.0-SNAPSHOT")
        result1.output.contains("GitFlow previous version is not available!")
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS

        when:
        List<String> args2 = [':createChangeLog', '-i', '-s']

        def result2 = getPreparedGradleRunner()
                .withArguments(args2)
                .withGradleVersion(gradleVersion)
                .build()
        File resultFile = new File(testProjectDir, "build/changelog/changelog.md")

        then:
        result2.task(":createChangeLog").outcome == TaskOutcome.SUCCESS
        resultFile.exists()
        resultFile.text.contains("This list contains changes since beginning.")

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'test path with team'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
                
                defaultVersion = "2.0.0"
                mainBranch = "master"
                developBranch = "develop"
                hotfixPrefix = "hotfix"
                featurePrefix = "features"
                releasePrefix = "release"
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorSpecialPath.initGitRepo(testProjectDir, buildFileContent)
        creator.setBranch("master")

        when:
        List<String> args = [':showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result1.output.contains("2.0.0-SNAPSHOT")

        when:
        creator.setBranch("hotfix/team1/12345-message")

        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result2.output.contains("12345.6993688732-SNAPSHOT")

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'test path with team - shortend'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
                
                defaultVersion = "2.0.0"
                mainBranch = "master"
                developBranch = "develop"
                hotfixPrefix = "hotfix"
                featurePrefix = "features"
                releasePrefix = "release"
                
                fullbranch = true
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorSpecialPath.initGitRepo(testProjectDir, buildFileContent)
        creator.setBranch("master")

        when:
        List<String> args = [':showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result1.output.contains("2.0.0-SNAPSHOT")

        when:
        creator.setBranch("hotfix/team1/12345-message")

        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result2.output.contains("12345-message-SNAPSHOT")

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'test path with team - long descr shortend'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
                
                defaultVersion = "2.0.0"
                mainBranch = "master"
                developBranch = "develop"
                hotfixPrefix = "hotfix"
                featurePrefix = "features"
                releasePrefix = "release"
            }
            
            version = gitflowVersion.version
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorSpecialPath.initGitRepoWithLongNames(testProjectDir, buildFileContent)
        creator.setBranch("master")

        when:
        List<String> args = [':showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result1.output.contains("2.0.0-SNAPSHOT")

        when:
        creator.setBranch("hotfix/team1/12345-message_ddfdffearer-erwrwear-efdgfwewrwerwe-ewwerwerwer")

        def result2 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result2.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result2.output.contains("12345.9293237624-SNAPSHOT")

        when:
        creator.setBranch("hotfix/team1/12345_message_ddfdffearer-ereterwrwear-ewewrwerwe-ewwerwerwer")

        def result3 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result3.task(":showVersion").outcome == TaskOutcome.SUCCESS
        result3.output.contains("12345.6889844814-SNAPSHOT")

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'plugin configuration integration test'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
                
                defaultVersion = "2.0.0"
                mainBranch = "trunk"
                developBranch = "prerelease"
                hotfixPrefix = "bugfix"
                featurePrefix = "story"
                releasePrefix = "prepared"
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorChangedDefaultNames.initTest15(testProjectDir, buildFileContent)
        String cid = creator.setBranch("trunk")
        creator.gitidCheckout(cid)

        when:
        List<String> args = [':showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.output.contains("5.0.0")
        result1.output.contains("4.1.0")
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS

        when:
        List<String> args2 = [':createChangeLog', '-i', '-s']

        def result2 = getPreparedGradleRunner()
                .withArguments(args2)
                .withGradleVersion(gradleVersion)
                .build()
        File resultFile = new File(testProjectDir, "build/changelog/changelog.md")

        then:
        result2.task(":createChangeLog").outcome == TaskOutcome.SUCCESS
        resultFile.exists()
        resultFile.text.contains("This list contains changes since 4.1.0.")

        where:
        gradleVersion << supportedGradleVersions
    }

    def 'plugin for development'() {
        given:
        def buildFileContent = """
            plugins {
                id 'com.intershop.gradle.version.gitflow'
            }
            
            gitflowVersion {
                versionType = "three"
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = GitCreatorThreeNumbers.initGitRepo(testProjectDir, buildFileContent)
        String cid = creator.setBranch("master")
        creator.gitidCheckout(cid)

        when:
        List<String> args = [':showVersion', '-i', '-s', '-PlocalVersion=true']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        result1.output.contains("local")
        result1.output.contains("GitFlow previous version is not available!")
        result1.task(":showVersion").outcome == TaskOutcome.SUCCESS

        where:
        gradleVersion << supportedGradleVersions
    }
}
