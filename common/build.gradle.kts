plugins {
    `java-plugin`
}

project.group = "${rootProject.group}"

dependencies {
    api(libs.bundles.databases) {
        exclude("org.checkerframework")
        exclude("org.slf4j")
    }
    api(project(":api"))
    api(libs.bundles.tinylog)

    // bundled by mojang, or other platforms
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.fusion.kyori)
    compileOnly(libs.gson)
}