package dev.strategia.aplino.text

/**
 * Loader for localized text messages, coming from in-memory map.
 */
open class MapTextProvider : TextProvider {
    protected val texts = mutableMapOf<String, Map<String, TextEntry>>()

    override fun getTextEntry(key: String, locale: String): TextEntry? {
        var entry: TextEntry? = null
        val localeMessages = texts[locale]
        if (localeMessages != null) {
            entry = localeMessages[key]
        }
        return entry
    }

    override fun getTextEntries(keyPattern: String?, locale: String): List<TextEntry> {
        val ret = arrayListOf<TextEntry>()
        val localeMessages = texts[locale]
        if (localeMessages != null) {
            ret.addAll(localeMessages.values)
        }
        return ret
    }

    open fun setMessages(forLocale: String, list: Map<String, String>) {
        val localeMessages = mutableMapOf<String, TextEntry>()
        list.forEach { m ->
            localeMessages[m.key] = TextEntry(m.key, m.value)
        }
        texts[forLocale] = localeMessages
    }

}
