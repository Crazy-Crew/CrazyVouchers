package com.badbones69.crazyvouchers.api.objects;

import org.jspecify.annotations.NonNull;
import java.util.List;

public class VoucherCommand {

    private final List<String> commands;
    private final double weight;

    public VoucherCommand(@NonNull final List<String> commands, final double weight) {
        this.commands = commands;
        this.weight = weight;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public double getWeight() {
        return this.weight;
    }
}