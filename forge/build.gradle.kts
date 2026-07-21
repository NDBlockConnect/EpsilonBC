import com.github.gmazzo.buildconfig.generators.BuildConfigKotlinGenerator

plugins {
    java
    `maven-publish`
    alias(libs.plugins.forgegradle)
    alias(libs.plugins.parchment.librarian)
    alias(libs.plugins.buildconfig)
}

val minecraftVersion: String by project.extra
val forgeVersion: String by project.extra
val parchmentVersion: String by project.extra
val javaVersion: String by project.extra

base {
    archivesName = "epsilon-forge"
    group = "com.tectato.epsilon"
    version = "1.0.0"
}

minecraft {
    mappings("parchment", parchmentVersion)
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    implementation(project(":common"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = javaVersion.toInt()
}

buildConfig {
    className("EpsilonBuildConfig")
    packageName("com.tectato.epsilon")
    useKotlinOutput {
        topLevelConstants = true
        internalVisibility = true
    }
    generator(BuildConfigKotlinGenerator(internalVisibility = true))

    buildConfigField("MOD_ID", "epsilon")
    buildConfigField("MOD_NAME", "Epsilon")
    buildConfigField("MOD_VERSION", provider { "${project.version}" })
    buildConfigField("MINECRAFT_VERSION", provider { minecraftVersion })
    buildConfigField("FORGE_VERSION", provider { forgeVersion })
}

publishing {
    publications {
        create<MavenPublication>("mavenForge") {
            from(components["java"])
        }
    }
}
