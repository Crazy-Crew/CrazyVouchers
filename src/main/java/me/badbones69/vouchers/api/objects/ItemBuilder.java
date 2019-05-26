package me.badbones69.vouchers.api.objects;

import me.badbones69.vouchers.api.enums.Version;
import me.badbones69.vouchers.api.itemnbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * The ItemBuilder is designed to make creating items easier by creating an easy to use Builder.
 * This will allow you to covert an existing ItemStack into an ItemBuilder to allow you to edit 
 * an existing ItemStack or make a new ItemStack from scratch.
 *
 * @author BadBones69
 *
 */
public class ItemBuilder {
	
	private Material material;
	private Short metaData;
	private String name;
	private List<String> lore;
	private Integer amount;
	private List<ItemFlag> flags;
	private HashMap<Enchantment, Integer> enchantments;
	private Boolean unbreakable;
	private ItemStack referenceItem;
	private HashMap<String, String> namePlaceholders;
	private HashMap<String, String> lorePlaceholders;
	
	/**
	 * The inishal starting point for making an item.
	 */
	public ItemBuilder() {
		this.material = Material.STONE;
		this.metaData = 0;
		this.name = "";
		this.lore = new ArrayList<>();
		this.amount = 1;
		this.flags = new ArrayList<>();
		this.enchantments = new HashMap<>();
		this.unbreakable = false;
		this.namePlaceholders = new HashMap<>();
		this.lorePlaceholders = new HashMap<>();
	}
	
	/**
	 * Convert an ItemStack to an ItemBuilder to allow easier editing of the ItemStack.
	 * @param item The ItemStack you wish to convert into an ItemBuilder.
	 * @return The ItemStack as an ItemBuilder with all the info from the item.
	 */
	public static ItemBuilder convertItemStack(ItemStack item) {
		ItemBuilder itemBuilder = new ItemBuilder()
		.setReferenceItem(item)
		.setAmount(item.getAmount())
		.setMaterial(item.getType())
		.setMetaData(item.getDurability())
		.setEnchantments(new HashMap<>(item.getEnchantments()));
		if(item.hasItemMeta()) {
			itemBuilder.setName(item.getItemMeta().getDisplayName())
			.setLore(item.getItemMeta().getLore());
			NBTItem nbt = new NBTItem(item);
			if(nbt.hasKey("Unbreakable")) {
				itemBuilder.setUnbreakable(nbt.getBoolean("Unbreakable"));
			}
		}
		return itemBuilder;
	}
	
	/**
	 * Get the type of item as a Material the builder is set to.
	 * @return The type of material the builder is set to.
	 */
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Set the type of item the builder is set to.
	 * @param material The material you wish to set.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setMaterial(Material material) {
		this.material = material;
		return this;
	}
	
	/**
	 * Set the type of item and its meta data in the builder.
	 * @param string The string must be in this form: %Material% or %Material%:%MetaData%
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setMaterial(String string) {
		Short metaData = 0;
		if(string.contains(":")) {
			String[] b = string.split(":");
			string = b[0];
			metaData = Short.parseShort(b[1]);
		}
		Material material = Material.getMaterial(string);
		if(material != null) {
			this.material = material;
			this.metaData = metaData;
		}
		return this;
	}
	
	/**
	 * Get the metadata(Item Durrability) of the builder.
	 * @return The metadata as a short.
	 */
	public Short getMetaData() {
		return metaData;
	}
	
