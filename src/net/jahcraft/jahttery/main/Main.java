package net.jahcraft.jahttery.main;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.jahcraft.jahttery.commands.LotteryCommand;
import net.jahcraft.jahttery.files.DataManager;
import net.jahcraft.jahttery.runnables.LotteryThread;
import net.jahcraft.jahttery.util.LotteryMath;
import net.jahcraft.jahttery.util.LotteryStorage;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {

	public Economy eco;
	public static DataManager data;
	
	public static boolean sendLottery = true;

	public LotteryMath lottoMath;
	public LotteryStorage lotteryStorage;
	
	@Override
	public void onEnable() {
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling Jahttery!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}
		
		Main.data = new DataManager(this);
		
		data.reloadConfig();
		
		lotteryStorage = new LotteryStorage();
		lottoMath = new LotteryMath(this);
		
		getCommand("lottery").setExecutor((CommandExecutor) new LotteryCommand(this));
		
		Bukkit.getScheduler().runTaskAsynchronously(this, new LotteryThread(this));	
		
		Bukkit.getLogger().info("Jahttery Loaded and Enabled!");
		
	}
	
	@Override
	public void onDisable() {
		
		sendLottery = false;
		lottoMath.refundBets(this);
		
		Bukkit.getLogger().info("Jahttery Disabled!");
		
	}
	
	private boolean setupEconomy() {
		
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		
		if (economy != null)
			eco = economy.getProvider();
		return (eco != null);
		
	}
	
}
