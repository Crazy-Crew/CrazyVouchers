package me.badbones69.vouchers.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import me.badbones69.vouchers.Main;
import me.badbones69.vouchers.Methods;

public class Voucher {
	
	private String name;
	private Boolean usesArgs;
	private String itemID;
	private String itemName;
	private List<String> itemLore = new ArrayList<>();
	private Boolean itemGlow;
	private String usedMessage;
	private Boolean whitelistToggle;
	private String whitelistNode;
	private Boolean blacklistToggle;
	private String blacklistMessage;
	private List<String> blacklistPermissions = new ArrayList<>();
	private Boolean limiterToggle;
	private Integer limiterLimit;
	private Boolean twostepAuthentication;
	private Boolean soundToggle;
	private List<Sound> sounds = new ArrayList<>();
	private Boolean fireworkToggle;
	private List<Color> fireworkColors = new ArrayList<>();
	private List<String> commands = new ArrayList<>();
	private List<ItemStack> items = new ArrayList<>();
	
	public Voucher(String name) {
		this.name = name;
		this.usesArgs = false;
		FileConfiguration config = Main.settings.getConfig();
		String path = "Vouchers." + name + ".";
		this.itemID = config.getString(path + "Item");
		this.itemName = config.getString(path + "Name");
		this.itemLore = config.getStringList(path + "Lore");
		if(this.itemName.toLowerCase().contains("%arg%")) {
			this.usesArgs = true;
		}
		if(!usesArgs) {
			for(String lore : this.itemLore) {
				if(lore.toLowerCase().contains("%arg%")) {
					this.usesArgs = true;
					break;
				}
			}
		}
		if(config.contains(path + "Glowing")) {
			this.itemGlow = config.getBoolean(path + "Glowing");
		}else {
			this.itemGlow = false;
		}
		if(config.contains(path + "Commands")) {
			this.commands = config.getStringList(path + "Commands");
		}
		if(config.contains(path + "Items")) {
			for(String itemString : config.getStringList(path + "Items")) {
				this.items.add(Methods.makeItem(itemString));
			}
		}
		if(config.contains(path + "Options.Message")) {
			this.usedMessage = config.getString(path + "Options.Message");
		}else {
			this.usedMessage = "";
		}
		if(config.contains(path + "Options.Whitelist-Permission")) {
			this.whitelistToggle = config.getBoolean(path + "Options.Whitelist-Permission.Toggle");
			this.whitelistNode = config.getString(path + "Options.Whitelist-Permission.Node").toLowerCase();
		}else {
			this.whitelistToggle = false;
		}
		if(config.contains(path + "Options.BlackList-Permissions")) {
			this.blacklistToggle = config.getBoolean(path + "Options.BlackList-Permissions.Toggle");
			if(config.contains(path + "Options.BlackList-Permissions.Message")) {
				this.blacklistMessage = config.getString(path + "Options.BlackList-Permissions.Message");
			}else {
				this.blacklistMessage = Main.settings.getMsgs().getString("Messages.Has-Blacklist-Permission");
			}
			this.blacklistPermissions = config.getStringList(path + "Options.BlackList-Permissions.Permissions");
		}else {
			this.blacklistToggle = false;
		}
		if(config.contains(path + "Options.Limiter")) {
			this.limiterToggle = config.getBoolean(path + "Options.Limiter.Toggle");
			this.limiterLimit = config.getInt(path + "Options.Limiter.Limit");
		}else {
			this.limiterToggle = false;
		}
		if(config.contains(path + "Options.Two-Step-Authentication")) {
			this.twostepAuthentication = config.getBoolean(path + "Options.Two-Step-Authentication.Toggle");
		}else {
			this.twostepAuthentication = false;
		}
		if(config.contains(path + "Options.Sound")) {
			this.soundToggle = config.getBoolean(path + "Options.Sound.Toggle");
			for(String sound : config.getStringList(path + "Options.Sound.Sounds")) {
				try {
					if(Sound.valueOf(sound) != null) {
						this.sounds.add(Sound.valueOf(sound));
					}
				}catch(Exception e) {}
			}
		}else {
			this.soundToggle = false;
		}
		if(config.contains(path + "Options.Firework")) {
			this.fireworkToggle = config.getBoolean(path + "Options.Firework.Toggle");
			for(String color : config.getString(path + "Options.Firework.Colors").split(", ")) {
				this.fireworkColors.add(Methods.getColor(color));
			}
		}else {
			this.fireworkToggle = false;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public Boolean usesArguments() {
		return usesArgs;
	}
	
	public ItemStack buildItem() {
		return Methods.addGlow(Methods.makeItem(itemID, 1, itemName, itemLore), itemGlow);
	}
	
	public ItemStack buildItem(int amount) {
		return Methods.addGlow(Methods.makeItem(itemID, amount, itemName, itemLore), itemGlow);
	}
	
	public ItemStack buildItem(String argument) {
		String name = itemName.replace("%Arg%", argument).replace("%arg%", argument);
		List<String> lore = new ArrayList<String>();
		for(String l : itemLore){
			lore.add(l.replace("%Arg%", argument).replace("%arg%", argument));
		}
		return Methods.addGlow(Methods.makeItem(itemID, 1, name,
				lore), itemGlow);
	}
	
	public ItemStack buildItem(String argument, int amount) {
		String name = itemName.replace("%Arg%", argument).replace("%arg%", argument);
		List<String> lore = new ArrayList<String>();
		for(String l : itemLore){
			lore.add(l.replace("%Arg%", argument).replace("%arg%", argument));
		}
		return Methods.addGlow(Methods.makeItem(itemID, amount, name,
				lore), itemGlow);
	}
	
	public String getVoucherUsedMessage() {
		return usedMessage;
	}
	
	public Boolean useWhiteListPermissions() {
		return whitelistToggle;
	}
	
	public String getWhiteListPermission() {
		return "voucher." + whitelistNode;
	}
	
	public Boolean useBlackListPermissions() {
		return blacklistToggle;
	}
	
	public List<String> getBlackListPermissions(){
		return blacklistPermissions;
	}
	
	public String getBlackListMessage() {
		return blacklistMessage;
	}
	
	public Boolean useLimiter() {
		return limiterToggle;
	}
	
	public Integer getLimiterLimit() {
		return limiterLimit;
	}
	
	public Boolean useTwoStepAuthentication() {
		return twostepAuthentication;
	}
	
	public Boolean playSounds() {
		return soundToggle;
	}
	
	public List<Sound> getSounds(){
		return sounds;
	}
	
	public Boolean useFirework() {
		return fireworkToggle;
	}
	
	public List<Color> getFireworkColors(){
		return fireworkColors;
	}
	
	public List<String> getCommands(){
		return commands;
	}
	
	public List<ItemStack> getItems(){
		return items;
	}
	
}