plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("io.papermc.paperweight", "paperweight-userdev", "1.7.0")

    implementation("com.github.johnrengelman", "shadow", "8.1.1")
}