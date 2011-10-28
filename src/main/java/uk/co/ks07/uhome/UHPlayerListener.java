package uk.co.ks07.uhome;

import uk.co.ks07.uhome.timers.WarmUp;

import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;

public class UHPlayerListener extends PlayerListener {

	private HomeList homeList;
	private Plugin plugin;

	public UHPlayerListener(HomeList homeList, Plugin plugin) {
		this.homeList = homeList;
		this.plugin = plugin;
	}

	@Override
	public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
		if(HomeConfig.bedsCanSethome != 0) {
			homeList.addHome(event.getPlayer(), plugin);
		}
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.isCancelled()) return;
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(HomeConfig.bedsDuringDay && event.getClickedBlock().getType() == Material.BED_BLOCK) {
			if(HomeConfig.bedsCanSethome != 0) {
				homeList.addHome(event.getPlayer(), plugin);
			}
		}
	}

	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(HomeConfig.loadChunks) {
			World world = event.getPlayer().getWorld();
			Chunk chunk = world.getChunkAt(event.getTo());
			int x = chunk.getX();
			int z = chunk.getZ();
			world.refreshChunk(x, z);
		}
	}

	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (HomeConfig.respawnToHome && homeList.homeExists(event.getPlayer().getName(), "home")) {
			Location location = homeList.getHomeFor(event.getPlayer()).getLocation(plugin.getServer());
			if (location != null) {
				event.setRespawnLocation(location);
			}
		}
	}

        @Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.isCancelled()) return;
		
		if(HomeConfig.abortOnMove) {
			Player player = event.getPlayer();
			if(WarmUp.isWarming(player)) {
				WarmUp.cancelWarming(player, plugin, WarmUp.Reason.MOVEMENT);
			}
		}
	}
}