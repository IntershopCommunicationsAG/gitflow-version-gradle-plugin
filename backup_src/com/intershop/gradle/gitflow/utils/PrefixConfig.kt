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
package com.intershop.gradle.gitflow.utils

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * This is the configuration class for the necessary prefixes
 * on special branches, so that it is possible to identify the
 * relevant branches and tags for version calculation.
 */
abstract class PrefixConfig: IPrefixConfig {

    /**
     * Inject service of ObjectFactory (See "Service injection" in Gradle documentation.
     */
    @get:Inject
    abstract val objectFactory: ObjectFactory

    private val releasePrefixProperty: Property<String> = objectFactory.property(String::class.java)
    private val branchPrefixProperty: ListProperty<String> = objectFactory.listProperty(String::class.java)
    private val developProperty: Property<String> = objectFactory.property(String::class.java)
    private val tagPrefixProperty: Property<String> = objectFactory.property(String::class.java)

    private val prefixSeperatorProperty: Property<String> = objectFactory.property(String::class.java)

    init {
        releasePrefixProperty.convention("release")
        branchPrefixProperty.convention(listOf("bugfix", "hotfix",  "feature"))
        tagPrefixProperty.convention("version")
        developProperty.convention("develop")
        prefixSeperatorProperty.convention("/")
    }

    /**
     * Prefix for release branches.
     *
     * @property releasePrefix
     */
    override var releasePrefix: String by releasePrefixProperty

    /**
     * Prefixes for branches.
     *
     * @property branchPrefix
     */
    override var branchPrefix: List<String> by branchPrefixProperty

    /**
     * Name of develop branche.
     *
     * @property developBranch
     */
    override var developBranch: String by developProperty

    /**
     * Prefix for release tags.
     *
     * @property tagPrefixProperty
     */
    override var tagPrefix: String by tagPrefixProperty

    /**
     * Separator between prefix and version.
     *
     * @property prefixSeperatorProperty
     */
    override var prefixSeperator: String by prefixSeperatorProperty
}
