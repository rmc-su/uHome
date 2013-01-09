package uk.co.ks07.uhome.telefix;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

public class RetryOnSuffocate extends EntityDamageFix {
    private static final String name = "Retry on suffocate";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enable(uHome plugin) {
        listener = new RetryOnSuffocateListener();
        super.enable(plugin);
    }
    
    private class RetryOnSuffocateListener extends EntityDamageListener {

        @Override
        public void onPlayerSuffocate(final Player player, final Home home, EntityDamageEvent event) {
            // Warp within the last 5 seconds. Cancel damage.
            event.setCancelled(true);
            // Re-send player.
            player.teleport(home.getLocation(Bukkit.getServer()));
        }
    }
}
