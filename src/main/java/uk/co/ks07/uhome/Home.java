package uk.co.ks07.uhome;

import java.util.Collection;
import java.util.TreeSet;
import org.bukkit.*;
import org.bukkit.entity.Player;

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
        public Collection<String> invitees;
	public static int nextIndex = 1;
	Location getLocation;

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
                this.invitees.addAll(invitees);
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
			player.sendMessage(ChatColor.RED + "Uh oh. The world with that home doesn't exist!");
		} else {
			Location location = new Location(currWorld, x, y, z, yaw, pitch);
			player.teleport(location);
		}
	}

	public boolean playerIsCreator(String player) {
		return owner.equals(player);
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
		if(currWorld == null) {
			return null;
		} else {
			return new Location(currWorld, x, y, z, yaw, pitch);
		}
	}

        public void addInvitees(String player) {
            if (this.invitees == null) {
                this.invitees = new TreeSet<String>();
            }
            this.invitees.add(player);
        }

        public void addInvitees(Collection<String> players) {
            if (this.invitees == null) {
                this.invitees = new TreeSet<String>(players);
            } else {
                this.invitees.addAll(players);
            }
        }

        public void removeInvitee(String player) {
            if (this.invitees != null || this.invitees.contains(player)) {
                this.invitees.remove(player);
            }
        }

        public void clearInvitees() {
            this.invitees = null;
        }

        public boolean playerIsInvited(String player) {
            return (this.invitees != null) && (HomeConfig.enableInvite) && (this.invitees.contains(player));
        }
}
