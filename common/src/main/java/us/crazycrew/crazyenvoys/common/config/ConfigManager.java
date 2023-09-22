package us.crazycrew.crazyenvoys.common.config;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import us.crazycrew.crazyenvoys.common.config.types.Config;
import us.crazycrew.crazyenvoys.common.config.types.Messages;
import java.io.File;

public class ConfigManager {

    private final File dataFolder;

    public ConfigManager(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    private SettingsManager messages;
    private SettingsManager config;

    public void load() {
        File messagesFile = new File(this.dataFolder, "messages.yml");

        this.messages = SettingsManagerBuilder
                .withYamlFile(messagesFile)
                .useDefaultMigrationService()
                .configurationData(Messages.class)
                .create();

        File configFile = new File(this.dataFolder, "config.yml");

        this.config = SettingsManagerBuilder
                .withYamlFile(configFile)
                .useDefaultMigrationService()
                .configurationData(ConfigurationDataBuilder.createConfiguration(Config.class))
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