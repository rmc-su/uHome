package uk.co.ks07.uhome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import uk.co.ks07.uhome.timers.HomeCoolDown;
import uk.co.ks07.uhome.timers.WarmUp;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HomeList {
	private HashMap<String, HashMap<String, Home>> homeList;
	private Server server;
	private final HomeCoolDown homeCoolDown = HomeCoolDown.getInstance();
	private final SetHomeCoolDown setHomeCoolDown = SetHomeCoolDown.getInstance();

	public HomeList(Server server, boolean needImport) {
		WarpDataSource.initialize(needImport);
		homeList = WarpDataSource.getMap();
		this.server = server;
	}

	public void addHome(Player player, Plugin plugin) {
		this.addHome(player, plugin, "home");
	}

	public void addHome(Player player, Plugin plugin, String name) {
                if (!(setHomeCoolDown.playerHasCooled(player))) {
                        player.sendMessage(ChatColor.RED + "You need to wait " +
                                        setHomeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
                                        setHomeCoolDown.getTimer(player) + " second cooldown before you can edit your homes.");
                } else {
                        if (!homeList.containsKey(player.getName())) {
                                // Player has no warps.
                                HashMap<String, Home> warps = new HashMap<String, Home>();
                                Home warp = new Home(player, name);
                                warps.put(name, warp);
                                homeList.put(player.getName(), warps);
                                WarpDataSource.addWarp(warp);
                                player.sendMessage(ChatColor.AQUA + "Welcome to your first home!");
                                setHomeCoolDown.addPlayer(player, plugin);
                        } else if (!this.homeExists(player.getName(), name)) {
                                if (this.playerCanSet(player)) {
                                        // Player has warps, but not with the given name.
                                        Home warp = new Home(player, name);
                                        homeList.get(player.getName()).put(name, warp);
                                        WarpDataSource.addWarp(warp);
                                        player.sendMessage(ChatColor.AQUA + "Welcome to your new home :).");
                                        setHomeCoolDown.addPlayer(player, plugin);
                                } else {
                                        // Player cannot set a new warp as they are at their warp limit.
                                        player.sendMessage(ChatColor.RED + "You have too many homes! You must delete one before you can set a new home!");
                                }
                        } else {
                                // Player has a warp with the given name.
                                Home warp = homeList.get(player.getName()).get(name);
                                warp.setLocation(player.getLocation());
                                WarpDataSource.moveWarp(warp);
                                player.sendMessage(ChatColor.AQUA + "Succesfully moved your home.");
                                setHomeCoolDown.addPlayer(player, plugin);
                        }
                }
	}

	public void adminAddHome(Player player, String owner, String name) {
                // Adds a home ignoring limits, ownership and cooldown.
		if (!homeList.containsKey(owner)) {
                        // Player has no warps.
                        HashMap<String, Home> warps = new HashMap<String, Home>();
                        Home warp = new Home(owner, player.getLocation(), name);
                        warps.put(name, warp);
                        homeList.put(owner, warps);
                        WarpDataSource.addWarp(warp);
                        player.sendMessage(ChatColor.AQUA + "Created first home for " + owner);
                } else if (!this.homeExists(owner, name)) {
                        // Player has warps, but not with the given name.
                        Home warp = new Home(owner, player.getLocation(), name);
                        homeList.get(owner).put(name, warp);
                        WarpDataSource.addWarp(warp);
                        player.sendMessage(ChatColor.AQUA + "Created new home for " + owner);
                } else {
                        // Player has a warp with the given name.
                        Home warp = homeList.get(owner).get(name);
                        warp.setLocation(player.getLocation());
                        WarpDataSource.moveWarp(warp);
                        player.sendMessage(ChatColor.AQUA + "Succesfully moved home for " + owner);
                }
	}

	public void warpTo(Player player, Plugin plugin) {
		this.warpTo(player.getName(), "home", player, plugin);
	}

	public void warpTo(String target, Player player, Plugin plugin) {
		this.warpTo(player.getName(), target, player, plugin);
	}

	public void warpTo(String targetOwner, String target, Player player, Plugin plugin) {
		MatchList matches = this.getMatches(target, player);
		target = matches.getMatch(target);
		if (homeList.get(targetOwner).containsKey(target)) {
			Home warp = homeList.get(targetOwner).get(target);
			if (warp.playerCanWarp(player)) {
				if (homeCoolDown.playerHasCooled(player)) {
                                        WarmUp.addPlayer(player, warp, plugin);
                                        homeCoolDown.addPlayer(player, plugin);
				} else {
					player.sendMessage(ChatColor.RED + "You need to wait " +
							homeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
							homeCoolDown.getTimer(player) + " second cooldown.");
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
					WarmUp.addPlayer(player, homeList.get(player.getName()).get("home"), plugin);
					homeCoolDown.addPlayer(player, plugin);
			} else {
				player.sendMessage(ChatColor.RED + "You need to wait " +
						homeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
						homeCoolDown.getTimer(player) + " second cooldown.");
			}
		}
	}

        public Location getHomeLocation(String owner, String name) {
                return this.homeList.get(owner).get(name).getLocation;
        }

	public boolean playerHasHome(Player player) {
		return this.homeExists(player.getName(), "home");
	}

        public boolean playerHasHomes(Player player) {
                return this.homeList.containsKey(player.getName());
        }

        public boolean playerCanWarp(Player player, String owner, String name) {
                return homeList.get(owner).get(name).playerCanWarp(player);
        }
        
        public int getPlayerWarpNo(String owner) {
                return homeList.get(owner).size();
        }

        public boolean playerCanSet(Player player) {
                if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassLimit)) {
                    return true;
                }

                int playerWarps = homeList.get(player.getName()).size();
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
                
                return (playerWarps < playerMaxWarps);
        }

	public void deleteHome(Player player) {
		if (this.homeExists(player.getName(), "home")) {
			Home warp = homeList.get(player.getName()).get("home");
			homeList.get(player.getName()).remove("home");
			WarpDataSource.deleteWarp(warp);
			player.sendMessage(ChatColor.AQUA + "You have deleted your home");
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to delete :(");
		}
	}

	public void deleteHome(Player owner, String name) {
		if (this.homeExists(owner.getName(), name)) {
			Home warp = homeList.get(owner.getName()).get(name);
			homeList.get(owner.getName()).remove(name);
			WarpDataSource.deleteWarp(warp);
			owner.sendMessage(ChatColor.AQUA + "You have deleted your home '" + name + "'.");
		} else {
			owner.sendMessage(ChatColor.RED + "You don't have a home called '" + name + "'!");
		}
	}

	public void deleteHome(String owner, String name, Player player) {
		if (this.homeExists(owner, name)) {
			Home warp = homeList.get(owner).get(name);
			homeList.get(owner).remove(name);
			WarpDataSource.deleteWarp(warp);
			player.sendMessage(ChatColor.AQUA + "You have deleted "+owner+"'s home '"+name+"'.");
		} else {
			player.sendMessage(ChatColor.RED + "There is no home '"+name+"' for " + owner + "!");
		}
	}

        public void deleteHome(String owner, String name) {
                if (this.homeExists(owner, name)) {
			Home warp = homeList.get(owner).get(name);
			homeList.get(owner).remove(name);
			WarpDataSource.deleteWarp(warp);
                }
        }

	public boolean homeExists(String owner, String name) {
                if (homeList.containsKey(owner)) {
                        return homeList.get(owner).containsKey(name);
                } else {
                        return false;
                }
	}

	public boolean hasHomes(String player) {
                return homeList.containsKey(player);
	}

	public void list(Player player) {
            if (hasHomes(player.getName())) {
		ArrayList<Home> results = new ArrayList(homeList.get(player.getName()).values());

		if (results.isEmpty()) {
			player.sendMessage(ChatColor.RED + "You have no homes!");
		} else {
			player.sendMessage(ChatColor.AQUA + "You have the following homes:");
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
            } else {
                    player.sendMessage(ChatColor.RED + "You have no homes!");
            }
	}

	public void listOther(Player player, String owner) {
		ArrayList<Home> results = new ArrayList(homeList.get(owner).values());

		if (results.isEmpty()) {
			player.sendMessage(ChatColor.RED + "That player has no homes.");
		} else {
			player.sendMessage(ChatColor.AQUA + "That player has the following homes:");
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
	}

	public String getPlayerList(String owner) {
		ArrayList<Home> results = new ArrayList(homeList.get(owner).values());
                
                if (results.isEmpty()) {
                    return null;
                } else {
                    String ret = results.toString().replace("[", "").replace("]", "");
                    return ret;
                }
	}
        
        public int getTotalWarps() {
                int ret = 0;
                for (HashMap<String, Home> pList : homeList.values()) {
                        ret += pList.size();
                }
                return ret;
        }

	private MatchList getMatches(String name, Player player) {
		ArrayList<Home> exactMatches = new ArrayList<Home>();
		ArrayList<Home> matches = new ArrayList<Home>();

		List<String> names = new ArrayList<String>(homeList.get(player.getName()).keySet());
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.SECONDARY);
		Collections.sort(names, collator);

		for (int i = 0; i < names.size(); i++) {
			String currName = names.get(i);
			Home warp = homeList.get(player.getName()).get(currName);
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

	public Home getHomeFor(Player player) {
		return homeList.get(player.getName()).get("home");
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