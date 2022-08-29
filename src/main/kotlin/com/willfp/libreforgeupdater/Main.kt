package com.willfp.libreforgeupdater

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.cli.required


fun main(args: Array<String>) {
    val parser = ArgParser("libreforgeupdater")

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

    val noCommit by parser.option(
        ArgType.Boolean,
        shortName = "nc",
        fullName = "nocommit",
        description = "If a commit should not be made"
    ).default(false)

    val directoryName by parser.argument(
        ArgType.String,
        description = "The directory to scan for projects"
    )

    parser.parse(args)

    val updater = Updater(Incrementer.of(incrementType), version)

    val projects = ProjectScanner(directoryName).getLibreforgeProjects().toMutableList()

    if (projects.isEmpty()) {
        println("No projects found!")
        return
    }

    projects.removeIf { it.name.lowercase() in excludes.map { e -> e.lowercase() }}

    println("Projects: ${projects.map { it.name }}")

    val failures = mutableListOf<String>()

    for (project in projects) {
        println("Updating ${project.name}...")

        try {
            updater.update(project, noCommit)
        } catch(e: Exception) {
            println("Error updating ${project.name}")
            failures.add(project.name)
        }

        println()
    }

    println("Updated projects!")
    if (failures.isNotEmpty()) {
        println("Failed builds / updates: ${failures.joinToString(", ")}")
    }
}
