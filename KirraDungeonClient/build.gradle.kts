val libVersion: String by project
val projectVersion: String by project

plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
}

group = "net.sakuragame.eternal.kirradungeon.client"
version = projectVersion

taboolib {
    description {
        dependencies {
            name("KirraParty")
            name("Zaphkiel")
            name("KirraCoreBukkit")
            name("DungeonClient")
            name("DataManager")
            name("KirraCoreBukkit")
            name("JustAttribute")
        }
    }
    install("common")
    install("common-5")
    install("module-lang")
    install("module-configuration")
    install("module-chat")
    install("module-database")
    install("platform-bukkit")
    install("expansion-command-helper")
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
    maven { url = uri("https://repo.tabooproject.org/repository/maven-releases/") }
    maven { url = uri("https://repo1.maven.org/maven2/net/luckperms/api/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.1.7-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:GemsEconomy:4.9.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:BetonQuest:1.13.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DungeonSystem-Client-API:1.1.3-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.1.8-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.4.8-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DataManager-Bukkit-API:1.3.2-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.0-SNAPSHOT@jar")
    compileOnly("biz.paluch.redis:lettuce:4.1.1.Final@jar")
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