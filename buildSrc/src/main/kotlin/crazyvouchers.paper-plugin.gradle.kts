plugins {
    id("crazyvouchers.root-plugin")
}

repositories {
    exclusiveContent {
        forRepository {
            maven("https://repo.papermc.io/repository/maven-public/")
        }

        filter {
            includeGroup("io.papermc.paper")
            includeGroup("com.mojang")
            includeGroup("net.md-5")
        }
    }

    /**
     * NBT API
     */
    maven("https://repo.codemc.io/repository/maven-public/")
}