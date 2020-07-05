package me.Baz.MinecraftHardcore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.EulerAngle;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	public static Map<UUID, String> players = new HashMap<>();
	public static Map<String, UUID> playerUUID = new HashMap<>();
	public static Map<UUID, Location> deadPlayers = new HashMap<>();
	public static Map<UUID, Location> playersToBeRevived = new HashMap<>();	
	
	public static ItemStack reviver;
	public static ItemStack compass;
	
	//TODO: registerRecipe
	//TODO: Armour stand w/ arm crafting recipe
	
	@Override
	public void onEnable() {
		toConsole(ChatColor.DARK_GREEN + "Minecraft Hardcore plugin is now starting up!");
		
		for(ShapedRecipe recipe : createReviverRecipe()) {
			this.getServer().addRecipe(recipe);
		}
		this.getServer().addRecipe(createCompassRecipe());
		
		this.getServer().getPluginManager().registerEvents(this, this);
		RecipesCommand recipes = new RecipesCommand();
		this.getServer().getPluginManager().registerEvents(recipes, this);
		
		this.getServer().getPluginCommand("dead").setExecutor(new DeathCommand());
		this.getServer().getPluginCommand("death").setExecutor(new DeathCommand());
		this.getServer().getPluginCommand("revive").setExecutor(new ReviveCommand(this));
		this.getServer().getPluginCommand("recipes").setExecutor(recipes);
		this.getServer().getPluginCommand("recipe").setExecutor(recipes);
		Scoreboard scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		if(scoreboard.getObjective("Deaths") == null) {
			scoreboard.registerNewObjective("Deaths", "deathCount", "Deaths");
		}
		if(scoreboard.getObjective("FreeRevive") == null) {
			scoreboard.registerNewObjective("FreeRevive", "dummy", "FreeRevive");
		}
		this.getServer().getPluginCommand("discord").setExecutor(new DiscordCommand());
		
		
		this.getServer().getWorld("world").setGameRule(GameRule.KEEP_INVENTORY, true);
		try {
			this.getConfig().save(this.getDataFolder().getPath() + "/config.yml");
		} catch(IOException e) {
			throw new RuntimeException("Error writing to config!");
		}
		loadFromConfig();
	}
	@Override
	public void onDisable() {
		toConsole(ChatColor.DARK_RED + "Minecraft Hardcore plugin is now shutting down!");
		saveToConfig();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		String gold = ChatColor.GOLD.toString(), bold = ChatColor.BOLD.toString(), reset = ChatColor.RESET.toString(),
				red = ChatColor.RED.toString();
		Player player = event.getEntity();
		Scoreboard scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		Objective freeRevive = scoreboard.getObjective("FreeRevive");
		int score = freeRevive.getScore(player.getName()).getScore();
		if(score < 1) { player.spigot().sendMessage(getFreeRevive(player)); }
		String playerName = player.getName();
		event.setKeepInventory(true);
		List<ItemStack> drops = Arrays.asList(player.getInventory().getContents());
		player.getInventory().clear();
		player.setExp(0);
		player.setLevel(0);
		Location loc = player.getLocation();
		event.setDeathMessage("");
		Bukkit.broadcastMessage(gold + "" + bold + playerName + reset + red
				+ " has died at " + gold + "" + bold + (int) loc.getX() + ", " + (int) loc.getY()
				+ ", " + (int) loc.getZ() + red + " in the " + Util.getWorldName(loc.getWorld()));
		for(Player players : Bukkit.getOnlinePlayers()) {
			players.playSound(players.getLocation(), Sound.ENTITY_WITHER_SPAWN, 10, 0);
		}
		Location newLoc = setGravestone(loc, playerName);
		Location itemLoc = new Location(newLoc.getWorld(), newLoc.getX(), newLoc.getY() + 1, newLoc.getZ());
		for(ItemStack item : drops) {
			if(item != null) { newLoc.getWorld().dropItemNaturally(itemLoc, item); } 
		}
		deadPlayers.put(player.getUniqueId(),
				new Location(newLoc.getWorld(), round(newLoc.getX()), round(newLoc.getY()), round(newLoc.getZ())));
		player.setGameMode(GameMode.SPECTATOR);
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwningPlayer(player);
		item.setItemMeta(meta);
		newLoc.getWorld().dropItemNaturally(newLoc, item);
		ArmorStand armorstand = (ArmorStand) player.getWorld().spawnEntity(newLoc, EntityType.ARMOR_STAND);
		armorstand.getEquipment().setHelmet(item);
		armorstand.getEquipment().setChestplate(player.getEquipment().getChestplate());
		armorstand.getEquipment().setLeggings(player.getEquipment().getLeggings());
		armorstand.getEquipment().setBoots(player.getEquipment().getBoots());
		armorstand.setBodyPose(new EulerAngle(Util.degreesToRadians(90f), 0f, 0f));
		armorstand.setHeadPose(new EulerAngle(Util.degreesToRadians(100f), 0f, Util.degreesToRadians(180f)));
		armorstand.setLeftArmPose(new EulerAngle(Util.degreesToRadians(100f), 0f, 0f));
		armorstand.setRightArmPose(new EulerAngle(Util.degreesToRadians(100f), 0f, 0f));
		armorstand.setRotation(180, 0f);
		armorstand.setCustomName(player.getName());
		armorstand.setCustomNameVisible(true);
		armorstand.setGravity(false);
		armorstand.setInvulnerable(true);
		armorstand.setBasePlate(false);
		armorstand.setArms(true);
	}

	@EventHandler
	public void onAmourStandInteract(PlayerArmorStandManipulateEvent event) {
		ArmorStand armor = event.getRightClicked();
		String deadPlayerName = armor.getCustomName();
		if(deadPlayerName == null) return;
		event.setCancelled(true);
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if(item.getType() == Material.ENCHANTED_GOLDEN_APPLE && item.hasItemMeta()) {
			UUID deadPlayerUUID = playerUUID.get(deadPlayerName);
			if(deadPlayers.get(deadPlayerUUID) == null) return;
			if(!revivePlayer(players.get(deadPlayerUUID))) return;
			item.setAmount(item.getAmount() - 1);
			player.getInventory().setItemInMainHand(item);
			armor.remove();
			player.playSound(player.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 10, 0);
			Player deadPlayer = Bukkit.getPlayer(deadPlayerUUID);
			if(deadPlayer != null)
				deadPlayer.playSound(deadPlayer.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 10, 0);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer(); 
		UUID uuid = player.getUniqueId();
		String playerName = player.getName();
		if(players.get(uuid) == null || !players.get(uuid).equals(player.getName())) {
			players.put(uuid, playerName);
			playerUUID.put(playerName, uuid);
			this.getServer().getScoreboardManager().getMainScoreboard().getObjective("FreeRevive").getScore(playerName).setScore(0);
		}
		if(playersToBeRevived.get(uuid) != null) {
			revivePlayer(player, playersToBeRevived.get(uuid));
			playersToBeRevived.remove(uuid);
		}
	}

	public boolean revivePlayer(String playerName) {
		Location locOne = deadPlayers.get(playerUUID.get(playerName)),
				locTwo = playersToBeRevived.get(playerUUID.get(playerName));
		if(locOne == null && locTwo == null)
			return false;
		if(locOne == null) {
			revivePlayer(playerName, locTwo);
			return true;
		}
		revivePlayer(playerName, locOne);
		return true;
	}

	public boolean revivePlayer(String playerName, Location loc) {
		if(playerUUID.get(playerName) == null)
			return false;
		this.getServer().broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + playerName + " has been revived!");
		Player player = Bukkit.getPlayer(playerName);
		if(player == null) {
			playersToBeRevived.put(playerUUID.get(playerName), loc);
			deadPlayers.remove(playerUUID.get(playerName));
			return true;
		}
		revivePlayer(player, loc);
		player.setAllowFlight(false);
		return true;
	}

	public void revivePlayer(Player player, Location loc) {
		UUID uuid = player.getUniqueId();
		if(loc == null) return;
		player.teleport(new Location(loc.getWorld(), loc.getX(), loc.getY() + 2, loc.getZ()));
		player.setGameMode(GameMode.SURVIVAL);
		loc.setY(loc.getY() + 1);
		loc.getBlock().setType(Material.STONE);
		loc.setZ(loc.getZ() + 1);
		loc.getBlock().setType(Material.STONE);
		loc.setZ(loc.getZ() - 1);
		loc.setY(loc.getY() + 5);
		loc.getBlock().setType(Material.AIR);
		loc.setY(loc.getY() + 1);
		if(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - 1).getBlock().getType() != Material.AIR) {
			loc.getBlock().setType(Material.OAK_WALL_SIGN);
			Sign sign = (Sign) loc.getBlock().getState();
			Directional wallSignData = (Directional) sign.getBlockData();
			wallSignData.setFacing(BlockFace.SOUTH);
			sign.setBlockData(wallSignData);
			sign.setLine(0, "Here laid");
			sign.setLine(1, player.getName());
			sign.update();
		}
		deadPlayers.remove(uuid);
	}
	
	private TextComponent getFreeRevive(Player player) { return getFreeRevive(player.getName()); }
	private TextComponent getFreeRevive(String playerName) {
		TextComponent text = new TextComponent(ChatColor.GREEN + "" + ChatColor.BOLD + "You have a free revive!");
		text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("§a§lClick here to revive yourself! §7(This will use up your free revive)").bold(true)
				.italic(true).create()));
		text.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/revive"));
		return text;
	}
	
	private ShapedRecipe[] createReviverRecipe() {
		ItemStack reviver = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1);
		ItemMeta meta = reviver.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GOLD + "Player Reviver");
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(ChatColor.DARK_PURPLE + "Revives players by right clicking on their corpse");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		reviver.setItemMeta(meta);
		Main.reviver = reviver;
		ShapedRecipe revive = new ShapedRecipe(new NamespacedKey(this, "player_reviver"), reviver);
		ShapedRecipe altRevive = new ShapedRecipe(new NamespacedKey(this, "player_reviver_alt"), reviver);
		revive.shape("*%*", "CBC", "*%*");
		revive.setIngredient('*', Material.AIR);
		revive.setIngredient('%', Material.GOLD_BLOCK);
		revive.setIngredient('C', Material.DIAMOND_BLOCK);
		revive.setIngredient('B', Material.GOLDEN_APPLE);
		altRevive.shape("***", "*%*", "***");
		altRevive.setIngredient('*', Material.GOLD_INGOT);
		altRevive.setIngredient('%', Material.ENCHANTED_GOLDEN_APPLE);
		ShapedRecipe[] recipes = { revive, altRevive };
		return recipes;
	}

	private ShapedRecipe createCompassRecipe() {
		ItemStack compassItem = new ItemStack(Material.COMPASS, 1);
		ItemMeta meta = compassItem.getItemMeta();
		meta.addEnchant(Enchantment.DURABILITY, 1, false);
		meta.setDisplayName(ChatColor.GOLD + "Death Compass");
		List<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(ChatColor.DARK_PURPLE + "Points to dead players' graves");
		meta.setLore(lore);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		compassItem.setItemMeta(meta);
		Main.compass = compassItem;
		ShapedRecipe compass = new ShapedRecipe(new NamespacedKey(this, "death_compass"), compassItem);
		compass.shape("***", "*C*", "***");
		compass.setIngredient('*', Material.GOLD_NUGGET);
		compass.setIngredient('C', Material.COMPASS);
		return compass;
	}
	
	private Location setGravestone(Location loc, String playerName) {
		World world = loc.getWorld();
		while((world.getBlockAt(loc).isEmpty() || world.getBlockAt(loc).getType() == Material.LAVA
				|| world.getBlockAt(loc).getType() == Material.WATER) &&  loc.getY() > 0) {
			loc.setY(loc.getY() - 1);
		}
		loc.setY(loc.getY() + 1);
		loc.setZ(loc.getZ() - 1);
		loc.getBlock().setType(Material.STONE);
		loc.setY(loc.getY() + 1);
		loc.getBlock().setType(Material.STONE);
		loc.setZ(loc.getZ() + 1);
		loc.getBlock().setType(Material.OAK_WALL_SIGN);
		Sign sign = (Sign) loc.getBlock().getState();
		Directional wallSignData = (Directional) sign.getBlockData();
		wallSignData.setFacing(BlockFace.SOUTH);
		sign.setBlockData(wallSignData);
		sign.setLine(0, "Here lies");
		sign.setLine(1, playerName);
		sign.update();
		loc.setX(loc.getX() - 1);
		loc.setY(loc.getY() - 6);
		loc.setZ(loc.getZ() - 1);
		for(int y = 0; y < 5; y++) {
			for(int x = 0; x < 3; x++) {
				for(int z = 0; z < 4; z++) {
					Location tempLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
					Material m;
					if(x == 1 && (z == 1 || z == 2)) {
						if(y == 0)
							m = Material.BEDROCK;
						else if(y == 4)
							m = Material.COARSE_DIRT;
						else
							m = Material.AIR;
					} else {
						if(y <= 1)
							m = Material.STONE;
						else if(y == 4)
							m = Material.GRASS_BLOCK;
						else
							m = Material.DIRT;
					}
					tempLoc.setX(tempLoc.getX() + x);
					tempLoc.setY(tempLoc.getY() + y);
					tempLoc.setZ(tempLoc.getZ() + z);
					tempLoc.getBlock().setType(m);
				}
			}
		}
		loc = sign.getLocation();
		loc.setY(loc.getY() - 1);
		loc.getBlock().setType(Material.POPPY);
		loc.setX(loc.getX() + 0.5);
		loc.setY(loc.getY() - 5.25);
		loc.setZ(loc.getZ() + 0.5);
		return loc;
	}

	private void loadFromConfig() {
		if(this.getConfig().getConfigurationSection("players") != null) {
			this.getConfig().getConfigurationSection("players").getKeys(false).forEach(uuid -> {
				UUID tempUUID = UUID.fromString(uuid);
				String playerName = this.getConfig().getString("players." + uuid);
				players.put(tempUUID, playerName);
				playerUUID.put(playerName, tempUUID);
			});
		}
		if(this.getConfig().getConfigurationSection("deadPlayers") != null) {
			this.getConfig().getConfigurationSection("deadPlayers").getKeys(false).forEach(uuid -> {
				UUID tempUUID = UUID.fromString(uuid);
				String worldName = this.getConfig().getString("deadPlayers." + uuid + ".worldName");
				double x = this.getConfig().getDouble("deadPlayers." + uuid + ".x");
				double y = this.getConfig().getDouble("deadPlayers." + uuid + ".y");
				double z = this.getConfig().getDouble("deadPlayers." + uuid + ".z");
				World world = this.getServer().getWorld(worldName);
				Location tempLoc = new Location(world, x, y, z);
				deadPlayers.put(tempUUID, tempLoc);
			});
		}
		if(this.getConfig().getConfigurationSection("playersToBeRevived") != null) {
			this.getConfig().getConfigurationSection("playersToBeRevived").getKeys(false).forEach(uuid -> {
				UUID tempUUID = UUID.fromString(uuid);
				String worldName = this.getConfig().getString("playersToBeRevived." + uuid + ".worldName");
				double x = this.getConfig().getDouble("playersToBeRevived." + uuid + ".x");
				double y = this.getConfig().getDouble("playersToBeRevived." + uuid + ".y");
				double z = this.getConfig().getDouble("playersToBeRevived." + uuid + ".z");
				World world = this.getServer().getWorld(worldName);
				Location tempLoc = new Location(world, x, y, z);
				playersToBeRevived.put(tempUUID, tempLoc);
			});
		}
		String link = this.getConfig().getString("discord");
		if(link == null) { toConsole("There is no link setup, you can set one up by using \"/discord set <link>\""); }
		else DiscordCommand.link = link;
	}

	private void saveToConfig() {
		for(UUID uuid : players.keySet()) { this.getConfig().set("players." + uuid, players.get(uuid)); }
		if(this.getConfig().get("deadPlayers") == null) this.getConfig().set("deadPlayers", "");
		for(UUID uuid : deadPlayers.keySet()) {
			Location loc = deadPlayers.get(uuid);
			Map<String, Object> greaterMap = new HashMap<>();
			greaterMap.put("worldName", loc.getWorld());
			greaterMap.put("x", loc.getX());
			greaterMap.put("y", loc.getY());
			greaterMap.put("z", loc.getZ());
			this.getConfig().set("deadPlayers." + uuid + ".worldName", loc.getWorld().getName());
			this.getConfig().set("deadPlayers." + uuid + ".x", loc.getX());
			this.getConfig().set("deadPlayers." + uuid + ".y", loc.getY());
			this.getConfig().set("deadPlayers." + uuid + ".z", loc.getZ());
		}
		if(this.getConfig().get("playersToBeRevived") == null) this.getConfig().set("playersToBeRevived", "");
		for(UUID uuid : playersToBeRevived.keySet()) {
			Location loc = playersToBeRevived.get(uuid);
			Map<String, Object> greaterMap = new HashMap<>();
			greaterMap.put("worldName", loc.getWorld());
			greaterMap.put("x", loc.getX());
			greaterMap.put("y", loc.getY());
			greaterMap.put("z", loc.getZ());
			this.getConfig().set("playersToBeRevived." + uuid + ".worldName", loc.getWorld().getName());
			this.getConfig().set("playersToBeRevived." + uuid + ".x", loc.getX());
			this.getConfig().set("playersToBeRevived." + uuid + ".y", loc.getY());
			this.getConfig().set("playersToBeRevived." + uuid + ".z", loc.getZ());
		}
		this.getConfig().set("discord", DiscordCommand.link);
		this.saveConfig();
	}

	private double round(double dub) {
		String tempDouble = "" + dub;
		int index = tempDouble.indexOf(".");
		index += 2;
		tempDouble = tempDouble.substring(0, index);
		return Double.valueOf(tempDouble);
	}

	private void toConsole(String string) {
		this.getServer().getConsoleSender().sendMessage(string);
	}
}
