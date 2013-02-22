package uk.co.ks07.uhome;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import uk.co.ks07.uhome.timers.WarmUp;

public class UHomeListener implements Listener {

    private uHome plugin;
    private HomeList homeList;

    public UHomeListener(uHome plugin, HomeList homeList) {
        this.plugin = plugin;
        this.homeList = homeList;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity) || HomeConfig.abortOnDamage == 0) {
            return;
        }

        final LivingEntity victim = (LivingEntity) event.getEntity();
        final Entity aggressor = ((EntityDamageByEntityEvent) event).getDamager();

        if (HomeConfig.abortOnDamage == 3) {
            if (victim instanceof Player) {
                Player vplayer = (Player) event.getEntity();
                WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
            }
            if (aggressor instanceof Player) {
                Player aplayer = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
            }
        } else if (HomeConfig.abortOnDamage == 2) {
            if (victim instanceof Player && (((aggressor instanceof Monster) || (aggressor instanceof Animals)) && !(aggressor instanceof Player))) {
                Player vplayer = (Player) event.getEntity();
                WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
            }
            if (aggressor instanceof Player && (((victim instanceof Monster) || (victim instanceof Animals)) && !(victim instanceof Player))) {
                Player aplayer = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
            }
        } else if (HomeConfig.abortOnDamage == 1) {
            if (victim instanceof Player && aggressor instanceof Player) {
                Player vplayer = (Player) event.getEntity();
                WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
                Player aplayer = (Player) ((EntityDamageByEntityEvent) event).getDamager();
                WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if (HomeConfig.bedsCanSethome != 0) {
            homeList.addHome(event.getPlayer(), plugin, uHome.DEFAULT_HOME, plugin.getLogger());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (HomeConfig.bedsDuringDay && event.getClickedBlock().getType() == Material.BED_BLOCK) {
            if (HomeConfig.bedsCanSethome != 0) {
                homeList.addHome(event.getPlayer(), plugin, uHome.DEFAULT_HOME, plugin.getLogger());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (HomeConfig.loadChunks && (! event.isCancelled())) {
            World world = event.getPlayer().getWorld();
            Chunk chunk = world.getChunkAt(event.getTo());
            if (!chunk.isLoaded()) {
                int x = chunk.getX();
                int z = chunk.getZ();
                world.refreshChunk(x, z);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (HomeConfig.respawnToHome && HomeConfig.isHomeRespawnWorld(event.getPlayer().getLocation().getWorld().getName()) && homeList.homeExists(event.getPlayer().getName(), "home")) {
            Location location = homeList.getPlayerDefaultHome(event.getPlayer().getName()).getLocation(plugin.getServer());
            if (location != null) {
                event.setRespawnLocation(location);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (HomeConfig.abortOnMove && (!event.isCancelled())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            // Don't cancel if the player is only looking around.
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                Player player = event.getPlayer();
                if (WarmUp.isWarming(player)) {
                    WarmUp.cancelWarming(player, plugin, WarmUp.Reason.MOVEMENT);
                }
            }
        }
    }
}
