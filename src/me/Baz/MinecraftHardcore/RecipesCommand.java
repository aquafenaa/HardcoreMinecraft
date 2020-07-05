package me.Baz.MinecraftHardcore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RecipesCommand implements CommandExecutor, Listener {

	private static ItemStack exit = createExit();
	private static Inventory mainInv = createMainInv();
	//private static Map<String, Map<Inventory, Map<ItemStack, Integer>>> map = new HashMap<>();
	private static Inventory[] reviverInv = createReviverInv();
	private static Inventory compassInv = createCompassInv();
	private static String[] recipes = { "reviver", "compass" };
	
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) { sender.sendMessage("BrUg"); return true; }
		Player player = (Player) sender;
		if(label.equalsIgnoreCase("recipes")) {
			player.openInventory(mainInv);
			player.sendMessage(ChatColor.GREEN + "Here are the custom recipes on this server!");
		}
		else if(label.equalsIgnoreCase("recipe")) {
			if(args.length < 1) { player.sendMessage(ChatColor.RED + "Usage: /recipe <Recipe Name>"); return true; }
			switch(args[0].toLowerCase()) {
			case "list":
				player.sendMessage(ChatColor.GREEN + "Here is the list of the custom recipes!");
				player.sendMessage(getRecipes());
			case "reviver":
			case "playerreviver":
			case "player": 
				player.openInventory(reviverInv[0]);
				player.sendMessage(ChatColor.GREEN + "Here is the recipe for the Player Reviver!");
				break;
			case "reviver2":
			case "revivertwo":
			case "playerreviver2":
			case "playerrevivertwo":
			case "player2":
			case "playertwo":
				player.openInventory(reviverInv[1]);
				player.sendMessage(ChatColor.GREEN + "Here is the recipe for the Player Reviver!");
				break;
			case "compass":
			case "deathcompass":
			case "deadcompass":
			case "dead":
				player.openInventory(compassInv);
				player.sendMessage(ChatColor.GREEN + "Here is the recipe for the Death Compass!");
				break;
			default:
				player.sendMessage(ChatColor.RED + "Unknown recipe! Do /recipe list to see them all!");
			}
			return true;
		}
		
		return false;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		ItemStack item = event.getCurrentItem();
		if(!(event.getWhoClicked() instanceof Player)) return;
		if(item == null) return;
		Player player = (Player) event.getWhoClicked();
		if(item.getType() == Material.BARRIER && item.hasItemMeta() && item.getItemMeta().hasLore()) { 
			event.setCancelled(true); 
			player.closeInventory(); 
			return; 
		}
		if(inv.equals(mainInv)) {
			Material[] mats = { Material.ENCHANTED_GOLDEN_APPLE, Material.COMPASS };
			if(isItem(item, mats) && item.getItemMeta().hasLore()) {
				event.setCancelled(true);
				player.performCommand("recipe " + getStringForRecipe(item));
			}
			return;
		}
		if(inv.equals(reviverInv[0])) {
			Material[] mats = { Material.ENCHANTED_GOLDEN_APPLE, Material.GOLD_BLOCK, 
					Material.DIAMOND_BLOCK, Material.GOLDEN_APPLE };
			if(isItem(item, mats))
				event.setCancelled(true);
			else if(item.getType() == Material.ARROW && item.getItemMeta().hasLore()) {
				event.setCancelled(true);
				player.openInventory(reviverInv[1]);
			}
			return;
		}
		if(inv.equals(reviverInv[1])) {
			Material[] mats = { Material.ENCHANTED_GOLDEN_APPLE, Material.GOLD_INGOT };
			if(isItem(item, mats))
				event.setCancelled(true);
			else if(item.getType() == Material.ARROW) {
				event.setCancelled(true);
				player.openInventory(reviverInv[0]);
			}
			return;
		}
		if(inv.equals(compassInv)) {
			Material[] mats = { Material.COMPASS, Material.GOLD_NUGGET };
			if(isItem(item, mats))
				event.setCancelled(true);
			return;
		}
	}
	
	private static Inventory createMainInv() {
		Inventory inv  = Bukkit.createInventory(null, 18, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Custom Recipes");
		inv.setItem(0, Main.reviver);
		inv.setItem(1, Main.compass);
		inv.setItem(17, exit);
		return inv;
	}
	
	private static Inventory[] createReviverInv() {
		Inventory[] invArr = new Inventory[2];
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Player Reviver Recipe");
		ItemStack goldBlock = new ItemStack(Material.GOLD_BLOCK, 1);
		ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE, 1);
		ItemStack diamondBlock = new ItemStack(Material.DIAMOND_BLOCK, 1);
		ItemStack arrow = new ItemStack(Material.ARROW, 1);
		ItemMeta meta = arrow.getItemMeta();
		meta.setDisplayName(ChatColor.GRAY + "Next Page");
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(ChatColor.DARK_PURPLE + "Click here to go to the second recipe for this item");
		meta.setLore(lore);
		arrow.setItemMeta(meta);
		inv.setItem(3, goldBlock);
		inv.setItem(11, diamondBlock);
		inv.setItem(12, goldenApple);
		inv.setItem(13, diamondBlock);
		inv.setItem(15, Main.reviver);
		inv.setItem(21, goldBlock);
		inv.setItem(25, arrow);
		inv.setItem(26, exit);
		invArr[0] = inv;
		inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Player Reviver Recipe");
		ItemStack goldIngot = new ItemStack(Material.GOLD_INGOT, 1);
		ItemStack notchApple = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
		for(int x = 2; x < 23; x++) {
			if(x == 5 || x == 14) x+= 6;
			if(x == 12) inv.setItem(x, notchApple);
			else inv.setItem(x, goldIngot);
		}
		inv.setItem(15, Main.reviver);
		meta.setDisplayName(ChatColor.GRAY + "Previous Page");
		lore.set(1, ChatColor.DARK_PURPLE + "Click here to go to the first recipe for this item");
		meta.setLore(lore);
		arrow.setItemMeta(meta);
		inv.setItem(18, arrow);
		inv.setItem(26, exit);
		invArr[1] = inv;
		return invArr;
	}
	
	private static Inventory createCompassInv() {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Death Compass Recipe");
		ItemStack goldNugget = new ItemStack(Material.GOLD_NUGGET, 1);
		ItemStack compass = new ItemStack(Material.COMPASS, 1);
		for(int x = 2; x < 23; x++) {
			if(x == 5 || x == 14) x+= 6;
			if(x == 12) inv.setItem(x, compass);
			else inv.setItem(x, goldNugget);
		}
		inv.setItem(15, Main.compass);
		inv.setItem(26, exit);
		return inv;
	}
	
	private static ItemStack createExit() {
		ItemStack barrier = new ItemStack(Material.BARRIER);
		ItemMeta meta = barrier.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.RED + "EXIT");
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(ChatColor.GRAY + "Click here to exit the menu");
		meta.setLore(lore);
		barrier.setItemMeta(meta);
		return barrier;
	}
	
	private String getStringForRecipe(ItemStack item) {
		Material m = item.getType();
		if(m == Material.ENCHANTED_GOLDEN_APPLE) return "reviver";
		return "compass";
	}
	
	private boolean isItem(ItemStack item, Material[] mats) {
		for(Material mat : mats) {
			if(item.getType() == mat)
				return true;
		}
		return false;
	}
	
	private String getRecipes() {
		String rtnString = "";
		for(String str : recipes) {
			rtnString += ChatColor.AQUA + str + ChatColor.RESET + ", ";
		}
		return rtnString;
	}
}
