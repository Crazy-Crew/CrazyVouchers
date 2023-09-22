package com.badbones69.crazyvouchers.paper.api.objects;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import com.badbones69.crazyvouchers.paper.support.PluginSupport;
import com.badbones69.crazyvouchers.paper.support.SkullCreator;
import com.ryderbelserion.cluster.bukkit.items.utils.DyeUtils;
import com.ryderbelserion.cluster.bukkit.utils.LegacyUtils;
import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final CrazyVouchers plugin = CrazyVouchers.getPlugin(CrazyVouchers.class);
    private final SkullCreator skullCreator = this.plugin.getSkullCreator();

    private NBTItem nbtItem;

    // Item Data
    private Material material;
    private TrimMaterial trimMaterial;
    private TrimPattern trimPattern;
    private int damage;
    private String itemName;
    private final List<String> itemLore;
    private int itemAmount;
    private String customMaterial;

    // Player
    private String player;

    // Skulls
    private boolean isHash;
    private boolean isURL;
    private boolean isHead;

    // Enchantments/Flags
    private boolean unbreakable;
    private boolean hideItemFlags;
    private boolean glowing;

    // Entities
    private final boolean isMobEgg;
    private EntityType entityType;

    // Potions
    private PotionType potionType;
    private Color potionColor;
    private boolean isPotion;

    // Armor
    private Color armorColor;
    private boolean isLeatherArmor;

    // Enchantments
    private HashMap<Enchantment, Integer> enchantments;

    // Shields
    private boolean isShield;

    // Banners
    private boolean isBanner;
    private List<Pattern> patterns;

    // Maps
    private boolean isMap;
    private Color mapColor;

    // Placeholders
    private HashMap<String, String> namePlaceholders;
    private HashMap<String, String> lorePlaceholders;

    // Misc
    private ItemStack referenceItem;
    private List<ItemFlag> itemFlags;

    // Custom Data
    private int customModelData;
    private boolean useCustomModelData;

    /**
     * Create a blank item builder.
     */
    public ItemBuilder() {
        this.nbtItem = null;
        this.material = Material.STONE;
        this.trimMaterial = null;
        this.trimPattern = null;
        this.damage = 0;
        this.itemName = "";
        this.itemLore = new ArrayList<>();
        this.itemAmount = 1;
        this.player = "";

        this.isHash = false;
        this.isURL = false;
        this.isHead = false;

        this.unbreakable = false;
        this.hideItemFlags = false;
        this.glowing = false;

        this.isMobEgg = false;
        this.entityType = EntityType.BAT;

        this.potionType = null;
        this.potionColor = null;
        this.isPotion = false;

        this.armorColor = null;
        this.isLeatherArmor = false;

        this.enchantments = new HashMap<>();

        this.isShield = false;

        this.isBanner = false;
        this.patterns = new ArrayList<>();

        this.isMap = false;
        this.mapColor = Color.RED;

        this.namePlaceholders = new HashMap<>();
        this.lorePlaceholders = new HashMap<>();

        this.itemFlags = new ArrayList<>();
    }

    /**
     * Deduplicate an item builder.
     *
     * @param itemBuilder The item builder to deduplicate.
     */
    public ItemBuilder(ItemBuilder itemBuilder) {
        this.nbtItem = itemBuilder.nbtItem;
        this.material = itemBuilder.material;
        this.trimMaterial = itemBuilder.trimMaterial;
        this.trimPattern = itemBuilder.trimPattern;
        this.damage = itemBuilder.damage;
        this.itemName = itemBuilder.itemName;
        this.itemLore = new ArrayList<>(itemBuilder.itemLore);
        this.itemAmount = itemBuilder.itemAmount;
        this.player = itemBuilder.player;

        this.referenceItem = itemBuilder.referenceItem;
        this.customModelData = itemBuilder.customModelData;
        this.useCustomModelData = itemBuilder.useCustomModelData;

        this.enchantments = new HashMap<>(itemBuilder.enchantments);

        this.isHash = itemBuilder.isHash;
        this.isURL = itemBuilder.isURL;
        this.isHead = itemBuilder.isHead;

        this.unbreakable = itemBuilder.unbreakable;
        this.hideItemFlags = itemBuilder.hideItemFlags;
        this.glowing = itemBuilder.glowing;

        this.isMobEgg = itemBuilder.isMobEgg;
        this.entityType = itemBuilder.entityType;

        this.potionType = itemBuilder.potionType;
        this.potionColor = itemBuilder.potionColor;
        this.isPotion = itemBuilder.isPotion;

        this.armorColor = itemBuilder.armorColor;
        this.isLeatherArmor = itemBuilder.isLeatherArmor;

        this.isShield = itemBuilder.isShield;

        this.isBanner = itemBuilder.isBanner;
        this.patterns = new ArrayList<>(itemBuilder.patterns);

        this.isMap = itemBuilder.isMap;
        this.mapColor = itemBuilder.mapColor;

        this.namePlaceholders = new HashMap<>(itemBuilder.namePlaceholders);
        this.lorePlaceholders = new HashMap<>(itemBuilder.lorePlaceholders);
        this.itemFlags = new ArrayList<>(itemBuilder.itemFlags);
    }

    /**
     * Gets the nbt item.
     */
    public NBTItem getNBTItem() {
        this.nbtItem = new NBTItem(build());
        return this.nbtItem;
    }

    /**
     * Gets the material.
     */
    public Material getMaterial() {
        return this.material;
    }

    /**
     * @return trim material
     */
    public TrimMaterial getTrimMaterial() {
        return this.trimMaterial;
    }

    /**
     * Checks if the item is a banner.
     */
    public boolean isBanner() {
        return this.isBanner;
    }

    /**
     * Checks if an item is a shield.
     */
    public boolean isShield() {
        return this.isShield;
    }

    /**
     * Checks if the item is a spawn mob egg.
     */
    public boolean isMobEgg() {
        return this.isMobEgg;
    }

    /**
     * Returns the player name.
     */
    public String getPlayerName() {
        return this.player;
    }

    /**
     * Get the entity type of the spawn mob egg.
     */
    public EntityType getEntityType() {
        return this.entityType;
    }

    /**
     * Get the name of the item.
     */
    public String getName() {
        return this.itemName;
    }

    /**
     * Get the lore on the item.
     */
    public List<String> getLore() {
        return this.itemLore;
    }

    /**
     * Returns the enchantments on the Item.
     */
    public HashMap<Enchantment, Integer> getEnchantments() {
        return this.enchantments;
    }

    /**
     * Return a list of Item Flags.
     */
    public List<ItemFlag> getItemFlags() {
        return this.itemFlags;
    }

    /**
     * Checks if flags are hidden.
     */
    public boolean isItemFlagsHidden() {
        return this.hideItemFlags;
    }

    /**
     * Check if item is Leather Armor
     */
    public boolean isLeatherArmor() {
        return this.isLeatherArmor;
    }

    /**
     * Checks if item is glowing.
     */
    public boolean isGlowing() {
        return this.glowing;
    }

    /**
     * Checks if the item is unbreakable.
     */
    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Returns the amount of the item stack.
     */
    public Integer getAmount() {
        return this.itemAmount;
    }

    /**
     * Get the patterns on the banners.
     */
    public List<Pattern> getPatterns() {
        return this.patterns;
    }

    /**
     * Get the item's name with all the placeholders added to it.
     *
     * @return The name with all the placeholders in it.
     */
    public String getUpdatedName() {
        String newName = this.itemName;

        for (String placeholder : this.namePlaceholders.keySet()) {
            newName = newName.replace(placeholder, this.namePlaceholders.get(placeholder)).replace(placeholder.toLowerCase(), this.namePlaceholders.get(placeholder));
        }

        return newName;
    }

    private boolean isArmor() {
        String name = this.material.name();

        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS") || name.equals(Material.TURTLE_HELMET.name());
    }

    /**
     * Builder the item from all the information that was given to the builder.
     *
     * @return The result of all the info that was given to the builder as an ItemStack.
     */
    public ItemStack build() {
        if (this.nbtItem != null) this.referenceItem = this.nbtItem.getItem();

        ItemStack item = this.referenceItem;

        if (item == null) {
            if (PluginSupport.ITEMS_ADDER.isPluginEnabled()) {
                CustomStack customStack = CustomStack.getInstance("ia:" + this.customMaterial);

                if (customStack != null) item = customStack.getItemStack();
            } else if (PluginSupport.ORAXEN.isPluginEnabled()) {
                io.th0rgal.oraxen.items.ItemBuilder oraxenItem = OraxenItems.getItemById(this.customMaterial);

                if (oraxenItem != null) item = oraxenItem.build();
            }
        }

        if (item == null) item = new ItemStack(this.material);

        if (item.getType() != Material.AIR) {
            if (this.isHead) { // Has to go 1st due to it removing all data when finished.
                if (this.isHash) { // Sauce: https://github.com/deanveloper/SkullCreator
                    if (this.isURL) {
                        this.skullCreator.itemWithUrl(item, this.player);
                    } else {
                        this.skullCreator.itemWithBase64(item, this.player);
                    }
                }
            }

            item.setAmount(this.itemAmount);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(getUpdatedName());
            itemMeta.setLore(getUpdatedLore());

            if (isArmor()) {
                if (this.trimPattern != null && this.trimMaterial != null) {
                    ((ArmorMeta) itemMeta).setTrim(new ArmorTrim(this.trimMaterial, this.trimPattern));
                }
            }

            if (this.isMap) {
                MapMeta mapMeta = (MapMeta) itemMeta;

                if (this.mapColor != null) mapMeta.setColor(this.mapColor);
            }

            if (itemMeta instanceof Damageable) {
                if (this.damage >= 1) {
                    if (this.damage >= item.getType().getMaxDurability()) {
                        ((Damageable) itemMeta).setDamage(item.getType().getMaxDurability());
                    } else {
                        ((Damageable) itemMeta).setDamage(this.damage);
                    }
                }
            }

            if (this.isPotion && (this.potionType != null || this.potionColor != null)) {
                PotionMeta potionMeta = (PotionMeta) itemMeta;

                if (this.potionType != null) potionMeta.setBasePotionData(new PotionData(this.potionType));

                if (this.potionColor != null) potionMeta.setColor(this.potionColor);
            }

            if (this.material == Material.TIPPED_ARROW && this.potionType != null) {
                PotionMeta potionMeta = (PotionMeta) itemMeta;
                potionMeta.setBasePotionData(new PotionData(this.potionType));
            }

            if (this.isLeatherArmor && this.armorColor != null) {
                LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
                leatherMeta.setColor(this.armorColor);
            }

            if (this.isBanner && !this.patterns.isEmpty()) {
                BannerMeta bannerMeta = (BannerMeta) itemMeta;
                bannerMeta.setPatterns(this.patterns);
            }

            if (this.isShield && !this.patterns.isEmpty()) {
                BlockStateMeta shieldMeta = (BlockStateMeta) itemMeta;
                Banner banner = (Banner) shieldMeta.getBlockState();
                banner.setPatterns(this.patterns);
                banner.update();
                shieldMeta.setBlockState(banner);
            }

            if (this.useCustomModelData) itemMeta.setCustomModelData(this.customModelData);

            this.itemFlags.forEach(itemMeta :: addItemFlags);
            item.setItemMeta(itemMeta);
            hideItemFlags(item);
            item.addUnsafeEnchantments(this.enchantments);
            addGlow(item);
            NBTItem nbt = new NBTItem(item);

            if (this.isHead && !this.isHash) nbt.setString("SkullOwner", this.player);

            if (this.isMobEgg) {
                if (this.entityType != null) nbt.addCompound("EntityTag").setString("id", "minecraft:" + entityType.name());
            }

            return nbt.getItem();
        } else {
            return item;
        }
    }

    /*
      Class based extensions.
     */

    /**
     * Set the type of item the builder is set to.
     *
     * @param material The material you wish to set.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        this.isHead = material == Material.PLAYER_HEAD;
        return this;
    }

    public ItemBuilder setTrimMaterial(TrimMaterial trimMaterial) {
        this.trimMaterial = trimMaterial;

        return this;
    }

    public ItemBuilder setTrimPattern(TrimPattern trimPattern) {
        this.trimPattern = trimPattern;

        return this;
    }

    /**
     * Set the type of item and its metadata in the builder.
     *
     * @param material The string must be in this form: %Material% or %Material%:%MetaData%
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder setMaterial(String material) {
        String metaData;
        //Store material inside iaNamespace (e.g. ia:myblock)
        this.customMaterial = material;

        if (material.contains(":")) { // Sets the durability or another value option.
            String[] b = material.split(":");
            material = b[0];
            metaData = b[1];

            if (metaData.contains("#")) { // <ID>:<Durability>#<CustomModelData>
                String modelData = metaData.split("#")[1];
                if (isInt(modelData)) { // Value is a number.
                    this.useCustomModelData = true;
                    this.customModelData = Integer.parseInt(modelData);
                }
            }

            metaData = metaData.replace("#" + this.customModelData, "");

            if (isInt(metaData)) { // Value is durability.
                this.damage = Integer.parseInt(metaData);
            } else { // Value is something else.
                this.potionType = getPotionType(PotionEffectType.getByName(metaData));
                this.potionColor = DyeUtils.getColor(metaData);
                this.armorColor = DyeUtils.getColor(metaData);
                this.mapColor = DyeUtils.getColor(metaData);
            }
        } else if (material.contains("#")) {
            String[] b = material.split("#");
            material = b[0];

            if (isInt(b[1])) { // Value is a number.
                this.useCustomModelData = true;
                this.customModelData = Integer.parseInt(b[1]);
            }
        }

        Material matchedMaterial = Material.matchMaterial(material);

        if (matchedMaterial != null) this.material = matchedMaterial;

        switch (this.material.name()) {
            case "PLAYER_HEAD" -> this.isHead = true;
            case "POTION", "SPLASH_POTION" -> this.isPotion = true;
            case "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS", "LEATHER_HORSE_ARMOR" -> this.isLeatherArmor = true;
            case "BANNER" -> this.isBanner = true;
            case "SHIELD" -> this.isShield = true;
            case "FILLED_MAP" -> this.isMap = true;
        }

        if (this.material.name().contains("BANNER")) this.isBanner = true;

        return this;
    }

    /**
     * @param damage The damage value of the item.
     * @return The ItemBuilder with an updated damage value.
     */
    public ItemBuilder setDamage(int damage) {
        this.damage = damage;
        return this;
    }

    public int getDamage() {
        return this.damage;
    }

    /**
     * @param itemName The name of the item.
     * @return The ItemBuilder with an updated name.
     */
    public ItemBuilder setName(String itemName) {
        if (itemName != null) this.itemName = LegacyUtils.color(itemName);

        return this;
    }

    /**
     * @param placeholders The placeholders that will be used.
     * @return The ItemBuilder with updated placeholders.
     */
    public ItemBuilder setNamePlaceholders(HashMap<String, String> placeholders) {
        this.namePlaceholders = placeholders;
        return this;
    }

    /**
     * Add a placeholder to the name of the item.
     *
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
     *
     * @param placeholder The placeholder you wish to remove.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder removeNamePlaceholder(String placeholder) {
        this.namePlaceholders.remove(placeholder);
        return this;
    }

    /**
     * Set the lore of the item in the builder. This will auto force color in all the lores that contains color code. (&a, &c, &7, etc...)
     *
     * @param lore The lore of the item in the builder.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder setLore(List<String> lore) {
        if (lore != null) {
            this.itemLore.clear();

            for (String line : lore) {
                this.itemLore.add(LegacyUtils.color(line));
            }
        }

        return this;
    }

    /**
     * Add a line to the current lore of the item. This will auto force color in the lore that contains color code. (&a, &c, &7, etc...)
     *
     * @param lore The new line you wish to add.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder addLore(String lore) {
        if (lore != null) this.itemLore.add(LegacyUtils.color(lore));
        return this;
    }

    /**
     * Set the placeholders that are in the lore of the item.
     *
     * @param placeholders The placeholders that you wish to use.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder setLorePlaceholders(HashMap<String, String> placeholders) {
        this.lorePlaceholders = placeholders;
        return this;
    }

    /**
     * Add a placeholder to the lore of the item.
     *
     * @param placeholder The placeholder you wish to replace.
     * @param argument The argument that will replace the placeholder.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder addLorePlaceholder(String placeholder, String argument) {
        this.lorePlaceholders.put(placeholder, argument);
        return this;
    }

    /**
     * Get the lore with all the placeholders added to it.
     *
     * @return The lore with all placeholders in it.
     */
    public List<String> getUpdatedLore() {
        List<String> newLore = new ArrayList<>();

        for (String item : itemLore) {
            for (String placeholder : lorePlaceholders.keySet()) {
                item = item.replace(placeholder, lorePlaceholders.get(placeholder)).replace(placeholder.toLowerCase(), lorePlaceholders.get(placeholder));
            }

            newLore.add(item);
        }

        return newLore;
    }

    /**
     * Remove a placeholder from the lore.
     *
     * @param placeholder The placeholder you wish to remove.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder removeLorePlaceholder(String placeholder) {
        this.lorePlaceholders.remove(placeholder);
        return this;
    }

    /**
     * @param entityType The entity type the mob spawn egg will be.
     * @return The ItemBuilder with an updated mob spawn egg.
     */
    public ItemBuilder setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Add patterns to the item.
     *
     * @param stringPattern The pattern you wish to add.
     */
    private void addPatterns(String stringPattern) {
        try {
            String[] split = stringPattern.split(":");

            for (PatternType pattern : PatternType.values()) {

                if (split[0].equalsIgnoreCase(pattern.name()) || split[0].equalsIgnoreCase(pattern.getIdentifier())) {
                    DyeColor color = DyeUtils.getDyeColor(split[1]);

                    if (color != null) addPattern(new Pattern(color, pattern));

                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * @param patterns The list of Patterns to add.
     * @return The ItemBuilder with updated patterns.
     */
    public ItemBuilder addPatterns(List<String> patterns) {
        patterns.forEach(this :: addPatterns);
        return this;
    }

    /**
     * @param pattern A pattern to add.
     * @return The ItemBuilder with an updated pattern.
     */
    public ItemBuilder addPattern(Pattern pattern) {
        patterns.add(pattern);
        return this;
    }

    /**
     * @param patterns Set a list of Patterns.
     * @return The ItemBuilder with an updated list of patterns.
     */
    public ItemBuilder setPattern(List<Pattern> patterns) {
        this.patterns = patterns;
        return this;
    }

    /**
     * @param amount The amount of the item stack.
     * @return The ItemBuilder with an updated item count.
     */
    public ItemBuilder setAmount(Integer amount) {
        this.itemAmount = amount;
        return this;
    }

    /**
     * Set the player that will be displayed on the head.
     *
     * @param playerName The player being displayed on the head.
     * @return The ItemBuilder with an updated Player Name.
     */
    public ItemBuilder setPlayerName(String playerName) {
        this.player = playerName;

        if (player != null && player.length() > 16) {
            this.isHash = true;
            this.isURL = player.startsWith("http");
        }

        return this;
    }

    /**
     * It will override any enchantments used in ItemBuilder.addEnchantment() below.
     *
     * @param enchantment A list of enchantments to add to the item.
     * @return The ItemBuilder with a list of updated enchantments.
     */
    public ItemBuilder setEnchantments(HashMap<Enchantment, Integer> enchantment) {
        if (enchantment != null) this.enchantments = enchantment;

        return this;
    }

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment The enchantment you wish to add.
     * @param level The level of the enchantment ( Unsafe levels included )
     * @return The ItemBuilder with updated enchantments.
     */
    public ItemBuilder addEnchantments(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    /**
     * Remove an enchantment from the item.
     *
     * @param enchantment The enchantment you wish to remove.
     * @return The ItemBuilder with updated enchantments.
     */
    public ItemBuilder removeEnchantments(Enchantment enchantment) {
        this.enchantments.remove(enchantment);
        return this;
    }

    /**
     * Set the flags that will be on the item in the builder.
     *
     * @param flagStrings The flag names as string you wish to add to the item in the builder.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder setFlagsFromStrings(List<String> flagStrings) {
        itemFlags.clear();

        for (String flagString : flagStrings) {
            ItemFlag flag = getFlag(flagString);

            if (flag != null) itemFlags.add(flag);
        }

        return this;
    }

    // Used for multiple Item Flags
    public ItemBuilder addItemFlags(List<String> flagStrings) {
        for (String flagString : flagStrings) {
            try {
                ItemFlag itemFlag = ItemFlag.valueOf(flagString.toUpperCase());

                if (itemFlag != null) addItemFlag(itemFlag);
            } catch (Exception ignored) {}
        }

        return this;
    }

    /**
     * Add a flag to the item in the builder.
     *
     * @param flagString The name of the flag you wish to add.
     * @return The ItemBuilder with updated info.
     */
    public ItemBuilder addFlags(String flagString) {
        ItemFlag flag = getFlag(flagString);

        if (flag != null) itemFlags.add(flag);
        return this;
    }

    /**
     * Adds an ItemFlag to a map which is added to an item.
     *
     * @param itemFlag The flag to add.
     * @return The ItemBuilder with an updated ItemFlag.
     */
    public ItemBuilder addItemFlag(ItemFlag itemFlag) {
        if (itemFlag != null) itemFlags.add(itemFlag);

        return this;
    }

    /**
     * Adds multiple ItemFlags in a list to a map which get added to an item.
     *
     * @param itemFlags The list of flags to add.
     * @return The ItemBuilder with a list of ItemFlags.
     */
    public ItemBuilder setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
        return this;
    }

    /**
     * @param hideItemFlags Hide item flags based on a boolean.
     * @return The ItemBuilder with an updated Boolean.
     */
    public ItemBuilder hideItemFlags(boolean hideItemFlags) {
        this.hideItemFlags = hideItemFlags;
        return this;
    }

    /**
     * @param item The item to hide flags on.
     * @return The ItemBuilder with an updated Item.
     */
    public ItemStack hideItemFlags(ItemStack item) {
        if (hideItemFlags) {
            if (item != null && item.hasItemMeta() && item.getItemMeta() != null) {
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.addItemFlags(ItemFlag.values());
                item.setItemMeta(itemMeta);
                return item;
            }
        }

        return item;
    }

    /**
     * Sets the converted item as a reference to try and save NBT tags and stuff.
     *
     * @param referenceItem The item that is being referenced.
     * @return The ItemBuilder with updated info.
     */
    private ItemBuilder setReferenceItem(ItemStack referenceItem) {
        this.referenceItem = referenceItem;
        return this;
    }

    /**
     * @param unbreakable Sets the item to be unbreakable.
     * @return The ItemBuilder with an updated Boolean.
     */
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * @param glow Sets whether to make an item to glow or not.
     * @return The ItemBuilder with an updated Boolean.
     */
    public ItemBuilder setGlow(boolean glow) {
        this.glowing = glow;
        return this;
    }

    /**
     * The text that will be displayed on the item.
     *
     * @param texture The skull texture.
     * @param profileUUID The uuid of the profile.
     * @return The ItemBuilder.
     */
    public ItemBuilder texture(String texture, UUID profileUUID) {
        return this;
    }

    /**
     * @param texture The skull texture.
     * @return The ItemBuilder.
     */
    public ItemBuilder texture(String texture) {
        return this;
    }

    /**
     * @param texture The owner of the skull.
     * @return The ItemBuilder.
     */
    public ItemBuilder owner(String texture) {
        return this;
    }

    // Other misc shit

    /**
     * Convert an ItemStack to an ItemBuilder to allow easier editing of the ItemStack.
     *
     * @param item The ItemStack you wish to convert into an ItemBuilder.
     * @return The ItemStack as an ItemBuilder with all the info from the item.
     */
    public static ItemBuilder convertItemStack(ItemStack item) {
        ItemBuilder itemBuilder = new ItemBuilder()
                .setReferenceItem(item)
                .setAmount(item.getAmount())
                .setMaterial(item.getType())
                .setEnchantments(new HashMap<>(item.getEnchantments()));

        if (item.hasItemMeta() && item.getItemMeta() != null) {
            ItemMeta itemMeta = item.getItemMeta();
            itemBuilder.setName(itemMeta.getDisplayName()).setLore(itemMeta.getLore());
            NBTItem nbt = new NBTItem(item);

            if (nbt.hasKey("Unbreakable")) itemBuilder.setUnbreakable(nbt.getBoolean("Unbreakable"));

            if (itemMeta instanceof org.bukkit.inventory.meta.Damageable) itemBuilder.setDamage(((org.bukkit.inventory.meta.Damageable) itemMeta).getDamage());
        }

        return itemBuilder;
    }

    /**
     * Converts a String to an ItemBuilder.
     *
     * @param itemString The String you wish to convert.
     * @return The String as an ItemBuilder.
     */
    public static ItemBuilder convertString(String itemString) {
        return convertString(itemString, null);
    }

    /**
     * Converts a string to an ItemBuilder with a placeholder for errors.
     *
     * @param itemString The String you wish to convert.
     * @param placeHolder The placeholder to use if there is an error.
     * @return The String as an ItemBuilder.
     */
    public static ItemBuilder convertString(String itemString, String placeHolder) {
        ItemBuilder itemBuilder = new ItemBuilder();

        try {
            for (String optionString : itemString.split(", ")) {
                String option = optionString.split(":")[0];
                String value = optionString.replace(option + ":", "").replace(option, "");

                switch (option.toLowerCase()) {
                    case "item" -> itemBuilder.setMaterial(value);
                    case "name" -> itemBuilder.setName(value);
                    case "amount" -> {
                        try {
                            itemBuilder.setAmount(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            itemBuilder.setAmount(1);
                        }
                    }
                    case "damage" -> {
                        try {
                            itemBuilder.setDamage(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            itemBuilder.setDamage(0);
                        }
                    }
                    case "lore" -> itemBuilder.setLore(Arrays.asList(value.split(",")));
                    case "player" -> itemBuilder.setPlayerName(value);
                    case "unbreakable-item" -> {
                        if (value.isEmpty() || value.equalsIgnoreCase("true")) itemBuilder.setUnbreakable(true);
                    }
                    case "trim-pattern" -> {
                        if (!value.isEmpty()) itemBuilder.setTrimPattern(Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(value.toLowerCase())));
                    }
                    case "trim-material" -> {
                        if (!value.isEmpty()) itemBuilder.setTrimMaterial(Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(value.toLowerCase())));
                    }
                    default -> {
                        Enchantment enchantment = getEnchantment(option);
                        if (enchantment != null && enchantment.getName() != null) {
                            try {
                                itemBuilder.addEnchantments(enchantment, Integer.parseInt(value));
                            } catch (NumberFormatException e) {
                                itemBuilder.addEnchantments(enchantment, 1);
                            }

                            break;
                        }
                        for (ItemFlag itemFlag : ItemFlag.values()) {
                            if (itemFlag.name().equalsIgnoreCase(option)) {
                                itemBuilder.addItemFlag(itemFlag);
                                break;
                            }
                        }
                        try {
                            for (PatternType pattern : PatternType.values()) {
                                if (option.equalsIgnoreCase(pattern.name()) || value.equalsIgnoreCase(pattern.getIdentifier())) {
                                    DyeColor color = DyeUtils.getDyeColor(value);
                                    if (color != null) itemBuilder.addPattern(new Pattern(color, pattern));
                                    break;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            itemBuilder.setMaterial(Material.RED_TERRACOTTA).setName("&c&lERROR").setLore(Arrays.asList("&cThere is an error", "&cFor : &c" + (placeHolder != null ? placeHolder : "")));
            e.printStackTrace();
        }

        return itemBuilder;
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders.
     *
     * @param itemStrings The list of Strings.
     * @return The list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(List<String> itemStrings) {
        return convertStringList(itemStrings, null);
    }

    /**
     * Converts a list of Strings to a list of ItemBuilders with a placeholder for errors.
     *
     * @param itemStrings The list of Strings.
     * @param placeholder The placeholder for errors.
     * @return The list of ItemBuilders.
     */
    public static List<ItemBuilder> convertStringList(List<String> itemStrings, String placeholder) {
        return itemStrings.stream().map(itemString -> convertString(itemString, placeholder)).collect(Collectors.toList());
    }

    /**
     * Add glow to an item.
     *
     * @param item The item to add glow to.
     */
    private void addGlow(ItemStack item) {
        if (glowing) {
            try {
                if (item != null && item.getItemMeta() != null) {
                    if (item.hasItemMeta()) {
                        if (item.getItemMeta().hasEnchants()) return;
                    }

                    item.addUnsafeEnchantment(Enchantment.LUCK, 1);
                    ItemMeta meta = item.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
            } catch (NoClassDefFoundError ignored) {}
        }
    }

    /**
     * Get the PotionEffect from a PotionEffectType.
     *
     * @param type The type of the potion effect.
     * @return The potion type.
     */
    private PotionType getPotionType(PotionEffectType type) {
        if (type != null) {
            if (type.equals(PotionEffectType.FIRE_RESISTANCE)) {
                return PotionType.FIRE_RESISTANCE;
            } else if (type.equals(PotionEffectType.HARM)) {
                return PotionType.INSTANT_DAMAGE;
            } else if (type.equals(PotionEffectType.HEAL)) {
                return PotionType.INSTANT_HEAL;
            } else if (type.equals(PotionEffectType.INVISIBILITY)) {
                return PotionType.INVISIBILITY;
            } else if (type.equals(PotionEffectType.JUMP)) {
                return PotionType.JUMP;
            } else if (type.equals(PotionEffectType.getByName("LUCK"))) {
                return PotionType.valueOf("LUCK");
            } else if (type.equals(PotionEffectType.NIGHT_VISION)) {
                return PotionType.NIGHT_VISION;
            } else if (type.equals(PotionEffectType.POISON)) {
                return PotionType.POISON;
            } else if (type.equals(PotionEffectType.REGENERATION)) {
                return PotionType.REGEN;
            } else if (type.equals(PotionEffectType.SLOW)) {
                return PotionType.SLOWNESS;
            } else if (type.equals(PotionEffectType.SPEED)) {
                return PotionType.SPEED;
            } else if (type.equals(PotionEffectType.INCREASE_DAMAGE)) {
                return PotionType.STRENGTH;
            } else if (type.equals(PotionEffectType.WATER_BREATHING)) {
                return PotionType.WATER_BREATHING;
            } else if (type.equals(PotionEffectType.WEAKNESS)) {
                return PotionType.WEAKNESS;
            }
        }

        return null;
    }

    /**
     * Get the enchantment from a string.
     *
     * @param enchantmentName The string of the enchantment.
     * @return The enchantment from the string.
     */
    private static Enchantment getEnchantment(String enchantmentName) {
        enchantmentName = stripEnchantmentName(enchantmentName);
        for (Enchantment enchantment : Enchantment.values()) {
            try {
                if (stripEnchantmentName(enchantment.getKey().getKey()).equalsIgnoreCase(enchantmentName)) return enchantment;

                HashMap<String, String> enchantments = getEnchantmentList();

                if (stripEnchantmentName(enchantment.getName()).equalsIgnoreCase(enchantmentName) || (enchantments.get(enchantment.getName()) != null &&
                        stripEnchantmentName(enchantments.get(enchantment.getName())).equalsIgnoreCase(enchantmentName))) return enchantment;
            } catch (Exception ignore) {}
        }

        return null;
    }

    /**
     * Strip extra characters from an enchantment name.
     *
     * @param enchantmentName The enchantment name.
     * @return The stripped enchantment name.
     */
    private static String stripEnchantmentName(String enchantmentName) {
        return enchantmentName != null ? enchantmentName.replace("-", "").replace("_", "").replace(" ", "") : null;
    }

    /**
     * Get the list of enchantments and their in-Game names.
     *
     * @return The list of enchantments and their in-Game names.
     */
    private static HashMap<String, String> getEnchantmentList() {
        HashMap<String, String> enchantments = new HashMap<>();
        enchantments.put("ARROW_DAMAGE", "Power");
        enchantments.put("ARROW_FIRE", "Flame");
        enchantments.put("ARROW_INFINITE", "Infinity");
        enchantments.put("ARROW_KNOCKBACK", "Punch");
        enchantments.put("DAMAGE_ALL", "Sharpness");
        enchantments.put("DAMAGE_ARTHROPODS", "Bane_Of_Arthropods");
        enchantments.put("DAMAGE_UNDEAD", "Smite");
        enchantments.put("DEPTH_STRIDER", "Depth_Strider");
        enchantments.put("DIG_SPEED", "Efficiency");
        enchantments.put("DURABILITY", "Unbreaking");
        enchantments.put("FIRE_ASPECT", "Fire_Aspect");
        enchantments.put("KNOCKBACK", "KnockBack");
        enchantments.put("LOOT_BONUS_BLOCKS", "Fortune");
        enchantments.put("LOOT_BONUS_MOBS", "Looting");
        enchantments.put("LUCK", "Luck_Of_The_Sea");
        enchantments.put("LURE", "Lure");
        enchantments.put("OXYGEN", "Respiration");
        enchantments.put("PROTECTION_ENVIRONMENTAL", "Protection");
        enchantments.put("PROTECTION_EXPLOSIONS", "Blast_Protection");
        enchantments.put("PROTECTION_FALL", "Feather_Falling");
        enchantments.put("PROTECTION_FIRE", "Fire_Protection");
        enchantments.put("PROTECTION_PROJECTILE", "Projectile_Protection");
        enchantments.put("SILK_TOUCH", "Silk_Touch");
        enchantments.put("THORNS", "Thorns");
        enchantments.put("WATER_WORKER", "Aqua_Affinity");
        enchantments.put("BINDING_CURSE", "Curse_Of_Binding");
        enchantments.put("MENDING", "Mending");
        enchantments.put("FROST_WALKER", "Frost_Walker");
        enchantments.put("VANISHING_CURSE", "Curse_Of_Vanishing");
        enchantments.put("SWEEPING_EDGE", "Sweeping_Edge");
        enchantments.put("RIPTIDE", "Riptide");
        enchantments.put("CHANNELING", "Channeling");
        enchantments.put("IMPALING", "Impaling");
        enchantments.put("LOYALTY", "Loyalty");

        return enchantments;
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }

        return true;
    }

    private ItemFlag getFlag(String flagString) {
        for (ItemFlag flag : ItemFlag.values()) {
            if (flag.name().equalsIgnoreCase(flagString)) return flag;
        }

        return null;
    }
}