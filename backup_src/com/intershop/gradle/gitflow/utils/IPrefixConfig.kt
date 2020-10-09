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

/**
 * This is the configuration class for the necessary prefixes
 * on special branches, so that it is possible to identify the
 * relevant branches and tags for version calculation.
 */
interface IPrefixConfig {

    companion object {
        /**
         * Regex pattern for banches with versions.
         */
        const val VERSION_PATTERN = "^\\d+(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?(-.*)?"
    }

    /**
     * Prefix for release branches.
     *
     * @property releasePrefix
     */
    var releasePrefix: String

    /**
     * Prefix for development branches (hotfix etc.).
     *
     * @property branchPrefix
     */
    var branchPrefix: List<String>

    /**
     * Prefix for develop branch.
     *
     * @property developBranch
     */
    var developBranch: String

    /**
     * Prefix for release tags.
     *
     * @property tagPrefix
     */
    var tagPrefix: String

    /**
     * Separator between prefix and version.
     *
     * @property prefixSeperator
     */
    var prefixSeperator: String

    val regexPrefixSeperator: String
        get() {
            return if (prefixSeperator == "/" || prefixSeperator == ".") {
                "\${prefixSeperator}"
            } else {
                prefixSeperator
            }
        }

    /**
     * Creates a search pattern for branches.
     *
     * @property featureBranchPattern Search pattern for branches.
     */
    val branchPattern: String
        get() {
            return "${branchPrefix.joinToString("|")}${regexPrefixSeperator}(.*)"
        }

    /**
     * Creates a search pattern for release branches.
     *
     * @property releaseBranchPattern Search pattern for release branches.
     */
    val releaseBranchPattern : String
        get() {
            return "${releasePrefix}${regexPrefixSeperator}${VERSION_PATTERN}"
        }

    /**
     * Creates a search pattern for tags.
     *
     * @property tagPattern Search pattern for version tags.
     */
    val tagPattern: String
        get() {
            return "${tagPrefix}${regexPrefixSeperator}${VERSION_PATTERN}"
        }
}
