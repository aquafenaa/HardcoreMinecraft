package me.Baz.MinecraftHardcore;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DiscordCommand implements CommandExecutor {

	public static String link;
		
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String green = ChatColor.GREEN.toString(), red = ChatColor.RED.toString(), darkRed = ChatColor.DARK_RED.toString(),
				gold = ChatColor.GOLD.toString();
		if(label.equalsIgnoreCase("discord")) {
			if(args.length < 1) { 
				if(link == null) { sender.sendMessage(red + "There is not a Discord link yet! Let an admin know of this!"); }
				else { sender.sendMessage(green + "Here is the link to the Discord!\n" + gold + DiscordCommand.link); }
				return true;
			}
			if(args[0].equalsIgnoreCase("help")) {
				if(args.length < 2) { sender.sendMessage(forHelp("default")); return true; }
				sender.sendMessage(forHelp(args[1])); return true;
			}
			if(args[0].equalsIgnoreCase("set")) {
				if(sender instanceof Player && !((Player)sender).hasPermission("set.use")) { sender.sendMessage(darkRed + "You do not have permission to use this command!"); return true; }
				if(args.length < 2) { sender.sendMessage(getUsage("set")); return true; }
				
				//discord.gg/invite/wZb9zYN/ -> discord.gg/invite/wZb9zYN
				if(args[1].charAt(args[1].length() - 1) == '/') { args[1] = args[1].substring(0, args[1].length() - 2); }
				//discord.gg/invite/wZb9zYN -> wZb9zYN
				if(args[1].contains("/")) { args[1] = args[1].substring(args[1].lastIndexOf("/") + 1); }
				//example: discord.gg/invite/wZb9zYN/ -> discord.gg/invite/wZb9zYN -> wZb9zYN
				
				DiscordCommand.link = "discord.gg/" + args[1];
				sender.sendMessage("You successfully set the Discord link to " + DiscordCommand.link);
				return true;
			}
			sender.sendMessage(red + "Unknown subcommand! Use \"/help\" for help!");
			return true;
		}
		return false;
	}

	private String forHelp(String str) {
		String green = ChatColor.GREEN.toString(), reset = ChatColor.RESET.toString(), red = ChatColor.RED.toString();
		
		switch(str.toLowerCase()) {
		case "default":
			return forHelp("discord") + "\n" + forHelp("help") + "\n" + forHelp("set");
		case "discord":
			return green + "Discord" + reset + ": a command to show the Discord linked to this server\n" + getUsage("discord");
		case "help":
			return green + "Help" + reset + ": shows information on subcommands\n" + getUsage("help");
		case "set":
			return green + "Set" + reset + ": sets the Discord link for this command\n" + getUsage("set");
		default:
			return red + "Unknown subcommand! Please use \"/help\" for help!";
		}
	}
	
	private String getUsage(String str) {
		String red = ChatColor.RED.toString();
		switch(str) {
		case "default":
			return getUsage("discord") + "\n" + getUsage("help") + "\n" + getUsage("set");
		case "discord": 
			return red + "Usage: /discord";
		case "help":
			return red + "Usage: /discord help <subcommand>";
		case "set":
			return red + "Usage: /discord set <link>";
		default:
			return "";
		}
	}
	
}
