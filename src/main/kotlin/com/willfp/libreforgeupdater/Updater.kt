package com.willfp.libreforgeupdater

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.*

class Updater(
    private val incrementer: Incrementer,
    private val version: String
) {
    fun update(root: File, noCommit: Boolean) {
        if (!root.isDirectory) {
            throw RuntimeException("Root must be a folder!")
        }

        println("Fetching from git")
        exec("git config pull.rebase false", root)
        exec("git pull", root)


        println("Incrementing version number")
        val newVersion = incrementVersion(File(root, "gradle.properties").throwNotExists())

        println("Setting libreforge version to $version")
        setLibreforgeVersion(File(root, "build.gradle"))
        setLibreforgeVersion(File(File(root, "eco-core"), "build.gradle"))

        if (!noCommit) {
            println("Pushing to git")
            pushToGit(root, newVersion)
        }

        println("Building project (this may take some time)")
        buildProject(root)
    }

    private fun incrementVersion(buildGradle: File): String {
        val prop = Properties()

        var before: String

        var newVersion: String

        FileInputStream(buildGradle).use {
            prop.load(it)
            before = prop.getProperty("version")
            newVersion = incrementer.increment(prop)
            val out = FileOutputStream(buildGradle)
            prop.store(out, "libreforge-updater")
        }

        val after = prop.getProperty("version")

        println("$before --> $after")
        return newVersion
    }

    private fun setLibreforgeVersion(buildGradle: File) {
        if (!buildGradle.exists()) {
            return
        }

        val newLines = mutableListOf<String>()

        for (line in buildGradle.readLines()) {
            // Fixed dumb bug

            if (line.contains("com.willfp:libreforge") || line.contains("com.willfplibreforge")) {
                val split = line.replace("com.willfplibreforge3", "com.willfp:libreforge:3").split(":")
                val newLine = StringBuilder()
                newLine.append(split[0])
                    .append(":")
                    .append(split[1])
                    .append(":$version'")

                newLines.add(newLine.toString())
            } else {
                newLines.add(line)
            }
        }

        Files.write(buildGradle.toPath(), newLines)
    }

    private fun pushToGit(root: File, newVersion: String) {
        exec("git add .", root)
        exec("git commit -m libreforge-updater", root)
        if (newVersion.length > 1) {
            exec("git tag $newVersion", root)
            exec("git push origin master --tags", root)
        } else {
            exec("git push origin master", root)
        }
    }

    private fun buildProject(root: File) {
        File(root, "gradlew").apply { setExecutable(true, false) }

        if (System.getProperty("os.name").lowercase().contains("windows")) {
            println("OS: Windows")
            exec(".\\gradlew.bat build", root)
        } else {
            println("OS: Unix")
            exec("./gradlew build", root)
        }
    }

    private fun File.throwNotExists(): File {
        if (!this.exists()) throw FileNotFoundException("File does not exist!")
        return this
    }
}
