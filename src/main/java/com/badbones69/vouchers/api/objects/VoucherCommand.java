package com.badbones69.vouchers.api.objects;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VoucherCommand {
    
    private Integer uuid;
    private List<String> commands;
    
    public VoucherCommand(String commandString) {
        this.uuid = new Random().nextInt();
        this.commands = Arrays.asList(commandString.split(", "));
    }
    
    public Integer getUUID() {
        return uuid;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public Boolean isSimilar(VoucherCommand voucherCommand) {
        return uuid.equals(voucherCommand.getUUID());
    }
    
}