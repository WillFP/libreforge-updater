package com.willfp.libreforgeupdater

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.cli.required
import java.io.File


fun main(args: Array<String>) {
    val parser = ArgParser("libreforge-updater")

    val incrementType by parser.option(
        ArgType.Choice<IncrementType>(),
        shortName = "i",
        fullName = "incrementer",
        description = "The way to increment the version number"
    ).required()

    val version by parser.option(
        ArgType.String,
        shortName = "v",
        fullName = "version",
        description = "The version of libreforge to update to"
    ).required()

    val excludes by parser.option(
        ArgType.String,
        shortName = "e",
        fullName = "exclude",
        description = "The name of a project to exclude"
    ).multiple()

    val whitelist by parser.option(
        ArgType.String,
        shortName = "w",
        fullName = "whitelist",
        description = "The name of a project to whitelist"
    ).multiple()

    val noCommit by parser.option(
        ArgType.Boolean,
        shortName = "nc",
        fullName = "nocommit",
        description = "If a commit should not be made"
    ).default(false)

    val fullErrors by parser.option(
        ArgType.Boolean,
        shortName = "fe",
        fullName = "fullerrors",
        description = "If full errors should be shown"
    ).default(false)

    val directoryName by parser.argument(
        ArgType.String,
        description = "The directory to scan for projects"
    )

    val message by parser.option(
        ArgType.String,
        shortName = "m",
        fullName = "message",
        description = "Ths commit message"
    ).default("libreforge-updater")

    val out by parser.option(
        ArgType.String,
        shortName = "o",
        fullName = "out",
        description = "The directory to put all jars in"
    )

    parser.parse(args)

    if (out != null) {
        println("Preparing out directory...")
        val directory = File(out!!)

        directory.deleteRecursively()
        directory.mkdirs()
    }

    val updater = Updater(Incrementer.of(incrementType), version, out, message)

    val projects = ProjectScanner(directoryName).getLibreforgeProjects().toMutableList()

    if (projects.isEmpty()) {
        println("No projects found!")
        return
    }

    projects.removeIf { it.name.lowercase() in excludes.map { e -> e.lowercase() } }
    if (whitelist.isNotEmpty()) {
        projects.removeIf { it.name.lowercase() !in whitelist.map { w -> w.lowercase() } }
    }

    println("Projects: ${projects.map { it.name }}")

    val failures = mutableListOf<String>()

    for (project in projects) {
        println("Updating ${project.name}...")

        try {
            updater.update(project, noCommit)
        } catch (e: Exception) {
            println("Error updating ${project.name}")
            if (fullErrors) {
                e.printStackTrace()
            }
            failures.add(project.name)
        }

        println()
    }

    println("Updated projects!")
    if (failures.isNotEmpty()) {
        println("Failed builds / updates: ${failures.joinToString(", ")}")
    }
}
