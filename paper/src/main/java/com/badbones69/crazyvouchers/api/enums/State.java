package com.badbones69.crazyvouchers.api.enums;

import org.jetbrains.annotations.NotNull;

public enum State {

    send_message("send_message"),
    send_actionbar("send_actionbar");

    private final String name;

    State(@NotNull final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}