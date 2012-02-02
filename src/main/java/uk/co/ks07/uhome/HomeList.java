package uk.co.ks07.uhome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

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
        homeList = WarpDataSource.getMap(log);
        inviteList = new HashMap<String, HashSet<Home>>();
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
                homeList.put(player.getName(), warps);
                WarpDataSource.addWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);
                return ExitStatus.SUCCESS_FIRST;
            } else if (!this.homeExists(player.getName(), name)) {
                if (this.playerCanSet(player)) {
                    // Player has warps, but not with the given name.
                    Home warp = new Home(player, name);
                    homeList.get(player.getName()).put(name, warp);
                    WarpDataSource.addWarp(warp, log);
                    setHomeCoolDown.addPlayer(player, plugin);
                    return ExitStatus.SUCCESS;
                } else {
                    return ExitStatus.AT_LIMIT;
                }
            } else {
                // Player has a warp with the given name.
                Home warp = homeList.get(player.getName()).get(name);
                warp.setLocation(player.getLocation());
                WarpDataSource.moveWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);
                return ExitStatus.SUCCESS_MOVED;
            }
        }
    }

    public ExitStatus adminAddHome(Location location, String owner, String name, Logger log) {
        // Adds a home ignoring limits, ownership and cooldown.
        if (!homeList.containsKey(owner)) {
            // Player has no warps.
            HashMap<String, Home> warps = new HashMap<String, Home>();
            Home warp = new Home(owner, location, name);
            warps.put(name, warp);
            homeList.put(owner, warps);
            WarpDataSource.addWarp(warp, log);
            return ExitStatus.SUCCESS_FIRST;
        } else if (!this.homeExists(owner, name)) {
            // Player has warps, but not with the given name.
            Home warp = new Home(owner, location, name);
            homeList.get(owner).put(name, warp);
            WarpDataSource.addWarp(warp, log);
            return ExitStatus.SUCCESS;
        } else {
            // Player has a warp with the given name.
            Home warp = homeList.get(owner).get(name);
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
        if (homeList.get(targetOwner).containsKey(target)) {
            Home warp = homeList.get(targetOwner).get(target);
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
        if (homeList.containsKey(player.getName())) {
            if (homeCoolDown.playerHasCooled(player)) {
                WarmUp.addPlayer(player, homeList.get(player.getName()).get(uHome.DEFAULT_HOME), plugin);
                homeCoolDown.addPlayer(player, plugin);
            } else {
                player.sendMessage(ChatColor.RED + "You need to wait "
                        + homeCoolDown.estimateTimeLeft(player) + " more seconds of the "
                        + homeCoolDown.getTimer(player) + " second cooldown.");
            }
        }
    }

    public Location getHomeLocation(String owner, String name) {
        return this.homeList.get(owner).get(name).getLocation;
    }

    public boolean playerHasDefaultHome(String player) {
        return this.homeExists(player, uHome.DEFAULT_HOME);
    }

    public boolean playerHasHomes(String player) {
        return this.homeList.containsKey(player) && this.getPlayerHomeCount(player) > 0;
    }

    public boolean playerCanWarp(Player player, String owner, String name) {
        return homeList.get(owner).get(name).playerCanWarp(player);
    }

    public boolean invitePlayer(String owner, String player, String name) {
        homeList.get(owner).get(name).addInvitees(player);

        if (!inviteList.containsKey(player)) {
            inviteList.put(player, new HashSet<Home>());
        }

        if (!inviteList.get(player).contains(homeList.get(owner).get(name))) {
            inviteList.get(player).add(homeList.get(owner).get(name));
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
        homeList.get(owner).get(name).removeInvitee(player);
        if (inviteList.containsKey(player)) {
            inviteList.get(player).remove(homeList.get(owner).get(name));
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
        return homeList.get(owner).size();
    }

    public boolean playerCanSet(Player player) {
        int playerWarps = homeList.get(player.getName()).size();
        int playerMaxWarps = this.playerGetLimit(player);

        return ((playerMaxWarps < 0) || (playerWarps < playerMaxWarps));
    }

    public int playerGetLimit(Player player) {
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassLimit)) {
            return -1;
        } else {
            int playerMaxWarps;

            if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitA)) {
                playerMaxWarps = HomeConfig.limits.get("a");
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitB)) {
                playerMaxWarps = HomeConfig.limits.get("b");
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitC)) {
                playerMaxWarps = HomeConfig.limits.get("c");
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitD)) {
                playerMaxWarps = HomeConfig.limits.get("d");
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitE)) {
                playerMaxWarps = HomeConfig.limits.get("e");
            } else {
                playerMaxWarps = HomeConfig.defaultLimit;
            }

            return playerMaxWarps;
        }
    }

    public ExitStatus deleteHome(Player owner, String name, Logger log) {
        if (this.homeExists(owner.getName(), name)) {
            Home warp = homeList.get(owner.getName()).get(name);
            homeList.get(owner.getName()).remove(name);
            WarpDataSource.deleteWarp(warp, log);
//            owner.sendMessage(ChatColor.AQUA + "You have deleted your home '" + name + "'.");
            return ExitStatus.SUCCESS;
        } else {
//            owner.sendMessage(ChatColor.RED + "You don't have a home called '" + name + "'!");
            return ExitStatus.NOT_EXISTS;
        }
    }

    public ExitStatus deleteHome(String owner, String name, Logger log) {
        if (this.homeExists(owner, name)) {
            Home warp = homeList.get(owner).get(name);
            homeList.get(owner).remove(name);
            WarpDataSource.deleteWarp(warp, log);
//            sender.sendMessage(ChatColor.AQUA + "You have deleted " + owner + "'s home '" + name + "'.");
            return ExitStatus.SUCCESS;
        } else {
//            sender.sendMessage(ChatColor.RED + "There is no home '" + name + "' for " + owner + "!");
            return ExitStatus.NOT_EXISTS;
        }
    }

    public boolean homeExists(String owner, String name) {
        if (this.hasHomes(owner)) {
            return homeList.get(owner).containsKey(name);
        } else {
            return false;
        }
    }

    public boolean hasHomes(String player) {
        return (homeList.containsKey(player) && homeList.get(player).size() > 0);
    }

    public boolean hasInvitedToHomes(String player) {
        return (inviteList.containsKey(player) && inviteList.get(player).size() > 0);
    }

    public void listInvitedTo(Player player) {
        String results = this.getInvitedToList(player.getName());

        if (results == null) {
            player.sendMessage(ChatColor.RED + "You have no invites!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have been invited to the following homes:");
            player.sendMessage(results);
        }
    }

    public void listRequests(Player player) {
        String results[] = this.getRequestList(player.getName());

        if (results == null) {
            player.sendMessage(ChatColor.RED + "You haven't invited anyone!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have invited others to the following homes:");
            for (String s : results) {
                player.sendMessage(s);
            }
        }
    }

    public String getPlayerList(String owner) {
        if (this.hasHomes(owner)) {
            ArrayList<Home> results = new ArrayList(homeList.get(owner).values());

            String ret = results.toString().replace("[", "").replace("]", "");
            return ret;
        } else {
            return null;
        }
    }

    public String getInvitedToList(String owner) {
        if (this.hasInvitedToHomes(owner)) {
            StringBuilder ret = new StringBuilder(32);

            for (Home home : inviteList.get(owner)) {
                ret.append(home.owner).append(" ").append(home.name).append(", ");
            }

            return ret.delete(ret.length() - 2, ret.length() - 1).toString();
        } else {
            return null;
        }
    }

    public String[] getRequestList(String owner) {
        if (this.hasHomes(owner)) {
            String[] reqs = new String[this.getPlayerHomeCount(owner)];
            boolean anyInvites = false;
            int i = -1;

            for (Home home : homeList.get(owner).values()) {
                if (home.hasInvitees()) {
                    anyInvites = true;
                    i += 1;

                    StringBuilder temp = new StringBuilder(32);
                    temp.append(home.name).append(" - (");

                    for (String invitee : home.getInvitees()) {
                        temp.append(invitee).append(", ");
                    }
                    temp.delete(temp.length() - 2, temp.length() - 1).append(")");

                    reqs[i] = temp.toString();
                }
            }

            if (anyInvites) {
                return reqs;
            } else {
                return null;
            }
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

        List<String> names = new ArrayList<String>(homeList.get(owner).keySet());
        Collator collator = Collator.getInstance();
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(names, collator);

        for (int i = 0; i < names.size(); i++) {
            String currName = names.get(i);
            Home warp = homeList.get(owner).get(currName);
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

    public Home getHomeFor(String player) {
        return homeList.get(player).get(uHome.DEFAULT_HOME);
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
