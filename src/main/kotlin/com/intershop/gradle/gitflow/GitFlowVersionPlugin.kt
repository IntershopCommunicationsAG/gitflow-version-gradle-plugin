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

import com.intershop.gradle.gitflow.extension.VersionExtension.Companion.VERSION_EXTENSION_NAME
import com.intershop.gradle.gitflow.extension.VersionExtension
import com.intershop.gradle.gitflow.tasks.CreateChangeLog
import com.intershop.gradle.gitflow.tasks.ShowVersion
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This is the plugin class!
 */
open class GitFlowVersionPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        with(project.rootProject) {
            logger.info("GitFlow version plugin will be initialized")

            val ext = extensions.findByType(
                VersionExtension::class.java
            ) ?: extensions.create(VERSION_EXTENSION_NAME, VersionExtension::class.java, this)

            tasks.register("showVersion", ShowVersion::class.java)
            tasks.register("createChangeLog", CreateChangeLog::class.java) {
                it.preVersion = ext.previousVersion
            }
        }
    }
}
