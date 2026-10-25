package us.crazycrew.crazyvouchers.api.enums;

import org.jspecify.annotations.NonNull;

public enum FileSystem {

    SINGLE("single"),
    MULTIPLE("multiple");

    private final String name;

    FileSystem(@NonNull final String name) {
        this.name = name;
    }

    public @NonNull final String getName() {
        return this.name;
    }
}