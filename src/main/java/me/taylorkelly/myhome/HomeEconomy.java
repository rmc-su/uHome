package me.taylorkelly.myhome;

import me.taylorkelly.myhome.utils.HomeLogger;

import org.bukkit.plugin.Plugin;
import com.nijikokun.register.Register;
import com.nijikokun.register.payment.Method;
import com.nijikokun.register.payment.Methods;

public class HomeEconomy {
	private static Method economy;
	private static boolean econEnabled = false;
	private static Plugin myhome;
	private static String econname = null; 
	private static String econver = null;
	
	public static void init(Plugin plugin) {
		HomeEconomy.myhome = plugin;
		if(HomeSettings.eConomyEnabled) {
			// Check if Register is already loaded
			Register plug = (Register) myhome.getServer().getPluginManager().getPlugin("Register");
			if (plug != null && plug.getClass().getName().equals("com.nijikokun.register.Register")) {
				enableEconomy();
				findEconomy();
			}
		}
	}
	
	public static void findEconomy() {
		if(!Methods.hasMethod()){
			// Register doesnt know what to use.. Find something
			HomeLogger.info("Finding an economy for Register.");
			if(Methods.setMethod(myhome.getServer().getPluginManager())){
				HomeEconomy.economy = Methods.getMethod();
				econname = HomeEconomy.economy.getName();
				econver = HomeEconomy.economy.getVersion();
				HomeLogger.info("Connected to " + HomeEconomy.economy.getName() + " v" + HomeEconomy.economy.getVersion() + " for economy support.");
			} else {
				// Register found nothing it can use. Abort abort abort.
				noCompatibleEcon();
			}
		} else if(Methods.hasMethod()) {
			// Register already knows what to use
			HomeEconomy.economy = Methods.getMethod();
			econname = HomeEconomy.economy.getName();
			econver = HomeEconomy.economy.getVersion();
			HomeLogger.info("Connected to " + HomeEconomy.economy.getName() + " v" + HomeEconomy.economy.getVersion() + " for economy support.");
		} else {
			// Register found nothing it can use. Abort abort abort.
			noCompatibleEcon();
		}
	}
	
	public static void noCompatibleEcon() {
		disableEconomy();
		HomeSettings.eConomyEnabled = false;
		HomeLogger.warning("===============================================");
		HomeLogger.warning("Economy support is enabled but Register was unable to find");
		HomeLogger.warning("a compatible economy plugin. See the following url for a list of");
		HomeLogger.warning("compatible plugins: http://forums.bukkit.org/threads/16849/");
		HomeLogger.warning("===============================================");

	}
	
	public static void hasUnhooked(Plugin deadplugin) {
		if (economy != null && Methods.hasMethod()) {
			Boolean check = Methods.checkDisabled(deadplugin);
			if(check) {
				economy = null;
				HomeLogger.info("Payment method: " + econname + " v" + econver + " was disabled. Suspending economy services.");
				disableEconomy();
				HomeSettings.eConomyEnabled = false;
			}
		}
	}

	public static void disableEconomy() {
		econEnabled = false;
	}
	
	public static void enableEconomy() {
		econEnabled = true;
	}

	public static boolean hookedEconomy() {
		return econEnabled;
	}
	
	public static boolean economyReady() {
		if(!econEnabled && HomeSettings.eConomyEnabled == true) {
			// Suspend future attempts to call economy stuff.
			HomeSettings.eConomyEnabled = false;
			HomeLogger.warning("===============================================");
			HomeLogger.warning("Economy support is enabled in config but Register was not found. Is it installed?");
			HomeLogger.warning("Economy support has been suspended. MyHome will not charge users until Register is installed.");
			HomeLogger.warning("To download Register see: http://forums.bukkit.org/threads/16849/");
			HomeLogger.warning("===============================================");
			return false;
		} else {
			return true;
		}
	}
	
	//------------------------------------------------------
		
	public static boolean hasAccount(String name){
		if(!economyReady()) { return false; }
		
		return economy.hasAccount(name);
	}

	public static boolean chargePlayer(String name, float amount){
		if(!economyReady()) { return false; }
		
		if(hasAccount(name)) {
			economy.getAccount(name).subtract(amount);
			return true;
		} else {
			HomeLogger.warning("Could not fetch economy details for " + name);
			return false;
		}
	}

	public static boolean hasEnough(String name, float amount) {
		if(!economyReady()) { return false; }
		
		return economy.getAccount(name).hasEnough(amount);
	}

	public static double balance(String name){
		if(!economyReady()) { return 0; }
		
		return economy.getAccount(name).balance();
	}

	public static String formattedBalance(double amount){
		if(!economyReady()) { return "Error"; }
		
		return economy.format(amount);
	}
}