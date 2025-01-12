package net.jahcraft.jahttery.runnables;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.jahcraft.jahttery.main.Main;
import net.jahcraft.jahttery.util.LotteryMath;
import net.jahcraft.jahttery.util.LotteryStorage;
import net.md_5.bungee.api.ChatColor;

public class LotteryThread implements Runnable {

	public static int drawSecond;
	
	public static String drawTime;
	public static String m1Time;
	public static String m2Time;
	public static String m3Time;
	public static String m4Time;
	public static String nextDrawTime;
	
	public static boolean m1Sent = false;
	public static boolean m2Sent = false;
	public static boolean m3Sent = false;
	public static boolean m4Sent = false;
	public static boolean drawing = false;
	public static boolean drawn = false;
	
	private static Main plugin;
	private static LotteryMath lottoMath;
	
	public LotteryThread(Main main) {
		plugin = main;
		lottoMath = main.lottoMath;
	}
	
	@Override
	public void run() {
			
		Main.sendLottery = true;
		
		resetLotto();
		
		try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendReloadMessage();
		
		while(Main.sendLottery) {
			
			if (getCurrentTime().contains(m1Time) && !m1Sent) sendM1();
			if (getCurrentTime().contains(m2Time) && !m2Sent) sendM2();
			if (getCurrentTime().contains(m3Time) && !m3Sent) sendM3();
			if (getCurrentTime().contains(m4Time) && !m4Sent) sendM4();
			if (getCurrentTime().contains(nextDrawTime) && !drawing) sendPreliminaryDraw();
			
			if (drawing) {
				if (getSeconds() == drawSecond && !drawn) draw();
			}
						
		}
		
	}
	
	private void sendPreliminaryDraw() {
		
		drawing = true;
		
		drawSecond = getSeconds() + 3;
		if (drawSecond >= 60) {
			drawSecond = drawSecond % 60;
		}
		
		Bukkit.getLogger().info("Lottery Drawing...");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "And the winner is...");
		}
		
	}

	private int getSeconds() {
		
		return Calendar.getInstance().get(Calendar.SECOND);
		
	}
	
	

	private void sendReloadMessage() {

		if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery Reloaded: 30 Minutes Remaining");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "Lottery reloaded! Drawing again in " + ChatColor.of("#00E8FF") + "30 minutes" + ChatColor.of("#779AD8") + ".");
		}
		
	}

	private void sendM1() {
		m1Sent = true;
		if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: 20 Minutes Remaining");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "Lottery will be drawn in " + ChatColor.of("#00E8FF") + "20 minutes" + ChatColor.of("#779AD8") + ".");
		}
	}

	private void sendM2() {
		m2Sent = true;	
		if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: 10 Minutes Remaining");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "Lottery will be drawn in " + ChatColor.of("#00E8FF") + "10 minutes" + ChatColor.of("#779AD8") + ".");
		}
		
	}

	private void sendM3() {
		m3Sent = true;	
		if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: 5 Minutes Remaining");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "Lottery will be drawn in " + ChatColor.of("#00E8FF") + "5 minutes" + ChatColor.of("#779AD8") + ".");
		}
	}

	private void sendM4() {
		m4Sent = true;		
		if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: 1 Minute Remaining");
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
					LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#779AD8") + "Lottery will be drawn in " + ChatColor.of("#00E8FF") + "1 minute" + ChatColor.of("#779AD8") + ".");
		}
	}

	public static void draw() {
		drawn = true;
		
		try {
			Player winner = lottoMath.getWinner(plugin);
			if (winner != null) {

				Bukkit.getLogger().info("Lottery: " + winner.getName() + " won " + LotteryStorage.previousWinnings);
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
							LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.of("#FFD700") + winner.getName() + "!" + ChatColor.of("#779AD8") + " The lottery will draw again in " + ChatColor.of("#00E8FF") + "30 minutes" + ChatColor.of("#779AD8") + ".");
				}
			} else {
				
				if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: There Was No Winner. 30 Minutes Remaining");
				for (Player p : Bukkit.getServer().getOnlinePlayers()) {
					p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
							LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.YELLOW + "Nobody!" + ChatColor.of("#779AD8") + " The lottery will draw again in " + ChatColor.of("#00E8FF") + "30 minutes" + ChatColor.of("#779AD8") + ".");
				}
			}
			
		} catch (Exception e) {
						
			if (Bukkit.getOnlinePlayers().size() > 0) Bukkit.getLogger().info("Lottery: There Was No Winner. 30 Minutes Remaining");
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				
				p.sendMessage(ChatColor.of("#FFD700") + "" + ChatColor.BOLD + 
						LotteryStorage.prefix + ChatColor.of("#00E8FF") + "» " + ChatColor.YELLOW + "Nobody!" + ChatColor.of("#779AD8") + " The lottery will draw again in " + ChatColor.of("#00E8FF") + "30 minutes" + ChatColor.of("#779AD8") + ".");
			}
			
		}
		
		resetLotto();
		
	}
	
	private static void resetLotto() {
		
		m1Sent = false;
		m2Sent = false;
		m3Sent = false;
		m4Sent = false;
		drawing = false;
		drawn = false;
		
		setUpTimes();
		
	}

	private static void setUpTimes() {
		
		Calendar cal = Calendar.getInstance();
		int hours = cal.get(Calendar.HOUR);
		int minutes = cal.get(Calendar.MINUTE);
		
		drawTime = getTimeString(hours, minutes);
		m1Time = getTimeString(hours, minutes + 10);
		m2Time = getTimeString(hours, minutes + 20);
		m3Time = getTimeString(hours, minutes + 25);
		m4Time = getTimeString(hours, minutes + 29);
		nextDrawTime = getTimeString(hours, minutes + 30);
		
	}
	
	public static String getCurrentTime() {
		
		Calendar cal = Calendar.getInstance();
		int hours = cal.get(Calendar.HOUR);
		int minutes = cal.get(Calendar.MINUTE);
		
		if (hours == 0) hours = 12;
		
		return getTimeString(hours, minutes);
		
	}
	
	private static String getTimeString(int hours, int minutes) {
		
		if (minutes >= 60) {
			minutes = minutes % 60;
			hours += 1;
		}
		if (hours > 12) {
			hours = 1;
		}
		
		if (hours == 0) hours = 12;
		
		String time;
		
		if (minutes < 10) {
			time = hours + ":0" + minutes;
		} else {
			time = hours + ":" + minutes;
		}
		
		return time;
		
	}
	
}
