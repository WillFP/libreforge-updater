package com.willfp.libreforgeupdater

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

private val runtime: Runtime = Runtime.getRuntime()

fun exec(
    command: String,
    dir: File
) {
    val proc = runtime.exec(command, null, dir)
    val input = StreamGobbler(proc.inputStream)
    val error = StreamGobbler(proc.errorStream)

    input.start()
    error.start()
    proc.waitFor()
}

// Shamelessly stolen from mr. overflow
class StreamGobbler(
    private val stream: InputStream
) : Thread() {
    override fun run() {
        try {
            val isr = InputStreamReader(stream)
            val br = BufferedReader(isr)
            var line: String?
            while (br.readLine().also { line = it } != null) println(line)
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }
}
