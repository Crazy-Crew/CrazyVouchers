plugins {
    id("com.gradleup.shadow")
    id("java-plugin")
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        mergeServiceFiles()

        exclude("META-INF/**")
    }
}