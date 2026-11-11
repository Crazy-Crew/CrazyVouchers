package us.crazycrew.crazyvouchers.api.enums.other;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum FileSystem {

    SINGLE("single"),
    MULTIPLE("multiple");

    private final String name;

    FileSystem(final String name) {
        this.name = name;
    }

    public final String getName() {
        return this.name;
    }
}