package net.jahcraft.jahttery.util;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import net.jahcraft.jahttery.main.Main;
import net.md_5.bungee.api.ChatColor;

public class LotteryMath {
	
	Main plugin;
	
	public LotteryMath(Main main) {
		
		plugin = main;
		
	}
	
	public Player getWinner(Main plugin) {
		
		Set<Player> betters = plugin.lotteryStorage.betStorage.keySet();

		if (betters.size() < 2) {
			return null;
		}
				
		int index = (int) (Math.random() * betters.size());
		ArrayList<Player> playerList = new ArrayList<>(betters);
		
		Player winner = playerList.get(index);
		
		LotteryStorage.previousWinner = winner;
		LotteryStorage.previousWinnings = getWinnings(winner);
		
		double winnings = plugin.lotteryStorage.betStorage.get(winner) * ( betters.size() / 2 ) + plugin.lotteryStorage.betStorage.get(winner);
		plugin.eco.depositPlayer(winner, winnings);
		plugin.lotteryStorage.betStorage.clear();
		winner.sendTitle(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + "You won the draw!", ChatColor.GREEN + "+$" + String.format("%,.2f", winnings), 1, 90, 1);
		winner.playSound(winner, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		
		winner.sendMessage(ChatColor.GREEN + "You've won $" + String.format("%,.2f", winnings) + "!");
		
		return winner;
		
		
	}
	
	public ArrayList<Player> getBetters() {
		
		Set<Player> betters = plugin.lotteryStorage.betStorage.keySet();
				
		return new ArrayList<>(betters);
		
	}
	
	public String getWinnings(Player p) {
		
		double winnings = plugin.lotteryStorage.betStorage.get(p) * ( getBetters().size() / 2.0 ) + plugin.lotteryStorage.betStorage.get(p);
		
		if (plugin.lotteryStorage.betStorage.size() <= 1) {
			winnings = plugin.lotteryStorage.betStorage.get(p);
		}
		
		return "$" + String.format("%,.2f", winnings);
		
	}
	
	public void refundBets(Main plugin) {
		
		for (Player p : getBetters()) {
			plugin.eco.depositPlayer(p, plugin.lotteryStorage.betStorage.get(p));
			plugin.lotteryStorage.betStorage.remove(p);
			p.sendMessage(ChatColor.of("#779AD8") + "Your bet has been refunded.");
		}
		
	}
	
	public void silentRefund(Player p, Main plugin) {
		
		plugin.eco.depositPlayer(p, plugin.lotteryStorage.betStorage.get(p));
		plugin.lotteryStorage.betStorage.remove(p);
		
	}

}
