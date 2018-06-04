package me.badbones69.vouchers.api.objects;

import de.tr7zw.itemnbtapi.NBTItem;
import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.FileManager.Files;
import me.badbones69.vouchers.api.enums.Messages;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Voucher {
	
	private String name;
	private Boolean usesArgs;
	private Material itemMaterial;
	private Short itemMetaData;
	private String itemName;
	private Boolean itemGlow;
	private String usedMessage;
	private Boolean whitelistToggle;
	private String whitelistNode;
	private Boolean blacklistToggle;
	private String blacklistMessage;
	private Boolean limiterToggle;
	private Integer limiterLimit;
	private Boolean twostepAuthentication;
	private Boolean soundToggle;
	private List<Sound> sounds = new ArrayList<>();
	private Boolean fireworkToggle;
	private List<Color> fireworkColors = new ArrayList<>();
	private List<String> itemLore;
	private List<String> commands = new ArrayList<>();
	private List<String> randomCoammnds = new ArrayList<>();
	private List<String> chanceCommands = new ArrayList<>();
	private List<String> blacklistPermissions = new ArrayList<>();
	private List<ItemStack> items = new ArrayList<>();
	
	public Voucher(String name) {
		this.name = name;
		this.usesArgs = false;
		FileConfiguration config = Files.CONFIG.getFile();
		String path = "Vouchers." + name + ".";
		String id = config.getString(path + "Item");
		itemMetaData = 0;
		if(id.contains(":")) {
			String[] b = id.split(":");
			id = b[0];
			itemMetaData = Short.parseShort(b[1]);
		}
		itemMaterial = Material.matchMaterial(id);
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
		this.itemGlow = config.contains(path + "Glowing") && config.getBoolean(path + "Glowing");
		if(config.contains(path + "Commands")) {
			this.commands = config.getStringList(path + "Commands");
		}
		if(config.contains(path + "Random-Commands")) {
			this.randomCoammnds = config.getStringList(path + "Random-Commands");
		}
		if(config.contains(path + "Chance-Commands")) {
			for(String line : config.getStringList(path + "Chance-Commands")) {
				try {
					String[] split = line.split(" ");
					String command = line.substring(split[0].length() + 1);
					int chance = Integer.parseInt(line.replace(" " + command, ""));
					for(int i = 1; i <= chance; i++) {
						chanceCommands.add(command);
					}
				}catch(Exception e) {
					System.out.println("[Vouchers] An issue occerted when trying to use chance commands.");
					e.printStackTrace();
				}
			}
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
		if(config.contains(path + "Options.Permission.Whitelist-Permission")) {
			this.whitelistToggle = config.getBoolean(path + "Options.Permission.Whitelist-Permission.Toggle");
			this.whitelistNode = config.getString(path + "Options.Permission.Whitelist-Permission.Node").toLowerCase();
		}else {
			this.whitelistToggle = false;
		}
		if(config.contains(path + "Options.Permission.Blacklist-Permissions")) {
			this.blacklistToggle = config.getBoolean(path + "Options.Permission.Blacklist-Permissions.Toggle");
			if(config.contains(path + "Options.Permission.Blacklist-Permissions.Message")) {
				this.blacklistMessage = config.getString(path + "Options.Permission.Blacklist-Permissions.Message");
			}else {
				this.blacklistMessage = Messages.HAS_BLACKLIST_PERMISSION.getMessageNoPrefix();
			}
			this.blacklistPermissions = config.getStringList(path + "Options.Permission.Blacklist-Permissions.Permissions");
		}else {
			this.blacklistToggle = false;
		}
		if(config.contains(path + "Options.Limiter")) {
			this.limiterToggle = config.getBoolean(path + "Options.Limiter.Toggle");
			this.limiterLimit = config.getInt(path + "Options.Limiter.Limit");
		}else {
			this.limiterToggle = false;
		}
		this.twostepAuthentication = config.contains(path + "Options.Two-Step-Authentication") && config.getBoolean(path + "Options.Two-Step-Authentication.Toggle");
		if(config.contains(path + "Options.Sound")) {
			this.soundToggle = config.getBoolean(path + "Options.Sound.Toggle");
			for(String sound : config.getStringList(path + "Options.Sound.Sounds")) {
				try {
					this.sounds.add(Sound.valueOf(sound));
				}catch(Exception e) {
				}
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
		ItemStack item = Methods.addGlow(new ItemBuilder()
		.setMaterial(itemMaterial)
		.setMetaData(itemMetaData)
		.setName(itemName)
		.setLore(itemLore)
		.build(), itemGlow);
		NBTItem nbt = new NBTItem(item);
		nbt.setString("voucher", name);
		return nbt.getItem();
	}
	
	public ItemStack buildItem(int amount) {
		ItemStack item = Methods.addGlow(new ItemBuilder()
		.setMaterial(itemMaterial)
		.setMetaData(itemMetaData)
		.setAmount(amount)
		.setName(itemName)
		.setLore(itemLore)
		.build(), itemGlow);
		NBTItem nbt = new NBTItem(item);
		nbt.setString("voucher", name);
		return nbt.getItem();
	}
	
	public ItemStack buildItem(String argument) {
		ItemStack item = Methods.addGlow(new ItemBuilder()
		.setMaterial(itemMaterial)
		.setMetaData(itemMetaData)
		.setName(itemName)
		.setLore(itemLore)
		.addLorePlaceholder("%arg%", argument)
		.addLorePlaceholder("%Arg%", argument)
		.addNamePlaceholder("%arg%", argument)
		.addNamePlaceholder("%Arg%", argument)
		.build(), itemGlow);
		NBTItem nbt = new NBTItem(item);
		nbt.setString("voucher", name);
		nbt.setString("argument", argument);
		return nbt.getItem();
		
	}
	
	public ItemStack buildItem(String argument, int amount) {
		ItemStack item = Methods.addGlow(new ItemBuilder()
		.setMaterial(itemMaterial)
		.setMetaData(itemMetaData)
		.setAmount(amount)
		.setName(itemName)
		.setLore(itemLore)
		.addLorePlaceholder("%arg%", argument)
		.addLorePlaceholder("%Arg%", argument)
		.addNamePlaceholder("%arg%", argument)
		.addNamePlaceholder("%Arg%", argument)
		.build(), itemGlow);
		NBTItem nbt = new NBTItem(item);
		nbt.setString("voucher", name);
		nbt.setString("argument", argument);
		return nbt.getItem();
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
	
	public List<String> getBlackListPermissions() {
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
	
	public List<Sound> getSounds() {
		return sounds;
	}
	
	public Boolean useFirework() {
		return fireworkToggle;
	}
	
	public List<Color> getFireworkColors() {
		return fireworkColors;
	}
	
	public List<String> getCommands() {
		return commands;
	}
	
	public List<String> getRandomCoammnds() {
		return randomCoammnds;
	}
	
	public List<String> getChanceCommands() {
		return chanceCommands;
	}
	
	public List<ItemStack> getItems() {
		return items;
	}
	
}