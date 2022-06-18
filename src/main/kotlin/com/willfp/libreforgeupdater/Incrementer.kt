package com.willfp.libreforgeupdater

import java.util.Properties

interface Incrementer {
    fun increment(properties: Properties): String

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
    override fun increment(properties: Properties): String {
        val currentVersion = properties.getProperty("version")
        val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
        split[1]++
        split[2] = 0
        val str = split.joinToString(".")
        properties.setProperty("version", str)
        return str
    }
}

private object MinorIncrementer : Incrementer {
    override fun increment(properties: Properties): String {
        val currentVersion = properties.getProperty("version")
        val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
        split[2]++
        val str = split.joinToString(".")
        properties.setProperty("version", split.joinToString("."))
        return str
    }
}

private object NoIncrementer : Incrementer {
    override fun increment(properties: Properties): String {
        return ""
    }
}
