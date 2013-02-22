package uk.co.ks07.uhome;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.timers.HomeCoolDown;
import uk.co.ks07.uhome.storage.WarpDataSource;

public class Home {
    public static final int UNRECORDED_ATIME = -1; // aTime disabled when created and (possibly) accessed.
    public static final int UNACCESSED_ATIME = 0; // Created but not accessed while aTime enabled.

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
    public long aTime;
    public static int nextIndex = 1;
    public boolean unlocked;

    public Home(int index, String owner, String name, String world, double x, double y, double z, int yaw, int pitch, long aTime, boolean unlocked) {
        this.index = index;
        this.name = name;
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.aTime = aTime;
        this.unlocked = unlocked;
        if (index > nextIndex) {
            nextIndex = index;
        }
        nextIndex++;
    }

    public Home(Player creator) {
        this(creator, uHome.DEFAULT_HOME);
    }

    public Home(Player creator, String name) {
        this(creator.getName(), creator.getLocation(), name);
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
        this.unlocked = false;

        if (HomeConfig.enableATime) {
            this.aTime = UNACCESSED_ATIME;
        } else {
            this.aTime = UNRECORDED_ATIME;
        }
    }

    public boolean playerCanWarp(Player player) {
        return ((this.unlocked && HomeConfig.enableUnlock) || this.playerIsCreator(player.getName()) || this.playerIsInvited(player.getName()) || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp));
    }

    public void warp(Player player, Plugin plugin, Server server) {
        World currWorld;
        if (world.equals("0")) {
            currWorld = server.getWorlds().get(0);
        } else {
            currWorld = server.getWorld(world);
        }
        if (currWorld == null) {
            player.sendMessage(LocaleManager.getString("error.warp.noworld", null, this));
        } else {
            if (player.getLocation().getWorld() == currWorld || SuperPermsManager.hasPermission(player, SuperPermsManager.allowCrossWorld)) {
                if (HomeConfig.enableATime) {
                    this.aTime = System.currentTimeMillis() / 1000;
                    WarpDataSource.updateATime(this, plugin.getLogger());
                }

                Location location = new Location(currWorld, x, y, z, yaw, pitch);
                player.teleport(location);
                HomeCoolDown.getInstance().addPlayer(player, plugin);

                if (playerIsCreator(player.getName())) {
                    player.sendMessage(LocaleManager.getString("own.warp.ok", null, this));
                } else {
                    // Do not print a welcome message if the home name begins and ends with '**'.
                    if (!isSilent(this.name)) {
                        player.sendMessage(LocaleManager.getString("other.warp.ok", null, this));
                    }
                }
            } else {
                player.sendMessage(LocaleManager.getString("error.warp.nocrossworld"));
            }
        }
    }

    public static boolean isSilent(final String name) {
        return name == null || (name.startsWith("**") && name.endsWith("**"));
    }

    public boolean playerIsCreator(String player) {
        return owner.equalsIgnoreCase(player);
    }

    @Override
    public String toString() {
        if (this.unlocked && HomeConfig.enableUnlock) {
            return ChatColor.AQUA + name + ChatColor.WHITE;
        } else {
            return name;
        }
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

    public boolean lastAccessedBefore(int time) {
        return (this.aTime != UNRECORDED_ATIME) && (time < this.aTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Home other = (Home) obj;
        if (this.index != other.index) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.owner == null) ? (other.owner != null) : !this.owner.equals(other.owner)) {
            return false;
        }
        if ((this.world == null) ? (other.world != null) : !this.world.equals(other.world)) {
            return false;
        }
        if (Double.doubleToLongBits(this.x) != Double.doubleToLongBits(other.x)) {
            return false;
        }
        if (Double.doubleToLongBits(this.y) != Double.doubleToLongBits(other.y)) {
            return false;
        }
        if (Double.doubleToLongBits(this.z) != Double.doubleToLongBits(other.z)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.index;
        return hash;
    }

    public static enum InviteStatus {
        SUCCESS,
        ALREADY_INVITED,
        AT_LIMIT
    }
}
