package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.uHome;

public class UpOnSuffocate extends EntityDamageFix {
    private static final String name = "Up on suffocate";
    private int maxSearchHeight = 3;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enable(uHome plugin) {
        listener = new UpOnSuffocateListener();
        super.enable(plugin);
    }
    
    private class UpOnSuffocateListener extends EntityDamageListener {
        @Override
        public void onPlayerSuffocate(final Player player, final Home home, EntityDamageEvent event) {
            // Warp within the last 5 seconds. Cancel damage.
            event.setCancelled(true);
            // Re-send player.
            Location newLoc = FixUtils.findSpaceAbove(player.getLocation(), maxSearchHeight);
            
            if (newLoc != null) {
                player.teleport(newLoc);
            } else {
                player.sendMessage(LocaleManager.getString("error.warp.nosafe"));
            }
        }
    }
}
