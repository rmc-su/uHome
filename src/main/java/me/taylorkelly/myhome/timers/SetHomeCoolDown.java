package me.taylorkelly.myhome.timers;


import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.permissions.HomePermissions;

import org.bukkit.entity.Player;

/**
 * Manages cooldown for the sethome command.
 */
public final class SetHomeCoolDown extends CoolDownManager {
    private static final SetHomeCoolDown singletonInstance = new SetHomeCoolDown();
    
    private static final String SET_HOME_PERMISSION_NAME = "myhome.timer.sethome";
    
    public static SetHomeCoolDown getInstance() {
        return singletonInstance;
    }

    private SetHomeCoolDown() {}

    @Override
    protected boolean isCoolingBypassed(Player player) {
        return HomePermissions.bypassSHCooling(player);
    }
    
    @Override
    protected int getCoolDownSetting() {
        return HomeSettings.coolDownSetHome;
    }
    
    @Override
    protected String getCoolDownPermissionName() {
        return SET_HOME_PERMISSION_NAME;
    }

}
