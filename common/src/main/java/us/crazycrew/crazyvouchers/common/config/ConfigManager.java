package us.crazycrew.crazyvouchers.common.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.resource.YamlFileResourceOptions;
import us.crazycrew.crazyvouchers.common.config.migrate.ConfigMigration;
import us.crazycrew.crazyvouchers.common.config.migrate.LocaleMigration;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;
import us.crazycrew.crazyvouchers.common.config.types.MessageKeys;
import java.io.File;

public class ConfigManager {

    private final File dataFolder;

    public ConfigManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    private SettingsManager messages;
    private SettingsManager config;

    public void load() {
        YamlFileResourceOptions builder = YamlFileResourceOptions.builder().indentationSize(2).build();

        File configFile = new File(this.dataFolder, "config.yml");

        this.config = SettingsManagerBuilder
                .withYamlFile(configFile, builder)
                .migrationService(new ConfigMigration())
                .configurationData(ConfigurationDataBuilder.createConfiguration(ConfigKeys.class))
                .create();

        File oldFile = new File(this.dataFolder, "Messages.yml");

        File localeDir = new File(this.dataFolder, "locale");

        if (oldFile.exists()) {
            if (!localeDir.exists()) localeDir.mkdirs();

            oldFile.renameTo(new File(localeDir, "en-US.yml"));
        }

        if (!localeDir.exists()) localeDir.mkdirs();

        File messagesFile = new File(localeDir, this.config.getProperty(ConfigKeys.locale_file) + ".yml");

        this.messages = SettingsManagerBuilder
                .withYamlFile(messagesFile, builder)
                .migrationService(new LocaleMigration())
                .configurationData(MessageKeys.class)
                .create();
    }

    public void reload() {
        // Reload config.yml
        this.config.reload();

        // Reload messages.yml
        this.messages.reload();
    }

    public SettingsManager getConfig() {
        return this.config;
    }

    public SettingsManager getMessages() {
        return this.messages;
    }
}