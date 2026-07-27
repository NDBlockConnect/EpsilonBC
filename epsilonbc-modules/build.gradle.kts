plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    // 依赖 epsilon-core
    api(project(":epsilon-core"))

    // Jetbrains annotations
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
