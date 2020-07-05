package me.Baz.MinecraftHardcore;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class Util {	
	
	public static String red = ChatColor.RED.toString(), darkRed = ChatColor.DARK_RED.toString(), bold = ChatColor.BOLD.toString(), 
			green = ChatColor.GREEN.toString();
	
    public static float radiansToDegrees(float radians) { return (float) ((radians * 180) / Math.PI); }
    public static float degreesToRadians(float degrees) { return (float) ((degrees * Math.PI)/180); }
	
	public static String getWorldName(World world) {
		switch(world.getName()) {
		case "world_nether":
			return "Nether";
		case "world_the_end":
			return "End";
		default:
			return "Overworld";
		}
	}
}
