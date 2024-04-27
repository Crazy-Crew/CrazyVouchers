package com.badbones69.crazyvouchers.api.objects;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class VoucherCommand {
    
    private final Integer uuid;
    private final List<String> commands;
    
    public VoucherCommand(String commandString) {
        this.uuid = ThreadLocalRandom.current().nextInt();
        this.commands = Arrays.asList(commandString.split(", "));
    }
    
    public int getUUID() {
        return this.uuid;
    }
    
    public List<String> getCommands() {
        return this.commands;
    }
    
    public boolean isSimilar(VoucherCommand voucherCommand) {
        return this.uuid.equals(voucherCommand.getUUID());
    }
}