package com.badbones69.crazyvouchers.api.plugin.migration;

import com.badbones69.crazyvouchers.CrazyVouchers;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MigrationService {

    @NotNull
    private final CrazyVouchers plugin = CrazyVouchers.get();

    public void migrate(boolean loadOldWay) {
        // Copy all vouchers into their own directory.
        copyVouchers(loadOldWay);

        // Copy all codes into their own directory.
        copyCodes(loadOldWay);

        // Rename file if found.
        File file = new File(this.plugin.getDataFolder(), "data.yml");
        if (file.exists()) file.renameTo(new File(this.plugin.getDataFolder(), "users.yml"));

        final File directory = new File(this.plugin.getDataFolder(), "backups");

        directory.mkdirs();

        // Move file if found.
        File codes = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");
        codes.renameTo(new File(directory, "VoucherCodes.yml"));

        // Delete file if found.
        File backupFile = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");
        backupFile.renameTo(new File(directory, "Vouchers-Backup.yml"));

        File config = new File(this.plugin.getDataFolder(), "config.yml");

        if (!config.exists()) {
            try {
                FileUtils.copyFile(new File(directory, "Vouchers-Backup.yml"), config);
            } catch (IOException e) {
                this.plugin.getLogger().warning("Failed to copy Vouchers-Backup.yml");
            }

            YamlConfiguration key = YamlConfiguration.loadConfiguration(config);

            key.set("Vouchers", null);

            key.set("settings.use-old-file-system", loadOldWay);

            try {
                key.save(config);
            } catch (IOException e) {
                this.plugin.getLogger().warning("Failed to save config.yml");
            }
        }
    }

    private void copyVouchers(boolean loadOldWay) {
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

        if (loadOldWay) {
            vouchers.getKeys(false).forEach(name -> {
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
                boolean twoStep = backup.getBoolean(path + "Options.Two-Step-Authentication.Toggle", false);

                // Sounds
                boolean soundToggle = backup.getBoolean(path + "Options.Sound.Toggle", false);

                List<String> sounds = backup.getStringList(path + "Options.Sound.Sounds");

                // Fireworks
                boolean fireworkToggle = backup.getBoolean(path + "Options.Firework.Toggle", false);

                String fireworkColors = backup.getString(path + "Options.Firework.Colors", "Black, Gray, Aqua");

                boolean isEdible = backup.getBoolean(path + "Options.Is-Edible", false);

                List<String> itemFlags = backup.getStringList(path + "Flags");

                String newPath = "vouchers." + name + ".";

                File newFile = new File(this.plugin.getDataFolder(), "vouchers.yml");

                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                YamlConfiguration voucher = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(newFile)).join();

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
                    voucher.set(newPath + "options.whitelist-worlds.toggle", worldsToggle);
                    voucher.set(newPath + "options.whitelist-worlds.message", convert("{prefix}" + worldsMessage));
                    voucher.set(newPath + "options.whitelist-worlds.worlds", worldsList);
                }

                if (backup.contains(path + "Options.Permission.Whitelist-Permission")) {
                    voucher.set(newPath + "options.permission.whitelist-permission.toggle", whitelistPermissionToggle);
                    voucher.set(newPath + "options.permission.whitelist-permission.message", convert("{prefix}" + whitelistMessage));
                    voucher.set(newPath + "options.permission.whitelist-permission.permissions", whiteListPermissions);
                }

                if (backup.contains(path + "Options.Permission.Blacklist-Permissions")) {
                    voucher.set(newPath + "options.permission.blacklist-permission.toggle", blacklistPermissionToggle);
                    voucher.set(newPath + "options.permission.blacklist-permission.message", convert("{prefix}" + blacklistMessage));
                    voucher.set(newPath + "options.permission.blacklist-permission.permissions", blackListPermissions);
                }

                if (backup.contains(path + "Options.Limiter")) {
                    voucher.set(newPath + "options.limiter.toggle", limiterToggle);
                    voucher.set(newPath + "options.limiter.amount", limiterAmount);
                }

                if (backup.contains(path + "Options.Two-Step-Authentication")) {
                    voucher.set(newPath + "options.two-step-authentication", twoStep);
                }

                if (backup.contains(path + "Options.Sound")) {
                    voucher.set(newPath + "options.sound.toggle", soundToggle);
                    voucher.set(newPath + "options.sound.pitch", 1.0);
                    voucher.set(newPath + "options.sound.volume", 1.0);
                    voucher.set(newPath + "options.sound.sounds", sounds);
                }

                if (backup.contains(path + "Options.Firework")) {
                    voucher.set(newPath + "options.firework.toggle", fireworkToggle);
                    voucher.set(newPath + "options.firework.colors", fireworkColors);
                }

                if (backup.contains(path + "Options.Is-Edible")) {
                    voucher.set(newPath + "options.is-edible", isEdible);
                }

                if (backup.contains(path + "Items")) {
                    voucher.set(newPath + "items", items);
                }

                if (backup.contains(path + "Commands")) {
                    ArrayList<String> commandList = new ArrayList<>();
                    commands.forEach(line -> commandList.add(convert(line)));
                    voucher.set(newPath + "commands", commandList);
                }

                if (backup.contains(path + "Random-Commands")) {
                    ArrayList<String> randomCommandList = new ArrayList<>();
                    randomCommands.forEach(line -> randomCommandList.add(convert(line)));
                    voucher.set(newPath + "random-commands", randomCommandList);
                }

                if (backup.contains(path + "Chance-Commands")) {
                    ArrayList<String> chanceCommandList = new ArrayList<>();
                    chanceCommands.forEach(line -> chanceCommandList.add(convert(line)));
                    voucher.set(newPath + "chance-commands", chanceCommandList);
                }

                if (backup.contains(path + "DisplayDamage")) {
                    int displayDamage = backup.getInt(path + "DisplayDamage");
                    // do your thing.
                    voucher.set(newPath + "display-damage", displayDamage);
                }

                if (backup.contains(path + "DisplayTrim.Material")) {
                    String trimMaterial = backup.getString(path + "DisplayTrim.Material");
                    // do your thing.
                    voucher.set(newPath + "display-trim.material", trimMaterial);
                }

                if (backup.contains(path + "Options.Permission.Whitelist-Permission.Node")) {
                    String singleNode = backup.getString(path + "Options.Permission.Whitelist-Permission.Node");

                    voucher.set(newPath + "options.permission.whitelist-permission.node", singleNode);
                }

                if (backup.contains(path + "DisplayTrim.Pattern")) {
                    String trimPattern = backup.getString(path + "DisplayTrim.Pattern");
                    // do your thing.
                    voucher.set(newPath + "display-trim.pattern", trimPattern);
                }

                try {
                    // Save to each file.
                    voucher.save(newFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            return;
        }

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
            boolean twoStep = backup.getBoolean(path + "Options.Two-Step-Authentication.Toggle", false);

            // Sounds
            boolean soundToggle = backup.getBoolean(path + "Options.Sound.Toggle", false);

            List<String> sounds = backup.getStringList(path + "Options.Sound.Sounds");

            // Fireworks
            boolean fireworkToggle = backup.getBoolean(path + "Options.Firework.Toggle", false);

            String fireworkColors = backup.getString(path + "Options.Firework.Colors", "Black, Gray, Aqua");

            boolean isEdible = backup.getBoolean(path + "Options.Is-Edible", false);

            List<String> itemFlags = backup.getStringList(path + "Flags");

            YamlConfiguration voucher = YamlConfiguration.loadConfiguration(newFile);
            
            String newPath = "voucher.";

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
                voucher.set(newPath + "options.whitelist-worlds.toggle", worldsToggle);
                voucher.set(newPath + "options.whitelist-worlds.message", convert("{prefix}" + worldsMessage));
                voucher.set(newPath + "options.whitelist-worlds.worlds", worldsList);
            }

            if (backup.contains(path + "Options.Permission.Whitelist-Permission")) {
                voucher.set(newPath + "options.permission.whitelist-permission.toggle", whitelistPermissionToggle);
                voucher.set(newPath + "options.permission.whitelist-permission.message", convert("{prefix}" + whitelistMessage));
                voucher.set(newPath + "options.permission.whitelist-permission.permissions", whiteListPermissions);
            }

            if (backup.contains(path + "Options.Permission.Blacklist-Permissions")) {
                voucher.set(newPath + "options.permission.blacklist-permission.toggle", blacklistPermissionToggle);
                voucher.set(newPath + "options.permission.blacklist-permission.message", convert("{prefix}" + blacklistMessage));
                voucher.set(newPath + "options.permission.blacklist-permission.permissions", blackListPermissions);
            }

            if (backup.contains(path + "Options.Limiter")) {
                voucher.set(newPath + "options.limiter.toggle", limiterToggle);
                voucher.set(newPath + "options.limiter.amount", limiterAmount);
            }

            if (backup.contains(path + "Options.Two-Step-Authentication")) {
                voucher.set(newPath + "options.two-step-authentication", twoStep);
            }

            if (backup.contains(path + "Options.Sound")) {
                voucher.set(newPath + "options.sound.toggle", soundToggle);
                voucher.set(newPath + "options.sound.pitch", 1.0);
                voucher.set(newPath + "options.sound.volume", 1.0);
                voucher.set(newPath + "options.sound.sounds", sounds);
            }

            if (backup.contains(path + "Options.Firework")) {
                voucher.set(newPath + "options.firework.toggle", fireworkToggle);
                voucher.set(newPath + "options.firework.colors", fireworkColors);
            }

            if (backup.contains(path + "Options.Is-Edible")) {
                voucher.set(newPath + "options.is-edible", isEdible);
            }

            if (backup.contains(path + "Items")) {
                voucher.set(newPath + "items", items);
            }

            if (backup.contains(path + "Commands")) {
                ArrayList<String> commandList = new ArrayList<>();
                commands.forEach(line -> commandList.add(convert(line)));
                voucher.set(newPath + "commands", commandList);
            }

            if (backup.contains(path + "Random-Commands")) {
                ArrayList<String> randomCommandList = new ArrayList<>();
                randomCommands.forEach(line -> randomCommandList.add(convert(line)));
                voucher.set(newPath + "random-commands", randomCommandList);
            }

            if (backup.contains(path + "Chance-Commands")) {
                ArrayList<String> chanceCommandList = new ArrayList<>();
                chanceCommands.forEach(line -> chanceCommandList.add(convert(line)));
                voucher.set(newPath + "chance-commands", chanceCommandList);
            }

            if (backup.contains(path + "DisplayDamage")) {
                int displayDamage = backup.getInt(path + "DisplayDamage");
                // do your thing.
                voucher.set(newPath + "display-damage", displayDamage);
            }

            if (backup.contains(path + "DisplayTrim.Material")) {
                String trimMaterial = backup.getString(path + "DisplayTrim.Material");
                // do your thing.
                voucher.set(newPath + "display-trim.material", trimMaterial);
            }

            if (backup.contains(path + "Options.Permission.Whitelist-Permission.Node")) {
                String singleNode = backup.getString(path + "Options.Permission.Whitelist-Permission.Node");

                voucher.set(newPath + "options.permission.whitelist-permission.node", singleNode);
            }

            if (backup.contains(path + "DisplayTrim.Pattern")) {
                String trimPattern = backup.getString(path + "DisplayTrim.Pattern");
                // do your thing.
                voucher.set(newPath + "display-trim.pattern", trimPattern);
            }

            try {
                // Save to each file.
                voucher.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyCodes(boolean loadOldWay) {
        File input = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");

        // If the input does not exist, We don't need to do anything else.
        if (!input.exists()) return;

        // Load configuration of input.
        YamlConfiguration config = CompletableFuture.supplyAsync(() -> YamlConfiguration.loadConfiguration(input)).join();

        // Get the configuration section.
        ConfigurationSection vouchers = config.getConfigurationSection("Voucher-Codes");

        // If the path is not found, return.
        if (vouchers == null) return;

        if (loadOldWay) {
            vouchers.getKeys(false).forEach(name -> {
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

                File newFile = new File(this.plugin.getDataFolder(), "voucher-codes.yml");

                if (!newFile.exists()) {
                    try {
                        newFile.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

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

            return;
        }

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
                .replaceAll("%voucher%", "{voucher}")
                .replaceAll("%Voucher%", "{voucher}")
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