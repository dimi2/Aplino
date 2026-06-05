package dev.strategia.aplino.util

/**
 * Parser for command line arguments. Expected format:
 * ```
 * -arg1 argValue1 -arg2 value3
 * ```
 * The parsing result comes as map. If specific argument does not have value, its value becomes null.
 */
open class CommandLineParser {
    protected var args = mutableMapOf<String, String?>()
    protected var parsedValues = mutableListOf<String>()
    protected var argPrefix = "-"

    constructor(args: Array<String>? = null) {
        if (args != null) {
            parse(args)
        }
    }

    /**
     * Parse specified command line arguments.
     * @param arguments Command line arguments.
     */
    open fun parse(arguments: Array<String>) {
        args = parseArguments(arguments)
    }

    /**
     * Check if specified argument was passed.
     * @param name Argument name.
     * @return True if this argument exists.
     */
    open fun hasArgument(name: String): Boolean {
        return args.containsKey(name)
    }

    /**
     * Get argument with specified name.
     * @param name Argument name.
     * @return Argument value or null.
     * @see parse
     */
    open fun getArgumentValue(name: String): String? {
        return getArgumentValue(name, null)
    }

    /**
     * Get argument with specified name.
     * @param name Argument name.
     * @param defaultValue Default value.
     * @return Argument value or the default value.
     * @see parse
     */
    open fun getArgumentValue(name: String, defaultValue: String?): String? {
        return args[name] ?: defaultValue
    }

    /**
     * Get parsed command line arguments.
     * @return Parsed arguments.
     * @see parse
     */
    open fun getArguments(): MutableMap<String, String?> {
        return args
    }

    /**
     * Get the command line values without the arguments. Example:
     * for `command -arg1 v1 arg2` the result will be [v1, arg2].
     * @return The value without argument name.
     */
    open fun getValues(): List<String> {
        return parsedValues
    }

    /**
     * Parse command line arguments (parameters). Parameters which contains spaces should be
     * enclosed with double quotas. Double quotas sign itself (if present in command value) should
     * be escaped with \.
     * @param args Command line arguments, passed to the application.
     * @return Map with parsed keys and values. Or empty map (if no command line options passed).
     */
    open fun parseArguments(args: Array<String>?): MutableMap<String, String?> {
        val opt = mutableMapOf<String, String?>()
        if (!args.isNullOrEmpty()) {
            var i = 0
            while (i < args.size) {
                val s = args[i]
                if (s.isBlank()) {
                    // Invalid argument. Skip it.
                    i++
                    continue
                }

                var sOption: String? = null
                var sValue: String? = null
                if (s.startsWith(argPrefix)) {
                    // Argument.
                    sOption = s.substring(argPrefix.length)
                    if (args.size - i > 1) {
                        // Argument value.
                        val ss = args[i + 1]
                        if (!ss.startsWith(argPrefix)) {
                            sValue = ss
                            i++
                        }
                    }
                } else {
                    // Value.
                    sValue = s
                }

                // Remove value enclosing quotas (if any) - either single or double quotes.
                val dQuota = "\""
                val sQuota = "'"
                val v = sValue
                if (v != null && v.length >= 2) {
                    val isDoubleQuoted = v.startsWith(dQuota) && v.endsWith(dQuota)
                    val isSingleQuoted = v.startsWith(sQuota) && v.endsWith(sQuota)
                    if (isDoubleQuoted || isSingleQuoted) {
                        sValue = v.substring(1, v.length - 1)
                    }
                }

                // Add to parameter map.
                if (sOption != null) {
                    opt[sOption] = sValue
                }
                if (sValue != null) {
                    parsedValues.add(sValue)
                }
                i++
            } //
        }

        return opt
    }

}