	/**
	 * Set the metadata of the builder.
	 * @param metaData The metadata you wish to use.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setMetaData(Short metaData) {
		this.metaData = metaData;
		return this;
	}
	
	/**
	 * Get the name the of the item in the builder.
	 * @return The name as a string that is already been color converted.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of the item in the builder. This will auto force color the name if it contains color code.
	 * @param name The name of the item in the builder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setName(String name) {
		if(name != null) {
			this.name = color(name);
		}
		return this;
	}
	
	/**
	 * Set the placeholders for the name of the item.
	 * @param placeholders The palceholders that will be used.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setNamePlaceholders(HashMap<String, String> placeholders) {
		this.namePlaceholders = placeholders;
		return this;
	}
	
	/**
	 * Add a placeholder to the name of the item.
	 * @param placeholder The placeholder that will be replaced.
	 * @param argument The argument you wish to replace the placeholder with.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder addNamePlaceholder(String placeholder, String argument) {
		this.namePlaceholders.put(placeholder, argument);
		return this;
	}
	
	/**
	 * Remove a placeholder from the list.
	 * @param placeholder The placeholder you wish to remove.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder removeNamePlaceholder(String placeholder) {
		this.namePlaceholders.remove(placeholder);
		return this;
	}
	
	/**
	 * Get the item's name with all the placeholders added to it.
	 * @return The name with all the placeholders in it.
	 */
	public String getUpdatedName() {
		String newName = name;
		for(String placeholder : lorePlaceholders.keySet()) {
			newName = newName.replace(placeholder, lorePlaceholders.get(placeholder));
		}
		return newName;
	}
	
	/**
	 * Get the lore of the item in the builder.
	 * @return The lore of the item in the builder. This will already be color coded.
	 */
	public List<String> getLore() {
		return lore;
	}
	
	/**
	 * Set the lore of the item in the builder. This will auto force color in all the lores that contains color code.
	 * @param lore The lore of the item in the builder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setLore(List<String> lore) {
		if(lore != null) {
			this.lore.clear();
			for(String i : lore) {
				this.lore.add(color(i));
			}
		}
		return this;
	}
	
	/**
	 * Add a line to the current lore of the item. This will auto force color in the lore that contains color code.
	 * @param lore The new line you wish to add.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder addLore(String lore) {
		if(lore != null) {
			this.lore.add(color(lore));
		}
		return this;
	}
	
	/**
	 * Set the placeholders that are in the lore of the item.
	 * @param placeholders The placeholders that you wish to use.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setLorePlaceholders(HashMap<String, String> placeholders) {
		this.lorePlaceholders = placeholders;
		return this;
	}
	
	/**
	 * Add a placeholder to the lore of the item.
	 * @param placeholder The placeholder you wish to replace.
	 * @param argument The arument that will replace the placeholder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder addLorePlaceholder(String placeholder, String argument) {
		this.lorePlaceholders.put(placeholder, argument);
		return this;
	}
	
	/**
	 * Remove a placeholder from the lore.
	 * @param placeholder The palceholder you wish to remove.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder removeLorePlaceholder(String placeholder) {
		this.lorePlaceholders.remove(placeholder);
		return this;
	}
	
	/**
	 * Get the lore with all the placeholders added to it.
	 * @return The lore with all placeholders in it.
	 */
	public List<String> getUpdatedLore() {
		List<String> newLore = new ArrayList<>();
		for(String i : lore) {
			for(String placeholder : lorePlaceholders.keySet()) {
				i = i.replace(placeholder, lorePlaceholders.get(placeholder));
			}
			newLore.add(i);
		}
		return newLore;
	}
	
	/**
	 * The amount of the item stack in the builder.
	 * @return The amount that is set in the builder.
	 */
	public Integer getAmount() {
		return amount;
	}
	
	/**
	 * Get the amount of the item stack in the builder.
	 * @param amount The amount that is in the item stack.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setAmount(Integer amount) {
		this.amount = amount;
		return this;
	}
	
	/**
	 * Get the flags that are set to the item in the builder.
	 * @return The flags that are on the item in the builder.
	 */
	public List<ItemFlag> getFlags() {
		return flags;
	}
	
