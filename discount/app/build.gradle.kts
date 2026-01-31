plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
    kotlin("plugin.serialization") version "2.2.20"
    application
}
repositories {
    mavenCentral()
    mavenLocal()
}
dependencies {
    implementation(libs.bundles.common)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.database)
    testImplementation(libs.bundles.ktor.test)
    testImplementation(kotlin("test"))
}
java { toolchain { languageVersion = JavaLanguageVersion.of(22) } }
application { mainClass = "io.nexure.discount.ApplicationKt" }

openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/src/main/resources/openapi.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)
    apiPackage.set("io.nexure.discount.generated.api")
    modelPackage.set("io.nexure.discount.generated.model")
    configOptions.set(mapOf(
        "dateLibrary" to "java8",
        "serializationLibrary" to "kotlinx_serialization",
        "enumPropertyNaming" to "UPPERCASE"
    ))
    globalProperties.set(mapOf(
        "models" to "",
        "apis" to "false"
    ))
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging { events("passed", "skipped", "failed") }
    }
    
    compileKotlin {
        dependsOn("openApiGenerate")
    }
    
    jar {
        manifest {
            attributes["Main-Class"] = "io.nexure.discount.ApplicationKt"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }
}
