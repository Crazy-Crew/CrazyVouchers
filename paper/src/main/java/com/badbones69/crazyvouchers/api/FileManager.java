package com.badbones69.crazyvouchers.api;

import ch.jalu.configme.SettingsManager;
import com.badbones69.crazyvouchers.CrazyVouchers;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import us.crazycrew.crazyvouchers.common.config.types.ConfigKeys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {

    private @NotNull final CrazyVouchers plugin = CrazyVouchers.get();

    private @NotNull final SettingsManager config = this.plugin.getCrazyHandler().getConfigManager().getConfig();

    private final Map<Files, File> files = new HashMap<>();
    private final List<String> homeFolders = new ArrayList<>();
    private final List<CustomFile> customFiles = new ArrayList<>();
    private final Map<String, String> jarHomeFolders = new HashMap<>();
    private final Map<String, String> autoGenerateFiles = new HashMap<>();
    private final Map<Files, FileConfiguration> configurations = new HashMap<>();

    private final Logger logger = this.plugin.getLogger();
    private final boolean isLogging = this.plugin.isLogging();

    /**
     * Sets up the plugin and loads all necessary files.
     */
    public void setup() {
        File oldFolder = new File(this.plugin.getServer().getPluginsFolder() + "/Vouchers");

        File dataFolder = this.plugin.getDataFolder();

        if (oldFolder.exists() && !dataFolder.exists()) {
            this.logger.warning("Renaming " + oldFolder.getPath() + " directory to " + dataFolder.getPath());

            if (!dataFolder.exists()) dataFolder.mkdirs();

            oldFolder.renameTo(dataFolder);
            oldFolder.delete();
        } else {
            if (dataFolder.exists() && oldFolder.exists()) {
                this.logger.warning("The folder does exist which prevents " + oldFolder.getName() + " from being renamed.");
                this.logger.warning("You must delete or take a backup of " + dataFolder.getName() + " if you wish to have it automatically convert");
                this.logger.warning("You can run /vouchers reload after deleting or taking a backup of " + dataFolder.getName() + " if needed.");
            }
        }

        if (!dataFolder.exists()) dataFolder.mkdirs();

        this.files.clear();
        this.customFiles.clear();
        this.configurations.clear();

        List<Files> files = new ArrayList<>() {{
            if (config.getProperty(ConfigKeys.mono_file)) {
                add(Files.voucher_codes);
                add(Files.vouchers);
            }

            add(Files.users);
        }};

        // Loads all the normal static files.
        for (Files file : files) {
            final String fileName = file.getFileName();

            File newFile = new File(dataFolder, file.getFileLocation());

            if (this.isLogging) this.logger.info("Loading the " + fileName);

            if (!newFile.exists()) {
                try {
                    File serverFile = new File(dataFolder, "/" + file.getFileLocation());
                    InputStream jarFile = getClass().getResourceAsStream("/" + file.getFileJar());

                    copyFile(jarFile, serverFile);
                } catch (Exception exception) {
                    this.logger.log(Level.SEVERE, "Failed to load file: " + fileName, exception);

                    continue;
                }
            }

            this.files.put(file, newFile);

            if (file.getFileName().endsWith(".yml")) this.configurations.put(file, YamlConfiguration.loadConfiguration(newFile));

            if (this.isLogging) this.logger.info("Successfully loaded " + fileName);
        }

        if (this.homeFolders.isEmpty()) return;

        // Starts to load all the custom files.
        if (this.isLogging) this.logger.info("Loading custom files.");

        for (String homeFolder : this.homeFolders) {
            File homeFile = new File(dataFolder, "/" + homeFolder);

            if (homeFile.exists()) {
                File[] filesList = homeFile.listFiles();

                if (filesList != null) {
                    for (File directory : filesList) {
                        if (directory.isDirectory()) {
                            String[] folder = directory.list();

                            if (folder != null) {
                                for (String name : folder) {
                                    if (!name.endsWith(".yml")) continue;

                                    CustomFile file = new CustomFile(name, homeFolder + "/", directory.getName());

                                    if (file.exists()) {
                                        this.customFiles.add(file);

                                        if (this.isLogging) this.logger.info("Loaded new custom file: " + homeFolder + "/" + directory.getName() + "/" + name + ".");
                                    }
                                }
                            }
                        } else {
                            String name = directory.getName();

                            if (!name.endsWith(".yml")) continue;

                            CustomFile file = new CustomFile(name, homeFolder);

                            if (file.exists()) {
                                this.customFiles.add(file);

                                if (this.isLogging) this.logger.info("Loaded new custom file: " + homeFolder + "/" + name + ".");
                            }
                        }
                    }
                }
            } else {
                homeFile.mkdir();

                if (this.isLogging) this.logger.info("The folder " + homeFolder + "/ was not found so it was created.");

                for (String fileName : this.autoGenerateFiles.keySet()) {
                    if (this.autoGenerateFiles.get(fileName).equalsIgnoreCase(homeFolder)) {
                        homeFolder = this.autoGenerateFiles.get(fileName);

                        try (InputStream jarFile = getClass().getResourceAsStream((this.jarHomeFolders.getOrDefault(fileName, homeFolder)) + "/" + fileName)) {
                            File serverFile = new File(dataFolder, homeFolder + "/" + fileName);

                            copyFile(jarFile, serverFile);

                            if (fileName.toLowerCase().endsWith(".yml")) this.customFiles.add(new CustomFile(fileName, homeFolder));

                            if (this.isLogging) this.logger.info("Created new default file: " + homeFolder + "/" + fileName + ".");
                        } catch (Exception exception) {
                            this.logger.log(Level.SEVERE, "Failed to create new default file: " + homeFolder + "/" + fileName + "!", exception);
                        }
                    }
                }
            }
        }

        if (this.isLogging) this.logger.info("Finished loading custom files.");
    }

    /**
     * Register a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
     *
     * @param homeFolder the folder that has custom files in it.
     */
    public FileManager registerCustomFilesFolder(String homeFolder) {
        this.homeFolders.add(homeFolder);

        return this;
    }

    /**
     * Unregister a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
     *
     * @param homeFolder the folder with custom files in it.
     */
    public FileManager unregisterCustomFilesFolder(String homeFolder) {
        this.homeFolders.remove(homeFolder);

        return this;
    }

    /**
     * Register a file that needs to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
     *
     * @param fileName the name of the file you want to auto-generate when the folder doesn't exist.
     * @param homeFolder the folder that has custom files in it.
     */
    public FileManager registerDefaultGenerateFiles(String fileName, String homeFolder) {
        this.autoGenerateFiles.put(fileName, homeFolder);

        return this;
    }

    /**
     * Register a file that needs to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
     *
     * @param fileName the name of the file you want to auto-generate when the folder doesn't exist.
     * @param homeFolder the folder that has custom files in it.
     * @param jarHomeFolder the folder that the file is found in the jar.
     */
    public FileManager registerDefaultGenerateFiles(String fileName, String homeFolder, String jarHomeFolder) {
        this.autoGenerateFiles.put(fileName, homeFolder);
        this.jarHomeFolders.put(fileName, jarHomeFolder);

        return this;
    }

    /**
     * Unregister a file that doesn't need to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
     *
     * @param fileName the file that you want to remove from auto-generating.
     */
    public FileManager unregisterDefaultGenerateFiles(String fileName) {
        this.autoGenerateFiles.remove(fileName);
        this.jarHomeFolders.remove(fileName);

        return this;
    }

    /**
     * Gets the file from the system.
     *
     * @return the file from the system.
     */
    public FileConfiguration getFile(Files file) {
        return this.configurations.get(file);
    }

    /**
     * Get a custom file from the loaded custom files instead of a hardcoded one.
     * This allows you to get custom files like Per player data files.
     *
     * @param name name of the crate you want. (Without the .yml)
     * @return the custom file you wanted otherwise if not found will return null.
     */
    public CustomFile getFile(String name) {
        for (CustomFile file : this.customFiles) {
            if (file.getName().equalsIgnoreCase(name)) return file;
        }

        return null;
    }

    /**
     * Remove a file by name.
     *
     * @param name name to use.
     */
    public void removeFile(String name) {
        this.customFiles.remove(getFile(name));
    }

    /**
     * Add a file by name.
     *
     * @param name name to use.
     * @param folder folder to add to.
     */
    public void addFile(String name, String folder) {
        this.customFiles.add(new CustomFile(name, folder));
    }

    /**
     * Saves the file from the loaded state to the file system.
     */
    public void saveFile(Files file) {
        try {
            this.configurations.get(file).save(this.files.get(file));
        } catch (IOException exception) {
            this.logger.log(Level.SEVERE, "Could not save " + file.getFileName() + "!", exception);
        }
    }

    /**
     * Save a custom file.
     *
     * @param name the name of the custom file.
     */
    public void saveFile(String name) {
        CustomFile file = getFile(name);

        if (file == null) {
            if (this.isLogging) this.logger.warning("The file " + name + ".yml could not be found!");

            return;
        }

        try {
            file.getFile().save(new File(this.plugin.getDataFolder(), file.getHomeFolder() + "/" + file.getFileName()));

            if (this.isLogging) this.logger.info("Successfully saved the " + file.getFileName() + ".");
        } catch (Exception exception) {
            this.logger.log(Level.SEVERE, "Could not save " + file.getFileName() + "!", exception);
        }
    }

    /**
     * Save a custom file.
     *
     * @param file the custom file you are saving.
     */
    public void saveFile(CustomFile file) {
        file.saveFile();
    }

    /**
     * Overrides the loaded state file and loads the file systems file.
     */
    public void reloadFile(Files file) {
        if (file.getFileName().endsWith(".yml")) {
            File key = this.files.get(file);

            if (key != null) {
                this.configurations.put(file, YamlConfiguration.loadConfiguration(key));
            }
        }
    }

    /**
     * Overrides the loaded state file and loads the file systems file.
     */
    public void reloadFile(String name) {
        CustomFile file = getFile(name);

        if (file != null) {
            try {
                file.file = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "/" + file.getHomeFolder() + "/" + file.getFileName()));

                if (this.isLogging) this.logger.info("Successfully reloaded the " + file.getFileName() + ".");
            } catch (Exception exception) {
                this.logger.log(Level.SEVERE, "Could not reload the " + file.getFileName() + "!", exception);
            }
        } else {
            if (this.isLogging) this.logger.warning("The file " + name + ".yml could not be found!");
        }
    }

    /**
     * Overrides the loaded state file and loads the filesystems file.
     */
    public void reloadFile(CustomFile file) {
        file.reloadFile();
    }

    /**
     * Reloads all files.
     */
    public void reloadAllFiles() {
        for (Files file : Files.values()) {
            if (file != null) {
                file.reloadFile();
            }
        }

        for (CustomFile file : this.customFiles) {
            if (file != null) {
                file.reloadFile();
            }
        }
    }

    /**
     * @return A list of crate names.
     */
    public List<String> getVouchers() {
        File directory = new File(this.plugin.getDataFolder(), "/vouchers");

        return getFiles(directory);
    }

    /**
     * @return A list of crate names.
     */
    public List<String> getCodes() {
        File directory = new File(this.plugin.getDataFolder(), "/codes");

        return getFiles(directory);
    }

    private List<String> getFiles(File dir) {
        List<String> files = new ArrayList<>();

        String[] file = dir.list();

        if (file != null) {
            File[] filesList = dir.listFiles();

            if (filesList != null) {
                for (File directory : filesList) {
                    if (directory.isDirectory()) {
                        String[] folder = directory.list();

                        if (folder != null) {
                            for (String name : folder) {
                                if (!name.endsWith(".yml")) continue;

                                files.add(name.replaceAll(".yml", ""));
                            }
                        }
                    }
                }
            }

            for (String name : file) {
                if (!name.endsWith(".yml")) continue;

                files.add(name.replaceAll(".yml", ""));
            }
        }

        return Collections.unmodifiableList(files);
    }

    /**
     * Was found here: <a href="https://bukkit.org/threads/extracting-file-from-jar.16962">...</a>
     */
    private void copyFile(InputStream in, File out) throws Exception {
        try (InputStream fis = in; FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buf = new byte[1024];
            int i;

            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        }
    }

    public enum Files {
        // ENUM_NAME("fileName.yml", "fileLocation.yml"),
        // ENUM_NAME("fileName.yml", "newFileLocation.yml", "oldFileLocation.yml"),
        voucher_codes("voucher-codes.yml", "voucher-codes.yml"),
        vouchers("vouchers.yml", "vouchers.yml"),
        users("users.yml", "users.yml");

        private final String fileName;
        private final String fileJar;
        private final String fileLocation;

        @NotNull
        private final CrazyVouchers plugin = CrazyVouchers.get();

        @NotNull
        private final FileManager fileManager = this.plugin.getFileManager();

        /**
         * The files that the server will try and load.
         *
         * @param fileName the file name that will be in the plugin's folder.
         * @param fileLocation the location the file in the plugin's folder.
         */
        Files(String fileName, String fileLocation) {
            this(fileName, fileLocation, fileLocation);
        }

        /**
         * The files that the server will try and load.
         *
         * @param fileName the file name that will be in the plugin's folder.
         * @param fileLocation the location of the file will be in the plugin's folder.
         * @param fileJar the location of the file in the jar.
         */
        Files(String fileName, String fileLocation, String fileJar) {
            this.fileName = fileName;
            this.fileLocation = fileLocation;
            this.fileJar = fileJar;
        }

        /**
         * Get the name of the file.
         *
         * @return the name of the file.
         */
        public String getFileName() {
            return this.fileName;
        }

        /**
         * The location the jar it is at.
         *
         * @return the location in the jar the file is in.
         */
        public String getFileLocation() {
            return this.fileLocation;
        }

        /**
         * Get the location of the file in the jar.
         *
         * @return the location of the file in the jar.
         */
        public String getFileJar() {
            return this.fileJar;
        }

        /**
         * Gets the file from the system.
         *
         * @return the file from the system.
         */
        public FileConfiguration getFile() {
            return this.fileManager.getFile(this);
        }

        /**
         * Saves the file from the loaded state to the file system.
         */
        public void saveFile() {
            this.fileManager.saveFile(this);
        }

        /**
         * Overrides the loaded state file and loads the file systems file.
         */
        public void reloadFile() {
            if (getFileName().endsWith(".yml")) this.fileManager.reloadFile(this);
        }
    }

    public static class CustomFile {

        private final String name;
        private final String fileName;
        private final String homeFolder;
        private FileConfiguration file;

        @NotNull
        private final CrazyVouchers plugin = CrazyVouchers.get();

        /**
         * A custom file that is being made.
         *
         * @param name name of the file.
         * @param homeFolder the home folder of the file.
         */
        public CustomFile(String name, String homeFolder) {
            this.name = name.replace(".yml", "");
            this.fileName = name;
            this.homeFolder = homeFolder;

            File root = new File(this.plugin.getDataFolder(), "/" + homeFolder);

            if (!root.exists()) {
                root.mkdirs();

                if (this.plugin.isLogging()) this.plugin.getLogger().info("The folder " + homeFolder + "/ was not found so it was created.");

                this.file = null;

                return;
            }

            File newFile = new File(root, "/" + name);

            if (newFile.exists()) {
                this.file = YamlConfiguration.loadConfiguration(newFile);

                return;
            }

            this.file = null;
        }

        /**
         * A custom file that is being made.
         *
         * @param name name of the file.
         * @param homeFolder the home folder of the file.
         */
        public CustomFile(String name, String homeFolder, String subFolder) {
            this.name = name.replace(".yml", "");
            this.fileName = name;
            this.homeFolder = homeFolder + "/" + subFolder;

            File root = new File(this.plugin.getDataFolder(), "/" + this.homeFolder);

            File newFile = new File(root, "/" + name);

            if (newFile.exists()) {
                this.file = YamlConfiguration.loadConfiguration(newFile);

                return;
            }

            this.file = null;
        }

        /**
         * Get the name of the file without the .yml part.
         *
         * @return the name of the file without the .yml.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Get the full name of the file.
         *
         * @return full name of the file.
         */
        public String getFileName() {
            return this.fileName;
        }

        /**
         * Get the name of the home folder of the file.
         *
         * @return the name of the home folder the files are in.
         */
        public String getHomeFolder() {
            return this.homeFolder;
        }

        /**
         * Get the ConfigurationFile.
         *
         * @return the ConfigurationFile of this file.
         */
        public FileConfiguration getFile() {
            return this.file;
        }

        /**
         * Check if the file actually exists in the file system.
         * @return true if it does and false if it doesn't.
         */
        public boolean exists() {
            return this.file != null;
        }

        /**
         * Save the custom file.
         */
        private void saveFile() {
            if (this.file == null) {
                if (this.plugin.isLogging()) this.plugin.getLogger().warning("There was a null custom file that could not be found!");

                return;
            }

            try {
                this.file.save(new File(this.plugin.getDataFolder(), this.homeFolder + "/" + this.fileName));

                if (this.plugin.isLogging()) this.plugin.getLogger().info("Successfully saved the " + this.fileName + ".");
            } catch (IOException exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not save " + this.fileName + "!", exception);
            }
        }

        /**
         * Overrides the loaded state file and loads the filesystems file.
         */
        public void reloadFile() {
            if (this.file == null) {
                if (this.plugin.isLogging()) this.plugin.getLogger().warning("There was a null custom file that could not be found!");

                return;
            }

            try {
                this.file = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "/" + this.homeFolder + "/" + this.fileName));

                if (this.plugin.isLogging()) this.plugin.getLogger().info("Successfully reloaded the " + this.fileName + ".");
            } catch (Exception exception) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not reload the " + this.fileName + "!", exception);
            }
        }
    }
}