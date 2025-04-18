import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.0-SNAPSHOT"
}

group = "com.github.muran2"
version = "25-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.8.4")
    implementation( "com.squareup.okhttp3:okhttp:3.14.9")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:2.23.4")

    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("Git4Idea")
        instrumentationTools()
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "223"
            untilBuild = "343.*"
        }
        changeNotes = """
            <ul>
            <li></li>
            </ul>
        """.trimIndent()
    }
//    publishing {
//        val intellijPublishToken: String by project
//        token = intellijPublishToken
//    }
    pluginVerification {
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
                channels = listOf(ProductRelease.Channel.RELEASE)
                sinceBuild = "223"
                untilBuild = "343.*"
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    buildPlugin {
        // Config for the output .zip file containing the .jar
    }
}