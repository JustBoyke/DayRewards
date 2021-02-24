package me.boykev.dayreward;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.ess3.api.events.AfkStatusChangeEvent;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener{
	
	private ConfigManager cm;
	private UserManager um;
	
	public static Economy econ = null;
	private boolean setupEconomy() {
	    RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
	    if (economyProvider != null) {
	        econ = economyProvider.getProvider();
	    }

	    return (econ != null);
	}
	
	public void onEnable() {
		System.out.println(ChatColor.GREEN + "Ja die staat aan.");
		PluginManager pm = Bukkit.getPluginManager();
		cm = new ConfigManager(this);
		cm.LoadDefaults();
		this.setupEconomy();
		pm.registerEvents(this, this);
		
	}
	
	public void onDisable() {
		System.out.println(ChatColor.RED + "Ja die staat uit.");
	}
	
	public HashMap<Player, Integer> mon = new HashMap<Player, Integer>();
	public HashMap<Player, BukkitTask> tasklist = new HashMap<Player, BukkitTask>();
	
	public void moneyGiver(Player p) {
		BukkitTask task = new BukkitRunnable() {
			@Override
			public void run() {
				econ.depositPlayer(p, 50);
				p.sendMessage(ChatColor.GREEN + "Je hebt 30 minuten playtime erbij, je hebt 50 euro gekregen!");
			}
		}.runTaskTimerAsynchronously(this, 36000, 36000);
		tasklist.put(p, task);
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
		}
		this.moneyGiver(p);
		p.sendMessage(ChatColor.GREEN + "Welkom terug op de server :) elke 30 minuten krijg je gratis geld voor je online time!");
		System.out.println("Player " + p.getName() + "has logged in");
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		int task = tasklist.get(e.getPlayer()).getTaskId();
		Bukkit.getScheduler().cancelTask(task);
		System.out.println("Player " + e.getPlayer().getName() + "has logged out");
	}
	
	@EventHandler
	public void afkChange(AfkStatusChangeEvent e) {
		@SuppressWarnings("deprecation")
		Player p = e.getAffected().getBase();
		if(e.getValue() == true) {
			int task = tasklist.get(p).getTaskId();
			Bukkit.getScheduler().cancelTask(task);
			p.sendMessage(ChatColor.RED + "Je bent AFK, je ontvangt geen geld meer.");
			return;
		}
		this.moneyGiver(p);
		p.sendMessage(ChatColor.GREEN + "Je bent niet meer AFK, elke 30 minuten ontvang je geld voor je online time.");
		return;
	}
	
}
