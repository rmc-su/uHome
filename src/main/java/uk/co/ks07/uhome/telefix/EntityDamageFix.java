package uk.co.ks07.uhome.telefix;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

public abstract class EntityDamageFix implements TeleportFix {
    protected EntityDamageListener listener;
    private Map<Player, LastWarp> lastWarps;

    @Override
    public void init(uHome plugin) {
        lastWarps = new HashMap<Player, LastWarp>();
        plugin.pm.registerEvents(listener, plugin);
        plugin.getLogger().info("Enabled " + this.getName());
    }

    @Override
    public void end(uHome plugin) {
        if (listener != null) {
            EntityDamageEvent.getHandlerList().unregister(listener);
            listener = null;
        }
        lastWarps = null;
        plugin.getLogger().info("Disabled " + this.getName());
    }

    @Override
    public void notifyTeleport(Player player, Home home) {
        lastWarps.put(player, new LastWarp(home, System.currentTimeMillis() / 1000L));
    }
    
    private static class LastWarp {
        public final Home home;
        public final Long aTime;
        
        public LastWarp(Home home, Long aTime) {
            this.home = home;
            this.aTime = aTime;
        }
    }
    
    protected abstract class EntityDamageListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(DamageCause.SUFFOCATION.equals(event.getCause())) || !(event.getEntity() instanceof Player)) {
                return;
            }

            final Player suffocating = (Player) event.getEntity();
            LastWarp warp = lastWarps.get(suffocating);

            if (warp != null) {
                long now = System.currentTimeMillis() / 1000L;
                if (now < (warp.aTime + 5)) {
                    onPlayerSuffocate(suffocating, warp.home, event);
                }

                // Remove player from the warps.
                lastWarps.remove(suffocating);
            }
        }
        
        public abstract void onPlayerSuffocate(final Player player, final Home home, EntityDamageEvent event);
    }
}
