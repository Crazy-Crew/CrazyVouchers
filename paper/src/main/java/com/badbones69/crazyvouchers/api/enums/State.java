package com.badbones69.crazyvouchers.api.enums;

import org.jspecify.annotations.NonNull;

public enum State {

    send_message("send_message"),
    send_actionbar("send_actionbar");

    private final String name;

    State(@NonNull final String name) {
        this.name = name;
    }

    public @NonNull final String getName() {
        return this.name;
    }
}