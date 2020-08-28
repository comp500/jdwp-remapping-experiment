plugins {
    java
    application
}

repositories {
    jcenter()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
}

dependencies {
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

application {
    // Define the main class for the application.
    mainClassName = "link.infra.jdwp.App"
}