	/**
	 * Add a flag to the item in the builder.
	 * @param flag The flag you wish to add.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder addFlags(ItemFlag flag) {
		this.flags.add(flag);
		return this;
	}
	
	/**
	 * Remove a flag that is on the item in the builder.
	 * @param flag The flag you wish to remove from the builder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder removeFlags(ItemFlag flag) {
		this.flags.remove(flag);
		return this;
	}
	
	/**
	 * Set the flags that will be on the item in the builder.
	 * @param flags The flags you wish to add to the item in the builder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setFlags(List<ItemFlag> flags) {
		if(flags != null) {
			this.flags = flags;
		}
		return this;
	}
	
	/**
	 * Get the enchantments that are on the item in the builder.
	 * @return The enchantments that are on the item in the builder.
	 */
	public HashMap<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}
	
	/**
	 * Add an enchantment to the item in the builder.
	 * @param enchantment The enchantment you wish to add.
	 * @param level The level of the enchantment. This can be unsafe levels.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder addEnchantments(Enchantment enchantment, Integer level) {
		this.enchantments.put(enchantment, level);
		return this;
	}
	
	/**
	 * Remove an enchantment from the item in the builder.
	 * @param enchantment The enchantment you wish to remove.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder removeEnchantments(Enchantment enchantment) {
		this.enchantments.remove(enchantment);
		return this;
	}
	
	/**
	 * Set a list of enchantments that will go onto the item in the builder. These can have unsafe levels.
	 * It will also override any enchantments used in the "ItemBuilder#addEnchantment()" method.
	 * @param enchantments A list of enchantments that will go onto the item in the builder.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setEnchantments(HashMap<Enchantment, Integer> enchantments) {
		if(enchantments != null) {
			this.enchantments = enchantments;
		}
		return this;
	}
	
	/**
	 * Check if the item in the builder is unbreakable.
	 * @return The ItemBuilder with updated info.
	 */
	public Boolean isUnbreakable() {
		return unbreakable;
	}
	
	/**
	 * Set if the item in the builder to be unbreakable or not.
	 * @param unbreakable True will set it to be unbreakable and false will make it able to take damage.
	 * @return The ItemBuilder with updated info.
	 */
	public ItemBuilder setUnbreakable(Boolean unbreakable) {
		this.unbreakable = unbreakable;
		return this;
	}
	
	/**
	 * Builder the item from all the information that was given to the builder.
	 * @return The result of all the info that was given to the builder as an ItemStack.
	 */
	public ItemStack build() {
		if(referenceItem == null && material == null) {
			ItemStack item;
			if(Version.getCurrentVersion().isNewer(Version.v1_12_R1)) {//Is 1.13+
				item = new ItemStack(Material.RED_TERRACOTTA);
			}else {//Is 1.12.2 down.
				item = new ItemStack(Material.matchMaterial("159"), 1, (short) 14);
			}
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(color("&4&lERROR"));
			itemMeta.setLore(Arrays.asList(
			color("&cMaterial Name/ID not found for item named &c" + getName()),
			color(""),
			color("&7Possible Issues:"),
			color("&7(&e*&7) &7Material name is &e&n1.13+&7 when using a &e&n1.12.2-&7 server."),
			color("&7(&e*&7) &7Material name is spelt incorrectly.")));
			item.setItemMeta(itemMeta);
			return item;
		}else {
			ItemStack item = referenceItem != null ? referenceItem : new ItemStack(material, amount, metaData);
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(getUpdatedName());
			itemMeta.setLore(getUpdatedLore());
			itemMeta.addItemFlags(flags.toArray(new ItemFlag[0]));
			item.setItemMeta(itemMeta);
			item.addUnsafeEnchantments(enchantments);
			if(unbreakable) {
				NBTItem nbt = new NBTItem(item);
				nbt.setBoolean("Unbreakable", true);
				nbt.setInteger("HideFlags", 4);
				return nbt.getItem();
			}
			return item;
		}
	}
	
	/**
	 * Sets the converted item as a reference to try and save NBT tags and stuff.
	 * @param referenceItem The item that is being referenced.
	 * @return The ItemBuilder with updated info.
	 */
	private ItemBuilder setReferenceItem(ItemStack referenceItem) {
		this.referenceItem = referenceItem;
		return this;
	}
	
	private String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
}