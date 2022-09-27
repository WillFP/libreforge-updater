package com.willfp.libreforgeupdater

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files

class ProjectScanner(
    private val root: String
) {
    fun getLibreforgeProjects(): List<File> {
        return Files.newDirectoryStream(FileSystems.getDefault().getPath(root))
            .filter { Files.isDirectory(it) }
            .map { it.toFile() }
            .filter { isProject(it) }
    }

    private fun isProject(file: File): Boolean {
        if (!file.isDirectory) {
            return false
        }

        val buildGradle = File(file, "build.gradle")
        val buildGradleKts = File(file, "build.gradle.kts")
        if (!buildGradle.exists() && !buildGradleKts.exists()) {
            return false
        }

        return buildGradle.readLines().any { it.contains("libreforge") }
    }
}
