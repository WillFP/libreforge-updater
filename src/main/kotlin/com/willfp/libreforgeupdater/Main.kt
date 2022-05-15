package com.willfp.libreforgeupdater

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
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

    val directoryName by parser.argument(
        ArgType.String,
        description = "The directory to scan for projects"
    )

    parser.parse(args)

    val updater = Updater(Incrementer.of(incrementType), version)

    val projects = ProjectScanner(directoryName).getLibreforgeProjects()

    if (projects.isEmpty()) {
        println("No projects found!")
        return
    }

    println("Projects: ${projects.map { it.name }}")

    for (project in projects) {
        println("Updating ${project.name}...")
        updater.update(project)
        println()
    }

    println("Updated projects!")
}