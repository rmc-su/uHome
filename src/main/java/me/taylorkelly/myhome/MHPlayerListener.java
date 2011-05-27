package me.taylorkelly.myhome;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class MHPlayerListener extends PlayerListener {

    private HomeList homeList;
    private Server server;
    private Plugin plugin;

    public MHPlayerListener(HomeList homeList, Server server, Plugin plugin) {
        this.homeList = homeList;
        this.server = server;
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (homeList.homeExists(event.getPlayer().getName())) {
            homeList.orientPlayer(event.getPlayer());
        }
    }
    
    @Override
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
    	if(HomeSettings.bedsCanSethome != 0) {
    		homeList.addHome(event.getPlayer(), plugin);
    	}
    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (HomeSettings.respawnToHome && homeList.homeExists(event.getPlayer().getName())) {
            Location location = homeList.getHomeFor(event.getPlayer()).getLocation(server);
            if (location != null) {
                event.setRespawnLocation(location);
                homeList.orientPlayer(event.getPlayer());
            }
        }
    }
}
