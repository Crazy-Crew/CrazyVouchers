package us.crazycrew.crazyvouchers.paper.api.plugin.migration;

import com.badbones69.crazyvouchers.paper.CrazyVouchers;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MigrationService {

    private final CrazyVouchers plugin = JavaPlugin.getPlugin(CrazyVouchers.class);

    public void migrate() {
        copyVouchers();

        copyConfig();

        copyMessages();

        File file = new File(this.plugin.getDataFolder(), "data.yml");

        if (file.exists()) file.renameTo(new File(this.plugin.getDataFolder(), "users.yml"));

        File codes = new File(this.plugin.getDataFolder(), "VoucherCodes.yml");

        if (codes.exists()) codes.renameTo(new File(this.plugin.getDataFolder(), "voucher-codes.yml"));
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

            // Default content.
            String item = backup.getString("Vouchers." + name + ".Item", "PAPER");
            String itemName = backup.getString("Vouchers." + name + ".Name", " ");
            List<String> lore = backup.getStringList("Vouchers." + name + ".Lore");
            String player = backup.getString("Vouchers." + name + ".Player", "");

            boolean glowing = backup.getBoolean("Vouchers." + name + ".Glowing", false);

            List<String> randomCommands = backup.getStringList("Vouchers." + name + ".Random-Commands");

            List<String> chanceCommands = backup.getStringList("Vouchers." + name + ".Chance-Commands");

            List<String> commands = backup.getStringList("Vouchers." + name + ".Commands");

            List<String> items = backup.getStringList("Vouchers." + name + ".Items");

            String message = backup.getString("Vouchers." + name + ".Options.Message", "");

            // Worlds.
            boolean worldsToggle = backup.getBoolean("Vouchers." + name + ".Options.Whitelist-Worlds.Toggle", false);

            String worldsMessage = backup.getString("Vouchers." + name + ".Options.Whitelist-Worlds.Message", "");

            List<String> worldsList = backup.getStringList("Vouchers." + name + ".Options.Whitelist-Worlds.Worlds");

            // Permissions.
            boolean whitelistPermissionToggle = backup.getBoolean("Vouchers." + name + ".Options.Permission.Whitelist-Permission.Toggle", false);

            String whitelistMessage = backup.getString("Vouchers." + name + ".Options.Permission.Whitelist-Permission.Message", "");

            List<String> whiteListPermissions = backup.getStringList("Vouchers." + name + ".Options.Permission.Whitelist-Permission.Permissions");

            boolean blacklistPermissionToggle = backup.getBoolean("Vouchers." + name + ".Options.Permission.Blacklist-Permissions.Toggle", false);

            String blacklistMessage = backup.getString("Vouchers." + name + ".Options.Permission.Blacklist-Permissions.Message", "");

            List<String> blackListPermissions = backup.getStringList("Vouchers." + name + ".Options.Permission.Blacklist-Permissions.Permissions");

            // Limiter
            boolean limiterToggle = backup.getBoolean("Vouchers." + name + ".Options.Limiter.Toggle", false);

            int limiterAmount = backup.getInt("Vouchers." + name + ".Options.Limiter.Limit", 10);

            // Two Step
            boolean twoStep = backup.getBoolean("Vouchers." + name + ".Options.Two-Step-Authentication", false);

            // Sounds
            boolean soundToggle = backup.getBoolean("Vouchers." + name + ".Options.Sound.Toggle", false);

            List<String> sounds = backup.getStringList("Vouchers." + name + ".Options.Sound.Sounds");

            // Fireworks
            boolean fireworkToggle = backup.getBoolean("Vouchers." + name + ".Options.Firework.Toggle", false);

            String fireworkColors = backup.getString("Vouchers." + name + ".Options.Firework.Colors", "");

            boolean isEdible = backup.getBoolean("Vouchers." + name + ".Options.Is-Edible", false);

            List<String> itemFlags = backup.getStringList("Vouchers." + name + ".Flags");

            YamlConfiguration voucher = YamlConfiguration.loadConfiguration(newFile);

            voucher.set("voucher.item", item);
            voucher.set("voucher.name", convert(itemName));

            ArrayList<String> loreLines = new ArrayList<>();

            lore.forEach(line -> loreLines.add(convert(line)));

            voucher.set("voucher.lore", loreLines);

            ArrayList<String> itemFlagsLines = new ArrayList<>();

            itemFlags.forEach(line -> itemFlagsLines.add(convert(line)));

            voucher.set("voucher.flags", itemFlagsLines);

            voucher.set("voucher.player", player);
            voucher.set("voucher.glowing", glowing);

            ConfigurationSection section = backup.getConfigurationSection("Vouchers." + name + ".Options.Required-Placeholders");

            if (section != null) {
                section.getKeys(false).forEach(line -> {
                    String placeholder = backup.getString("Vouchers." + name + ".Options.Required-Placeholders." + line + ".Placeholder");
                    String value = backup.getString("Vouchers." + name + ".Options.Required-Placeholders." + line + ".Value");

                    voucher.set("voucher.options.required-placeholders." + line + ".Placeholder", placeholder);
                    voucher.set("voucher.options.required-placeholders." + line + ".Value", value);
                });
            }

            voucher.set("voucher.options.message", convert("{prefix}" + message));

            voucher.set("voucher.options.whitelist-worlds", worldsToggle);
            voucher.set("voucher.options.whitelist-worlds.message", convert("{prefix}" + worldsMessage));
            voucher.set("voucher.options.whitelist-worlds.worlds", worldsList);

            voucher.set("voucher.options.permission.whitelist-permission.toggle", whitelistPermissionToggle);
            voucher.set("voucher.options.permission.whitelist-permission.message", convert("{prefix}" + whitelistMessage));
            voucher.set("voucher.options.permission.whitelist-permission.permissions", whiteListPermissions);

            voucher.set("voucher.options.permission.blacklist-permission.toggle", blacklistPermissionToggle);
            voucher.set("voucher.options.permission.blacklist-permission.message", convert("{prefix}" + blacklistMessage));
            voucher.set("voucher.options.permission.blacklist-permission.permissions", blackListPermissions);

            voucher.set("voucher.options.limiter.toggle", limiterToggle);
            voucher.set("voucher.options.limiter.amount", limiterAmount);

            voucher.set("voucher.options.two-step-authentication", twoStep);

            voucher.set("voucher.options.sound.toggle", soundToggle);
            voucher.set("voucher.options.sound.sounds", sounds);

            voucher.set("voucher.options.firework.toggle", fireworkToggle);
            voucher.set("voucher.options.firework.colors", fireworkColors);

            voucher.set("voucher.options.is-edible", isEdible);

            voucher.set("voucher.items", items);

            ArrayList<String> commandList = new ArrayList<>();
            commands.forEach(line -> commandList.add(convert(line)));
            voucher.set("voucher.commands", commandList);

            ArrayList<String> chanceCommandList = new ArrayList<>();
            chanceCommands.forEach(line -> chanceCommandList.add(convert(line)));
            voucher.set("voucher.chance-commands", chanceCommandList);

            ArrayList<String> randomCommandList = new ArrayList<>();
            randomCommands.forEach(line -> randomCommandList.add(convert(line)));
            voucher.set("voucher.random-commands", randomCommandList);

            if (backup.contains("Vouchers." + name + ".DisplayDamage")) {
                int displayDamage = backup.getInt("Vouchers." + name + ".DisplayDamage");
                // do your thing.
                voucher.set("voucher.display-damage", displayDamage);
            }

            if (backup.contains("Vouchers." + name + ".DisplayTrim.Material")) {
                String trimMaterial = backup.getString("Vouchers." + name + ".DisplayTrim.Material");
                // do your thing.
                voucher.set("voucher.display-trim.material", trimMaterial);
            }

            if (backup.contains("Vouchers." + name + ".Options.Permission.Whitelist-Permission.Node")) {
                String singleNode = backup.getString("Vouchers." + name + ".Options.Permission.Whitelist-Permission.Node");

                voucher.set("voucher.options.permission.whitelist-permission.node", singleNode);
            }

            if (backup.contains("Vouchers." + name + ".DisplayTrim.Pattern")) {
                String trimPattern = backup.getString("Vouchers." + name + ".DisplayTrim.Pattern");
                // do your thing.
                voucher.set("voucher.display-trim.pattern", trimPattern);
            }

            try {
                voucher.save(newFile);
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

        String codeUnavailable = config.getString("Messages.CodeUnAvailable");

        String codeUsed = config.getString("Messages.CodeUsed");

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