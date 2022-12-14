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
            name("KirraDungeonCommon")
            name("Zaphkiel")
            name("KirraPartyBukkit")
            name("KirraCoreBukkit")
            name("DungeonClient")
            name("DataManager-Bukkit")
            name("KirraCoreBukkit")
            name("JustAttribute")
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
    install("module-database")
    install("platform-bukkit")
    install("expansion-command-helper")
    install("expansion-player-database")
    classifier = null
    version = libVersion
}

repositories {
    maven { url = uri("https://repo.tabooproject.org/repository/maven-releases/") }
    maven { url = uri("https://repo1.maven.org/maven2/net/luckperms/api/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:2.0.1-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:GemsEconomy:4.9.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame:DungeonSystem-Client-API:1.1.3-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:KirraCore-Bukkit:1.2.0-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:DragonCore:2.4.8-SNAPSHOT@jar")
    compileOnly("com.taylorswiftcn:UIFactory:1.0.0-SNAPSHOT@jar")
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    compileOnly("net.sakuragame:datamanager-bukkit-api:2.0.0-SNAPSHOT") {
        isTransitive = true
    }
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