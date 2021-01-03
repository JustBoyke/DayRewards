package me.boykev.dayreward;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener{
	
	private ConfigManager cm;
	private UserManager um;
	
	public void onEnable() {
		System.out.println(ChatColor.GREEN + "Ja die staat aan.");
		PluginManager pm = Bukkit.getPluginManager();
		cm = new ConfigManager(this);
		cm.LoadDefaults();
		
		pm.registerEvents(this, this);
		
	}
	
	public void onDisable() {
		System.out.println(ChatColor.RED + "Ja die staat uit.");
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		um = new UserManager(this, p);
		cm = new ConfigManager(this);
		File configFile = new File(this.getDataFolder() + File.separator + "players", p.getUniqueId().toString() + ".yml");
		if(!configFile.exists()) {
			um.LoadDefaults();
			um.editConfig().set("PlayerName", p.getName().toString());
			
			um.editConfig().set("lastLogin", System.currentTimeMillis());
			um.save();
			return;
		}
		Long Oday = um.getConfig().getLong("lastLogin");
		long Nday = System.currentTimeMillis();
		
		Integer day = 86400000;
		Integer calculate = (int) (Nday - Oday);
		
		if(calculate > day) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + p.getName() + " " + cm.getConfig().getInt("geldjes.amount"));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', cm.getConfig().getString("geldjes.message")));
			um.editConfig().set("lastLogin", System.currentTimeMillis());
			um.save();
			return;
		}
		
		
		
	}
	
}
