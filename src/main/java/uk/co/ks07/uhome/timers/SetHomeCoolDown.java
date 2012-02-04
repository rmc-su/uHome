package uk.co.ks07.uhome.timers;

import org.bukkit.entity.Player;

import uk.co.ks07.uhome.HomeConfig;
import uk.co.ks07.uhome.SuperPermsManager;

/**
 * Manages cooldown for the sethome command.
 */
public final class SetHomeCoolDown extends CoolDownManager {

    private static final SetHomeCoolDown singletonInstance = new SetHomeCoolDown();

    public static SetHomeCoolDown getInstance() {
        return singletonInstance;
    }

    private SetHomeCoolDown() {
    }

    @Override
    protected boolean isCoolingBypassed(Player player) {
        return SuperPermsManager.hasPermission(player, SuperPermsManager.bypassCooldown);
    }

    @Override
    protected int playerGetPermissionsCooldown(Player player) {
        int ret;

        // TODO: Seperate cooldown limits for home and sethome.
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownA)) {
            ret = HomeConfig.coolDowns[0];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownB)) {
            ret = HomeConfig.coolDowns[1];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownC)) {
            ret = HomeConfig.coolDowns[2];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownD)) {
            ret = HomeConfig.coolDowns[3];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownE)) {
            ret = HomeConfig.coolDowns[4];
        } else {
            ret = HomeConfig.defaultCoolDown;
        }

        return ret;
    }

    @Override
    protected int getDefaultCoolDownSetting() {
        return HomeConfig.defaultCoolDown;
    }
}
