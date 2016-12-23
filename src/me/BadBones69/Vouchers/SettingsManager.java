package me.BadBones69.Vouchers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class SettingsManager {

	static SettingsManager instance = new SettingsManager();

	public static SettingsManager getInstance() {
		return instance;
	}

	Plugin p;

	FileConfiguration config;
	File cfile;

	FileConfiguration data;
	File dfile;
	
	FileConfiguration msg;
	File mfile;
	
	FileConfiguration code;
	File cofile;
	
	public void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}

		cfile = new File(p.getDataFolder(), "Config.yml");
		if (!cfile.exists()) {
			try{
        		File en = new File(p.getDataFolder(), "/Config.yml");
         		InputStream E = getClass().getResourceAsStream("/Config.yml");
         		copyFile(E, en);
         	}catch (Exception e) {
         		e.printStackTrace();
         	}
		}
		config = YamlConfiguration.loadConfiguration(cfile);
		
		dfile = new File(p.getDataFolder(), "Data.yml");
		if (!dfile.exists()) {
			try{
        		File en = new File(p.getDataFolder(), "/Data.yml");
         		InputStream E = getClass().getResourceAsStream("/Data.yml");
         		copyFile(E, en);
         	}catch (Exception e) {
         		e.printStackTrace();
         	}
		}
		data = YamlConfiguration.loadConfiguration(dfile);
		
		mfile = new File(p.getDataFolder(), "Messages.yml");
		if (!mfile.exists()) {
			try{
        		File en = new File(p.getDataFolder(), "/Messages.yml");
         		InputStream E = getClass().getResourceAsStream("/Messages.yml");
         		copyFile(E, en);
         	}catch (Exception e) {
         		e.printStackTrace();
         	}
		}
		msg = YamlConfiguration.loadConfiguration(mfile);
		
		cofile = new File(p.getDataFolder(), "VoucherCodes.yml");
		if (!cofile.exists()) {
			try{
        		File en = new File(p.getDataFolder(), "/VoucherCodes.yml");
         		InputStream E = getClass().getResourceAsStream("/VoucherCodes.yml");
         		copyFile(E, en);
         	}catch (Exception e) {
         		e.printStackTrace();
         	}
		}
		code = YamlConfiguration.loadConfiguration(cofile);
	}

	public FileConfiguration getMsgs() {
		return msg;
	}
	public FileConfiguration getData() {
		return data;
	}
	public FileConfiguration getCode() {
		return code;
	}
	public void saveMsgs() {
		try {
			msg.save(mfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save Messages.yml!");
		}
	}
	public void saveData() {
		try {
			data.save(dfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save Data.yml!");
		}
	}
	public void saveCode() {
		try {
			code.save(cofile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save VoucherCodes.yml!");
		}
	}
	public void reloadMsgs() {
		msg = YamlConfiguration.loadConfiguration(mfile);
	}
	public void reloadData() {
		data = YamlConfiguration.loadConfiguration(dfile);
	}
	public void reloadCode() {
		code = YamlConfiguration.loadConfiguration(cofile);
	}
	public FileConfiguration getConfig() {
		return config;
	}

	public void saveConfig() {
		try {
			config.save(cfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save Config.yml!");
		}
	}

	public void reloadConfig() {
		config = YamlConfiguration.loadConfiguration(cfile);
	}

	public PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
	public static void copyFile(InputStream in, File out) throws Exception { // https://bukkit.org/threads/extracting-file-from-jar.16962/
        InputStream fis = in;
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
