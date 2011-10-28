package uk.co.ks07.uhome.timers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.SuperPermsManager;
import uk.co.ks07.uhome.HomeConfig;

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
        return SuperPermsManager.hasPermission(player, SuperPermsManager.bypassCooldown);
    }
    
    @Override
    protected int getDefaultCoolDownSetting() {
        return HomeConfig.defaultCoolDown;
    }

    @Override
    protected int playerGetPermissionsCooldown(Player player) {
                int ret;

                // TODO: Seperate cooldown limits for home and sethome.
                if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownA)) {
                    ret = HomeConfig.coolDowns.get("a");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownB)) {
                    ret = HomeConfig.coolDowns.get("b");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownC)) {
                    ret = HomeConfig.coolDowns.get("c");
                } else {
                    ret = HomeConfig.defaultCoolDown;
                }

                return ret;
        }

    @Override
    protected void onCoolDownExpiry(Player player) {
        super.onCoolDownExpiry(player);
        if (HomeConfig.coolDownNotify) {
            player.sendMessage(ChatColor.AQUA + "You have cooled down, feel free to /home");
        }
    }
}
