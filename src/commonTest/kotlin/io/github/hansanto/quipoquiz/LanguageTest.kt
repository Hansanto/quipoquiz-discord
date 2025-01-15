package io.github.hansanto.quipoquiz

import de.comahe.i18n4k.createLocale
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.discord.extension.I18nLocale
import io.github.hansanto.quipoquiz.discord.framework.extension.KordLocale
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LanguageTest : ShouldSpec({

    should("withLocale iterates over all languages and locales") {
        val maps = mutableMapOf<I18nLocale, MutableCollection<KordLocale>>()
        Language.withLocale { i18nLocale, kordLocale ->
            maps.getOrPut(i18nLocale) { mutableListOf() }.add(kordLocale)
        }

        maps.size shouldBe 2
        maps[Language.FRENCH.i18nLocale] shouldBe setOf(KordLocale.FRENCH)
        maps[Language.ENGLISH.i18nLocale] shouldBe setOf(
            KordLocale.ENGLISH_GREAT_BRITAIN,
            KordLocale.ENGLISH_UNITED_STATES
        )
    }

    should("from kordLocale returns the language from the locale") {
        Language.from(KordLocale.FRENCH) shouldBe Language.FRENCH
        Language.from(KordLocale.ENGLISH_GREAT_BRITAIN) shouldBe Language.ENGLISH
        Language.from(KordLocale.ENGLISH_UNITED_STATES) shouldBe Language.ENGLISH
    }

    should("from kordLocale returns the default language if the locale is not found") {
        Language.from(KordLocale.GERMAN) shouldBe BotConfiguration.defaultLanguage
        Language.from(null) shouldBe BotConfiguration.defaultLanguage
        Language.from(KordLocale.CZECH) shouldBe BotConfiguration.defaultLanguage
    }

    should("from key returns the language by matching the name of the enum") {
        Language.from("FRENCH") shouldBe Language.FRENCH
        Language.from("ENGLISH") shouldBe Language.ENGLISH
    }

    should("from key returns the language by matching the i18n locale") {
        Language.from("fr") shouldBe Language.FRENCH
        Language.from("en") shouldBe Language.ENGLISH
    }

    should("french has correct locale") {
        Language.FRENCH.i18nLocale shouldBe createLocale("fr")
        Language.FRENCH.kordLocales shouldBe listOf(KordLocale.FRENCH)
    }

    should("english has correct locale") {
        Language.ENGLISH.i18nLocale shouldBe createLocale("en")
        Language.ENGLISH.kordLocales shouldBe listOf(KordLocale.ENGLISH_GREAT_BRITAIN, KordLocale.ENGLISH_UNITED_STATES)
    }
})
