package com.badbones69.crazyvouchers.api.objects;

import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VoucherCommand {
    
    private final UUID uuid;
    private final List<String> commands;
    
    public VoucherCommand(@NotNull final String commandString) {
        this.uuid = UUID.randomUUID();
        this.commands = Arrays.asList(commandString.split(", "));
    }
    
    public UUID getUUID() {
        return this.uuid;
    }
    
    public List<String> getCommands() {
        return this.commands;
    }
    
    public boolean isSimilar(@NotNull final UUID uuid) {
        return this.uuid.equals(uuid);
    }
}