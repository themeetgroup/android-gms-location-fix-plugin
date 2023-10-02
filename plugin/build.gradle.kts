plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    `java-gradle-plugin`
    `maven-publish`
}

group = "tmg.gradle"
version = "1.0-SNAPSHOT"

gradlePlugin {
    plugins {
        create("tmgLocationFixPlugin") {
            id = "tmg.gms.location.fix"
            implementationClass = "tmg.gradle.plugin.gms.location.TmgLocationFixPlugin"
        }
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(gradleApi())
    compileOnly("org.ow2.asm:asm:9.2")
    compileOnly("com.android.tools.build:gradle-api:7.4.2")
}

kotlin {
    explicitApi()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/themeetgroup/android-gms-location-fix-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
