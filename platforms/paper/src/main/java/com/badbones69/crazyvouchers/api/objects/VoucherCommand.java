package com.badbones69.crazyvouchers.api.objects;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VoucherCommand {
    
    private final Integer uuid;
    private final List<String> commands;
    
    public VoucherCommand(String commandString) {
        this.uuid = new Random().nextInt();
        this.commands = Arrays.asList(commandString.split(", "));
    }
    
    public int getUUID() {
        return uuid;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public boolean isSimilar(VoucherCommand voucherCommand) {
        return uuid.equals(voucherCommand.getUUID());
    }
}