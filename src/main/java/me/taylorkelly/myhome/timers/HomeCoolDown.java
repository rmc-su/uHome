package me.taylorkelly.myhome.timers;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.locale.LocaleManager;
import me.taylorkelly.myhome.permissions.HomePermissions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Manages cooldown for the home command.
 */
public final class HomeCoolDown extends CoolDownManager {
    private static final HomeCoolDown singletonInstance = new HomeCoolDown();
    
    private static final String COOLDOWN_PERMISSION_NAME = "myhome.timer.cooldown";
    
    /**
     * @return  An instance for managing home cooldown.
     */
    public static HomeCoolDown getInstance() {
        return singletonInstance;
    }
    
    private HomeCoolDown() {}
    
    @Override
    protected boolean isCoolingBypassed(Player player) {
        return HomePermissions.bypassCooling(player);
    }
    
    @Override
    protected int getCoolDownSetting() {
        return HomeSettings.coolDown;
    }
    
    @Override
    protected String getCoolDownPermissionName() {
        return COOLDOWN_PERMISSION_NAME;
    }

    @Override
    protected void onCoolDownExpiry(Player player) {
        super.onCoolDownExpiry(player);
        if (HomeSettings.coolDownNotify) {
            player.sendMessage(LocaleManager.getString("timer.home.cooled"));
        }
    }
}
