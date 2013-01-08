package uk.co.ks07.uhome.timers;

import java.util.HashMap;

import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.HomeConfig;
import uk.co.ks07.uhome.SuperPermsManager;
import uk.co.ks07.uhome.locale.LocaleManager;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WarmUp {

    private static final HashMap<String, Integer> players = new HashMap<String, Integer>();

    public enum Reason {
        DAMAGE, MOVEMENT, EVENTCANCEL, NONE
    }

    public static void addPlayer(Player player, Home home, Plugin plugin) {
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmup)) {
            home.warp(player, plugin, plugin.getServer());
            return;
        }

        int timer = getTimer(player);

        if (timer > 0) {
            if (isWarming(player)) {
                plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
            }

            if (HomeConfig.warmUpNotify) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("WU_TIME", Integer.toString(timer));

                player.sendMessage(LocaleManager.getString("warmup.wait", params));
            }

            int taskIndex = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new WarmTask(player, home, plugin), timer * 20);
            players.put(player.getName(), taskIndex);
        } else {
            home.warp(player, plugin, plugin.getServer());
        }
    }

    public static boolean playerHasWarmed(Player player) {
        return players.containsKey(player.getName());
    }

    private static void sendPlayer(Player player, Home home, Plugin plugin, Server server) {
        int timer = getTimer(player);
        if (HomeConfig.warmUpNotify && timer > 0) {
            player.sendMessage(LocaleManager.getString("warmup.finished"));
        }
        home.warp(player, plugin, server);
    }

    public static boolean isWarming(Player player) {
        if (players.containsKey(player.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public static void cancelWarming(Player player, Plugin plugin, Reason reason) {
        if (reason == Reason.DAMAGE && SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmupDamage)) {
            return;
        }

        if (reason == Reason.MOVEMENT && SuperPermsManager.hasPermission(player, SuperPermsManager.bypassWarmupMovement)) {
            return;
        }

        if (isWarming(player)) {
            plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
            players.remove(player.getName());

            if (reason == Reason.DAMAGE) {
                player.sendMessage(LocaleManager.getString("warmup.aborted.combat"));
            } else if (reason == Reason.MOVEMENT) {
                player.sendMessage(LocaleManager.getString("warmup.aborted.movement"));
            }
        }
    }

    public static int getTimer(Player player) {
        int timer;
        if (HomeConfig.timerByPerms) {
            timer = SuperPermsManager.getWarmup(player);
            if (HomeConfig.additionalTime) {
                timer += HomeConfig.defaultWarmUp;
            }
        } else {
            timer = HomeConfig.defaultWarmUp;
        }
        return timer;
    }

    private static class WarmTask implements Runnable {

        private Player player;
        private Home home;
        private Plugin plugin;

        public WarmTask(Player player, Home home, Plugin plugin) {
            this.player = player;
            this.home = home;
            this.plugin = plugin;
        }

        @Override
        public void run() {
            sendPlayer(player, home, plugin, plugin.getServer());
            players.remove(player.getName());
        }
    }
}
