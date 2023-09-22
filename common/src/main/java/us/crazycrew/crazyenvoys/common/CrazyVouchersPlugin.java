package us.crazycrew.crazyenvoys.common;

import us.crazycrew.crazyenvoys.common.api.AbstractPlugin;
import us.crazycrew.crazyenvoys.common.config.ConfigManager;
import java.io.File;

public class CrazyVouchersPlugin extends AbstractPlugin {

    private final ConfigManager configManager;

    public CrazyVouchersPlugin(File dataFolder) {
        this.configManager = new ConfigManager(dataFolder);
    }

    public void enable() {
        this.configManager.load();
    }

    public void disable() {
        this.configManager.reload();
    }

    @Override
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
}