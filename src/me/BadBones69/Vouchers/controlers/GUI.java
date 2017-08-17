package me.badbones69.vouchers.controlers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.badbones69.vouchers.Methods;
import me.badbones69.vouchers.api.Voucher;
import me.badbones69.vouchers.api.Vouchers;

public class GUI implements Listener{
	
	public static void openGUI(Player player){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		int slots = 9;
		for(Voucher voucher : Vouchers.getVouchers()){
			items.add(voucher.buildItem());
		}
		for(int size = items.size(); size > 9 && slots < 54; size -= 9, slots += 9){}
		Inventory inv = Bukkit.createInventory(null, slots, Methods.color("&8&l&nVouchers"));
		for(ItemStack i : items)inv.addItem(i);
		player.openInventory(inv);
	}
	
	@EventHandler
	public void invClick(InventoryClickEvent e){
		Inventory inv = e.getClickedInventory();
		Player player = (Player) e.getWhoClicked();
		if(inv!=null){
			if(inv.getName().equals(Methods.color("&8&l&nVouchers"))){
				if(e.getCurrentItem().getType() != Material.AIR){
					e.setCancelled(true);
					player.getInventory().addItem(e.getCurrentItem());
				}
			}
		}
	}
	
}