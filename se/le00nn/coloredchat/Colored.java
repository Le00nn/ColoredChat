// Author: Le00nn
// Date: 18/08/2023
// Version: 1.0.0

package se.le00nn.coloredchat;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Colored extends JavaPlugin implements Listener {

	@Override
	public void onDisable() {
		Bukkit.getLogger().warning("["+getName()+"] (Version: "+getVersion()+") disabled!"); // Plugin disabled!
	}

	@Override
	public void onEnable() {		
		Bukkit.getLogger().info("["+getName()+"] (Version: "+getVersion()+") enabled!"); // Plugin running!
		
		getConfiguration().load(); // Load configuration file (config.yml).
		Bukkit.getLogger().info("["+getName()+"] configuration loaded!");
		
		Bukkit.getLogger().info("["+getName()+"] Configuration version: "+getCfgVersion());
		getConfiguration().setProperty("cfgversion", getCfgVersion());
		getConfiguration().save(); // Set cfgversion in configuration file (config.yml).
		
		getServer().getPluginManager().registerEvents(this, this);
		if(getConfiguration().getProperty("settings.reload.enabled") == null) {
			Bukkit.getLogger().info("["+getName()+"] Creating reload settings configurations.");
			getConfiguration().setProperty("settings.reload.enabled", true);
			getConfiguration().setProperty("settings.reload.info", "Enable wether or not if the plugin should reload every 15 seconds. It is recommended to keep this enabled! Changing this configuration setting will require a server restart.");
			getConfiguration().save(); // Save settings to configuration file (config.yml).
		}
		
		if(getConfiguration().getKeys().size() <= 1) {
			Bukkit.getLogger().info("["+getName()+"] Creating examples configuration.");
			getConfiguration().setProperty("players.###example.namecolor", "&c");
			getConfiguration().setProperty("players.###example.chatcolor", "&6");
			getConfiguration().setProperty("players.###example.info", "This example can be removed.\nThis example will put the user ###example in bright red color\nand their messages in gold color.");
			getConfiguration().save(); // Save examples to configuration file (config.yml).
		} else {
			Bukkit.getLogger().info("["+getName()+"] Loaded " + (getConfiguration().getKeys().size()) + " configuration entries.");
		}
		

		if(getConfiguration().getBoolean("settings.reload.enabled", true) == true) {
			Runnable reloadFile = new Runnable() {
				public void run() {
					getConfiguration().load(); // Load configuration file (config.yml).
					Bukkit.getLogger().info("["+getName()+"] configuration reloaded!");
					List<String> players = getConfiguration().getKeys("players");
					updatePlayers(players);
				}
			};
			
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
			executor.scheduleAtFixedRate(reloadFile, 0, 15, TimeUnit.SECONDS);
		}
		
	}
	
	@EventHandler
	public void onPlayerChat(PlayerChatEvent e) {
		Player player  = e.getPlayer(); // Get player from event.
		String message = e.getMessage(); // Get message from event.
		Object txcolor = getConfiguration().getProperty("players."+player.getName()+".chatcolor"); // Check player.chatcolor property in configuration file (config.yml).
		Object plcolor = getConfiguration().getProperty("players."+player.getName()+".namecolor"); // Check player.namecolor property in configuration file (config.yml).
		if(plcolor == null || txcolor == null) return;
		String playerColor = getConfiguration().getString("players."+player.getName()+".namecolor"); // Convert player name color to string.
			   playerColor = playerColor.toLowerCase();
		String chatColor = getConfiguration().getString("players."+player.getName()+".chatcolor"); // Convert player name color to string.
			   chatColor = chatColor.toLowerCase();
		if (playerColor.matches("&[0-9a-f]")) {
			e.setCancelled(true);
			String messageWithColor = ChatColor.WHITE + "<" + this.convertColorCode(player.getName(), playerColor) + player.getName() + ChatColor.WHITE + "> " + this.convertColorCode(player.getName(), chatColor) + message;
			Bukkit.broadcastMessage(messageWithColor);
		} else {
			e.setCancelled(false);
			Bukkit.getLogger().warning("["+getName()+"] Invalid color code for player "+player.getName()+"."); // Warning for default fallback
			Bukkit.getLogger().warning("["+getName()+"] Falling back to default color (white - &f).");
		}
	}
	
	// Get plugin name
	private String getName() {
		return this.getDescription().getName();
	}
	
	// Get plugion version
	private String getVersion() {
		return this.getDescription().getVersion();
	}
	
	// Config version
	private int getCfgVersion() {
		return 1; // First version of Config version.
	}
	
	// Utility to convert color codes into actual colors.
	private ChatColor convertColorCode(String playerName, String input) {
		if(input.matches("&0")) return ChatColor.BLACK;
		if(input.matches("&1")) return ChatColor.DARK_BLUE;
		if(input.matches("&2")) return ChatColor.DARK_GREEN;
		if(input.matches("&3")) return ChatColor.DARK_AQUA;
		if(input.matches("&4")) return ChatColor.DARK_RED;
		if(input.matches("&5")) return ChatColor.DARK_PURPLE;
		if(input.matches("&6")) return ChatColor.GOLD;
		if(input.matches("&7")) return ChatColor.GRAY;
		if(input.matches("&8")) return ChatColor.DARK_GRAY;
		if(input.matches("&9")) return ChatColor.BLUE;
		if(input.matches("&a")) return ChatColor.GREEN;
		if(input.matches("&b")) return ChatColor.AQUA;
		if(input.matches("&c")) return ChatColor.RED;
		if(input.matches("&d")) return ChatColor.LIGHT_PURPLE;
		if(input.matches("&e")) return ChatColor.YELLOW;
		if(input.matches("&f")) return ChatColor.WHITE;
		
		Bukkit.getLogger().warning("["+getName()+"] Invalid color code for player "+playerName+"."); // Warning for default fallback.
		Bukkit.getLogger().warning("["+getName()+"] Falling back to default color (white - &f).");
		return ChatColor.WHITE; // Default fall back on error.
	}
	
	
	
	// Update player configurations via timertask (if enabled).
	private void updatePlayers(List<String> players) {
		for(String player : players) {
			Object plcolor = getConfiguration().getProperty("players."+player+".color"); // Check player.color property in configuration file (config.yml).
			if(plcolor == null) return;
			String playerColor = getConfiguration().getString("players."+player+".color"); // Convert player name color to string.
				   playerColor = playerColor.toLowerCase();
			if (playerColor.matches("&[0-9a-f]")) {
				int count = 0;
				String nameWithColor = this.convertColorCode(player, playerColor) + player + ChatColor.WHITE;
				try {
					++count;
					Bukkit.getPlayer(player).setDisplayName(nameWithColor);
				} catch(Exception e) {
					// Do nothing (player might not be online or is removed from server?)
				}
				Bukkit.getLogger().warning("["+getName()+"] Updated "+count+" plauyers.");
			} else {
				Bukkit.getLogger().warning("["+getName()+"] Invalid color code for player "+player+"."); // Warning for default fallback
				Bukkit.getLogger().warning("["+getName()+"] Falling back to default color (white - &f).");
			}
		}
	}

}
