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
package com.intershop.gradle.gitflow.extension

import com.intershop.gradle.gitflow.utils.GitVersionService
import com.intershop.release.version.Version
import com.intershop.release.version.VersionType
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.util.*
import javax.inject.Inject

/**
 * This is the extension of this plugin.
 * @param objectFactory factory for objects
 * @param layout directory layout
 */
open class VersionExtension @Inject constructor(objectFactory: ObjectFactory,
                                                layout: ProjectLayout,
                                                providerFactory: ProviderFactory ) {

    companion object {
        /**
         * Name for the plugin extension.
         */
        const val VERSION_EXTENSION_NAME = "gitflowVersion"
    }

    private val defaultVersionProperty = objectFactory.property(String::class.java)
    private val mainBranchProperty = objectFactory.property(String::class.java)
    private val developBranchProperty = objectFactory.property(String::class.java)
    private val hotfixPrefixProperty = objectFactory.property(String::class.java)
    private val featurePrefixProperty = objectFactory.property(String::class.java)
    private val releasePrefixProperty = objectFactory.property(String::class.java)
    private val versionPrefixProperty = objectFactory.property(String::class.java)
    private val separatorProperty = objectFactory.property(String::class.java)
    private val versionTypeProperty = objectFactory.property(String::class.java)
    private val fullbranchProperty = objectFactory.property(Boolean::class.java)

    private val versionProperty = objectFactory.property(String::class.java)
    private val branchProperty = objectFactory.property(String::class.java)
    private val previousVersionProperty = objectFactory.property(String::class.java)
    private val containerVersionProperty = objectFactory.property(String::class.java)

    private val isMergeBranchProperty = objectFactory.property(Boolean::class.java)
    private val sourceBranchProperty = objectFactory.property(String::class.java)
    private val pullRequestIDProperty = objectFactory.property(String::class.java)
    private val buildIDProperty = objectFactory.property(String::class.java)

    init {
        defaultVersionProperty.convention("1.0.0")
        mainBranchProperty.convention("master")
        developBranchProperty.convention("develop")
        hotfixPrefixProperty.convention("hotfix")
        featurePrefixProperty.convention("feature")
        releasePrefixProperty.convention("release")
        versionPrefixProperty.convention("version")
        separatorProperty.convention("/")
        versionTypeProperty.convention("three")
        fullbranchProperty.convention(false)

        versionProperty.convention("")
        previousVersionProperty.convention("")
        containerVersionProperty.convention("")
        branchProperty.convention("")
    }

    /**
     * This is provider for the defaultVersion property.
     */
    val defaultVersionProvider: Provider<String>
        get() = defaultVersionProperty

    /**
     * This is defaultVersion property.
     */
    var defaultVersion : String
        get() = defaultVersionProperty.get()
        set(value) = defaultVersionProperty.set(value)

    /**
     * This is provider for the mainBranch property.
     */
    val mainBranchProvider: Provider<String>
        get() = mainBranchProperty

    /**
     * This is mainBranch property.
     */
    var mainBranch : String
        get() = mainBranchProperty.get()
        set(value) = mainBranchProperty.set(value)

    /**
     * This is provider for the developBranch property.
     */
    val developBranchProvider: Provider<String>
        get() = developBranchProperty

    /**
     * This is developBranch property.
     */
    var developBranch : String
        get() = developBranchProperty.get()
        set(value) = developBranchProperty.set(value)

    /**
     * This is provider for the hotfixPrefix property.
     */
    val hotfixPrefixProvider: Provider<String>
        get() = hotfixPrefixProperty

    /**
     * This is hotfixPrefix property.
     */
    var hotfixPrefix : String
        get() = hotfixPrefixProperty.get()
        set(value) = hotfixPrefixProperty.set(value)

    /**
     * This is provider for the featurePrefix property.
     */
    val featurePrefixProvider: Provider<String>
        get() = featurePrefixProperty

    /**
     * This is featurePrefix property.
     */
    var featurePrefix : String
        get() = featurePrefixProperty.get()
        set(value) = featurePrefixProperty.set(value)

    /**
     * This is provider for the releasePrefix property.
     */
    val releasePrefixProvider: Provider<String>
        get() = releasePrefixProperty

    /**
     * This is releasePrefix property.
     */
    var releasePrefix : String
        get() = releasePrefixProperty.get()
        set(value) = releasePrefixProperty.set(value)

    /**
     * This is provider for the versionPrefix property.
     */
    val versionPrefixProvider: Provider<String>
        get() = versionPrefixProperty

    /**
     * This is versionPrefix property.
     */
    var versionPrefix : String
        get() = versionPrefixProperty.get()
        set(value) = versionPrefixProperty.set(value)

    /**
     * This is provider for the separator property.
     */
    val separatorProvider: Provider<String>
        get() = separatorProperty

    /**
     * This is separator property.
     */
    var separator : String
        get() = separatorProperty.get()
        set(value) = separatorProperty.set(value)

    /**
     * This is provider for the versionType property.
     */
    val versionTypeProvider: Provider<String>
        get() = versionTypeProperty

    /**
     * This is versionType property.
     */
    var versionType : String
        get() = versionTypeProperty.get()
        set(value) = versionTypeProperty.set(value)

    /**
     * This is provider for the shortened property.
     */
    val shortenedProvider: Provider<Boolean>
        get() = fullbranchProperty

    /**
     * This is shortened property.
     */
    var fullbranch : Boolean
        get() = fullbranchProperty.get()
        set(value) = fullbranchProperty.set(value)

    /**
     * Pull request ID of pull request
     */
    val pullRequestID : String
        get() = pullRequestIDProperty.getOrElse("")

    /**
     * This is the service for version calculation.
     */
    val versionService: GitVersionService by lazy {

        val vType = if(versionType.lowercase(Locale.getDefault()) == "four" || versionType == "4") {
                        VersionType.fourDigits
                    } else {
                        VersionType.threeDigits
                    }

        val versionService = GitVersionService( layout.projectDirectory.asFile, vType)
        versionService.defaultVersion = Version.forString(defaultVersionProperty.get(), vType)
        versionService.mainBranch = mainBranchProperty.get()
        versionService.developBranch = developBranchProperty.get()
        versionService.featurePrefix = featurePrefixProperty.get()
        versionService.hotfixPrefix = hotfixPrefixProperty.get()
        versionService.releasePrefix = releasePrefixProperty.get()
        versionService.versionPrefix = versionPrefixProperty.get()
        versionService.separator = separatorProperty.get()
        versionService.fullbranch = fullbranchProperty.get()

        val localVersion: Provider<String> = providerFactory.gradleProperty("localVersion").
                                                             forUseAtConfigurationTime()
        val mergeBuild = providerFactory.systemProperty("MERGE_BUILD").
                                                             forUseAtConfigurationTime()

        versionService.localOnly = localVersion.getOrElse("false").lowercase(Locale.getDefault()) == "true"
        versionService.isMergeBuild = mergeBuild.getOrElse("false").lowercase(Locale.getDefault()) == "true"

        versionService.sourceBranch = providerFactory.systemProperty("PR_SOURCE_BRANCH").
                                                             forUseAtConfigurationTime().getOrElse("")
        versionService.pullRequestID = providerFactory.systemProperty("PR_REQUEST_ID").
                                                             forUseAtConfigurationTime().getOrElse("")
        versionService.buildID = providerFactory.systemProperty("PR_BUILD_ID").
                                                             forUseAtConfigurationTime().getOrElse("")

        versionService
    }

    /**
     * This is the version string.
     */
    val version: String by lazy {
        if(versionProperty.get().isEmpty()) {
            versionProperty.set(this.versionService.version)
        }
        versionProperty.get()
    }

    /**
     * This is the version for containers.
     * There is no "Snapshot"
     */
    val containerVersion: String by lazy {
        if(containerVersionProperty.get().isEmpty()) {
            containerVersionProperty.set(this.versionService.containerVersion)
        }
        containerVersionProperty.get()
    }

    /**
     * This is the previous version string.
     */
    val previousVersion: String by lazy {
        if(previousVersionProperty.get().isEmpty()) {
            previousVersionProperty.set(this.versionService.previousVersion ?: "")
        }
        previousVersionProperty.get()
    }

    /**
     * This is the branch name.
     */
    val branchName: String by lazy {
        if(branchProperty.get().isEmpty()) {
            branchProperty.set(this.versionService.branch)
        }
        branchProperty.get()
    }
}
