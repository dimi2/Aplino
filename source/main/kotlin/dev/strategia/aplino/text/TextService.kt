package dev.strategia.aplino.text

import dev.strategia.aplino.application.AppService

/**
 * Application text service. It supports texts in different languages (localization).
 * The locale is represented as string in ISO format - two-letter code (like 'de' = German).
 */
interface TextService : AppService {
    /**
     * Get localized message text for specified locale.
     * @param key Unique message key.
     * @param locale Desired locale (null means 'the default locale').
     * @param args Additional message arguments (to be replaced into the message text).
     * @return Localized message text (or the key itself if there is no localization available).
     */
    fun getText(key: String, locale: String, vararg args: Any): String

    /**
     * Get text entry for specified locale.
     * @param key Unique message key.
     * @param locale Desired locale (null means 'the default locale').
     * @return Message text entry.
     */
    fun getTextEntry(key: String, locale: String): TextEntry

    /**
     * Get all the texts for specified locale.
     * @param keyPattern Regex selector for the required setting names. Null means 'all'.
     * @param locale Desired locale.
     * @return Map of texts. Key = textKey, Value = textValue.
     */
    fun getTextEntries(keyPattern: String, locale: String): List<TextEntry>
}
