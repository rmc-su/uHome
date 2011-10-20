package uk.co.ks07.uhome.timers;

import uk.co.ks07.uhome.HomeSettings;

import org.bukkit.entity.Player;

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
        return player.hasPermission("uhome.bypass.cooldown");
    }

    @Override
    protected int playerGetPermissionsCooldown(Player player) {
                int ret;

                // TODO: Seperate cooldown limits for home and sethome.
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
    protected int getDefaultCoolDownSetting() {
        return HomeSettings.coolDownSetHome;
    }

}
