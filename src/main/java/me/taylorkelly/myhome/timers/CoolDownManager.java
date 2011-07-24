package me.taylorkelly.myhome.timers;

import java.util.concurrent.ConcurrentHashMap;

import me.taylorkelly.myhome.HomePermissions;

import me.taylorkelly.myhome.HomeSettings;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Manages cooldown for a command. Subclasses determine specific settings that
 * determine cooldown timings and applicability.
 */
public abstract class CoolDownManager{
    private static final int SERVER_TICKS_PER_MSEC = 20;

    private final ConcurrentHashMap<String, Integer> players = new ConcurrentHashMap<String, Integer>();

    /**
     * Activates cooldown for the specified player, using the current
     * confuration settings. Players that are configured to bypass cooldown will
     * be ignored.
     * 
     * @param player
     *            Player for whom cooldown is active.
     * @param plugin
     *            Plugin invoking the cooldown.
     */
    public void addPlayer(Player player, Plugin plugin) {
        if (isCoolingBypassed(player)) {
            return;
        }
        int timer = getTimer(player);
        
        if(timer > 0) {
            if (players.containsKey(player.getName())) {
                plugin.getServer().getScheduler().cancelTask(players.get(player.getName()));
            }

            int taskIndex = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin, new CoolTask(player, this), timer * SERVER_TICKS_PER_MSEC);
            players.put(player.getName(), taskIndex);
        }
    }

    /**
     * Whether or not the specified player has cooled down.
     * 
     * @param player
     *            Player to check.
     * @return True if the player has cooled down, otherwise false.
     */
    public boolean playerHasCooled(Player player) {
        return !players.containsKey(player.getName());
    }

    public int timeLeft(Player player) {
        if (players.containsKey(player.getName())) {
            // TODO
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * Gets the total cooldown currently configured for the specified player,
     * including global cooldown and group/player-specific time.
     * 
     * @param player
     *            Player for whom cooldown is returned.
     * @return Total cooldown for the specified player.
     */
    public int getTimer(Player player) {
        int timer = 0;
        if (HomeSettings.timerByPerms) {
            timer = HomePermissions.integer(player, getCoolDownPermissionName(), getCoolDownSetting());
            if (HomeSettings.additionalTime) {
                timer += getCoolDownSetting();
            }
        } else {
            timer = getCoolDownSetting();
        }
        return timer;
    }

    /**
     * @return  Currently configured cooldown time.
     */
    protected abstract int getCoolDownSetting();
    
    /**
     * @return  Permission that controls the cooldown.
     */
    protected abstract String getCoolDownPermissionName();
    
    /**
     * Removes the player with the specified name from the cooldown list.
     * 
     * @param playerName
     *            Name of the player to remove.
     */
    protected void removePlayer(String playerName) {
        players.remove(playerName);
    }
    
    /**
     * Returns true if cooldown is bypassed for the specified player, otherwise
     * false.
     * 
     * @param player
     *            Player to check for cooldown bypass.
     * @return True if cooldown is bypassed, otherwise false.
     */
    protected abstract boolean isCoolingBypassed(Player player);

    /**
     * Invoked when cooldown has expired for the specified player, before the player is removed from cooldown list.
     * 
     * @param player
     *            Player for whom the cooldown has expired.
     */
    protected void onCoolDownExpiry(Player player) {}

    /**
     * Task used for scheduling, invoked when cooldown has expired.
     */
    private static class CoolTask implements Runnable {

        protected final Player player;
        
        protected final CoolDownManager coolDownManager;

        public CoolTask(Player player, CoolDownManager coolDownManager) {
            this.player = player;
            this.coolDownManager = coolDownManager;
        }

        public void run() {
            coolDownManager.onCoolDownExpiry(player);
            coolDownManager.removePlayer(player.getName());
        }
    }
}
