package uk.co.ks07.uhome.telefix;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

public interface TeleportFix {
    public String getName();
    
    public void enable(uHome plugin);
    
    public void disable(uHome plugin);
    
    public Location notifyTeleport(Player player, Home home);
}
