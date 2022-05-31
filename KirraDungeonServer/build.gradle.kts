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
            name("DataManager")
            name("KirraDungeonCommon")
            name("KirraCoreBukkit")
            name("DungeonServer")
            name("DragonCore")
            name("JustAttribute")
            name("UIFactory")
            name("Adyeshach")
            name("KirraMiner")
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
    install("expansion-command-helper")
    install("module-database")
    install("module-ui")
    install("module-nms", "module-nms-util")
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
    maven { url = uri("https://lss233.littleservice.cn/repositories/minecraft/") }
    mavenCentral()
}

dependencies {
    compileOnly("public:MythicMobs:4.12.0@jar")
    compileOnly("net.sakuragame:DungeonSystem-Server-API:1.1.3-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DataManager-Bukkit-API:1.3.2-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.2.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.4.8-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustAttribute:1.0.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.0-SNAPSHOT@jar")
    compileOnly("biz.paluch.redis:lettuce:4.1.1.Final@jar")
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
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