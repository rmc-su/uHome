package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
        private boolean isBlockSuitable(Block block) {
            return (!block.getType().isSolid());
        }

        private Location findSpaceAbove(Location current) {
            int i;
            
            for (i = current.getBlockY(); (i < current.getWorld().getMaxHeight()) && (i - current.getBlockY() < maxSearchHeight); i++) {
                Block newBlock = current.add(0, i, 0).getBlock();
                
                if (isBlockSuitable(newBlock)) {
                    if (isBlockSuitable(newBlock.getRelative(0, 1, 0))) {
                        return newBlock.getLocation().add(0.5, 0, 0.5);
                    }
                }
            } 
            
            return null;
        }
        
        @Override
        public void onPlayerSuffocate(final Player player, final Home home, EntityDamageEvent event) {
            // Warp within the last 5 seconds. Cancel damage.
            event.setCancelled(true);
            // Re-send player.
            Location newLoc = findSpaceAbove(player.getLocation());
            
            if (newLoc != null) {
                player.teleport(newLoc);
            } else {
                player.sendMessage(LocaleManager.getString("error.warp.nosafe"));
            }
        }
    }
}
