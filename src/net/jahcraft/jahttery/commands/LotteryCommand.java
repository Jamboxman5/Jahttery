package net.jahcraft.jahttery.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.jahcraft.jahttery.main.Main;
import net.jahcraft.jahttery.runnables.LotteryThread;
import net.jahcraft.jahttery.util.LotteryMath;
import net.jahcraft.jahttery.util.LotteryStorage;
import net.md_5.bungee.api.ChatColor;

public class LotteryCommand implements CommandExecutor {

	private Main plugin;
	private LotteryStorage lottoStorage;
	private LotteryMath lottoMath;
	
	public LotteryCommand(Main main) {
		plugin = main;
		lottoStorage = main.lotteryStorage;
		lottoMath = main.lottoMath;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (label.equalsIgnoreCase("lottery") ||
			label.equalsIgnoreCase("lotto") ||
			label.equalsIgnoreCase("jahttery")) {
			
			if (args.length == 0 && sender.hasPermission("jahttery.admin")) {
				sendAdminUsage(sender);
				return true;
			} 
			if (args.length == 0) {
				sendPlayerUsage(sender);
				return true;
			}
			if (args.length >= 1) {
				if (!args[0].equalsIgnoreCase("bet") &&
					!args[0].equalsIgnoreCase("draw") &&
					!args[0].equalsIgnoreCase("refund") &&
					!args[0].equalsIgnoreCase("reload") &&
					!args[0].equalsIgnoreCase("status")){
					if (sender.hasPermission("jahterry.admin")) {
						sendAdminUsage(sender);
						return true;
					} else {
						sendPlayerUsage(sender);
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("bet")) { 
					
					if (!(sender instanceof Player)) return true;
					Player p = (Player) sender;

					sendBet(p, args);
					return true;
					
				}
				if (args[0].equalsIgnoreCase("status")) {
					sendStatus(sender);
					sendBets(sender);
					return true;
				}
				if (args[0].equalsIgnoreCase("draw")) {
					manualDraw(sender);
					return true;
				}
				if (args[0].equalsIgnoreCase("refund")) {
					lottoMath.refundBets(plugin);
					return true;
				}
				if (args[0].equalsIgnoreCase("reload")) {
		
					Main.data.reloadConfig();
					LotteryStorage.checkConfig();
					sender.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
							LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + 
							"Configuration reloaded!");					
					return true;
				}
				
			}

		}

		return false;

	}

	private void manualDraw(CommandSender sender) {

		if (!sender.hasPermission("jahttery.admin")) {
			sendPlayerUsage(sender);
			return;
		} 

		LotteryThread.nextDrawTime = LotteryThread.getCurrentTime();
		
	}

