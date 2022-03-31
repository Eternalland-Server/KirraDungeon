val libVersion: String by project
val projectVersion: String by project

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

group = "net.sakuragame.eternal.kirradungeon.plot"
version = projectVersion

taboolib {
    description {
        dependencies {
            name("MythicMobs")
            name("DataManager")
            name("KirraCoreBukkit")
            name("DungeonServer")
            name("DragonCore")
            name("JustAttribute")
            name("UIFactory")
            name("JustMessage")
        }
    }
    install("common")
    install("common-5")
    install("module-configuration")
    install("module-chat")
    install("platform-bukkit")
    classifier = null
    version = libVersion
}

repositories {
    maven {
        credentials {
            username = "a5phyxia"
            password = "zxzbc13456"
        }
        url = uri("https://maven.ycraft.cn/repository/maven-snapshots/")
    }
    maven {
        url = uri("https://repo.tabooproject.org/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("public:MythicMobs:4.12.0@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.1.7-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:NergiganteScript:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DungeonSystem-Server-API:1.1.3-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.0.9-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.4.8-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustAttribute:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:BetonQuest:1.13.0-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.0-SNAPSHOT@jar")
    compileOnly("ink.ptms.core:v11200:11200")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}