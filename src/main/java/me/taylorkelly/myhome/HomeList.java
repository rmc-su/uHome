package me.taylorkelly.myhome;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import me.taylorkelly.myhome.timers.HomeCoolDown;
import me.taylorkelly.myhome.timers.WarmUp;
import me.taylorkelly.myhome.timers.SetHomeCoolDown;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HomeList {
	private HashMap<String, Home> homeList;
	private Server server;
	private final HomeCoolDown homeCoolDown = HomeCoolDown.getInstance();
	private final SetHomeCoolDown setHomeCoolDown = SetHomeCoolDown.getInstance();

	public HomeList(Server server) {
		WarpDataSource.initialize();
		homeList = WarpDataSource.getMap();
		this.server = server;
	}

	public void addHome(Player player, Plugin plugin) {
		int cost = 0;
		if (!(setHomeCoolDown.playerHasCooled(player))) {
			player.sendMessage(ChatColor.RED + "You need to wait " +
					setHomeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
					setHomeCoolDown.getTimer(player) + " second cooldown before you can change your home.");
		} else if (HomeSettings.eConomyEnabled && !HomePermissions.setHomeFree(player) ) {
			if (HomeSettings.costByPerms) {
				cost = HomePermissions.integer(player, "myhome.costs.sethome", HomeSettings.setHomeCost);
				if(HomeSettings.additionalCosts) {
					cost += HomeSettings.setHomeCost;
				}
			} else {
				cost = HomeSettings.setHomeCost;
			}
			if (HomeEconomy.chargePlayer(player.getName(), cost)) {
				if (homeList.containsKey(player.getName())) {
					Home warp = homeList.get(player.getName());
					warp.setLocation(player.getLocation());
					WarpDataSource.moveWarp(warp);
					player.sendMessage(ChatColor.AQUA + "Welcome to your new home :).");
					setHomeCoolDown.addPlayer(player, plugin);
				} else {
					Home warp = new Home(player);
					homeList.put(player.getName(), warp);
					WarpDataSource.addWarp(warp);
					player.sendMessage(ChatColor.AQUA + "Successfully created your home");
					setHomeCoolDown.addPlayer(player, plugin);
					if (HomePermissions.invite(player)) {
						player.sendMessage("If you'd like to invite friends to it,");
						player.sendMessage("Use: " + ChatColor.RED + "/home invite <player>");
					}
				}
				player.sendMessage(HomeEconomy.formattedBalance(cost) + " has been deducted from your account.");
				MyHome.setCompass(player, player.getLocation());
			} else {
				player.sendMessage("Setting a home requires: " + HomeEconomy.formattedBalance(cost) + ". You have " + HomeEconomy.balance(player.getName()));
				return;
			}   
		} else {
			if (homeList.containsKey(player.getName())) {
				Home warp = homeList.get(player.getName());
				warp.setLocation(player.getLocation());
				WarpDataSource.moveWarp(warp);
				player.sendMessage(ChatColor.AQUA + "Welcome to your new home :).");
				setHomeCoolDown.addPlayer(player, plugin);
			} else {
				Home warp = new Home(player);
				homeList.put(player.getName(), warp);
				WarpDataSource.addWarp(warp);
				player.sendMessage(ChatColor.AQUA + "Successfully created your home");
				setHomeCoolDown.addPlayer(player, plugin);
				if (HomePermissions.invite(player)) {
					player.sendMessage("If you'd like to invite friends to it,");
					player.sendMessage("Use: " + ChatColor.RED + "/home invite <player>");
				}
			}
			MyHome.setCompass(player, player.getLocation());
		}

	}

	public void blindAdd(Home warp) {
		homeList.put(warp.name, warp);
	}

	public void warpTo(String name, Player player, Plugin plugin) {
		MatchList matches = this.getMatches(name, player);
		name = matches.getMatch(name);
		int cost = 0;
		if (homeList.containsKey(name)) {
			Home warp = homeList.get(name);
			if (warp.playerCanWarp(player) || HomePermissions.adminAnyHome(player)) {
				if (homeCoolDown.playerHasCooled(player)) {
					if (HomeSettings.eConomyEnabled && !HomePermissions.homeFree(player)) {
						if (HomeSettings.costByPerms) {
							cost = HomePermissions.integer(player, "myhome.costs.home", HomeSettings.homeCost);
							if(HomeSettings.additionalCosts) {
								cost += HomeSettings.homeCost;
							}
						} else {
							cost = HomeSettings.homeCost;
						}
						if (HomeEconomy.chargePlayer(player.getName(), cost)) {
							player.sendMessage(HomeEconomy.formattedBalance(cost) + " has been deducted from your account.");
							WarmUp.addPlayer(player, warp, plugin);
							homeCoolDown.addPlayer(player, plugin);
						} else {
							player.sendMessage("Warping home requires: " + HomeEconomy.formattedBalance(cost) + ". You have " + HomeEconomy.balance(player.getName()));
						}
					} else {
						WarmUp.addPlayer(player, warp, plugin);
						homeCoolDown.addPlayer(player, plugin);
					}
				} else {
					player.sendMessage(ChatColor.RED + "You need to wait " +
							homeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
							homeCoolDown.getTimer(player) + " second cooldown.");
				}
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to warp to " + name + "'s home");
			}
		} else {
			player.sendMessage(ChatColor.RED + name + " doesn't have a home :(");
		}
	}

	public void sendPlayerHome(Player player, Plugin plugin) {
		int cost = 0;
		if (homeList.containsKey(player.getName())) {
			if (homeCoolDown.playerHasCooled(player)) {
				if (HomeSettings.eConomyEnabled && !HomePermissions.homeFree(player) ) {
					if (HomeSettings.costByPerms) {
						cost = HomePermissions.integer(player, "myhome.costs.home", HomeSettings.homeCost);
						if(HomeSettings.additionalCosts) {
							cost += HomeSettings.homeCost;
						}
					} else {
						cost = HomeSettings.homeCost;
					}
					if (HomeEconomy.chargePlayer(player.getName(), cost)) {
						player.sendMessage(HomeEconomy.formattedBalance(cost) + " has been deducted from your account.");
						WarmUp.addPlayer(player, homeList.get(player.getName()), plugin);
						homeCoolDown.addPlayer(player, plugin);
					} else {
						player.sendMessage("Warping home requires: " + HomeEconomy.formattedBalance(cost) + ". You have " + HomeEconomy.balance(player.getName()));
					}
				} else {
					WarmUp.addPlayer(player, homeList.get(player.getName()), plugin);
					homeCoolDown.addPlayer(player, plugin);
				}
			} else {
				player.sendMessage(ChatColor.RED + "You need to wait " +
						homeCoolDown.estimateTimeLeft(player) + " more seconds of the " +
						homeCoolDown.getTimer(player) + " second cooldown.");
			}
		}
	}

	public boolean playerHasHome(Player player) {
		return homeList.containsKey(player.getName());
	}

	public void deleteHome(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			homeList.remove(player.getName());
			WarpDataSource.deleteWarp(warp);
			player.sendMessage(ChatColor.AQUA + "You have deleted your home");
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to delete :(");
		}
	}

	public void clearHome(String srchplayer, Player player) {
		if (homeList.containsKey(srchplayer)) {
			Home warp = homeList.get(srchplayer);
			homeList.remove(srchplayer);
			WarpDataSource.deleteWarp(warp);
			player.sendMessage(ChatColor.AQUA + "You have deleted "+srchplayer+"'s home");
		} else {
			player.sendMessage(ChatColor.RED + "There is no home for " + srchplayer);
		}
	}

	public void privatize(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			warp.publicAll = 0;
			WarpDataSource.publicizeWarp(warp, 0);
			player.sendMessage(ChatColor.AQUA + "You have privatized your home");
			if (HomePermissions.invite(player)) {
				player.sendMessage("If you'd like to invite others to it,");
				player.sendMessage("Use: " + ChatColor.RED + "/home invite <player>");
			}
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to privatize :(");
		}
	}

	public void publicize(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			warp.publicAll = 1;
			WarpDataSource.publicizeWarp(warp, 1);
			player.sendMessage(ChatColor.AQUA + "You have publicized your home.");
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to publicize :(");
		}
	}

	public void invite(Player player, String inviteeName) {
		if (homeList.containsKey(player.getName())) {
			// TODO match player stuff
			Home warp = homeList.get(player.getName());
			if (warp.playerIsInvited(inviteeName)) {
				player.sendMessage(ChatColor.RED + inviteeName + " is already invited to your home.");
			} else if (warp.playerIsCreator(inviteeName)) {
				player.sendMessage(ChatColor.RED + "This is your home!");
			} else {
				warp.invite(inviteeName);
				WarpDataSource.updatePermissions(warp);
				player.sendMessage(ChatColor.AQUA + "You have invited " + inviteeName + " to your home");
				if (warp.publicAll == 1) {
					player.sendMessage(ChatColor.RED + "But your home is still public!");
				}
				for (Player match : server.getOnlinePlayers()) {
					if (match.getName().equalsIgnoreCase(inviteeName)) {
						match.sendMessage(ChatColor.AQUA + "You've been invited to " + player.getName() + "'s home");
						match.sendMessage("Use: " + ChatColor.RED + "/home " + player.getName() + ChatColor.WHITE + " to warp to it.");
					}
				}	
			}
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to invite people to :(");
		}
	}

	public void uninvite(Player player, String inviteeName) {
		if (homeList.containsKey(player.getName())) {
			// TODO player match stuff
			Home warp = homeList.get(player.getName());
			if (!warp.playerIsInvited(inviteeName)) {
				player.sendMessage(ChatColor.RED + inviteeName + " is not invited to your home.");
			} else if (warp.playerIsCreator(inviteeName)) {
				player.sendMessage(ChatColor.RED + "Why would you want to uninivite yourself?");
			} else {
				warp.uninvite(inviteeName);
				WarpDataSource.updatePermissions(warp);
				player.sendMessage(ChatColor.AQUA + "You have uninvited " + inviteeName + " from your home");
				if (warp.publicAll == 1) {
					player.sendMessage(ChatColor.RED + "But your home is still public.");
				}
				for (Player match : server.getOnlinePlayers()) {
					if (match.getName().equalsIgnoreCase(inviteeName)) {
						match.sendMessage(ChatColor.AQUA + "You've been invited to " + player.getName() + "'s home");
						match.sendMessage("Use: " + ChatColor.RED + "/home " + player.getName() + ChatColor.WHITE + " to warp to it.");
					}
				}	
			}
		} else {
			player.sendMessage(ChatColor.RED + "You have no home to uninvite people from :(");
		}
	}

	public boolean homeExists(String name) {
		return homeList.containsKey(name);
	}

	public void list(Player player) {
		ArrayList<Home> results = homesInvitedTo(player);

		if (results.size() == 0) {
			player.sendMessage(ChatColor.RED + "You are invited to no one's home.");
		} else {
			player.sendMessage(ChatColor.AQUA + "You are invited to the homes of:");
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
	}

	public void listall(Player player) {
		ArrayList<Home> results = allHomes();

		if (results.size() == 0) {
			player.sendMessage(ChatColor.RED + "There are no homes.");
		} else {
			player.sendMessage(ChatColor.AQUA + "There are the following Homes:");
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
	}

	public void ilist(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			if (warp.permissions.size() == 0) {
				player.sendMessage(ChatColor.AQUA + "No one is invited to your house");
			} else {
				player.sendMessage(ChatColor.AQUA + warp.invitees() + " invited to your house");
			}
		} else {
			player.sendMessage(ChatColor.RED + "You have no home :(");
		}
	}

	private ArrayList<Home> allHomes() {
		ArrayList<Home> results = new ArrayList<Home>();
		for (Home home : homeList.values()) {
			results.add(home);
		}
		return results;
	}

	private ArrayList<Home> homesInvitedTo(Player player) {
		ArrayList<Home> results = new ArrayList<Home>();
		for (Home home : homeList.values()) {
			if (home.playerCanWarp(player) && !home.playerIsCreator(player.getName())) {
				results.add(home);
			}
		}
		return results;
	}

	public void orientPlayer(Player player) {
		if (playerHasHome(player)) {
			Home home = homeList.get(player.getName());
			World world = player.getWorld();
			Location location = new Location(world, home.x, home.y, home.z);
			MyHome.setCompass(player, location);
		}
	}

	public MatchList getMatches(String name, Player player) {
		ArrayList<Home> exactMatches = new ArrayList<Home>();
		ArrayList<Home> matches = new ArrayList<Home>();

		List<String> names = new ArrayList<String>(homeList.keySet());
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.SECONDARY);
		Collections.sort(names, collator);

		for (int i = 0; i < names.size(); i++) {
			String currName = names.get(i);
			Home warp = homeList.get(currName);
			if (warp.playerCanWarp(player) || HomePermissions.adminAnyHome(player)) {
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
		return homeList.get(player.getName());
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
		if (exactMatches.size() == 0 && matches.size() == 1) {
			return matches.get(0).name;
		}
		return name;
	}
}
