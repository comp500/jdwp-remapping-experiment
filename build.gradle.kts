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

    // TODO: remove?
    testImplementation("junit:junit:4.13")
}

application {
    // Define the main class for the application.
    mainClassName = "link.infra.jdwp.App"
}
