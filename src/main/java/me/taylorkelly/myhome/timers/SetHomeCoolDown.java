package me.taylorkelly.myhome.timers;

import java.util.HashMap;
import me.taylorkelly.myhome.HomePermissions;

import me.taylorkelly.myhome.HomeSettings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SetHomeCoolDown {

    private static HashMap<String, Integer> players = new HashMap<String, Integer>();

    public static void addPlayer(Player player, Plugin plugin) {
        if (HomePermissions.bypassSHCooling(player)) {
            return;
        }

        int timer = getTimer(player);
        if (timer > 0) {
            if (players.containsKey(player.getName())) {
                plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
            }

            int taskIndex = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new CoolSHTask(player), timer * 20);
            players.put(player.getName(), taskIndex);
        }
    }

    public static boolean playerHasCooled(Player player) {
        return !players.containsKey(player.getName());
    }

    public static int timeLeft(Player player) {
        if (players.containsKey(player.getName())) {
            // TODO
            return 0;
        } else {
            return 0;
        }
    }

    public static int getTimer(Player player) {
        int timer = 0;
    	if (HomeSettings.timerByPerms) {
			timer = HomePermissions.integer(player, "myhome.timer.sethome", HomeSettings.coolDownSetHome);
			if(HomeSettings.additionalTime) {
				timer += HomeSettings.coolDownSetHome;
			}
		} else {
			timer = HomeSettings.coolDownSetHome;
		}
        return timer;
    }
    
    private static class CoolSHTask implements Runnable {

        private Player player;

        public CoolSHTask(Player player) {
            this.player = player;
        }

        public void run() {
        	players.remove(player.getName());
        }
    }
}
