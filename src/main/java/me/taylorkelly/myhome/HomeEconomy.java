/**
 * 
 */
package me.taylorkelly.myhome;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

/**
 * @author sleaker
 *
 */
public class HomeEconomy {
    public enum EconomyHandler {
        ICONOMY5, NONE
    }
    
    public static EconomyHandler handler;
    
    public static void initialize(Server server) {
        Plugin iConomy5 = null;
        if (server.getPluginManager().getPlugin("iConomy").getDescription().getVersion().contains("5.")) {
            iConomy5 = server.getPluginManager().getPlugin("iConomy");
        }
        
        if (iConomy5 != null) {
            handler = EconomyHandler.ICONOMY5;
            String version = iConomy5.getDescription().getVersion();
            HomeLogger.info("Economy enabled using: iConomy v" + version);
        } else {
            handler = EconomyHandler.NONE;
            HomeLogger.warning("An economy plugin isn't loaded.");
        }
    }
    
    public static double checkAccount (String player) {
        double pHoldings = 0;
        if (handler == EconomyHandler.ICONOMY5) {
            pHoldings = iConomy.getAccount(player).getHoldings().balance();
        }
        return pHoldings;
    }
    
    
    public static boolean chargePlayer(String player, double amount) {
        if (handler == EconomyHandler.ICONOMY5) {
            
            Account pAccount = iConomy.getAccount(player);
            if (pAccount == null) {
                HomeLogger.warning("[MyHome] - Error fetching iConomy account for " + player);
                return false;
            }
            Holdings pHoldings = pAccount.getHoldings();
            
            if (!pHoldings.hasEnough(amount)) return false;
            pHoldings.subtract(amount);
            
            return true;
        }
        return true;
    }

    public static EconomyHandler getHandler() {
        return handler;
    }

    public static void setHandler(EconomyHandler handler) {
        HomeEconomy.handler = handler;
    }
}
