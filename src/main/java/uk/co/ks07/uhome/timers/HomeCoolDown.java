package uk.co.ks07.uhome.timers;

import uk.co.ks07.uhome.HomeSettings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manages cooldown for the home command.
 */
public final class HomeCoolDown extends CoolDownManager {
    private static final HomeCoolDown singletonInstance = new HomeCoolDown();
    
    
    /**
     * @return  An instance for managing home cooldown.
     */
    public static HomeCoolDown getInstance() {
        return singletonInstance;
    }
    
    private HomeCoolDown() {}
    
    @Override
    protected boolean isCoolingBypassed(Player player) {
        return player.hasPermission("uhome.bypass.cooldown");
    }
    
    @Override
    protected int getDefaultCoolDownSetting() {
        return HomeSettings.defaultCoolDown;
    }

    @Override
    protected int playerGetPermissionsCooldown(Player player) {
                int ret;

                if (player.hasPermission("uhome.cooldown.a")) {
                    ret = HomeSettings.coolDowns.get("a");
                } else if (player.hasPermission("uhome.cooldown.b")) {
                    ret = HomeSettings.coolDowns.get("b");
                } else if (player.hasPermission("uhome.cooldown.c")) {
                    ret = HomeSettings.coolDowns.get("c");
                } else {
                    ret = HomeSettings.defaultCoolDown;
                }

                return ret;
        }

    @Override
    protected void onCoolDownExpiry(Player player) {
        super.onCoolDownExpiry(player);
        if (HomeSettings.coolDownNotify) {
            player.sendMessage(ChatColor.AQUA + "You have cooled down, feel free to /home");
        }
    }
}
