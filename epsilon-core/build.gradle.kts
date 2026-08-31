plugins {
    id("java-library")
    alias(libs.plugins.neoforged.moddev)
}

repositories {
    mavenCentral()
}

neoForge {
    neoFormVersion = project.property("neo_form_version").toString()
}

dependencies {
    // 第三方库
    api("com.google.code.gson:gson:2.10.1")
    api("org.joml:joml:1.10.5")

    // Jetbrains annotations
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

