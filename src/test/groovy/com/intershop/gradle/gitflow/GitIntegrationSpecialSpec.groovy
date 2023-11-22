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

import com.intershop.gradle.gitflow.utils.GitCreatorSpecialVersions
import com.intershop.gradle.gitflow.utils.GitCreatorThreeNumbers
import com.intershop.gradle.gitflow.utils.GitVersionService
import com.intershop.gradle.gitflow.utils.TestRepoCreator
import com.intershop.gradle.test.AbstractIntegrationGroovySpec
import com.intershop.release.version.VersionType
import groovy.util.logging.Slf4j

@Slf4j
class GitIntegrationSpecialSpec extends AbstractIntegrationGroovySpec {

    def getConfGitVersionService(File dir) {
        GitVersionService gvs = new GitVersionService(dir, VersionType.threeDigits)
    }

    def 'test init - no releases available'() {
        given:
        TestRepoCreator creator = GitCreatorSpecialVersions.initTest9(testProjectDir, "")

        when: 'on master'
        creator.setBranch("master")
        GitVersionService gvs = getConfGitVersionService(creator.directory)
        String version = gvs.version
        String preVersion = gvs.previousVersion

        then: 'Version is 1.0.0-SNAPSHOT (default version)'
        version == "1.0.0-SNAPSHOT"
        println(" - master -> 1.0.0-SNAPSHOT")
        preVersion == null
        println(' - master -> pre version == null')

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

        when: 'on master'
        creator.setBranch("feature/major")
        gvs = getConfGitVersionService(creator.directory)
        version = gvs.version
        preVersion = null

        then: 'version is major-SNAPSHOT'
        version == "major-SNAPSHOT"
        println(" - feature/major -> major-SNAPSHOT")
        preVersion == null
        println(' - feature/major -> pre version is null')
    }

}
