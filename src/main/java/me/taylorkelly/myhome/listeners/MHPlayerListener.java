package me.taylorkelly.myhome.listeners;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.data.HomeList;
import me.taylorkelly.myhome.permissions.HomePermissions;
import me.taylorkelly.myhome.timers.WarmUp;

import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;

public class MHPlayerListener implements Listener {

	private HomeList homeList;
	private Plugin plugin;

	public MHPlayerListener(HomeList homeList, Plugin plugin) {
		this.homeList = homeList;
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (homeList.homeExists(event.getPlayer().getName())) {
			homeList.orientPlayer(event.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
		if(!HomeSettings.bedsDuringDay && HomeSettings.bedsCanSethome != 0) {
			if(HomeSettings.bedsCanSethome != 0) {
				homeList.addHome(event.getPlayer(), plugin);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		if(!HomePermissions.set(event.getPlayer())) return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(HomeSettings.bedsDuringDay && event.getClickedBlock().getType() == Material.BED_BLOCK) {
			if(HomeSettings.bedsCanSethome != 0) {
				homeList.addHome(event.getPlayer(), plugin);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(HomeSettings.loadChunks) {
			World world = event.getPlayer().getWorld();
			Chunk chunk = world.getChunkAt(event.getTo());
			int x = chunk.getX();
			int z = chunk.getZ();
			world.refreshChunk(x, z);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST) 
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!HomeSettings.noHomeRespawnWorlds.contains(event.getPlayer().getLocation().getWorld().getName()) && HomeSettings.respawnToHome && homeList.homeExists(event.getPlayer().getName())) {
			Location location = homeList.getHomeFor(event.getPlayer()).getLocation(plugin.getServer());
			if (location != null) {
				event.setRespawnLocation(location);
				homeList.orientPlayer(event.getPlayer());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.isCancelled()) return;
		
		if(HomeSettings.abortOnMove) {
			Player player = event.getPlayer();
			if(WarmUp.isWarming(player)) {
				if(WarmUp.hasMoved(player)) {
					WarmUp.cancelWarming(player, plugin, WarmUp.Reason.MOVEMENT);
				}
			}
		}
	}
}