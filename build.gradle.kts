// SPDX-License-Identifier: Apache-2.0

fun lookupProperty(key: String) = project.findProperty(key).toString()

val javaVersion by lazy {
    JavaLanguageVersion.of(lookupProperty("javaVersion"))
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.intellij") version "1.5.3"
    id("org.jetbrains.grammarkit") version "2021.2.2"
    java
}

repositories {
    mavenCentral()
}

version = lookupProperty("pluginVersion")

intellij {
    version.set(lookupProperty("ideaVersion"))
    pluginName.set("sleigh-idea")
    downloadSources.set(true)
    updateSinceUntilBuild.set(false)
    plugins.set(lookupProperty("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

java.toolchain.languageVersion.set(javaVersion)
kotlin {
    jvmToolchain {
        version = javaVersion
    }
}

sourceSets["main"].java.srcDirs("src/main/kotlin", "src/main/gen")

apply(plugin="org.jetbrains.grammarkit")

tasks {
    wrapper {
        gradleVersion = lookupProperty("gradleVersion")
    }

    generateLexer {
        source.set("src/main/grammar/SleighLexer.flex")
        targetDir.set("src/main/gen/it/frob/sleighidea/lexer")
        targetClass.set("SleighLexer")
        purgeOldFiles.set(true)
    }

    generateParser {
        source.set("src/main/grammar/SleighGrammar.bnf")
        targetRoot.set("src/main/gen")
        pathToParser.set("it/frob/sleighidea/parser/SleighParser.java")
        pathToPsiRoot.set("it/frob/sleighidea/psi")
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn.add(generateLexer)
        dependsOn.add(generateParser)
    }

    compileKotlin {
        dependsOn.add(generateLexer)
        dependsOn.add(generateParser)
    }
}
