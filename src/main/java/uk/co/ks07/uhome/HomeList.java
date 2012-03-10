package uk.co.ks07.uhome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import uk.co.ks07.uhome.Home.InviteStatus;
import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.timers.HomeCoolDown;
import uk.co.ks07.uhome.timers.WarmUp;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

public class HomeList {

    private HashMap<String, HashMap<String, Home>> homeList;
    private Server server;
    private uHome plugin;
    private final HomeCoolDown homeCoolDown = HomeCoolDown.getInstance();
    private final SetHomeCoolDown setHomeCoolDown = SetHomeCoolDown.getInstance();
    private HashMap<String, HashSet<Home>> inviteList;

    public HomeList(uHome plugin, boolean needImport, Logger log) {
        WarpDataSource.initialize(needImport, server, log);
        WarpData wD = WarpDataSource.getMap(log);
        homeList = wD.homeMap;
        inviteList = wD.inviteMap;
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    public ExitStatus addHome(Player player, Plugin plugin, String name, Logger log) {
        if (!(setHomeCoolDown.playerHasCooled(player))) {
            return ExitStatus.NEED_COOLDOWN;
        } else {
            if (!homeList.containsKey(player.getName().toLowerCase())) {
                // Player has no warps.
                HashMap<String, Home> warps = new HashMap<String, Home>();
                Home warp = new Home(player, name);
                warps.put(name, warp);
                homeList.put(player.getName().toLowerCase(), warps);
                WarpDataSource.addWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);
                
                if (this.checkSetCosts(player)) {
                    return ExitStatus.SUCCESS_FIRST;
                } else {
                    return ExitStatus.NOT_ENOUGH_MONEY;
                }
            } else if (!this.homeExists(player.getName().toLowerCase(), name)) {
                if (this.playerCanSet(player)) {
                    // Player has warps, but not with the given name.
                    Home warp = new Home(player, name);
                    homeList.get(player.getName().toLowerCase()).put(name, warp);
                    WarpDataSource.addWarp(warp, log);
                    setHomeCoolDown.addPlayer(player, plugin);
                    
                    if (this.checkSetCosts(player)) {
                        return ExitStatus.SUCCESS;
                    } else {
                        return ExitStatus.NOT_ENOUGH_MONEY;
                    }
                } else {
                    return ExitStatus.AT_LIMIT;
                }
            } else {
                // Player has a warp with the given name.
                Home warp = homeList.get(player.getName().toLowerCase()).get(name);
                warp.setLocation(player.getLocation());
                WarpDataSource.moveWarp(warp, log);
                setHomeCoolDown.addPlayer(player, plugin);

                if (this.checkSetCosts(player)) {
                    return ExitStatus.SUCCESS_MOVED;
                } else {
                    return ExitStatus.NOT_ENOUGH_MONEY;
                }
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

    public ExitStatus warpTo(String target, Player player, Plugin plugin) {
        return this.warpTo(player.getName(), target, player, plugin);
    }
    
    public boolean checkWarpCosts(Player player) {
    	if ((HomeConfig.enableEcon) && (this.plugin.economy != null) && (! SuperPermsManager.hasPermission(player, SuperPermsManager.bypassEcon))) {
            if (HomeConfig.warpCost > 0) {
                EconomyResponse resp = this.plugin.economy.withdrawPlayer(player.getName(), HomeConfig.warpCost);
                if (resp.transactionSuccess()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
    	} else {
            return true;
        }
    }

    public boolean checkSetCosts(Player player) {
    	if ((HomeConfig.enableEcon) && (this.plugin.economy != null) && (! SuperPermsManager.hasPermission(player, SuperPermsManager.bypassEcon))) {
            if (HomeConfig.setCost > 0) {
                EconomyResponse resp = this.plugin.economy.withdrawPlayer(player.getName(), HomeConfig.setCost);
                if (resp.transactionSuccess()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
    	} else {
            return true;
        }
    }

    public ExitStatus warpTo(String targetOwner, String target, Player player, Plugin plugin) {
        MatchList matches = this.getMatches(target, player, targetOwner);
        target = matches.getMatch(target);
        if (this.homeExists(targetOwner, target)) {
            Home warp = homeList.get(targetOwner.toLowerCase()).get(target);
            if (warp.playerCanWarp(player)) {
                if (homeCoolDown.playerHasCooled(player)) {
                	if (checkWarpCosts(player)) {
                            WarmUp.addPlayer(player, warp, plugin);
                            homeCoolDown.addPlayer(player, plugin);
                            return ExitStatus.SUCCESS;
                	} else {
                            return ExitStatus.NOT_ENOUGH_MONEY;
                	}
                } else {
                    return ExitStatus.NEED_COOLDOWN;
                }
            } else {
                return ExitStatus.NOT_PERMITTED;
            }
        } else {
            return ExitStatus.NOT_EXISTS;
        }
    }

    public ExitStatus warpToExact(String targetOwner, String target, Player player, Plugin plugin) {
        if (this.homeExists(targetOwner, target)) {
            Home warp = homeList.get(targetOwner.toLowerCase()).get(target);
            if (warp.playerCanWarp(player)) {
                if (homeCoolDown.playerHasCooled(player)) {
                    WarmUp.addPlayer(player, warp, plugin);
                    homeCoolDown.addPlayer(player, plugin);
                    return ExitStatus.SUCCESS;
                } else {
                    return ExitStatus.NEED_COOLDOWN;
                }
            } else {
                return ExitStatus.NOT_PERMITTED;
            }
        } else {
            return ExitStatus.NOT_EXISTS;
        }
    }

    public ExitStatus sendPlayerHome(Player player, Plugin plugin) {
        if (playerHasDefaultHome(player.getName())) {
            if (homeCoolDown.playerHasCooled(player)) {
                WarmUp.addPlayer(player, homeList.get(player.getName().toLowerCase()).get(uHome.DEFAULT_HOME), plugin);
                homeCoolDown.addPlayer(player, plugin);
                return ExitStatus.SUCCESS;
            } else {
                return ExitStatus.NEED_COOLDOWN;
            }
        } else {
            return ExitStatus.NOT_EXISTS;
        }
    }

    public Location getHomeLocation(String owner, String name) {
        return this.homeList.get(owner.toLowerCase()).get(name).getLocation(this.server);
    }

    public boolean playerHasDefaultHome(String player) {
        return this.homeExists(player.toLowerCase(), uHome.DEFAULT_HOME);
    }

    public boolean playerHasHomes(String player) {
        return this.homeList.containsKey(player.toLowerCase()) && this.getPlayerHomeCount(player) > 0;
    }

    public boolean playerCanWarp(Player player, String owner, String name) {
        return homeList.get(owner.toLowerCase()).get(name).playerCanWarp(player);
    }

    public ExitStatus invitePlayer(String owner, String player, String name) {
        Home inviteTo = homeList.get(owner.toLowerCase()).get(name);
        InviteStatus result = inviteTo.addInvitee(player);
        
        switch (result) {
            case SUCCESS:
                WarpDataSource.addInvite(inviteTo.index, player, this.plugin.getLogger());

                if (!inviteList.containsKey(player.toLowerCase())) {
                    inviteList.put(player.toLowerCase(), new HashSet<Home>());
                }

                inviteList.get(player.toLowerCase()).add(inviteTo);

                Player invitee = server.getPlayerExact(player);
                if (invitee != null) {
                    invitee.sendMessage(LocaleManager.getString("own.invite.notify", null, inviteTo));
                }

                return ExitStatus.SUCCESS;
            case AT_LIMIT:
                return ExitStatus.AT_LIMIT;
            default:
                return ExitStatus.DUPLICATE;
        }
    }

    public boolean uninvitePlayer(String owner, String player, String name) {
        Home inviteHome = homeList.get(owner.toLowerCase()).get(name);
        if (inviteHome.removeInvitee(player)) {
            WarpDataSource.deleteInvite(inviteHome.index, player, this.plugin.getLogger());
        }

        if (inviteList.containsKey(player.toLowerCase()) && inviteList.get(player.toLowerCase()).remove(inviteHome)) {
            Player invitee = server.getPlayerExact(player);
            if (invitee != null) {
                invitee.sendMessage(LocaleManager.getString("own.uninvite.notify", null, inviteHome));
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
        int playerMaxWarps = SuperPermsManager.getHomeLimit(player);

        return ((playerMaxWarps < 0) || (playerWarps < playerMaxWarps));
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

        for (String currName : homeList.get(owner.toLowerCase()).keySet()) {
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
    
    public String guessHomeName(Player player, String owner, String name) {
        MatchList matches = this.getMatches(name, player, owner);
        return matches.getMatch(name);
    }
    
    public Home getNamedHome(String owner, String name) {
        return homeList.get(owner.toLowerCase()).get(name);
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
        DUPLICATE,
        UNKNOWN,
        NOT_ENOUGH_MONEY;
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
