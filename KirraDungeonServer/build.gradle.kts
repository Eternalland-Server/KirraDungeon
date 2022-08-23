val libVersion: String by project
val projectVersion: String by project

plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
}

group = "net.sakuragame.eternal.kirradungeon.server"
version = projectVersion

taboolib {
    description {
        dependencies {
            name("MythicMobs")
            name("MythicMobsExtension")
            name("DataManager-Bukkit")
            name("KirraDungeonCommon")
            name("KirraCoreBukkit")
            name("DungeonServer")
            name("DragonCore")
            name("JustAttribute")
            name("UIFactory")
            name("Adyeshach")
            name("KirraMiner")
            name("Skript")
            name("Waypoints")
        }
        contributors {
            name("闲蛋")
        }
    }
    install("common")
    install("common-5")
    install("module-lang")
    install("module-configuration")
    install("module-chat")
    install("platform-bukkit")
    install("module-database")
    install("module-ui")
    install("module-nms", "module-nms-util")
    install("expansion-command-helper")
    install("expansion-player-database")
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
        url = uri("https://repo.skriptlang.org/releases")
    }
    mavenCentral()
}

dependencies {
    compileOnly("net.sakuragame.eternal:Waypoints:1.1.0-SNAPSHOT@jar")
    compileOnly("com.github.SkriptLang:Skript:2.6.1")
    compileOnly("public:MythicMobs:4.12.0@jar")
    compileOnly("net.sakuragame:DungeonSystem-Server-API:1.1.3-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:2.0.2-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.6.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustAbility:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustAttribute:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustLevel:1.1.8-SNAPSHOT@jar")
    compileOnly("ink.ptms.core:v11200:11200@jar")
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    compileOnly("net.sakuragame:datamanager-bukkit-api:2.0.0-SNAPSHOT") {
        isTransitive = true
    }
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinCompile::class).all {
    kotlinOptions {
        // For creation of default methods in interfaces
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}