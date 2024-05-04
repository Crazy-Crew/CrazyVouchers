package com.badbones69.crazyvouchers.api.objects.v2;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import com.badbones69.crazyvouchers.api.builders.ItemBuilder;
import com.badbones69.crazyvouchers.api.enums.Messages;
import com.badbones69.crazyvouchers.api.objects.VoucherCommand;
import com.badbones69.crazyvouchers.platform.config.ConfigManager;
import com.ryderbelserion.vital.util.DyeUtil;
import com.ryderbelserion.vital.util.ItemUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractVoucher {

    protected final @NotNull CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    protected final @NotNull SettingsManager config = ConfigManager.getConfig();

    protected final @NotNull Server server = this.plugin.getServer();

    protected final ConfigurationSection section;

    protected ItemBuilder builder = null;

    protected final String fileName;

    protected String plainName = "";

    protected boolean isEdible = false;

    // The options shared by both vouchers and voucher codes.
    protected String message;

    protected boolean whitelistWorldToggle;
    protected String whitelistWorldMessage;
    protected List<String> whitelistWorldCommands = new ArrayList<>();
    protected List<String> whitelistWorlds = new ArrayList<>();

    protected boolean whitelistPermissionToggle;
    protected String whitelistPermissionMessage;
    protected List<String> whitelistPermissions = new ArrayList<>();
    protected List<String> whitelistCommands = new ArrayList<>();

    protected boolean blacklistPermissionToggle;
    protected String blacklistPermissionMessage;
    protected List<String> blacklistPermissions = new ArrayList<>();
    protected List<String> blacklistCommands = new ArrayList<>();

    protected boolean limiterToggle;
    protected int limiterAmount;

    protected boolean soundToggle;
    protected float volume;
    protected float pitch;
    protected List<Sound> sounds = new ArrayList<>();

    protected boolean fireworkToggle;
    protected List<Color> fireworkColors = new ArrayList<>();

    protected boolean twoStep;

    /**
     * Builds a voucher code
     *
     * @param section the configuration section
     * @param file the file
     * @param dummy a dummy string that does nothing
     */
    public AbstractVoucher(@NotNull final ConfigurationSection section, @NotNull final String file, @Nullable String dummy) {
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
    public AbstractVoucher(@NotNull final ConfigurationSection section, @NotNull final String file) {
        this.section = section;

        populate();

        this.builder = new ItemBuilder()
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

        //todo() I don't think this works anymore due to recent 1.20.6 changes.
        section.getStringList("flags").forEach(flag -> this.builder.addItemFlag(ItemFlag.valueOf(flag)));

        this.isEdible = this.builder.getMaterial().isEdible();

        this.plainName = PlainTextComponentSerializer.plainText().serialize(this.builder.getDisplayName());
        this.fileName = file;
    }

    public abstract boolean execute(@NotNull final Player player, @Nullable final String argument);

    public abstract boolean execute(@NotNull final Player player);

    // It should always be false as default.
    private boolean isCancelled = false;

    /**
     * Cancels the voucher
     *
     * @param isCancelled true or false
     */
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * @return true or false
     */
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

    public boolean isSoundToggle() {
        return this.soundToggle;
    }

    public List<Sound> getSounds() {
        return Collections.unmodifiableList(this.sounds);
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getVolume() {
        return this.volume;
    }

    public boolean isFireworkToggle() {
        return this.fireworkToggle;
    }

    public List<Color> getFireworkColors() {
        return this.fireworkColors;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isLimiterToggle() {
        return this.limiterToggle;
    }

    protected List<VoucherCommand> randomCommands = new ArrayList<>();
    protected List<VoucherCommand> chanceCommands = new ArrayList<>();

    protected List<String> commands = new ArrayList<>();

    public List<VoucherCommand> getRandomCommands() {
        return Collections.unmodifiableList(this.randomCommands);
    }

    public List<VoucherCommand> getChanceCommands() {
        return Collections.unmodifiableList(this.chanceCommands);
    }

    public List<String> getCommands() {
        return Collections.unmodifiableList(this.commands);
    }

    protected List<ItemBuilder> builders = new ArrayList<>();

    public List<ItemBuilder> getBuilders() {
        return Collections.unmodifiableList(this.builders);
    }

    private void populate() {
        this.builders = ItemBuilder.convertStringList(this.section.getStringList("items"));

        this.section.getStringList("random-commands").forEach(command -> this.randomCommands.add(new VoucherCommand(command)));

        this.section.getStringList("chance-commands").forEach(command -> {
            try {
                String[] divider = command.split(" ");

                VoucherCommand voucherCommand = new VoucherCommand(command.substring(divider[0].length() + 1));

                for (int count = 1; count <= Integer.parseInt(divider[0]); count++) {
                    this.chanceCommands.add(voucherCommand);
                }
            } catch (Exception exception) {
                this.plugin.getLogger().warning("An issue occurred when trying to cache the chance commands.");
            }
        });

        this.commands = this.section.getStringList("commands");

        ConfigurationSection section = this.section.getConfigurationSection("options");

        if (section != null) {
            this.message = section.getString("message", "");

            String whitelist = "whitelist-worlds.";

            this.whitelistWorldMessage = section.getString(whitelist + "message", Messages.not_in_whitelisted_world.getString());
            this.whitelistWorldCommands = section.getStringList(whitelist + "commands");
            this.whitelistWorlds = section.getStringList(whitelist + "worlds").stream().map(String::toLowerCase).toList();
            this.whitelistWorldToggle = !this.whitelistWorlds.isEmpty() && section.getBoolean(whitelist + "toggle", false);

            String permission = "permission.";

            this.whitelistPermissionToggle = section.getBoolean(permission + "whitelist-permission.toggle", false);
            this.whitelistPermissionMessage = section.getString(permission + "whitelist-permission.message", Messages.no_permission_to_use_voucher.getString());

            if (section.contains(permission + "whitelist-permission.node")) {
                String key = section.getString(permission + "whitelist-permission.node", "");

                if (key.isEmpty()) return;

                this.whitelistPermissions.add(key.toLowerCase());
            }

            this.whitelistPermissions = section.getStringList(permission + "whitelist-permission.permissions").stream().map(String::toLowerCase).toList();
            this.whitelistCommands = section.getStringList(permission + "whitelist-permission.commands");

            this.blacklistPermissionToggle = section.getBoolean(permission + "blacklist-permission.toggle", false);
            this.blacklistPermissionMessage = section.getString(permission + "blacklist-permission.message", Messages.has_blacklist_permission.getString());
            this.blacklistPermissions = section.getStringList(permission + "blacklist-permission.permissions");
            this.blacklistCommands = section.getStringList(permission + "blacklist-permission.commands");

            String limiter = "limiter.";

            this.limiterToggle = section.getBoolean(limiter + "toggle", false);
            this.limiterAmount = section.getInt(limiter + "amount", 10);

            String sounds = "sound.";

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

            this.fireworkToggle = section.getBoolean(firework + "toggle", false);

            if (this.fireworkToggle) {
                for (String key : section.getString(firework + "colors", "Black, Gray, Aqua").split(", ")) {
                    Color color = DyeUtil.getColor(key);

                    if (color != null) {
                        this.fireworkColors.add(color);
                    }
                }
            }
        }
    }
}