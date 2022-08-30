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
        val str = if (currentVersion.contains("-b")) {
            val split = currentVersion.split("-b").toMutableList()
            split[1] = (split[1].toInt() + 1).toString()
            split.joinToString("-b")
        } else {
            val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
            split[1]++
            split[2] = 0
            split.joinToString(".")
        }
        properties.setProperty("version", str)
        return str
    }
}

private object MinorIncrementer : Incrementer {
    override fun increment(properties: Properties): String {
        val currentVersion = properties.getProperty("version")
        val str = if (currentVersion.contains("-b")) {
            val split = currentVersion.split("-b").toMutableList()
            split[1] = (split[1].toInt() + 1).toString()
            split.joinToString("-b")
        } else {
            val split = currentVersion.split(".").map { it.toInt() }.toMutableList()
            split[2]++
            split.joinToString(".")
        }
        properties.setProperty("version", str)
        return str
    }
}

private object NoIncrementer : Incrementer {
    override fun increment(properties: Properties): String {
        return ""
    }
}
