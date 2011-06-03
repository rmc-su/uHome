package me.taylorkelly.myhome;

import com.nijikokun.register.payment.Method;

public class HomeEconomy {
	public static Method economy;

	public static boolean hasAccount(String name){
		return economy.hasAccount(name);
	}

	public static boolean chargePlayer(String name, float amount){
		if(hasAccount(name)) {
			economy.getAccount(name).subtract(amount);
			return true;
		} else {
			HomeLogger.warning("Could not fetch economy details for " + name);
			return false;
		}
	}

	public static boolean hasEnough(String name, float amount) {
		return economy.getAccount(name).hasEnough(amount);
	}

	public static double balance(String name){
		return economy.getAccount(name).balance();
	}

	public static String formattedBalance(double amount){
		return economy.format(amount);
	}
}