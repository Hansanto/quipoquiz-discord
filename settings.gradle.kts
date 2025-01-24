pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.1.0")
            version("kotlin-serialization", "1.8.0")
            version("kotlinx-io", "0.6.0")
            version("kotlinx-datetime", "0.6.1")
            version("kotest", "6.0.0.M1")
            version("logging", "7.0.3")
            version("slf4j", "2.0.16")
            version("detekt", "1.23.7")
            version("ktlint", "12.1.2")
            version("graphql", "4.1.1")
            version("graphql-ktor", "0.1.1")
            version("graphql-mockserver", "0.1.0")
            version("kord", "feature-native-SNAPSHOT")
            version("kord-emoji", "feature-mpp-SNAPSHOT")
            version("i18n", "0.10.0")
            version("mock", "2.6.1")
            version("mockk", "1.13.16")

            plugin("kt-multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("kt-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kotest", "io.kotest.multiplatform").versionRef("kotest")
            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").versionRef("ktlint")
            plugin("graphql", "com.apollographql.apollo").versionRef("graphql")
            plugin("i18n", "de.comahe.i18n4k").versionRef("i18n")
            plugin("mock", "dev.mokkery").versionRef("mock")

            library("kt-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlin-serialization")
            library("kt-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").versionRef("kotlinx-datetime")
            library("kt-io", "org.jetbrains.kotlinx", "kotlinx-io-core").versionRef("kotlinx-io")
            library("kt-node", "org.jetbrains.kotlin-wrappers", "kotlin-node").version {
                // Version used in com.apollographql.apollo
                // https://github.com/search?q=repo%3Aapollographql%2Fapollo-kotlin+kotlin-node&type=code
                // This is a workaround until Apollo uses a version that is compatible with the kord version.
                strictly("18.16.12-pre.634")
            }

            library("graphql-ktor", "com.apollographql.ktor", "apollo-engine-ktor").versionRef("graphql-ktor")
            library("graphql-mockserver", "com.apollographql.mockserver", "apollo-mockserver").versionRef("graphql-mockserver")

            library("kord", "dev.kord", "kord-core").versionRef("kord")
            library("kord-emoji", "dev.kord.x", "emoji").versionRef("kord-emoji")

            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")
            library("logging", "io.github.oshai", "kotlin-logging").versionRef("logging")

            library("i18n", "de.comahe.i18n4k", "i18n4k-core").versionRef("i18n")
            library("i18n-plural", "de.comahe.i18n4k", "i18n4k-cldr-plural-rules").versionRef("i18n")

            library("kotest-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-engine", "io.kotest", "kotest-framework-engine").versionRef("kotest")
            library("kotest-junit5", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-json", "io.kotest", "kotest-assertions-json").versionRef("kotest")

            library("mockk", "io.mockk", "mockk").versionRef("mockk")

            bundle("graphql-client", listOf("graphql-ktor"))
            bundle("kotest-common", listOf("kotest-core", "kotest-engine", "kotest-json"))
            bundle("kt-common", listOf("kt-serialization-json", "kt-io", "kt-datetime"))
            bundle("i18n-common", listOf("i18n", "i18n-plural"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "quipoquiz-discord"
include("app:jvm")
findProject(":app:jvm")?.name = "jvm"
