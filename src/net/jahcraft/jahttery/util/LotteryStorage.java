package net.jahcraft.jahttery.util;

import java.util.HashMap;

import org.bukkit.entity.Player;

import net.jahcraft.jahttery.main.Main;
import net.md_5.bungee.api.ChatColor;

public class LotteryStorage {
	
	public HashMap<Player, Double> betStorage = new HashMap<>();
	
	public static Player previousWinner;
	public static String previousWinnings;
	
	public static String prefix = ChatColor.translateAlternateColorCodes('&', Main.data.getConfig().getString("prefix") + " ");
	
	
	public static void checkConfig() {
		
		prefix = ChatColor.translateAlternateColorCodes('&', Main.data.getConfig().getString("prefix") + " ");
		
	}
	
	

}
