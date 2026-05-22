package dev.strategia.aplino.text

import dev.strategia.aplino.error.DataTransferException
import dev.strategia.aplino.util.FileUtil
import dev.strategia.aplino.util.PropertiesFileParser
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Loader for localized text messages, stored in 'properties' files.
 * We use improved version of the file format, so we use extension 'res' for them.
 */
open class PropertiesTextProvider : TextProvider {
    var fileExtension: String? = "res"
    protected var baseDir: String = ""
    protected var texts = mutableMapOf<String, Map<String, String>>()

    constructor(resLocation: String) {
        val f = File(resLocation).absoluteFile
        if (!f.isDirectory || !f.canRead()) {
            throw IllegalArgumentException("Cannot access resource location '$f'."
                + " Check the directory name and the access permissions.")
        }

        this.baseDir = f.absolutePath
    }

    override fun getTextEntry(key: String, locale: String): TextEntry? {
        // Find the message.
        var messages: Map<String, String>? = texts[locale]
        if (messages == null) {
            // No texts for this language. Try to init them.
            loadResources(baseDir, locale)
            messages = texts[locale]
        }
        var ret: TextEntry? = null
        if (messages != null) {
            val message = messages[key]
            if (message != null) {
                ret = createEntry(key, message)
            }
        }
        return ret
    }

    override fun getTextEntries(keyPattern: String?, locale: String): List<TextEntry> {
        val ret = ArrayList<TextEntry>()
        var messages: Map<String, String>? = texts[locale]
        if (messages == null) {
            // No texts for this language. Try to init them.
            loadResources(baseDir, locale)
            messages = texts[locale]
        }
        if (messages != null) {
            for (key in messages.keys) {
                if (keyPattern == null || key.matches(keyPattern.toRegex())) {
                    val text = messages[key]
                    if (text != null) {
                        val entry = createEntry(key, text)
                        ret.add(entry)
                    }
                }
            } //
        }
        return ret
    }

    open fun clearTextEntries() {
        texts.clear()
    }

    /**
     * Load texts for specified locale. If the locale is not available, search texts for
     * similar and default locales (accessible in classpath).
     *
     * The Properties files contain texts by locale. We have to load the texts in bulk for each locale, and
     * cache them. The text conversion to TextEntry is postponed until required. Thus, we will not create
     * text entries for unused texts (but we expect the caller to cache the results).
     * @param directory The resource directory.
     * @param locale Locale name.
     */
    @Synchronized
    protected open fun loadResources(directory: String, locale: String) {
        val parser = PropertiesFileParser()
        val fileMask = getFileMask(locale, fileExtension)
        val files = FileUtil.collectFiles(directory, fileMask, true)
        try {
            val messages = linkedMapOf<String, String>()
            for (file in files) {
                val mes = parser.readFile(FileInputStream(file))
                @Suppress("UNCHECKED_CAST")
                messages.putAll(mes as Map<String, String>)
            } //
            texts[locale] = messages
        } catch (e: FileNotFoundException) {
            throw DataTransferException("Error reading resource file from '$directory'", e)
        }
    }

    protected open fun getFileMask(locale: String?, extension: String?): String? {
        var ret: String? = null
        if (!extension.isNullOrEmpty()) {
            ret = ".+"
            if (!locale.isNullOrBlank()) {
                ret += "_$locale"
            }
            ret += "\\.$extension"
        }
        return ret
    }

    protected open fun createEntry(key: String, text: String): TextEntry {
        return TextEntry(key, text)
    }

}