	private void sendBet(Player p, String[] args) {

		//VALID COMMAND?
		if (args.length == 1) {
			p.sendMessage(ChatColor.RED + "Usage: /lottery bet <amount>");
			return;
		}
		
		//VALID BET?
		double bet;
		try {
			bet = Double.parseDouble(args[1]);
		} catch (Exception e) {
			p.sendMessage(ChatColor.RED + "Invalid bet!");
			return;
		}
		if (bet <= 0.0) {
			p.sendMessage(ChatColor.RED + "Invalid bet!");
			return;
		}
		if (plugin.eco.getBalance(p) < bet && !lottoStorage.betStorage.containsKey(p)) {
			p.sendMessage(ChatColor.RED + "You cannot afford that bet!");
			return;
		}
		
		//RAISED BET?
		boolean raisedBet = false;
		if (lottoStorage.betStorage.containsKey(p)) {
			if (lottoStorage.betStorage.get(p) >= bet) {
				p.sendMessage(ChatColor.RED + "That bet is too low!");
				return;
			}
			if ((plugin.eco.getBalance(p) + lottoStorage.betStorage.get(p)) < bet) {
				p.sendMessage(ChatColor.RED + "You cannot afford that bet!");
				return;
			}
			lottoMath.silentRefund(p, plugin);
			raisedBet = true;
			
		}
		
		//ADD BET
		lottoStorage.betStorage.putIfAbsent(p, bet);
		plugin.eco.withdrawPlayer(p, bet);
		
		//ANNOUNCE BET
		if (raisedBet) {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				player.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.YELLOW + p.getName() + 
					ChatColor.of("#779AD8") + " has raised their bet to " + ChatColor.of("#00E8FF") + "$" + String.format("%,.2f", bet) + 
					ChatColor.of("#779AD8") + "!");
			}
		} else {
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				player.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.YELLOW + p.getName() + 
					ChatColor.of("#779AD8") + " has just bet " + ChatColor.of("#00E8FF") + "$" + String.format("%,.2f", bet) + 
					ChatColor.of("#779AD8") + "!");
			}
		}
		
	}

	private void sendBets(CommandSender sender) {
		
		sender.sendMessage(ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "--------" + ChatColor.translateAlternateColorCodes('&', "&7&l[ " + ChatColor.of("#FFD700") + "&lBets &7&l]") + ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "--------");
		
		if (lottoStorage.betStorage.size() == 0) {
			sender.sendMessage(ChatColor.of("#779AD8") + "No bets have been placed yet.");
		} else {
			for (Player p : lottoMath.getBetters()) {
				sender.sendMessage(ChatColor.of("#779AD8") + p.getName() + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#00E8FF") + "$" + String.format("%,.2f", lottoStorage.betStorage.get(p)) + ChatColor.GRAY + " (" + lottoMath.getWinnings(p) + ")");
			}
		}
		
	}

	private void sendStatus(CommandSender sender) {
		
		String prevWinnerName;
		if (LotteryStorage.previousWinner == null) {
			prevWinnerName = "None";
		} else {
			prevWinnerName = LotteryStorage.previousWinner.getName() + ChatColor.GRAY + " (" + LotteryStorage.previousWinnings + ")";
		}
		
		sender.sendMessage(ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---=====" + ChatColor.translateAlternateColorCodes('&', "&7&l[ " + ChatColor.of("#FFD700") + "&lLottery Status &7&l]") + ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "=====---");
		sender.sendMessage(ChatColor.of("#779AD8") + "Previous Winner" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#00E8FF") + prevWinnerName);
		sender.sendMessage(ChatColor.of("#779AD8") + "Last Draw" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#00E8FF") + getLastDrawTime());
		sender.sendMessage(ChatColor.of("#779AD8") + "Next Draw" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#00E8FF") + getNextDrawTime());
		
	}

	private String getNextDrawTime() {
		if (LotteryThread.nextDrawTime != null) {
			return LotteryThread.nextDrawTime;
		} else {
			return "Unknown";
		}
	}

	private String getLastDrawTime() {
		if (LotteryThread.drawTime != null) {
			return LotteryThread.drawTime;
		} else {
			return "Unknown";
		}
		
	}

	private void sendPlayerUsage(CommandSender sender) {
		sender.sendMessage(ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---=====" + ChatColor.translateAlternateColorCodes('&', "&7&l[ " + ChatColor.of("#FFD700") + "&lLottery &7&l]") + ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "=====---");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery " + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Shows this message.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery status" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Shows entered players & their bets.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery bet <amount>" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Enters the specified bet.");
		
	}

	private void sendAdminUsage(CommandSender sender) {
		sender.sendMessage(ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "---=====" + ChatColor.translateAlternateColorCodes('&', "&7&l[ " + ChatColor.of("#FFD700") + "&lLottery &7&l]") + ChatColor.of("#49B3FF") + "" + ChatColor.BOLD + "" + ChatColor.STRIKETHROUGH + "=====---");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery " + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Shows this message.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery status" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Shows entered players & their bets.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery bet <amount>" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Enters the specified bet.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery draw" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Instantly draws and resets the lottery.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery refund" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Refunds all entered bets.");
		sender.sendMessage(ChatColor.of("#00E8FF") + "/lottery reload" + ChatColor.of("#AAAAAA") + " - " + ChatColor.of("#779AD8") + "Reloads the configuration.");
		
	}
	
}
