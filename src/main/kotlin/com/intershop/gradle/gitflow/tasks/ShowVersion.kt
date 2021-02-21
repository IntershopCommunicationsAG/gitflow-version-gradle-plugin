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

package com.intershop.gradle.gitflow.tasks

import com.intershop.gradle.gitflow.extension.VersionExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * This is an helper task to show the calculated version.
 */
open class ShowVersion: DefaultTask() {

    /**
     * Main function of this task.
     */
    @TaskAction
    fun run() {
        val versionExt = project.extensions.getByType(VersionExtension::class.java)
        val preVersion = versionExt.previousVersion
        println("------------------------------------------------------------------")
        println("-- GitFlow version is")
        println("--   " + project.version)
        if(preVersion.isNotEmpty()) {
            println("-- GitFlow previous version is")
            println("--   " + versionExt.previousVersion)
        } else {
            println("-- GitFlow previous version is not available!")
        }
        println("--")
        println("-- Branche name is")
        println("--   " + versionExt.branchName)
        println("------------------------------------------------------------------")
    }
}
