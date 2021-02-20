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

import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec

class PluginIntegrationSpec extends AbstractIntegrationGroovySpec {

    TestRepoCreator createTest1(File dir, String buildFileContent)  {
        TestRepoCreator creator = new TestRepoCreator(dir)
        //add build filesx
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
                id 'com.intershop.gradle.gitflowversion'
            }
            
            gitflowVersion {
                versionType = "three"
            }
            
            version = gitflowVersion.version
            
            
        """.stripIndent()

        TestRepoCreator creator = createTest1(testProjectDir, buildFileContent)
        creator.setBranch("master")

        when:
        List<String> args = ['showVersion', '-i', '-s']

        def result1 = getPreparedGradleRunner()
                .withArguments(args)
                //.withGradleVersion(gradleVersion)
                .build()

        then:
            true

    }
}
