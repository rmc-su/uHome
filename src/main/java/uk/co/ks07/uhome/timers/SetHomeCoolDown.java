package uk.co.ks07.uhome.timers;

import org.bukkit.entity.Player;

import uk.co.ks07.uhome.HomeSettings;
import uk.co.ks07.uhome.SuperPermsManager;

/**
 * Manages cooldown for the sethome command.
 */
public final class SetHomeCoolDown extends CoolDownManager {
    private static final SetHomeCoolDown singletonInstance = new SetHomeCoolDown();
    
    public static SetHomeCoolDown getInstance() {
        return singletonInstance;
    }

    private SetHomeCoolDown() {}

    @Override
    protected boolean isCoolingBypassed(Player player) {
        return SuperPermsManager.hasPermission(player, SuperPermsManager.bypassCooldown);
    }

    @Override
    protected int playerGetPermissionsCooldown(Player player) {
                int ret;

                // TODO: Seperate cooldown limits for home and sethome.
                if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownA)) {
                    ret = HomeSettings.coolDowns.get("a");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownB)) {
                    ret = HomeSettings.coolDowns.get("b");
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownC)) {
                    ret = HomeSettings.coolDowns.get("c");
                } else {
                    ret = HomeSettings.defaultCoolDown;
                }

                return ret;
        }
    
    @Override
    protected int getDefaultCoolDownSetting() {
        return HomeSettings.coolDownSetHome;
    }

}
