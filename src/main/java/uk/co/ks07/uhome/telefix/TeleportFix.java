package uk.co.ks07.uhome.telefix;

import org.bukkit.entity.Player;

import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.uHome;

public interface TeleportFix {
    public String getName();
    
    public void init(uHome plugin);
    
    public void end(uHome plugin);
    
    public void notifyTeleport(Player player, Home home);
}
