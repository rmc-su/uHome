package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

public class CheckUp implements TeleportFix {
    private static final String name = "Pre warp check";
    private boolean active = false;
    private int maxSearchHeight = 3;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enable(uHome plugin) {
        active = true;
        plugin.getLogger().info("Enabled " + this.getName());
    }

    @Override
    public void disable(uHome plugin) {
        active = false;
        plugin.getLogger().info("Disabled " + this.getName());
    }

    @Override
    public Location notifyTeleport(Player player, Home home) {
        if (active) {
            if (!FixUtils.isSpaceForPlayer(player.getLocation().getBlock())) {
                Location newDest = FixUtils.findSpaceAbove(player.getLocation(), maxSearchHeight);
                if (newDest != null) {
                    return newDest;
                }
            }
        }
            
        return home.getLocation(player.getServer());
    }
    
}
