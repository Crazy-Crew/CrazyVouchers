package me.badbones69.vouchers.api;

import org.bukkit.Bukkit;

public enum Version {
	
	TOO_OLD(-1),
	v1_7_R4(174),
	v1_8_R1(181), v1_8_R2(182), v1_8_R3(183),
	v1_9_R1(191), v1_9_R2(192),
	v1_10_R1(1101),
	v1_11_R1(1111),
	TOO_NEW(-2);
	
	private Integer versionInteger;
	
	private Version(Integer versionInteger){
		this.versionInteger = versionInteger;
	}
	
	/**
	 * 
	 * @return Get the server's Minecraft version.
	 */
	public static Version getVersion(){
		String ver = Bukkit.getServer().getClass().getPackage().getName();
		ver = ver.substring(ver.lastIndexOf('.')+1);
		ver = ver.replaceAll("_", "").replaceAll("R", "").replaceAll("v", "");
		int version = Integer.parseInt(ver);
		if(version == 1111) return Version.v1_11_R1;
		if(version == 1101) return Version.v1_10_R1;
		if(version == 192) return Version.v1_9_R2;
		if(version == 191) return Version.v1_9_R1;
		if(version == 183) return Version.v1_8_R3;
		if(version == 182) return Version.v1_8_R2;
		if(version == 181) return Version.v1_8_R1;
		if(version == 174) return Version.v1_7_R4;
		if(version > 1111) return Version.TOO_NEW;
		return Version.TOO_OLD;
	}
	
	/**
	 * 
	 * @return The server's minecraft version as an integer.
	 */
	public Integer getVersionInteger(){
		return this.versionInteger;
	}
	
}