package uk.co.ks07.uhome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import uk.co.ks07.uhome.timers.HomeCoolDown;
import uk.co.ks07.uhome.timers.WarmUp;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

public class HomeList {

    private HashMap<String, HashMap<String, Home>> homeList;
    private Server server;
    private final HomeCoolDown homeCoolDown = HomeCoolDown.getInstance();
    private final SetHomeCoolDown setHomeCoolDown = SetHomeCoolDown.getInstance();
    private HashMap<String, HashSet<Home>> inviteList;

    public HomeList(Server server, boolean needImport, Logger log) {
        WarpDataSource.initialize(needImport, server, log);
        WarpData wD = WarpDataSource.getMap(log);
        homeList = wD.homeMap;
        inviteList = wD.inviteMap;
        this.server = server;
    }

    public ExitStatus addHome(Player player, Plugin plugin, String name, Logger log) {
        if (!(setHomeCoolDown.playerHasCooled(player))) {
            return ExitStatus.NEED_COOLDOWN;
        } else {
            if (!homeList.containsKey(player.getName())) {
                // Player has no warps.
                HashMap<String, Home> warps = new HashMap<String, Home>();
                Home warp = new Home(player, name);
                warps.put(name, warp);
                homeList.put(player.getName().toLowerCase(), warps);
                WarpDataSource.addWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);
                return ExitStatus.SUCCESS_FIRST;
            } else if (!this.homeExists(player.getName(), name)) {
                if (this.playerCanSet(player)) {
                    // Player has warps, but not with the given name.
                    Home warp = new Home(player, name);
                    homeList.get(player.getName().toLowerCase()).put(name, warp);
                    WarpDataSource.addWarp(warp, log);
                    setHomeCoolDown.addPlayer(player, plugin);
                    return ExitStatus.SUCCESS;
                } else {
                    return ExitStatus.AT_LIMIT;
                }
            } else {
                // Player has a warp with the given name.
                Home warp = homeList.get(player.getName().toLowerCase()).get(name);
                warp.setLocation(player.getLocation());
                WarpDataSource.moveWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);
                return ExitStatus.SUCCESS_MOVED;
            }
        }
    }

    public ExitStatus adminAddHome(Location location, String owner, String name, Logger log) {
        // Adds a home ignoring limits, ownership and cooldown.
        if (!homeList.containsKey(owner.toLowerCase())) {
            // Player has no warps.
            HashMap<String, Home> warps = new HashMap<String, Home>();
            Home warp = new Home(owner, location, name);
            warps.put(name, warp);
            homeList.put(owner.toLowerCase(), warps);
            WarpDataSource.addWarp(warp, log);
            return ExitStatus.SUCCESS_FIRST;
        } else if (!this.homeExists(owner, name)) {
            // Player has warps, but not with the given name.
            Home warp = new Home(owner, location, name);
            homeList.get(owner.toLowerCase()).put(name, warp);
            WarpDataSource.addWarp(warp, log);
            return ExitStatus.SUCCESS;
        } else {
            // Player has a warp with the given name.
            Home warp = homeList.get(owner.toLowerCase()).get(name);
            warp.setLocation(location);
            WarpDataSource.moveWarp(warp, log);
            return ExitStatus.SUCCESS_MOVED;
        }
    }

    public void warpTo(String target, Player player, Plugin plugin) {
        this.warpTo(player.getName(), target, player, plugin);
    }

    public void warpTo(String targetOwner, String target, Player player, Plugin plugin) {
        MatchList matches = this.getMatches(target, player, targetOwner);
        target = matches.getMatch(target);
        if (homeList.get(targetOwner.toLowerCase()).containsKey(target)) {
            Home warp = homeList.get(targetOwner.toLowerCase()).get(target);
            if (warp.playerCanWarp(player)) {
                if (homeCoolDown.playerHasCooled(player)) {
                    WarmUp.addPlayer(player, warp, plugin);
                    homeCoolDown.addPlayer(player, plugin);
                } else {
                    player.sendMessage(ChatColor.RED + "You need to wait "
                            + homeCoolDown.estimateTimeLeft(player) + " more seconds of the "
                            + homeCoolDown.getTimer(player) + " second cooldown.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to warp to " + targetOwner + "'s home");
            }
        } else {
            player.sendMessage(ChatColor.RED + "The warp " + target + " doesn't exist!");
        }
    }

    public void sendPlayerHome(Player player, Plugin plugin) {
        if (homeList.containsKey(player.getName().toLowerCase())) {
            if (homeCoolDown.playerHasCooled(player)) {
                WarmUp.addPlayer(player, homeList.get(player.getName().toLowerCase()).get(uHome.DEFAULT_HOME), plugin);
                homeCoolDown.addPlayer(player, plugin);
            } else {
                player.sendMessage(ChatColor.RED + "You need to wait "
                        + homeCoolDown.estimateTimeLeft(player) + " more seconds of the "
                        + homeCoolDown.getTimer(player) + " second cooldown.");
            }
        }
    }

    public Location getHomeLocation(String owner, String name) {
        return this.homeList.get(owner.toLowerCase()).get(name).getLocation;
    }

    public boolean playerHasDefaultHome(String player) {
        return this.homeExists(player, uHome.DEFAULT_HOME);
    }

    public boolean playerHasHomes(String player) {
        return this.homeList.containsKey(player.toLowerCase()) && this.getPlayerHomeCount(player) > 0;
    }

    public boolean playerCanWarp(Player player, String owner, String name) {
        return homeList.get(owner.toLowerCase()).get(name).playerCanWarp(player);
    }

    public boolean invitePlayer(String owner, String player, String name) {
        homeList.get(owner.toLowerCase()).get(name).addInvitees(player);

        if (!inviteList.containsKey(player.toLowerCase())) {
            inviteList.put(player.toLowerCase(), new HashSet<Home>());
        }

        if (!inviteList.get(player.toLowerCase()).contains(homeList.get(owner.toLowerCase()).get(name))) {
            inviteList.get(player.toLowerCase()).add(homeList.get(owner.toLowerCase()).get(name));
            Player invitee = server.getPlayerExact(player);
            if (invitee != null) {
                invitee.sendMessage("You have been invited to " + owner + "'s home " + name);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean uninvitePlayer(String owner, String player, String name) {
        homeList.get(owner.toLowerCase()).get(name).removeInvitee(player);
        if (inviteList.containsKey(player.toLowerCase())) {
            inviteList.get(player.toLowerCase()).remove(homeList.get(owner.toLowerCase()).get(name));
            Player invitee = server.getPlayerExact(player);
            if (invitee != null) {
                invitee.sendMessage("You have been uninvited from " + owner + "'s home " + name);
            }

            return true;
        } else {
            return false;
        }
    }

    public int getPlayerHomeCount(String owner) {
        return homeList.get(owner.toLowerCase()).size();
    }

    public boolean playerCanSet(Player player) {
        int playerWarps = homeList.get(player.getName().toLowerCase()).size();
        int playerMaxWarps = this.playerGetLimit(player);

        return ((playerMaxWarps < 0) || (playerWarps < playerMaxWarps));
    }

    public int playerGetLimit(Player player) {
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassLimit)) {
            return -1;
        } else {
            int playerMaxWarps;

            if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitA)) {
                playerMaxWarps = HomeConfig.limits[0];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitB)) {
                playerMaxWarps = HomeConfig.limits[1];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitC)) {
                playerMaxWarps = HomeConfig.limits[2];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitD)) {
                playerMaxWarps = HomeConfig.limits[3];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitE)) {
                playerMaxWarps = HomeConfig.limits[4];
            } else {
                playerMaxWarps = HomeConfig.defaultLimit;
            }

            return playerMaxWarps;
        }
    }

    public ExitStatus deleteHome(String owner, String name, Logger log) {
        if (this.homeExists(owner, name)) {
            Home warp = homeList.get(owner.toLowerCase()).get(name);
            homeList.get(owner.toLowerCase()).remove(name);
            WarpDataSource.deleteWarp(warp, log);
            return ExitStatus.SUCCESS;
        } else {
            return ExitStatus.NOT_EXISTS;
        }
    }

    public boolean homeExists(String owner, String name) {
        if (this.hasHomes(owner)) {
            return homeList.get(owner.toLowerCase()).containsKey(name);
        } else {
            return false;
        }
    }

    public boolean hasHomes(String player) {
        return (homeList.containsKey(player.toLowerCase()) && homeList.get(player.toLowerCase()).size() > 0);
    }

    public boolean hasInvitedToHomes(String player) {
        return (inviteList.containsKey(player.toLowerCase()) && inviteList.get(player.toLowerCase()).size() > 0);
    }

    public String getPlayerList(String owner) {
        if (this.hasHomes(owner)) {
            ArrayList<Home> results = new ArrayList(homeList.get(owner.toLowerCase()).values());

            String ret = results.toString().replace("[", "").replace("]", "");
            return ret;
        } else {
            return null;
        }
    }

    public Home[] getInvitedToList(String owner) {
        if (this.hasInvitedToHomes(owner)) {
            return inviteList.get(owner.toLowerCase()).toArray(new Home[0]);
        } else {
            return null;
        }
    }

    public Collection<Home> getInvitedToList(String owner, String invitedBy) {
        if (this.hasInvitedToHomes(owner)) {
            ArrayList<Home> ret = new ArrayList<Home>();

            for (Home home : inviteList.get(owner.toLowerCase())) {
                if (home.owner.equalsIgnoreCase(invitedBy)) {
                    ret.add(home);
                }
            }

            return ret;
        } else {
            return null;
        }
    }

    public Collection<Home> getRequestList(String owner) {
        if (this.hasHomes(owner)) {
            ArrayList<Home> ret = new ArrayList<Home>();

            for (Home home : homeList.get(owner.toLowerCase()).values()) {
                if (home.hasInvitees()) {
                    ret.add(home);
                }
            }

            return ret;
        } else {
            return null;
        }
    }

    public int getTotalWarps() {
        int ret = 0;
        for (HashMap<String, Home> pList : homeList.values()) {
            ret += pList.size();
        }
        return ret;
    }

    private MatchList getMatches(String name, Player player, String owner) {
        ArrayList<Home> exactMatches = new ArrayList<Home>();
        ArrayList<Home> matches = new ArrayList<Home>();

        if (!this.hasHomes(owner)) {
            return new MatchList(exactMatches, matches);
        }

        List<String> names = new ArrayList<String>(homeList.get(owner.toLowerCase()).keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (int i = 0; i < names.size(); i++) {
            String currName = names.get(i);
            Home warp = homeList.get(owner.toLowerCase()).get(currName);
            if (warp.playerCanWarp(player)) {
                if (warp.name.equalsIgnoreCase(name)) {
                    exactMatches.add(warp);
                } else if (warp.name.toLowerCase().contains(name.toLowerCase())) {
                    matches.add(warp);
                }
            }
        }
        if (exactMatches.size() > 1) {
            for (Home warp : exactMatches) {
                if (!warp.name.equals(name)) {
                    exactMatches.remove(warp);
                    matches.add(0, warp);
                }
            }
        }
        return new MatchList(exactMatches, matches);
    }

    public Home getPlayerDefaultHome(String player) {
        return homeList.get(player.toLowerCase()).get(uHome.DEFAULT_HOME);
    }

    public static String getOnlinePlayerCapitalisation(String name) {
        Player player = Bukkit.getPlayer(name);
        
        if (player != null && name.equalsIgnoreCase(player.getName())) {
            return player.getName();
        } else {
            return name;
        }
    }

    public static enum ExitStatus {
        SUCCESS,
        SUCCESS_MOVED,
        SUCCESS_FIRST,
        NOT_EXISTS,
        NOT_PERMITTED,
        AT_LIMIT,
        NEED_COOLDOWN,
        UNKNOWN;
    }
}

class MatchList {

    public MatchList(ArrayList<Home> exactMatches, ArrayList<Home> matches) {
        this.exactMatches = exactMatches;
        this.matches = matches;
    }
    public ArrayList<Home> exactMatches;
    public ArrayList<Home> matches;

    public String getMatch(String name) {
        if (exactMatches.size() == 1) {
            return exactMatches.get(0).name;
        }
        if (exactMatches.isEmpty() && matches.size() == 1) {
            return matches.get(0).name;
        }
        return name;
    }
}
