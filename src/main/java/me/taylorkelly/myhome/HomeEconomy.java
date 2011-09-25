package me.taylorkelly.myhome;

import org.bukkit.plugin.Plugin;
import com.nijikokun.register.Register;
import com.nijikokun.register.payment.Method;

public class HomeEconomy {
	public static Method economy;
	public static boolean econEnabled = false;
	
	public static boolean hasAccount(String name){
		if(!hookedEconomy()) return false;
		
		return economy.hasAccount(name);
	}

	public static boolean chargePlayer(String name, float amount){
		if(!hookedEconomy()) return false;
		
		if(hasAccount(name)) {
			economy.getAccount(name).subtract(amount);
			return true;
		} else {
			HomeLogger.warning("Could not fetch economy details for " + name);
			return false;
		}
	}

	public static boolean hasEnough(String name, float amount) {
		if(!hookedEconomy()) return false;
		
		return economy.getAccount(name).hasEnough(amount);
	}

	public static double balance(String name){
		if(!hookedEconomy()) return 0;
		
		return economy.getAccount(name).balance();
	}

	public static String formattedBalance(double amount){
		if(!hookedEconomy()) return null;
		
		return economy.format(amount);
	}
	
	public static void disableEconomy() {
		econEnabled = false;
		HomeLogger.warning("Could not interface with Register for Economy support. Is it installed?");
	}
	
	public static void enableEconomy() {
		econEnabled = true;
	}

	public static boolean hookedEconomy() {
		if(econEnabled) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean checkRegister(Plugin plugin) {
		Plugin register = plugin.getServer().getPluginManager().getPlugin("Register");
		if(register == null) {
			disableEconomy();
			return false;
		} else {
			enableEconomy();
			return true;
		}
	}
}