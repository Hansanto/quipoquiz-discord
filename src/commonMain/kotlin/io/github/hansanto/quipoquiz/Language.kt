package io.github.hansanto.quipoquiz

import de.comahe.i18n4k.createLocale
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.discord.extension.I18nLocale
import io.github.hansanto.quipoquiz.discord.framework.extension.KordLocale
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizSiteId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Perform an action for each language and associated locale.
 * @param action The action to perform.
 * @receiver The [Language] companion object.
 */
inline fun Language.Companion.withLocale(action: (I18nLocale, KordLocale) -> Unit) {
    Language.entries.forEach { language ->
        val i18nLocale = language.i18nLocale
        language.kordLocales.forEach {
            action(i18nLocale, it)
        }
    }
}

/**
 * Get the language from the locale.
 * @param locale The locale to get the language from.
 * @return The language from the locale or the default language if the locale is not found.
 */
fun Language.Companion.from(locale: KordLocale?): Language {
    return Language.entries.firstOrNull { locale in it.kordLocales } ?: BotConfiguration.defaultLanguage
}

/**
 * Get the language by matching the name of the enum, the i18n locale, or the Kord locale.
 * @param key The key to match the language.
 * @return The language if found, otherwise null.
 */
fun Language.Companion.from(key: String): Language? {
    return Language.entries.find {
        it.name.equals(key, ignoreCase = true) || // FRENCH, ENGLISH
            it.i18nLocale.getLanguage().equals(key, ignoreCase = true) || // fr, en
            it.kordLocales.any { kordLocale -> kordLocale.language.equals(key, ignoreCase = true) }
    }
}

@Serializable
enum class Language(
    val site: QuipoQuizSiteId,
    @Transient val i18nLocale: I18nLocale,
    @Transient val kordLocales: Collection<KordLocale>
) {

    /**
     * French language.
     */
    FRENCH(
        QuipoQuizSiteId.FR,
        createLocale(language = "fr"),
        listOf(KordLocale.FRENCH)
    ),

    /**
     * English language.
     */
    ENGLISH(
        QuipoQuizSiteId.EN,
        createLocale(language = "en"),
        listOf(KordLocale.ENGLISH_GREAT_BRITAIN, KordLocale.ENGLISH_UNITED_STATES)
    )
}
