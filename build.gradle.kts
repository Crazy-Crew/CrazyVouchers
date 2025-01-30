plugins {
    id("root-plugin")
}

rootProject.group = "com.badbones69.crazyvouchers"
rootProject.description = "Give your players as many rewards as you like in a compact form called a voucher!"

val buildNumber: String? = System.getenv("BUILD_NUMBER")

rootProject.version = if (buildNumber != null) "${libs.versions.minecraft.get()}-$buildNumber" else "4.1.0"