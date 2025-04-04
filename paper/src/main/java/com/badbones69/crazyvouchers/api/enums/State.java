package com.badbones69.crazyvouchers.api.enums;

public enum State {

    send_message("send_message"),
    send_actionbar("send_actionbar");

    private final String name;

    State(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}