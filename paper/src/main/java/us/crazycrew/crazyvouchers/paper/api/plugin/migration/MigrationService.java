package us.crazycrew.crazyvouchers.paper.api.plugin.migration;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyenvoys.common.config.types.Messages;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MigrationService {

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    private SettingsManager config;

    public void migrate() {
        // Copy all vouchers into their own directory.
        copyVouchers();

        // Copy all codes into their own directory.
        copyCodes();

        // Migrate what's left from the voucher-backup to the new config.yml
        copyConfig();

        // Migrate all messages to en-US.yml then delete Messages.yml
        copyMessages();

        // Rename file if found.
        File file = new File(this.plugin.getDataFolder(), "data.yml");
        if (file.exists()) file.renameTo(new File(this.plugin.getDataFolder(), "users.yml"));

        // Delete file if found.
        File codes = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");
        if (codes.exists()) codes.delete();

        // Delete file if found.
        File backupFile = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");
        if (backupFile.exists()) backupFile.delete();
    }

    private void copyVouchers() {
        File file = new File(this.plugin.getDataFolder(), "Config.yml");

        // Load configuration of input.
        YamlConfiguration config = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(file)).join();

        // Get the configuration section.
        ConfigurationSection vouchers = config.getConfigurationSection("Vouchers");

        // If we can't see the section in the config.yml, we do nothing.
        if (vouchers == null) return;

        File backupFile = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");

        // Rename to back up file.
        file.renameTo(backupFile);

        // Load configuration of backup.
        YamlConfiguration backup = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(backupFile)).join();

        // check if vouchers folder exists.
        File newFolder = new File(this.plugin.getDataFolder(), "vouchers");
        if (!newFolder.exists()) newFolder.mkdirs();

        // Loop through voucher section.
        vouchers.getKeys(false).forEach(name -> {
            // Create voucher file in vouchers folder based on voucher name.
            File newFile = new File(newFolder, name + ".yml");
            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            String path = "Vouchers." + name + ".";

            // Default content.
            String item = backup.getString(path + "Item", "PAPER");
            String itemName = backup.getString(path + "Name", " ");
            List<String> lore = backup.getStringList(path + "Lore");
            String player = backup.getString(path + "Player", "");

            boolean glowing = backup.getBoolean(path + "Glowing", false);

            List<String> randomCommands = backup.getStringList(path + "Random-Commands");

            List<String> chanceCommands = backup.getStringList(path + "Chance-Commands");

            List<String> commands = backup.getStringList(path + "Commands");

            List<String> items = backup.getStringList(path + "Items");

            String message = backup.getString(path + "Options.Message", "Wow, you won something.");

            // Worlds.
            boolean worldsToggle = backup.getBoolean(path + "Options.Whitelist-Worlds.Toggle", false);

            String worldsMessage = backup.getString(path + "Options.Whitelist-Worlds.Message", "You must be in one of the whitelisted worlds to use this.");

            List<String> worldsList = backup.getStringList(path + "Options.Whitelist-Worlds.Worlds");

            // Permissions.
            boolean whitelistPermissionToggle = backup.getBoolean(path + "Options.Permission.Whitelist-Permission.Toggle", false);

            String whitelistMessage = backup.getString(path + "Options.Permission.Whitelist-Permission.Message", "You need {permission} so you can use this.");

            List<String> whiteListPermissions = backup.getStringList(path + "Options.Permission.Whitelist-Permission.Permissions");

            boolean blacklistPermissionToggle = backup.getBoolean(path + "Options.Permission.Blacklist-Permissions.Toggle", false);

            String blacklistMessage = backup.getString(path + "Options.Permission.Blacklist-Permissions.Message", "You have {permission} already so you can''t use this.");

            List<String> blackListPermissions = backup.getStringList(path + "Options.Permission.Blacklist-Permissions.Permissions");

            // Limiter
            boolean limiterToggle = backup.getBoolean(path + "Options.Limiter.Toggle", false);

            int limiterAmount = backup.getInt(path + "Options.Limiter.Limit", 10);

            // Two Step
            boolean twoStep = backup.getBoolean(path + "Options.Two-Step-Authentication", false);

            // Sounds
            boolean soundToggle = backup.getBoolean(path + "Options.Sound.Toggle", false);

            List<String> sounds = backup.getStringList(path + "Options.Sound.Sounds");

            // Fireworks
            boolean fireworkToggle = backup.getBoolean(path + "Options.Firework.Toggle", false);

            String fireworkColors = backup.getString(path + "Options.Firework.Colors", "Black, Gray, Aqua");

            boolean isEdible = backup.getBoolean(path + "Options.Is-Edible", false);

            List<String> itemFlags = backup.getStringList(path + "Flags");

            YamlConfiguration voucher = YamlConfiguration.loadConfiguration(newFile);
            
            String newPath = path + "voucher.";

            voucher.set(newPath + "item", item);
            voucher.set(newPath + "name", convert(itemName));

            ArrayList<String> loreLines = new ArrayList<>();

            lore.forEach(line -> loreLines.add(convert(line)));

            voucher.set(newPath + "lore", loreLines);

            if (backup.contains(path + "Flags")) {
                ArrayList<String> itemFlagsLines = new ArrayList<>();

                itemFlags.forEach(line -> itemFlagsLines.add(convert(line)));

                voucher.set(newPath + "flags", itemFlagsLines);
            }

            voucher.set(newPath + "player", player);
            
            if (backup.contains(path + "Glowing")) voucher.set(newPath + "glowing", glowing);

            ConfigurationSection section = backup.getConfigurationSection(path + "Options.Required-Placeholders");

            if (section != null) {
                section.getKeys(false).forEach(line -> {
                    String placeholder = backup.getString(path + "Options.Required-Placeholders." + line + ".Placeholder");
                    String value = backup.getString(path + "Options.Required-Placeholders." + line + ".Value");

                    voucher.set(newPath + "options.required-placeholders." + line + ".Placeholder", placeholder);
                    voucher.set(newPath + "options.required-placeholders." + line + ".Value", value);
                });
            }

            if (backup.contains(path + "Options.Message")) {
                voucher.set(newPath + "options.message", convert("{prefix}" + message));
            }

            if (backup.contains(path + "Options.Whitelist-Worlds")) {
                voucher.set(path + "options.whitelist-worlds.toggle", worldsToggle);
                voucher.set(path + "options.whitelist-worlds.message", convert("{prefix}" + worldsMessage));
                voucher.set(path + "options.whitelist-worlds.worlds", worldsList);
            }

            if (backup.contains(path + "Options.Permission.Whitelist-Permission")) {
                voucher.set(path + "options.permission.whitelist-permission.toggle", whitelistPermissionToggle);
                voucher.set(path + "options.permission.whitelist-permission.message", convert("{prefix}" + whitelistMessage));
                voucher.set(path + "options.permission.whitelist-permission.permissions", whiteListPermissions);
            }

            if (backup.contains(path + "Options.Permission.Blacklist-Permissions")) {
                voucher.set(path + "options.permission.blacklist-permission.toggle", blacklistPermissionToggle);
                voucher.set(path + "options.permission.blacklist-permission.message", convert("{prefix}" + blacklistMessage));
                voucher.set(path + "options.permission.blacklist-permission.permissions", blackListPermissions);
            }

            if (backup.contains(path + "Options.Limiter")) {
                voucher.set(path + "options.limiter.toggle", limiterToggle);
                voucher.set(path + "options.limiter.amount", limiterAmount);
            }

            if (backup.contains(path + "Options.Two-Step-Authentication")) {
                voucher.set(path + "options.two-step-authentication", twoStep);
            }

            if (backup.contains(path + "Options.Sound")) {
                voucher.set(path + "options.sound.toggle", soundToggle);
                voucher.set(path + "options.sound.pitch", 1.0);
                voucher.set(path + "options.sound.volume", 1.0);
                voucher.set(path + "options.sound.sounds", sounds);
            }

            if (backup.contains(path + "Options.Firework")) {
                voucher.set(path + "options.firework.toggle", fireworkToggle);
                voucher.set(path + "options.firework.colors", fireworkColors);
            }

            if (backup.contains(path + "Options.Is-Edible")) {
                voucher.set(path + "options.is-edible", isEdible);
            }

            if (backup.contains(path + "Items")) {
                voucher.set(path + "items", items);
            }

            if (backup.contains(path + "Commands")) {
                ArrayList<String> commandList = new ArrayList<>();
                commands.forEach(line -> commandList.add(convert(line)));
                voucher.set(path + "commands", commandList);
            }

            if (backup.contains(path + "Random-Commands")) {
                ArrayList<String> randomCommandList = new ArrayList<>();
                randomCommands.forEach(line -> randomCommandList.add(convert(line)));
                voucher.set(path + "random-commands", randomCommandList);
            }

            if (backup.contains(path + "Chance-Commands")) {
                ArrayList<String> chanceCommandList = new ArrayList<>();
                chanceCommands.forEach(line -> chanceCommandList.add(convert(line)));
                voucher.set(path + "chance-commands", chanceCommandList);
            }

            if (backup.contains(path + "DisplayDamage")) {
                int displayDamage = backup.getInt(path + "DisplayDamage");
                // do your thing.
                voucher.set(path + "display-damage", displayDamage);
            }

            if (backup.contains(path + "DisplayTrim.Material")) {
                String trimMaterial = backup.getString(path + "DisplayTrim.Material");
                // do your thing.
                voucher.set(path + "display-trim.material", trimMaterial);
            }

            if (backup.contains(path + "Options.Permission.Whitelist-Permission.Node")) {
                String singleNode = backup.getString(path + "Options.Permission.Whitelist-Permission.Node");

                voucher.set(path + "options.permission.whitelist-permission.node", singleNode);
            }

            if (backup.contains(path + "DisplayTrim.Pattern")) {
                String trimPattern = backup.getString(path + "DisplayTrim.Pattern");
                // do your thing.
                voucher.set(path + "display-trim.pattern", trimPattern);
            }

            try {
                // Save to each file.
                voucher.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyCodes() {
        File input = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");

        // If the input does not exist, We don't need to do anything else.
        if (!input.exists()) return;

        // Load configuration of input.
        YamlConfiguration config = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(input)).join();

        // Get the configuration section.
        ConfigurationSection vouchers = config.getConfigurationSection("Voucher-Codes");

        // If the path is not found, return.
        if (vouchers == null) return;

        // If dir does not exist, create it.
        File newFolder = new File(this.plugin.getDataFolder(), "codes");
        if (!newFolder.exists()) newFolder.mkdirs();

        // Loop through the configuration section.
        vouchers.getKeys(false).forEach(name -> {
            // Create the new file based on the voucher name
            File newFile = new File(newFolder, name + ".yml");
            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Get the configuration path.
            String path = "Voucher-Codes." + name + ".";

            // Default content.
            String code = config.getString(path + "Code", UUID.randomUUID().toString());

            List<String> commands = config.getStringList(path + "Commands");

            List<String> randomCommands = config.getStringList(path + "Random-Commands");

            List<String> chanceCommands = config.getStringList(path + "Chance-Commands");

            List<String> items = config.getStringList(path + "Items");

            boolean enabled = config.getBoolean(path + "Options.Enabled", true);

            boolean caseSensitive = config.getBoolean(path + "Options.Case-Sensitive", false);

            String message = config.getString(path + "Options.Message", "Wow you redeemed a code.");

            // Worlds.
            boolean worldsToggle = config.getBoolean(path + "Options.Whitelist-Worlds.Toggle", false);

            String worldsMessage = config.getString(path + "Options.Whitelist-Worlds.Message", "You must be in one of the whitelisted worlds to use this.");

            List<String> worldsList = config.getStringList(path + "Options.Whitelist-Worlds.Worlds");

            List<String> worldsCommands = config.getStringList(path + "Options.Whitelist-Worlds.Commands");

            // Permissions.
            boolean whitelistPermissionToggle = config.getBoolean(path + "Options.Permission.Whitelist-Permission.Toggle", false);

            String whitelistMessage = config.getString(path + "Options.Permission.Whitelist-Permission.Message", "You need {permission} so you can use this.");

            List<String> whiteListPermissions = config.getStringList(path + "Options.Permission.Whitelist-Permission.Permissions");

            List<String> whiteListCommands = config.getStringList(path + "Options.Permission.Whitelist-Permission.Commands");

            boolean blacklistPermissionToggle = config.getBoolean(path + "Options.Permission.Blacklist-Permissions.Toggle", false);

            String blacklistMessage = config.getString(path + "Options.Permission.Blacklist-Permissions.Message", "You have {permission} already so you can''t use this.");

            List<String> blackListPermissions = config.getStringList(path + "Options.Permission.Blacklist-Permissions.Permissions");

            List<String> blackListCommands = config.getStringList(path + "Options.Permission.Blacklist-Permissions.Commands");

            // Limiter
            boolean limiterToggle = config.getBoolean(path + "Options.Limiter.Toggle", false);

            int limiterAmount = config.getInt(path + "Options.Limiter.Limit", 10);

            // Sounds
            boolean soundToggle = config.getBoolean(path + "Options.Sound.Toggle", false);
            List<String> sounds = config.getStringList(path + "Options.Sound.Sounds");

            // Fireworks
            boolean fireworkToggle = config.getBoolean(path + "Options.Firework.Toggle", false);

            String fireworkColors = config.getString(path + "Options.Firework.Colors", "Green, Lime");

            YamlConfiguration voucher = YamlConfiguration.loadConfiguration(newFile);

            String other = "voucher-code.";

            voucher.set(other + "code", code);

            voucher.set(other + "options.case-sensitive", caseSensitive);
            voucher.set(other + "options.enabled", enabled);

            if (config.contains(path + "Options.Message")) {
                voucher.set(other + "options.message", convert("{prefix}" + message));
            }

            if (config.contains(path + "Options.Whitelist-Worlds")) {
                voucher.set(other + "options.whitelist-worlds.toggle", worldsToggle);
                voucher.set(other + "options.whitelist-worlds.message", convert("{prefix}" + worldsMessage));
                voucher.set(other + "options.whitelist-worlds.worlds", worldsList);

                ArrayList<String> commandList = new ArrayList<>();
                worldsCommands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "options.whitelist-worlds.commands", commandList);
            }

            if (config.contains(path + "Options.Permission.Whitelist-Permission")) {
                voucher.set(other + "options.permission.whitelist-permission.toggle", whitelistPermissionToggle);
                voucher.set(other + "options.permission.whitelist-permission.message", convert("{prefix}" + whitelistMessage));
                voucher.set(other + "options.permission.whitelist-permission.permissions", whiteListPermissions);

                ArrayList<String> commandList = new ArrayList<>();
                whiteListCommands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "options.permission.whitelist-permission.commands", commandList);
            }

            if (config.contains(path + "Options.Permission.Blacklist-Permissions")) {
                voucher.set(other + "options.permission.blacklist-permission.toggle", blacklistPermissionToggle);
                voucher.set(other + "options.permission.blacklist-permission.message", convert("{prefix}" + blacklistMessage));
                voucher.set(other + "options.permission.blacklist-permission.permissions", blackListPermissions);

                ArrayList<String> commandList = new ArrayList<>();
                blackListCommands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "options.permission.blacklist-permission.commands", commandList);
            }

            if (config.contains(path + "Options.Limiter")) {
                voucher.set(other + "options.limiter.toggle", limiterToggle);
                voucher.set(other + "options.limiter.amount", limiterAmount);
            }

            if (config.contains(path + "Options.Sound")) {
                voucher.set(other + "options.sound.toggle", soundToggle);
                voucher.set(other + "options.sound.pitch", 1.0);
                voucher.set(other + "options.sound.volume", 1.0);
                voucher.set(other + "options.sound.sounds", sounds);
            }

            if (config.contains(path + "Options.Firework")) {
                voucher.set(other + "options.firework.toggle", fireworkToggle);
                voucher.set(other + "options.firework.colors", fireworkColors);
            }

            if (config.contains(path + "Items")) {
                voucher.set(other + "items", items);
            }

            if (config.contains(path + "Commands")) {
                ArrayList<String> commandList = new ArrayList<>();
                commands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "commands", commandList);
            }

            if (config.contains(path + "Random-Commands")) {
                ArrayList<String> commandList = new ArrayList<>();
                randomCommands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "random-commands", commandList);
            }

            if (config.contains(path + "Chance-Commands")) {
                ArrayList<String> commandList = new ArrayList<>();
                chanceCommands.forEach(line -> commandList.add(convert(line)));
                voucher.set(other + "chance-commands", commandList);
            }

            if (config.contains(path + "Options.Permission.Whitelist-Permission.Node")) {
                String singleNode = config.getString(path + "Options.Permission.Whitelist-Permission.Node");

                voucher.set(other + "options.permission.whitelist-permission.node", singleNode);
            }

            try {
                // Save to the new file.
                voucher.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyConfig() {
        File input = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");

        // If the input does not exist, We don't need to do anything else.
        if (!input.exists()) return;

        YamlConfiguration file = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(input)).join();

        String prefix = file.getString("Settings.Prefix");

        boolean mustBeInSurvival = file.getBoolean("Settings.Must-Be-In-Survival");

        boolean voucherRecipes = file.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Toggle");
        boolean voucherAlert = file.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Alert");

        boolean toggleMetrics = file.getBoolean("Settings.Toggle-Metrics");

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");

        this.config = SettingsManagerBuilder
                .withYamlFile(configFile)
                .useDefaultMigrationService()
                .configurationData(Config.class)
                .create();

        if (prefix != null) {
            this.config.setProperty(Config.command_prefix, prefix);
        }

        this.config.setProperty(Config.must_be_in_survival, mustBeInSurvival);
        this.config.setProperty(Config.prevent_using_vouchers_in_recipes_toggle, voucherRecipes);
        this.config.setProperty(Config.prevent_using_vouchers_in_recipes_alert, voucherAlert);

        this.config.setProperty(Config.toggle_metrics, toggleMetrics);

        // Save the config file.
        this.config.save();

        // Delete the input.
        input.delete();
    }

    private void copyMessages() {
        File input = new File(this.plugin.getDataFolder(), "Messages.yml");

        // If the input does not exist, We don't need to do anything else.
        if (!input.exists()) return;

        // Load configuration of input.
        YamlConfiguration file = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(input)).join();

        // Check if directory exists and create it if not.
        File localeDir = new File(this.plugin.getDataFolder(), "locale");
        if (!localeDir.exists()) localeDir.mkdirs();

        // Create messages file.
        File messagesFile = new File(localeDir, this.config.getProperty(Config.locale_file) + ".yml");
        SettingsManager messages = SettingsManagerBuilder
                .withYamlFile(messagesFile)
                .useDefaultMigrationService()
                .configurationData(Messages.class)
                .create();

        String configReload = convert("{prefix}" + file.getString("Messages.Config-Reload"));

        String survivalOnly = convert("{prefix}" + file.getString("Messages.Survival-Mode"));

        String playerOnly = convert("{prefix}" + file.getString("Messages.Players-Only"));

        String noPermission = convert("{prefix}" + file.getString("Messages.No-Permission"));

        String noPermissionUse = convert("{prefix}" + file.getString("Messages.No-Permission-To-Voucher"));

        String noPermOffHand = convert("{prefix}" + file.getString("Messages.No-Permission-To-Use-Voucher-In-OffHand"));

        String cannotPutTable = convert("{prefix}" + file.getString("Messages.Cannot-Put-Items-In-Crafting-Table"));

        String notOnline = convert("{prefix}" + file.getString("Messages.Not-Online"));

        String notANumber = convert("{prefix}" + file.getString("Messages.Not-A-Number"));

        String notAVoucher = convert("{prefix}" + file.getString("Messages.Not-A-Voucher"));

        String codeUnavailable = convert("{prefix}" + file.getString("Messages.Code-UnAvailable"));

        String codeUsed = convert("{prefix}" + file.getString("Messages.Code-Used"));

        String sentVoucher = convert("{prefix}" + file.getString("Messages.Given-A-Voucher"));

        String sentEveryoneVoucher = convert("{prefix}" + file.getString("Messages.Given-All-Players-Voucher"));

        String hitLimit = convert("{prefix}" + file.getString("Messages.Hit-Limit"));

        String twoStep = convert("{prefix}" + file.getString("Messages.Two-Step-Authentication"));

        String hasBlacklistPerm = convert("{prefix}" + file.getString("Messages.Has-Blacklist-Permission"));

        String notInWorld = convert("{prefix}" + file.getString("Messages.Not-In-Whitelisted-World"));

        String unstack = convert("{prefix}" + file.getString("Messages.Unstack-Item"));

        List<String> help = file.getStringList("Messages.Help");

        messages.setProperty(Messages.no_permission, noPermission);
        messages.setProperty(Messages.no_permission_to_use_voucher, noPermissionUse);

        messages.setProperty(Messages.no_permission_to_use_voucher_in_offhand, noPermOffHand);
        messages.setProperty(Messages.cannot_put_items_in_crafting_table, cannotPutTable);
        messages.setProperty(Messages.survival_mode, survivalOnly);

        messages.setProperty(Messages.not_online, notOnline);
        messages.setProperty(Messages.two_step_authentication, twoStep);
        messages.setProperty(Messages.hit_voucher_limit, hitLimit);

        messages.setProperty(Messages.not_a_number, notANumber);
        messages.setProperty(Messages.not_a_voucher, notAVoucher);
        messages.setProperty(Messages.not_in_whitelist_world, notInWorld);
        messages.setProperty(Messages.unstack_item, unstack);
        messages.setProperty(Messages.has_blacklist_permission, hasBlacklistPerm);
        messages.setProperty(Messages.code_used, codeUsed);
        messages.setProperty(Messages.code_unavailable, codeUnavailable);
        messages.setProperty(Messages.sent_voucher, sentVoucher);
        messages.setProperty(Messages.sent_everyone_voucher, sentEveryoneVoucher);

        messages.setProperty(Messages.player_only, playerOnly);
        messages.setProperty(Messages.config_reload, configReload);
        messages.setProperty(Messages.help, help);

        // Save the file.
        messages.save();

        // Delete the Messages.yml
        input.delete();
    }

    private String convert(String message) {
        return message
                .replaceAll("%Arg%", "{arg}")
                .replaceAll("%arg%", "{arg}")
                .replaceAll("%Player%", "{player}")
                .replaceAll("%player%", "{player}")
                .replaceAll("%Prefix%", "")
                .replaceAll("%prefix%", "")
                .replaceAll("%Random%", "{random}")
                .replaceAll("%random%", "{random}")
                .replaceAll("%World%", "{world}")
                .replaceAll("%world%", "{world}")
                .replaceAll("%X%", "{x}")
                .replaceAll("%x%", "{x}")
                .replaceAll("%Y%", "{y}")
                .replaceAll("%y%", "{y}")
                .replaceAll("%Z%", "{z}")
                .replaceAll("%z%", "{z}")
                .replaceAll("%Permission%", "{permission}")
                .replaceAll("%permission%", "{permission}");
    }
}