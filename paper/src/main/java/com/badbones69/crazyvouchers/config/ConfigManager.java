package com.badbones69.crazyvouchers.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.resource.YamlFileResourceOptions;
import com.badbones69.crazyvouchers.config.migrate.ConfigMigration;
import com.badbones69.crazyvouchers.config.migrate.LocaleMigration;
import com.badbones69.crazyvouchers.config.types.ConfigKeys;
import com.badbones69.crazyvouchers.config.types.locale.MessageKeys;
import java.io.File;

public class ConfigManager {

    private static SettingsManager config;

    private static SettingsManager messages;

    /**
     * Loads configuration files.
     */
    public static void load(final File dataFolder) {
        YamlFileResourceOptions builder = YamlFileResourceOptions.builder().indentationSize(2).build();

        File configFile = new File(dataFolder, "config.yml");

        config = SettingsManagerBuilder
                .withYamlFile(configFile, builder)
                .migrationService(new ConfigMigration())
                .configurationData(ConfigurationDataBuilder.createConfiguration(ConfigKeys.class))
                .create();

        File oldFile = new File(dataFolder, "Messages.yml");

        File localeDir = new File(dataFolder, "locale");

        if (oldFile.exists()) {
            if (!localeDir.exists()) localeDir.mkdirs();

            oldFile.renameTo(new File(localeDir, "en-US.yml"));
        }

        if (!localeDir.exists()) localeDir.mkdirs();

        File messagesFile = new File(localeDir, config.getProperty(ConfigKeys.locale_file) + ".yml");

        messages = SettingsManagerBuilder
                .withYamlFile(messagesFile, builder)
                .migrationService(new LocaleMigration())
                .configurationData(MessageKeys.class)
                .create();
    }

    /**
     * Refreshes configuration files.
     */
    public static void refresh() {
        config.reload();
        messages.reload();
    }

    /**
     * @return gets config.yml
     */
    public static SettingsManager getConfig() {
        return config;
    }

    /**
     * @return gets messages.yml
     */
    public static SettingsManager getMessages() {
        return messages;
    }
}