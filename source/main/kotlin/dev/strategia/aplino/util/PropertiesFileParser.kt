package dev.strategia.aplino.util

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Properties file parser. This implementation works with standard java properties file, but it
 * does not use [java.util.Properties] class. This implementation has multiple advantages:
 * - Keep Unicode characters without escaping (\uXXXX).
 * - Preserve original key order.
 * - Supports Ruby-style multi-line values. For example:
 * ```
 *     key = << END_MARK
 *     value1
 *     value2
 *     END_MARK
 * ```
 * The "END_MARK" marker says where the value ends (it could be any string).
 */
open class PropertiesFileParser {

    /**
     * Read resource file. Expected encoding is UTF-8.
     * @param fileName The file name.
     * @return Map of resource keys and their values.
     */
    open fun readFile(fileName: String): Map<String, Any?> {
        val f = File(fileName)

        // Read from the file.
        val ret: Map<String, Any?>
        try {
            ret = readFile(FileInputStream(f))
        }
        catch (e: FileNotFoundException) {
            throw IllegalArgumentException("Cannot find resource file '${f.absolutePath}'", e)
        }

        return ret
    }

    /**
     * Read resource file. Expected defaultEncoding "UTF-8".
     * @param res File data.
     * @return Map of resource keys and their values.
     */
    open fun readFile(res: InputStream): MutableMap<String, Any?> {
        val multiLineMarker = "<<"
        val ret = linkedMapOf<String, Any?>()
        try {
            val f = BufferedReader(InputStreamReader(res))
            var isMultiLineMode = false
            var endMarker: String? = null
            var multiKey: String? = null
            val buf = StringBuilder(res.available())
            var line: String?
            var row = 0
            while (true) {
                line = f.readLine()
                if (line == null) {
                    break
                }
                row++
                if (line.isBlank()) {
                    // Empty line. Skip.
                    continue
                }
                if (row == 1) {
                    // Can have UTF-8 BOM signature.
                    if (line[0] == '\ufeff') {
                        // Strip it.
                        line = line.substring(1)
                    }
                }
                if (line[0] == '#') {
                    // Comment line. Skip it.
                    continue
                }

                val key: String
                var value: String?
                if (!isMultiLineMode) {
                    // Single line value.
                    val idx = line.indexOf('=')
                    if (idx == -1) {
                        // No '=' char. Wrong input data.
                        throw IllegalArgumentException("Resource file error at line $row")
                    }
                    key = line.take(idx).trim()
                    value = line.substring(idx + 1).trim()
                    if (value.startsWith(multiLineMarker)) {
                        // Multi-line start.
                        isMultiLineMode = true
                        multiKey = key
                        endMarker = value.substring(multiLineMarker.length).trim()
                        continue
                    }
                    // Replace empty value with null.
                    if (value.isEmpty()) {
                        value = null
                    }
                    ret[key] = value
                } else {
                    // Multi-line value.
                    if (line.startsWith(endMarker!!)) {
                        // Multi-line end.
                        ret[multiKey!!] = buf.toString()
                        isMultiLineMode = false
                        endMarker = null
                        buf.delete(0, buf.length)
                        continue
                    }
                    // Collect multi-line content.
                    buf.append(line)
                    buf.append("\n")
                }
            } //
            f.close()
        }
        catch (exc: IOException) {
            throw RuntimeException("Error reading resource data", exc)
        }

        return ret
    }

}
