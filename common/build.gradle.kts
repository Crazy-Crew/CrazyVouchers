plugins {
    `java-plugin`
}

project.group = "${rootProject.group}"

dependencies {
    compileOnly(libs.bundles.adventure)
    compileOnly(libs.fusion.kyori)

    implementation(libs.hikari.cp) {
        exclude("org.slf4j")
    }

    api(project(":api"))
}