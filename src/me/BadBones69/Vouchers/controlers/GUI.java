package me.BadBones69.Vouchers.controlers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.BadBones69.Vouchers.Methods;
import me.BadBones69.Vouchers.Main;
import me.BadBones69.Vouchers.API.Vouchers;

public class GUI implements Listener{
	
	public static void openGUI(Player player){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		int size = 0;
		for(String voucher : Main.settings.getConfig().getConfigurationSection("Vouchers").getKeys(false)){
			items.add(Vouchers.getVoucher(voucher, "%Arg%"));
		}
		if(items.size()<=9)size=9;
		if(items.size()>9&&items.size()<=18)size=18;
		if(items.size()>18&&items.size()<=27)size=27;
		if(items.size()>27&&items.size()<=36)size=36;
		if(items.size()>36&&items.size()<=45)size=45;
		if(items.size()>45&&items.size()<=54)size=54;
		Inventory inv = Bukkit.createInventory(null, size, Methods.color("&8&l&nVouchers"));
		for(ItemStack i : items)inv.addItem(i);
		player.openInventory(inv);
	}
	
	@EventHandler
	public void invClick(InventoryClickEvent e){
		Inventory inv = e.getClickedInventory();
		Player player = (Player) e.getWhoClicked();
		if(inv!=null){
			if(inv.getName().equals(Methods.color("&8&l&nVouchers"))){
				if(e.getCurrentItem().getType()!=Material.AIR){
					e.setCancelled(true);
					player.getInventory().addItem(e.getCurrentItem());
				}
			}
		}
	}
	
}