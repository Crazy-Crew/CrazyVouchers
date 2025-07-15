package com.badbones69.crazyvouchers.api.enums;

import org.jetbrains.annotations.NotNull;

public enum FileSystem {

    SINGLE("single"),
    MULTIPLE("multiple");

    private final String name;

    FileSystem(@NotNull final String name) {
        this.name = name;
    }

    public @NotNull final String getName() {
        return this.name;
    }
}