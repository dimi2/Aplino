package dev.strategia.aplino.util

import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.FileWriter

/**
 * Properties file writer, which does not use [java.util.Properties] class, but custom one, which does not
 * make Unicode escape chars (\uXXXX) and keeps the order of the properties as is.
 */
open class PropertiesFileWriter : Closeable {
    protected val file: File
    protected val writer: BufferedWriter
    protected var commentPrefix = "#"
    protected var valueSeparator = " = "

    constructor(fileName: String) {
        file = File(fileName).absoluteFile
        writer = BufferedWriter(FileWriter(file))
    }

    fun writeComment(comment: String?) {
        if (comment != null) {
            writer.write(commentPrefix)
            writer.write(comment)
            writer.newLine()
        }
    }

    fun writeProperty(name: String, value: Any?) {
        writer.write(name)
        writer.write(valueSeparator)
        writer.write(asString(value))
        writer.newLine()
    }

    fun writeFile(lines: Map<String, Any?>) {
        for (entry in lines) {
            writeProperty(entry.key, entry.value)
        } //
    }

    override fun close() {
        writer.close()
    }

    protected fun asString(value: Any?): String {
        var res: String? = null
        if (value != null) {
            res = value.toString()
        }
        return res ?: ""
    }
}
