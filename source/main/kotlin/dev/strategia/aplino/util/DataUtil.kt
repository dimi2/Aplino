package dev.strategia.aplino.util

import java.io.Closeable
import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.ceil
import kotlin.math.pow

/**
 * Provide common methods for generic data manipulations:
 * - Ensure that a text matches given regex pattern.
 * - Close streams (and other closeable objects) without try-catch ceremony.
 * - Round numbers to specific precision, without string conversion and temp objects.
 */
open class DataUtil {
    companion object {
        /** Empty string instance.  */
        const val EMPTY_STRING = ""

        /**
         * Make sure that the provided value matches predefined regex syntax. The benefit is -
         * single line check.
         * @param value The value to be checked.
         * @param regex Regex to be ensured to match the value.
         *
         * @throws IllegalArgumentException if the provided value does not match the required
         * regex pattern.
         */
        fun ensureSyntax(value: String?, regex: String?) {
            var re: Pattern? = null
            if (regex != null) {
                re = Pattern.compile(regex)
            }
            ensureSyntax(value, re)
        }

        /**
         * Make sure that the provided value matches predefined regex syntax. The benefit is -
         * single line check.
         * @param value The value to be checked.
         * @param regex Regex to be ensured to match the value.
         * @throws IllegalArgumentException if the provided value does not match the required
         * regex pattern.
         */
        fun ensureSyntax(value: String?, regex: Pattern?) {
            if (regex != null && !value.isNullOrEmpty()) {
                if (!regex.matcher(value).matches()) {
                    throw IllegalArgumentException("Invalid value '$value'")
                }
            }
        }

        /**
         * Close specified stream or any closable object. There is a mistake in Java IO design -
         * it forces the programmer to catch exception during stream close, but we can do nothing in
         * such case. This method saves writing unnecessary code to handle closing exception.
         * @param stream The stream to close.
         */
        fun close(stream: Closeable?) {
            if (stream != null) {
                try {
                    stream.close()
                }
                catch (_: IOException) {
                    // Ignored. What could we possibly do?
                }
            }
        }

        /**
         * Round specified float point number to desired precision.
         *
         * Example: for d = `17.4960` and precision = 2, the result will be `17.50`.
         *
         * It is useful to avoid endless fraction numbers like 2.6457513110645905905016157536393.
         * @param d The float number.
         * @param precision Desired precision (digits after the point).
         * @return Rounded up number.
         */
        fun round(d: Double, precision: Int): Double {
            val fraction = 10.0.pow(precision.toDouble())
            return ceil(d * fraction) / fraction
        }

        /**
         * Mask specified array using simple XOR conversion. It is, just to prevent searching for the
         * string in the compiled class file. Calling the method second time (with same mask),
         * returns the original string.
         * @param data The data to be masked.
         * @param mask XOR mask.
         */
        fun mask(data: CharArray, mask: CharArray) {
            for (i in data.indices) {
                data[i] = (data[i].code xor mask[i % mask.size].code).toChar()
            } //
        }

        /**
         * Convert string with asterisk wildcards (*) into regex string.
         * Example: 'az*' becomes '^az.*?'.
         * The wildcard character is escaped with backslash (\).
         * @param value String containing wildcards.
         * @return Corresponding regular expression.
         */
        fun toRegexString(value: String?): String? {
            var re = value
            if (value != null) {
                val wildcard = "*"
                val reWildcard = ".*?"
                if (re.contains(wildcard)) {
                    re = value.replace("(?<!\\\\)\\$wildcard".toRegex(), reWildcard)
                    if (re.startsWith(reWildcard) && !re.endsWith(reWildcard)) {
                        re += "$"
                    }
                    if (!re.startsWith(reWildcard) && re.endsWith(reWildcard)) {
                        re = "^$re"
                    }
                }
            }
            return re
        }

        /**
         * Get two letter language code for the specified locale.
         * @param locale The locale.
         * @return Locale language code (like 'en').
         */
        fun getLocaleCode(locale: Locale?): String? {
            var localeCode: String? = null
            if (locale != null) {
                localeCode = locale.language
            }
            return localeCode
        }

        /**
         * Parse two letter locale code from the input string.
         * @param xxCode Locale input string (like 'en_US').
         * @return Locale language code (like 'en').
         */
        fun getLocaleCode(xxCode: String?): String? {
            var ret: String? = null
            if (!xxCode.isNullOrEmpty()) {
                if (xxCode.length >= 2) {
                    ret = xxCode.lowercase(Locale.getDefault()).substring(0, 2)
                }
            }
            return ret
        }

    }
}
