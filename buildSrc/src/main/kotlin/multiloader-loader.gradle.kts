plugins {
    id("multiloader-common")
}

val commonJava by configurations.creating {
    isCanBeResolved = true
}
val commonResources by configurations.creating {
    isCanBeResolved = true
}

val commonProject = project(":common")
val commonBuildConfig = commonProject.tasks.named("generateBuildConfigClasses")
val commonBuildConfigJava = commonProject.layout.buildDirectory.dir("generated/sources/buildConfig/main")

val epsilonCoreProject = project(":epsilon-core")
val epsilonCoreClasses = epsilonCoreProject.tasks.named<JavaCompile>("compileJava")
val epsilonCoreOutput = epsilonCoreProject.sourceSets.main.get().output

dependencies {
    val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
    compileOnly(project(":common")) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
    // epsilon-core 需要打包进最终 jar（运行时依赖）
    implementation(project(":epsilon-core"))

    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava, commonBuildConfig)
    source(commonJava)
    source(commonBuildConfigJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.named<Javadoc>("javadoc") {
    dependsOn(commonJava, commonBuildConfig)
    source(commonJava)
    source(commonBuildConfigJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)
    dependsOn(commonBuildConfig)
    from(commonBuildConfigJava)
    dependsOn(commonResources)
    from(commonResources)
    dependsOn(epsilonCoreClasses)
    from(epsilonCoreOutput)
}

tasks.named<Jar>("jar") {
    dependsOn(epsilonCoreClasses)
    from(epsilonCoreOutput)
    // epsilon-core 的类可能与 common 重复（common 依赖 epsilon-core 并重新编译了接口）
    // 使用 EXCLUDE 策略：保留第一次遇到的文件，忽略后续重复
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
