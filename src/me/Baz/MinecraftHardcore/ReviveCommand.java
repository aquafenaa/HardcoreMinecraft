package me.Baz.MinecraftHardcore;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ReviveCommand implements CommandExecutor {

	private Main instance;
	public ReviveCommand(Main instance) {
		this.instance = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(label.equalsIgnoreCase("revive")) {
			if(sender instanceof Player) {
				Player player = (Player) sender;
				UUID uuid = player.getUniqueId();
				if(args.length == 0) {
					boolean isDead = Main.deadPlayers.get(uuid) != null;
					if(player.hasPermission("revive.use")) {
						if(!isDead) { player.sendMessage(isDead + Util.red + "You cannot use this command on yourself when you are alive!"); return true; }
						instance.revivePlayer(player.getName());
						return true;
					}
					if(!isDead) { player.sendMessage(Util.red + "You cannot use this command on yourself when you are alive!"); return true; }
					Scoreboard scoreboard = instance.getServer().getScoreboardManager().getMainScoreboard();
					Score score = scoreboard.getObjective("FreeRevive").getScore(player.getName());
					if(score.getScore() == 0) { instance.revivePlayer(player.getName()); score.setScore(score.getScore() + 1); return true; }
					player.sendMessage(Util.red + "You have already used your free revive! Wait for someone to revive you!");
					return true;
				}
				if(!player.hasPermission("revive.use")) { sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command!"); return true; }
			}
			if(args.length < 1) { sender.sendMessage(ChatColor.RED + "Usage: /revive <Player Username>"); return true; }
			UUID uuid = Main.playerUUID.get(args[0]);
			if(uuid == null) { sender.sendMessage(ChatColor.RED + "There is not a player with that name! Make sure you spelled it correctly!"); return true; }
			if(Main.deadPlayers.get(uuid) == null) { sender.sendMessage(ChatColor.RED + "That player is not dead!"); return true; }
			instance.revivePlayer(args[0]);
			sender.sendMessage(ChatColor.GREEN + "Successfully revived that player!");
			return true;
		}
		return false;
	}

}
