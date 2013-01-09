package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

// Dummy class that performs no correction of teleport locations.
public class NoOp implements TeleportFix {
    private static final String name = "Disabled";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void enable(uHome plugin) {}

    @Override
    public void disable(uHome plugin) {}

    @Override
    public Location notifyTeleport(Player player, Home home) {
        return home.getLocation(player.getServer());
    }
    
}
