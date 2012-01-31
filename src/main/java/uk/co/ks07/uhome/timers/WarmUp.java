package uk.co.ks07.uhome.timers;

import java.util.HashMap;

import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.HomeConfig;
import uk.co.ks07.uhome.SuperPermsManager;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WarmUp {
    private static HashMap<String, Integer> players = new HashMap<String, Integer>();
    public enum Reason {
    	DAMAGE, MOVEMENT, EVENTCANCEL, NONE
    }

    public static void addPlayer(Player player, Home home, Plugin plugin) {
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmup)) {
            home.warp(player, plugin.getServer());
            return;
        }
        
        int timer = getTimer(player);
    	
        if (timer > 0) {
            if (isWarming(player)) {
                plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
            }
            
            if (HomeConfig.warmUpNotify) {
                player.sendMessage(ChatColor.RED + "You will have to warm up for " + timer + " secs");
            }
            
            int taskIndex = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new WarmTask(player, home, plugin.getServer()), timer*20);
            players.put(player.getName(), taskIndex);
        } else {
            home.warp(player, plugin.getServer());
        }
    }

    public static boolean playerHasWarmed(Player player) {
        return players.containsKey(player.getName());
    }

    private static void sendPlayer(Player player, Home home, Server server) {
        int timer = getTimer(player);
    	if (HomeConfig.warmUpNotify && timer > 0)
            player.sendMessage(ChatColor.RED + "You have warmed up! Sending you /home");
        home.warp(player, server);
    }

    public static boolean isWarming(Player player) {
    	if(players.containsKey(player.getName())) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public static void cancelWarming(Player player, Plugin plugin, Reason reason) {
   		if(reason == Reason.DAMAGE && SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmupDamage))
   			return;
   		
   		if(reason == Reason.MOVEMENT && SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmupMovement))
   			return;
    	
    	if (isWarming(player)) {
   			plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
   			players.remove(player.getName());
   			
   			if(reason == Reason.DAMAGE) { 
   				player.sendMessage(ChatColor.RED + "Your /home has been aborted due to combat");
   			} else if(reason == Reason.MOVEMENT) { 
   				player.sendMessage(ChatColor.RED + "Your /home has been aborted due to movement");
   			}
   		}
    }
    
    public static int getTimer(Player player) {
        int timer = 0;
    	if (HomeConfig.timerByPerms) {
			timer = playerGetWarmup(player);
                        if(HomeConfig.additionalTime) {
				timer += HomeConfig.defaultWarmUp;
			}
		} else {
			timer = HomeConfig.defaultWarmUp;
		}
    	return timer;
    }

    private static int playerGetWarmup(Player player) {
                int ret;

                if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupA)) {
                    ret = HomeConfig.warmUps.get("a");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupB)) {
                    ret = HomeConfig.warmUps.get("b");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupC)) {
                    ret = HomeConfig.warmUps.get("c");
                } else {
                    ret = HomeConfig.defaultWarmUp;
                }

                return ret;
        }
    
    
    private static class WarmTask implements Runnable {
        private Player player;
        private Home home;
        private Server server;

        public WarmTask(Player player, Home home, Server server) {
            this.player = player;
            this.home = home;
            this.server = server;
        }

        public void run() {
            sendPlayer(player, home, server);
            players.remove(player.getName());
        }
    }
}
