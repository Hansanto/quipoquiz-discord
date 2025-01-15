import de.comahe.i18n4k.gradle.plugin.i18n4k
import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask

plugins {
    libs.plugins.run {
        alias(kt.multiplatform)
        alias(kt.serialization)
        alias(detekt)
        alias(ktlint)
        alias(graphql)
        alias(i18n)
        alias(kotest)
        alias(mock)
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

val reportFolder = rootProject.file("reports")
val graphqlSchemaFile = file("src/commonMain/graphql/quipoquiz.graphqls")
val generatedSources = layout.buildDirectory.dir("generated/sources").get()

detekt {
    ignoreFailures = System.getenv("DETEKT_IGNORE_FAILURES")?.toBooleanStrictOrNull() ?: false
    config.from(file("detekt.yml"))
    reportsDir = reportFolder.resolve("detekt")
}

ktlint {
    filter {
        exclude("**/generated/**")
    }
}

val jvmTargetVersion = JvmTarget.JVM_21

kotlin {
    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }

    jvmToolchain(jvmTargetVersion.target.toInt())

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = jvmTargetVersion
        }

        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    js {
        binaries.executable()
        nodejs()
        useCommonJs()
    }

    // wasmJs()
    // wasmWasi()

    // Native tiers: https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()

    // Tier 2
    // linuxX64()
    // linuxArm64()
    watchosSimulatorArm64()
    // watchosX64()
    // watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    // Tier 3
    // androidNativeArm32()
    // androidNativeArm64()
    // androidNativeX86()
    // androidNativeX64()
    // mingwX64()
    // watchosDeviceArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries {
            executable()
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("dev.kord.common.annotation.KordPreview")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.logging)
                implementation(libs.bundles.i18n.common)
                implementation(libs.bundles.graphql.client)
                implementation(libs.bundles.kt.common)
                implementation(libs.kord)
                implementation(libs.kord.emoji)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.bundles.kotest.common)
                implementation(libs.graphql.mockserver)

                // Remove dependencies when https://github.com/apollographql/apollo-kotlin/pull/5923
                implementation(libs.logging)
                implementation(libs.i18n)
                implementation(libs.bundles.graphql.client)
                implementation(libs.bundles.kt.common)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.slf4j.simple)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.junit5)
                implementation(libs.mockk)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(libs.kt.node)
            }
        }
        val jsTest by getting
    }
}

tasks {
    register("generateSources") {
        group = "build"
        description = "Generate all sources"
        if (!graphqlSchemaFile.exists()) {
            dependsOn("downloadQuipoquizApolloSchemaFromIntrospection")
        }
        dependsOn(generateApolloSources)
        dependsOn(generateI18n4kFiles)
    }

    register("detektAll") {
        group = JavaBasePlugin.VERIFICATION_GROUP
        allprojects {
            this@register.dependsOn(tasks.withType<Detekt>())
        }
    }

    configure<KtlintExtension> {
        reporters {
            reporter(ReporterType.HTML)
            reporter(ReporterType.CHECKSTYLE)
        }
    }

    withType<GenerateReportsTask> {
        reportsOutputDirectory.set(reportFolder.resolve("klint/$name"))
    }

    withType<Detekt>().configureEach {
        jvmTarget = jvmTargetVersion.target
        exclude("**/generated/**")
        mustRunAfter(generateI18n4kFiles)

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

apollo {
    service("quipoquiz") {
        packageName.set("$group.generated.graphql")

        outputDir.set(generatedSources.dir("apollo"))

        introspection {
            val token = System.getenv("QUIPOQUIZ_TOKEN")
            val url = System.getenv("QUIPOQUIZ_URL") ?: "https://cms.quipoquiz.com/api"

            endpointUrl.set(url)
            headers.set(mapOf("Authorization" to "Bearer $token"))
            schemaFile.set(graphqlSchemaFile)
        }
    }
}

i18n4k {
    this.packageName = "$group.generated.i18n"
    sourceCodeOutputDirectory = generatedSources.dir("i18n4k").asFile.path
    this.inputDirectory = "src/commonMain/resources/i18n"
}
