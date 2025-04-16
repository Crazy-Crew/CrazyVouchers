plugins {
    id("root-plugin")
}

rootProject.group = "com.badbones69.crazyvouchers"
rootProject.description = "Give your players as many rewards as you like in a compact form called a voucher!"

val buildNumber: String? = System.getenv("BUILD_NUMBER")
val isPublishing: String? = System.getenv("IS_PUBLISHING")

rootProject.version = if (buildNumber != null && isPublishing == null) "${libs.versions.minecraft.get()}-$buildNumber" else rootProject.properties["version"].toString()