package uk.co.ks07.uhome.timers;

import org.bukkit.entity.Player;

import uk.co.ks07.uhome.SuperPermsManager;
import uk.co.ks07.uhome.HomeConfig;
import uk.co.ks07.uhome.locale.LocaleManager;

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

    private HomeCoolDown() {
    }

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
        return SuperPermsManager.getHomeCooldown(player);
    }

    @Override
    protected void onCoolDownExpiry(Player player) {
        super.onCoolDownExpiry(player);
        if (HomeConfig.coolDownNotify) {
            player.sendMessage(LocaleManager.getString("cooldown.finished"));
        }
    }
}
