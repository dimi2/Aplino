package dev.strategia.aplino.text

/**
 * Load application message texts. Decouples the text service from the text storage mechanism.
 */
interface TextProvider {

    /**
     * Get the text entry for specified key.
     * @param key Unique key of the desired text.
     * @param locale Desired locale.
     * @return The text for this locale (null if no such text).
     */
    fun getTextEntry(key: String, locale: String): TextEntry?

    /**
     * Get all the textCache for specified locale.
     * @param keyPattern Regex selector for the required setting names. Null means 'all'.
     * @param locale Desired locale.
     * @return Map of textCache. Key = textKey, Value = textValue.
     */
    fun getTextEntries(keyPattern: String?, locale: String): List<TextEntry>

}
