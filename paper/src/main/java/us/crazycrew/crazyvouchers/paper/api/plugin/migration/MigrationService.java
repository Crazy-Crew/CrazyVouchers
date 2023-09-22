package us.crazycrew.crazyvouchers.paper.api.plugin.migration;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MigrationService {

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    public void migrate() {
        copyVouchers();

        File codes = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");

        if (codes.exists()) codes.renameTo(new File(this.plugin.getDataFolder(), "voucher-codes.yml"));

        copyCodes();

        copyConfig();

        copyMessages();

        File file = new File(this.plugin.getDataFolder(), "data.yml");

        if (file.exists()) file.renameTo(new File(this.plugin.getDataFolder(), "users.yml"));
    }

    public void copyVouchers() {
        File file = new File(this.plugin.getDataFolder(), "Config.yml");

        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection vouchers = config.getConfigurationSection("Vouchers");

        if (vouchers == null) return;

        File backupFile = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");

        file.renameTo(backupFile);
        if (file.exists()) file.delete();

        YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);
        
        File newFolder = new File(this.plugin.getDataFolder(), "vouchers");

        if (!newFolder.exists()) newFolder.mkdirs();

        vouchers.getKeys(false).forEach(name -> {
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
                voucher.save(newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void copyCodes() {
        File file = new File(this.plugin.getDataFolder(), "voucher-codes.yml");

        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection vouchers = config.getConfigurationSection("Voucher-Codes");

        if (vouchers == null) return;

        File newFolder = new File(this.plugin.getDataFolder(), "codes");

        if (!newFolder.exists()) newFolder.mkdirs();

        vouchers.getKeys(false).forEach(name -> {
            File newFile = new File(newFolder, name + ".yml");

            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

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
                voucher.save(newFile);

                file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void copyConfig() {
        File backupFile = new File(this.plugin.getDataFolder(), "Vouchers-Backup.yml");

        if (!backupFile.exists()) return;

        YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);

        String prefix = backup.getString("Settings.Prefix");

        boolean mustBeInSurvival = backup.getBoolean("Settings.Must-Be-In-Survival");

        boolean voucherRecipes = backup.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Toggle");
        boolean voucherAlert = backup.getBoolean("Settings.Prevent-Using-Vouchers-In-Recipes.Alert");

        boolean toggleMetrics = backup.getBoolean("Settings.Toggle-Metrics");

        backup.set("settings.prefix", prefix);

        backup.set("settings.survival-only", mustBeInSurvival);

        backup.set("settings.recipes.toggle", voucherRecipes);
        backup.set("settings.recipes.alert", voucherAlert);

        backup.set("settings.toggle_metrics", toggleMetrics);

        try {
            backup.save(backupFile);

            backup.set("Vouchers", null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        backupFile.renameTo(new File(this.plugin.getDataFolder(), "config.yml"));
    }

    public void copyMessages() {
        File oldFile = new File(this.plugin.getDataFolder(), "Messages.yml");

        File localeDir = new File(this.plugin.getDataFolder(), "locale");

        if (!localeDir.exists()) localeDir.mkdirs();

        File newFile = new File(localeDir, "en-US.yml");

        if (newFile.exists()) return;

        if (!newFile.exists()) {
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(oldFile);

        String configReload = config.getString("Messages.Config-Reload");

        String survivalOnly = config.getString("Messages.Survival-Mode");

        String playerOnly = config.getString("Messages.Players-Only");

        String noPermission = config.getString("Messages.No-Permission");

        String noPermissionUse = config.getString("Messages.No-Permission-To-Voucher");

        String noPermOffHand = config.getString("Messages.No-Permission-To-Use-Voucher-In-OffHand");

        String cannotPutTable = config.getString("Messages.Cannot-Put-Items-In-Crafting-Table");

        String notOnline = config.getString("Messages.Not-Online");

        String notANumber = config.getString("Messages.Not-A-Number");

        String notAVoucher = config.getString("Messages.Not-A-Voucher");

        String codeUnavailable = config.getString("Messages.Code-UnAvailable");

        String codeUsed = config.getString("Messages.Code-Used");

        String sentVoucher = config.getString("Messages.Given-A-Voucher");

        String sentEveryoneVoucher = config.getString("Messages.Given-All-Players-Voucher");

        String hitLimit = config.getString("Messages.Hit-Limit");

        String twoStep = config.getString("Messages.Two-Step-Authentication");

        String hasBlacklistPerm = config.getString("Messages.Has-Blacklist-Permission");

        String notInWorld = config.getString("Messages.Not-In-Whitelisted-World");

        String unstack = config.getString("Messages.Unstack-Item");

        List<String> help = config.getStringList("Messages.Help");

        YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(newFile);

        newConfig.set("player.no-permission", convert("{prefix}" + noPermission));
        newConfig.set("player.voucher.no-permission", convert("{prefix}" + noPermissionUse));
        newConfig.set("player.voucher.no-permission-offhand", convert("{prefix}" + noPermOffHand));
        newConfig.set("player.voucher.cant-put-in-crafting-table", convert("{prefix}" + cannotPutTable));
        newConfig.set("player.survival-only", convert("{prefix}" + survivalOnly));

        newConfig.set("player.target-not-online", convert("{prefix}" + notOnline));

        newConfig.set("player.two-step-authentication", convert("{prefix}" + twoStep));
        newConfig.set("player.hit-limit", convert("{prefix}" + hitLimit));
        
        newConfig.set("voucher.requirements.not-a-number", convert("{prefix}" + notANumber));
        newConfig.set("voucher.requirements.not-a-voucher", convert("{prefix}" + notAVoucher));

        newConfig.set("voucher.requirements.not-in-world", convert("{prefix}" + notInWorld));
        newConfig.set("voucher.requirements.un-stack-item", convert("{prefix}" + unstack));
        newConfig.set("voucher.requirements.has-blacklist-perm",convert("{prefix}" + hasBlacklistPerm));

        newConfig.set("voucher.code.used", convert("{prefix}" + codeUsed));
        newConfig.set("voucher.code.unavailable", convert("{prefix}" + codeUnavailable));

        newConfig.set("voucher.sent-voucher", convert("{prefix}" + sentVoucher));
        newConfig.set("voucher.sent-everyone-voucher", convert("{prefix}" + sentEveryoneVoucher));

        newConfig.set("misc.player-only", convert("{prefix}" + playerOnly));
        newConfig.set("misc.config-reload", convert("{prefix}" + configReload));
        newConfig.set("misc.help", help);

        try {
            newConfig.save(newFile);

            oldFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convert(String message) {
        return message
                .replaceAll("%Arg%", "{arg}")
                .replaceAll("%arg%", "{arg}")
                .replaceAll("%Player%", "{player}")
                .replaceAll("%player%", "{player}")
                .replaceAll("%Prefix%", "{prefix}")
                .replaceAll("%prefix%", "{prefix}")
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