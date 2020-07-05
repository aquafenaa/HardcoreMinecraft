package me.Baz.MinecraftHardcore;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class DeathCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("dead")) {
			sender.sendMessage(ChatColor.DARK_GREEN + "The list of currently dead players:");
			for(UUID uuid : Main.deadPlayers.keySet()) {
				Location loc = Main.deadPlayers.get(uuid);
				String worldName = Util.getWorldName(loc.getWorld());
				TextComponent message = new TextComponent(ChatColor.AQUA + Main.players.get(uuid) + ChatColor.RESET + ": " + ChatColor.GREEN + worldName + " (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/death " + Main.players.get(uuid)));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click here with your Death Compass in hand to help locate " + 
						Main.players.get(uuid)).color(ChatColor.GRAY).italic(true).create()));
				sender.spigot().sendMessage(message);
			}
		}
		if(label.equalsIgnoreCase("death")) {
			if(!(sender instanceof Player)) { sender.sendMessage("Bruh"); return true; }
			Player player = (Player) sender;
			ItemStack item = player.getInventory().getItemInMainHand();
			if(item.getType() != Material.COMPASS || !item.getItemMeta().hasLore()) { player.sendMessage(ChatColor.RED + "You must be holding your compass to use this command!"); return true; }
			if(args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /death <Player Username>"); return true; }
			UUID uuid = Main.playerUUID.get(args[0]);
			if(uuid == null) { sender.sendMessage(ChatColor.RED + "There is not a player with this name! Make sure you spelled it right!"); return true; }
			Location loc = Main.deadPlayers.get(uuid);
			if(loc == null) { sender.sendMessage(ChatColor.RED + "This player is not dead!"); return true; }
			player.setCompassTarget(loc);
			String worldName = Util.getWorldName(loc.getWorld());
			player.sendMessage(ChatColor.GREEN + "Your compass is now pointing towards " + ChatColor.AQUA + Main.players.get(uuid) + ChatColor.RESET + ": " + ChatColor.GREEN + worldName + " (" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");

		}
		return false;
	}

}
