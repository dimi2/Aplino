package dev.strategia.aplino.text

/**
 * Resource text provider which just returns the requested resource key.
 * It can be used in early development phases when the resource messages are not yet created.
 */
open class BaseTextService: TextService {
    protected var textProvider: TextProvider? = null
    protected var defaultLocale: String
    protected var textCache = mutableMapOf<String, MutableMap<String, TextEntry>>()
    protected var unknownPrefix: String = ""

    constructor(defaultLocale: String, provider: TextProvider? = null) {
        this.defaultLocale = defaultLocale
        if (provider != null) {
            textProvider = provider
        }
    }

    override fun getText(key: String, locale: String, vararg args: Any): String {
        val ret: String
        // Get the appropriate text entry.
        val textEntry = getEntry(key, locale)

        // Get the text.
        if (args.isEmpty()) {
            // No text arguments. Return the result as is.
            ret = textEntry.text
        } else {
            // There are text arguments to embed into the result.
            ret = format(textEntry.text, *args)
        }
        return ret
    }

    override fun getTextEntry(key: String, locale: String): TextEntry {
        return getEntry(key, locale)
    }

    override fun getTextEntries(keyPattern: String, locale: String): List<TextEntry> {
        // This call will be rare. Do not cache.
        return textProvider!!.getTextEntries(keyPattern, locale)
    }

    override fun start() {
        textCache.clear()
        if (textProvider == null) {
            textProvider = createTextProvider("")
        }
    }

    /**
     * Create new text entry.
     * @param locale Locale for the entry.
     * @param key Unique key for the entry.
     * @param text Text for the entry.
     * @return The created text entry.
     */
    @Synchronized
    protected open fun createEntry(locale: String, key: String, text: String): TextEntry {
        var entries = textCache[locale]
        if (entries == null) {
            entries = linkedMapOf()
            textCache[locale] = entries
        }
        val entry = TextEntry(key, text)
        entries[key] = entry
        return entry
    }

    /**
     * Get text entry for specified key and locale.
     * @param key Desired entry key.
     * @param locale Desired locale.
     * @return Corresponding text entry. It is never null - if such entry does not exist, new one
     * will be created with text value equals to the key (and optionally prefixed with
     * the configured 'unknownPrefix' value).
     */
    protected open fun getEntry(key: String, locale: String): TextEntry {
        var ret = lookupEntry(key, locale)
        if (ret == null) {
            // Fall back to the parent locale (e.g. 'en_us' -> 'en'), then to the default locale.
            val parent = getParentLocale(locale)
            if (parent != locale) {
                ret = lookupEntry(key, parent)
            }
            if (ret == null && defaultLocale != locale && defaultLocale != parent) {
                ret = lookupEntry(key, defaultLocale)
            }
            // Cache the resolved text (or an 'unknown' placeholder) under the requested locale,
            // to shorten subsequent requests.
            val resolved = ret?.text ?: (unknownPrefix + key)
            ret = createEntry(locale, key, resolved)
        }
        return ret
    }

    /**
     * Look up a text entry for the given key and locale, checking the cache first, then the provider.
     * @param key Desired entry key.
     * @param locale Desired locale.
     * @return Corresponding text entry, or null if it does not exist for this locale.
     */
    protected open fun lookupEntry(key: String, locale: String): TextEntry? {
        val localeEntries = textCache[locale]
        var ret = localeEntries?.get(key)
        if (ret == null) {
            ret = textProvider!!.getTextEntry(key, locale)
        }
        return ret
    }

    /**
     * Create formatted text using indexed placeholders for variables.
     * @param template Template string containing placeholders like "{0}", "{1}" etc.
     * @param args Arguments to be embedded into the template.
     * @return Formatted string.
     */
    protected open fun format(template: String, vararg args: Any): String {
        val sb = StringBuilder(template.length + args.size * 10)
        var i = 0
        while (i < template.length) {
            val ch = template[i]
            if (ch == '{' && i + 2 < template.length && template[i + 1].isDigit()) {
                val end = template.indexOf('}', i)
                if (end != -1) {
                    val index = template.substring(i + 1, end).toIntOrNull()
                    if (index != null && index < args.size) {
                        sb.append(args[index])
                        i = end + 1
                        continue
                    }
                }
            }
            sb.append(ch)
            i++
        }
        return sb.toString()
    }

    /**
     * Create default text provider.
     * @param resLocation Text resources location (usually directory name).
     * @return Default text provider instance.
     */
    protected open fun createTextProvider(resLocation: String): TextProvider {
        return PropertiesTextProvider(resLocation)
    }

    /**
     * Get parent locale of specified locale.
     * @param fLocale Full locale code in lowercase (like 'en_us', 'zh-sg', 'haw').
     * @return Parent locale code. For example: 'en_us' = 'en', 'zh-sg' = 'zh', 'haw' = 'haw'.
     */
    protected open fun getParentLocale(fLocale: String): String {
        var loc = fLocale
        for ((i, ch) in fLocale.withIndex()) {
            if ((ch == '-') || (ch == '_')) {
                loc = fLocale.substring(0, i)
                break
            }
        } //
        return loc
    }

}
