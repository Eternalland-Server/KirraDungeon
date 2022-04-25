val libVersion: String by project
val projectVersion: String by project

plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
}

group = "net.sakuragame.eternal.kirradungeon.common"
version = projectVersion

taboolib {
    description {
        contributors {
            name("闲蛋")
        }
        dependencies {
            name("KirraDungeonClient").optional(true)
            name("KirraDungeonServer").optional(true)
        }
    }
    install("common")
    install("common-5")
    install("module-lang")
    install("module-configuration")
    install("module-chat")
    install("platform-bukkit")
    classifier = null
    version = libVersion
}

repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/maven-releases/") }
    mavenCentral()
}

dependencies {
    compileOnly(project(":KirraDungeonClient"))
    compileOnly(project(":KirraDungeonServer"))
    compileOnly("ink.ptms.core:v11200:11200")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}