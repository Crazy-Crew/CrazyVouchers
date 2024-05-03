package com.badbones69.crazyvouchers.api.objects.v2;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.vital.items.AbstractItemHandler;
import com.ryderbelserion.vital.items.ItemHandler;
import com.ryderbelserion.vital.util.ItemUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractVoucher {

    protected final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    protected final @NotNull Server server = this.plugin.getServer();

    protected final ConfigurationSection section;

    protected AbstractItemHandler builder = null;

    protected final String fileName;

    protected String plainName = "";

    protected boolean isEdible = false;

    // The options shared by both vouchers and voucher codes.
    protected String message;

    protected boolean whitelistWorldToggle;
    protected String whitelistWorldMessage;
    protected List<String> whitelistWorldCommands;
    protected List<String> whitelistWorlds;

    protected boolean whitelistPermissionToggle;
    protected String whitelistPermissionMessage;
    protected List<String> whitelistPermissions;
    protected List<String> whitelistCommands;

    protected boolean blacklistPermissionToggle;
    protected String blacklistPermissionMessage;
    protected List<String> blacklistPermissions;
    protected List<String> blacklistCommands;

    protected boolean limiterToggle;
    protected int limiterAmount;

    protected boolean soundToggle;
    protected float volume;
    protected float pitch;
    protected List<Sound> sounds;

    protected boolean fireworkToggle;
    protected String fireworkColors;

    protected boolean twoStep;

    /**
     * Builds a voucher code
     *
     * @param section the configuration section
     * @param file the file
     * @param dummy a dummy string that does nothing
     */
    public AbstractVoucher(ConfigurationSection section, String file, String dummy) {
        this.section = section;

        populate();

        this.fileName = file;
    }

    protected final Map<String, String> requiredPlaceholders = new HashMap<>();

    protected String requiredPlaceholderMessage;

    /**
     * Builds a voucher
     *
     * @param section the configuration section
     * @param file the file
     */
    public AbstractVoucher(ConfigurationSection section, String file) {
        this.section = section;

        populate();

        this.builder = new ItemHandler()
                .setMaterial(this.section.getString("item"))
                .setDisplayName(this.section.getString("name"))
                .setDisplayLore(this.section.getStringList("lore"))
                .setGlowing(this.section.getBoolean("glowing", false))
                .setItemDamage(this.section.getInt("display-damage", 0))
                .setPlayer(this.section.getString("player", ""));

        if (this.section.contains("display-trim")) {
            this.builder.setTrimMaterial(this.section.getString("display-trim.material"));
            this.builder.setTrimPattern(this.section.getString("display-trim.pattern"));
        }

        this.twoStep = this.section.getBoolean("options.two-step-authentication", false);

        if (section.contains("options.required-placeholders-message")) {
            this.requiredPlaceholderMessage = section.getString("options.required-placeholders-message");
        }

        if (section.contains("options.required-placeholders")) {
            ConfigurationSection keys = section.getConfigurationSection("options.required-placeholders");

            if (keys != null) {
                keys.getKeys(false).forEach(key -> this.requiredPlaceholders.put(keys.getString(key + ".placeholder"), keys.getString(key + ".value")));
            }
        }

        this.isEdible = this.builder.getMaterial().isEdible();

        this.plainName = PlainTextComponentSerializer.plainText().serialize(this.builder.getDisplayName());
        this.fileName = file;
    }

    public abstract boolean execute(Player player);

    private boolean isCancelled;

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * @return the file name without .yml
     */
    public String getFileName() {
        return getFileName(true);
    }

    /**
     *
     * @param chomp remove .yml from the name
     * @return the file name
     */
    public String getFileName(boolean chomp) {
        if (chomp) {
            return this.fileName.replace(".yml", "");
        }

        return this.fileName;
    }

    private void populate() {
        ConfigurationSection section = this.section.getConfigurationSection("options");

        if (section != null) {
            this.message = section.getString("message", "");

            String whitelist = "whitelist-worlds.";

            this.whitelistWorldToggle = section.getBoolean(whitelist + "toggle", false);
            this.whitelistWorldMessage = section.getString(whitelist + "message", "");
            this.whitelistWorldCommands = section.getStringList(whitelist + "commands");
            this.whitelistWorlds = section.getStringList(whitelist + "worlds");

            String permission = "permission.";

            this.whitelistPermissionToggle = section.getBoolean(permission + "whitelist-permission.toggle", false);
            this.whitelistPermissionMessage = section.getString(permission + "whitelist-permission.message", "");
            this.whitelistPermissions = section.getStringList(permission + "whitelist-permission.permissions");
            this.whitelistCommands = section.getStringList(permission + "whitelist-permission.commands");

            this.blacklistPermissionToggle = section.getBoolean(permission + "blacklist-permission.toggle", false);
            this.blacklistPermissionMessage = section.getString(permission + "blacklist-permission.message", "");
            this.blacklistPermissions = section.getStringList(permission + "blacklist-permission.permissions");
            this.blacklistCommands = section.getStringList(permission + "blacklist-permission.commands");

            String limiter = "limiter.";

            this.limiterToggle = section.getBoolean(limiter + "toggle", false);
            this.limiterAmount = section.getInt(limiter + "amount", 10);

            String sounds = "sound.";

            this.soundToggle = section.getBoolean(sounds + "toggle", false);
            this.volume = (float) section.getDouble(sounds + "volume", 1.0f);
            this.pitch = (float) section.getDouble(sounds + "pitch", 1.0f);

            if (this.soundToggle) {
                section.getStringList(sounds + "sounds").forEach(key -> {
                    Sound sound = ItemUtil.getSound(key);

                    if (sound != null) {
                        this.sounds.add(sound);
                    }
                });
            }

            String firework = "firework.";

            this.fireworkToggle = section.getBoolean("firework.toggle", false);
            this.fireworkColors = section.getString("firework.colors", "Black, Gray, Aqua");
        }
    }
}