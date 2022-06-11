package com.willfp.libreforgeupdater

import java.util.Properties

interface Incrementer {
    fun increment(properties: Properties)

    companion object {
        fun of(type: IncrementType): Incrementer = when(type) {
            IncrementType.MAJOR -> MajorIncrementer
            IncrementType.MINOR -> MinorIncrementer
            IncrementType.NONE -> NoIncrementer
        }
    }
}

enum class IncrementType {
    MAJOR,
    MINOR,
    NONE
}

private object MajorIncrementer : Incrementer {
    override fun increment(properties: Properties) {
        val currentVersion = properties.getProperty("version")
        val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
        split[1]++
        split[2] = 0
        properties.setProperty("version", split.joinToString("."))
    }
}

private object MinorIncrementer : Incrementer {
    override fun increment(properties: Properties) {
        val currentVersion = properties.getProperty("version")
        val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
        split[2]++
        properties.setProperty("version", split.joinToString("."))
    }
}

private object NoIncrementer : Incrementer {
    override fun increment(properties: Properties) {

    }
}
