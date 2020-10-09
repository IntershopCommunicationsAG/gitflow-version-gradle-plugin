package com.intershop.gradle.gitflow.task

import org.gradle.api.DefaultTask

/**
 * Base class for all tasks of this plugin.
 */
abstract class AbstractReleaseVersionTask: DefaultTask() {

    init {
        group = "Release Version Plugin"
    }
}
