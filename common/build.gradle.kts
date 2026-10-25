plugins {
    `java-plugin`
}

project.group = "${rootProject.group}"

dependencies {
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.fusion.kyori)

    api(project(":api"))
}