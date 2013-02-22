package uk.co.ks07.uhome.timers;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import uk.co.ks07.uhome.HomeConfig;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Manages cooldown for a command. Subclasses determine specific settings that
 * determine cooldown timings and applicability.
 */
public abstract class CoolDownManager {

    private static final int SERVER_TICKS_PER_SEC = 20;
    private final HashMap<String, PlayerTaskDetails> players =
            new HashMap<String, PlayerTaskDetails>();

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

        if (timer > 0) {
            if (players.containsKey(player.getName())) {
                plugin.getServer().getScheduler().cancelTask(
                        players.get(player.getName()).getTaskIndex());
            }

            int taskIndex = plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin, new CoolTask(player, this), timer * SERVER_TICKS_PER_SEC);
            players.put(
                    player.getName(),
                    new PlayerTaskDetails(taskIndex, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timer)));
        }
    }

    /**
     * Whether or not the specified player has cooled down. This includes when
     * the estimated cooldown time has been reached, so that players may be
     * cooled down before the timed task has been executed.
     *
     * @param player
     *            Player to check.
     * @return True if the player has cooled down, otherwise false.
     */
    public boolean playerHasCooled(Player player) {
        return !isCoolingTimerPresent(player) || estimateTimeLeft(player) == 0;
    }

    /**
     * Whether or not a cooling timer for the specified player is present.
     *
     * @param player
     *            Player to check
     * @return True if the cooling timer is not present, otherwise false.
     */
    public boolean isCoolingTimerPresent(Player player) {
        return players.containsKey(player.getName());
    }

    /**
     * Estimates and returns time left for the specified player's cooldown. If
     * the player is not currently cooling down, returns 0. Note that small
     * discrepancies in timing between the expected and actual cooldown expiry,
     * may exist.
     *
     * @param player
     *            Player for whom the remaining cooldown is calculated and
     *            returned.
     * @return Estimated remaining cooldown, in seconds, zero if no cooldown
     *         remains or if expected cooldown time has passed.
     */
    public int estimateTimeLeft(Player player) {
        PlayerTaskDetails taskDetails = players.get(player.getName());
        if (taskDetails == null) {
            return 0;
        }
        int secondsLeft = (int) TimeUnit.MILLISECONDS.toSeconds(
                taskDetails.getFinishTime() - System.currentTimeMillis());
        return (secondsLeft > 0) ? secondsLeft : 0;
    }

    /**
     * Gets the total cooldown currently configured for the specified player,
     * including global cooldown and group/player-specific time.
     *
     * @param player
     *            Player for whom cooldown is returned.
     * @return Total cooldown in seconds for the specified player.
     */
    public int getTimer(Player player) {
        int timer;
        if (HomeConfig.timerByPerms) {
            timer = playerGetPermissionsCooldown(player);
            if (HomeConfig.additionalTime) {
                timer += getDefaultCoolDownSetting();
            }
        } else {
            timer = getDefaultCoolDownSetting();
        }
        return timer;
    }

    /**
     * Gets the permissions cooldown currently configured for the specified player.
     *
     * @param player
     *            Player for whom cooldown is returned.
     * @return Permissions cooldown in seconds for the specified player.
     */
    protected abstract int playerGetPermissionsCooldown(Player player);

    /**
     * @return  Currently configured cooldown time.
     */
    protected abstract int getDefaultCoolDownSetting();

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
    protected void onCoolDownExpiry(Player player) {
    }

    /**
     * Task used for scheduling, invoked when cooldown has expired.
     */
    private static class CoolTask implements Runnable {

        private final Player player;
        private final CoolDownManager coolDownManager;

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
