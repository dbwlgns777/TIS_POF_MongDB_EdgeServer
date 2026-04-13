import org.gradle.api.tasks.compile.JavaCompile

plugins {
    application
    java
}

group = "kr.co.zestech"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mongodb:mongodb-driver-sync:5.1.2")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

application {
    mainClass.set("com.zes.device.ZES_DeviceApplication")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "processResources", "classes")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}
