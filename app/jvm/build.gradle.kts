plugins {
    java
    application
}
//https://youtrack.jetbrains.com/issue/KT-41409/Gradle-Support-binaries.executable-for-jvm-targets

dependencies {
    implementation(rootProject)
}

application {
    mainClass = "io.github.hansanto.quipoquiz.MainKt"
}
