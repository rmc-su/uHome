package uk.co.ks07.uhome;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.locale.LocaleManager;

public class Home {

    public int index;
    public String name;
    public String owner;
    public String world;
    public double x;
    public double y;
    public double z;
    public int yaw;
    public int pitch;
    public Set<String> invitees;
    public static int nextIndex = 1;

    public Home(int index, String owner, String name, String world, double x, double y, double z, int yaw, int pitch) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        if (index > nextIndex) {
            nextIndex = index;
        }
        nextIndex++;
    }

    public Home(int index, String owner, String name, String world, double x, double y, double z, int yaw, int pitch, Collection<String> invitees) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.invitees = new HashSet<String>(invitees);
        if (index > nextIndex) {
            nextIndex = index;
        }
        nextIndex++;
    }

    public Home(Player creator) {
        this(creator, "home");
    }

    public Home(Player creator, String name) {
        this.index = nextIndex;
        nextIndex++;
        this.name = name;
        this.owner = creator.getName();
        this.world = creator.getWorld().getName();
        this.x = creator.getLocation().getX();
        this.y = creator.getLocation().getY();
        this.z = creator.getLocation().getZ();
        this.yaw = Math.round(creator.getLocation().getYaw()) % 360;
        this.pitch = Math.round(creator.getLocation().getPitch()) % 360;
    }

    public Home(String owner, Location location, String name) {
        this.index = nextIndex;
        nextIndex++;
        this.name = name;
        this.owner = owner;
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = Math.round(location.getYaw()) % 360;
        this.pitch = Math.round(location.getPitch()) % 360;
    }

    public boolean playerCanWarp(Player player) {
        return (this.playerIsCreator(player.getName()) || this.playerIsInvited(player.getName()) || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp));
    }

    public void warp(Player player, Server server) {
        World currWorld = null;
        if (world.equals("0")) {
            currWorld = server.getWorlds().get(0);
        } else {
            currWorld = server.getWorld(world);
        }
        if (currWorld == null) {
            player.sendMessage(LocaleManager.getString("error.warp.noworld", null, this));
        } else {
            if (player.getLocation().getWorld() == currWorld || SuperPermsManager.hasPermission(player, SuperPermsManager.allowCrossWorld)) {
                Location location = new Location(currWorld, x, y, z, yaw, pitch);
                player.teleport(location);
                
                if (playerIsCreator(player.getName())) {
                    player.sendMessage(LocaleManager.getString("own.warp.ok", null, this));
                } else {
                    player.sendMessage(LocaleManager.getString("other.warp.ok", null, this));
                }
            } else {
                player.sendMessage(LocaleManager.getString("error.warp.nocrossworld"));
            }
        }
    }

    public boolean playerIsCreator(String player) {
        return owner.equalsIgnoreCase(player);
    }

    @Override
    public String toString() {
        return name;
    }

    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = Math.round(location.getYaw()) % 360;
        this.pitch = Math.round(location.getPitch()) % 360;
    }

    public Location getLocation(Server server) {
        World currWorld = server.getWorld(world);
        if (currWorld == null) {
            return null;
        } else {
            return new Location(currWorld, x, y, z, yaw, pitch);
        }
    }

    public InviteStatus addInvitee(String player) {
        if (this.invitees == null) {
            this.invitees = new HashSet<String>();
        }

        if ((SuperPermsManager.getInviteLimit(Bukkit.getPlayerExact(owner)) < 0) || (this.invitees.size() < SuperPermsManager.getInviteLimit(Bukkit.getPlayerExact(owner)))) {
            // True if added, false if already invited.
            if (this.invitees.add(player)) {
                return InviteStatus.SUCCESS;
            } else {
                return InviteStatus.ALREADY_INVITED;
            }
        } else {
            return InviteStatus.AT_LIMIT;
        }
    }

    public void addInvitees(Collection<String> players) {
        if (this.invitees == null) {
            this.invitees = new HashSet<String>(players);
        } else {
            this.invitees.addAll(players);
        }
    }

    public boolean removeInvitee(String player) {
        if (this.invitees != null) {
            // True if invited before, else false.
            return this.invitees.remove(player);
        } else {
            return false;
        }
    }

    public void clearInvitees() {
        this.invitees = null;
    }

    public boolean playerIsInvited(String player) {
        return (this.invitees != null) && (HomeConfig.enableInvite) && (this.invitees.contains(player));
    }

    public boolean hasInvitees() {
        return (this.invitees != null) && (HomeConfig.enableInvite) && (!this.invitees.isEmpty());
    }

    public Collection<String> getInvitees() {
        return this.invitees;
    }

    public String inviteesToString() {
        return this.invitees.toString().replace("[", "").replace("]", "");
    }

    public static enum InviteStatus {
        SUCCESS,
        ALREADY_INVITED,
        AT_LIMIT
    }
}
