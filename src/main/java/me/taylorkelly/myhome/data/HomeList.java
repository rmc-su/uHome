package me.taylorkelly.myhome.data;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.locale.LocaleManager;
import me.taylorkelly.myhome.permissions.HomePermissions;
import me.taylorkelly.myhome.sql.WarpDataSource;
import me.taylorkelly.myhome.timers.HomeCoolDown;
import me.taylorkelly.myhome.timers.WarmUp;
import me.taylorkelly.myhome.timers.SetHomeCoolDown;
import me.taylorkelly.myhome.utils.HomeLogger;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HomeList {
	private HashMap<String, Home> homeList;
	private Map<String, String> localedata = new HashMap<String, String>();
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
			localedata.put("TIMER.SHCDREM", Integer.toString(setHomeCoolDown.estimateTimeLeft(player)));
			localedata.put("TIMER.SHCDT", Integer.toString(setHomeCoolDown.estimateTimeLeft(player)));
			player.sendMessage(LocaleManager.getString("timer.sethome.cooldown", localedata));
			localedata.clear();
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
					player.sendMessage(LocaleManager.getString("home.move"));
					setHomeCoolDown.addPlayer(player, plugin);
				} else {
					Home warp = new Home(player);
					homeList.put(player.getName(), warp);
					WarpDataSource.addWarp(warp);
					player.sendMessage(LocaleManager.getString("home.create"));
					setHomeCoolDown.addPlayer(player, plugin);
					if (HomePermissions.invite(player)) {
						player.sendMessage(LocaleManager.getString("home.invite.others"));
						player.sendMessage(LocaleManager.getString("usage.invite"));
					}
				}
				localedata.put("ECO.SHCOST", HomeEconomy.formattedBalance(cost));
				player.sendMessage(LocaleManager.getString("eco.deduct", localedata));
				localedata.clear();	
				pointCompass(player, player.getLocation());
			} else {
				localedata.put("ECO.SHCOST", HomeEconomy.formattedBalance(cost));
				localedata.put("ECO.PLAYERBAL", Double.toString(HomeEconomy.balance(player.getName())));
				player.sendMessage(LocaleManager.getString("eco.shnotenough", localedata));
				localedata.clear();
				return;
			}   
		} else {
			if (homeList.containsKey(player.getName())) {
				Home warp = homeList.get(player.getName());
				warp.setLocation(player.getLocation());
				WarpDataSource.moveWarp(warp);
				player.sendMessage(LocaleManager.getString("home.move"));
				setHomeCoolDown.addPlayer(player, plugin);
			} else {
				Home warp = new Home(player);
				homeList.put(player.getName(), warp);
				WarpDataSource.addWarp(warp);
				player.sendMessage(LocaleManager.getString("home.create"));
				setHomeCoolDown.addPlayer(player, plugin);
				if (HomePermissions.invite(player)) {
					player.sendMessage(LocaleManager.getString("home.invite.others"));
					player.sendMessage(LocaleManager.getString("usage.invite"));
				}
			}
			pointCompass(player, player.getLocation());
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
							localedata.put("ECO.SHCOST", HomeEconomy.formattedBalance(cost));
							player.sendMessage(LocaleManager.getString("eco.deduct", localedata));
							localedata.clear();	
							WarmUp.addPlayer(player, warp, plugin);
							homeCoolDown.addPlayer(player, plugin);
						} else {
							localedata.put("ECO.HOMECOST", HomeEconomy.formattedBalance(cost));
							localedata.put("ECO.PLAYERBAL", Double.toString(HomeEconomy.balance(player.getName())));
							player.sendMessage(LocaleManager.getString("eco.homenotenough", localedata));
							localedata.clear();
						}
					} else {
						WarmUp.addPlayer(player, warp, plugin);
						homeCoolDown.addPlayer(player, plugin);
					}
				} else {
					localedata.put("TIMER.HOMECDREM", Integer.toString(homeCoolDown.estimateTimeLeft(player)));
					localedata.put("TIMER.HOMECDT", Integer.toString(homeCoolDown.estimateTimeLeft(player)));
					player.sendMessage(LocaleManager.getString("timer.home.cooldown", localedata));
					localedata.clear();
				}
			} else {
				localedata.put("TARGET", name);
				player.sendMessage(LocaleManager.getString("error.nopermission", localedata));
				localedata.clear();
			}
		} else {
			localedata.put("TARGET", name);
			player.sendMessage(LocaleManager.getString("error.nosuchhome", localedata));
			localedata.clear();
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
						localedata.put("ECO.SHCOST", HomeEconomy.formattedBalance(cost));
						player.sendMessage(LocaleManager.getString("eco.deduct", localedata));
						localedata.clear();	
						WarmUp.addPlayer(player, homeList.get(player.getName()), plugin);
						homeCoolDown.addPlayer(player, plugin);
					} else {
						localedata.put("ECO.HOMECOST", HomeEconomy.formattedBalance(cost));
						localedata.put("ECO.PLAYERBAL", Double.toString(HomeEconomy.balance(player.getName())));
						player.sendMessage(LocaleManager.getString("eco.homenotenough", localedata));
						localedata.clear();
					}
				} else {
					WarmUp.addPlayer(player, homeList.get(player.getName()), plugin);
					homeCoolDown.addPlayer(player, plugin);
				}
			} else {
				localedata.put("TIMER.HOMECDREM", Integer.toString(homeCoolDown.estimateTimeLeft(player)));
				localedata.put("TIMER.HOMECDT", Integer.toString(homeCoolDown.estimateTimeLeft(player)));
				player.sendMessage(LocaleManager.getString("timer.home.cooldown", localedata));
				localedata.clear();
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
			player.sendMessage(LocaleManager.getString("home.delete.done"));
		} else {
			player.sendMessage(LocaleManager.getString("home.delete.fail"));
		}
	}

	public void clearHome(String srchplayer, Player player) {
		if (homeList.containsKey(srchplayer)) {
			Home warp = homeList.get(srchplayer);
			homeList.remove(srchplayer);
			WarpDataSource.deleteWarp(warp);
			
			localedata.put("TARGET", srchplayer);
			player.sendMessage(LocaleManager.getString("admin.delete.done", localedata));
			localedata.clear();
		} else {
			localedata.put("TARGET", srchplayer);
			player.sendMessage(LocaleManager.getString("admin.delete.fail", localedata));
			localedata.clear();
		}
	}
	
	public void consoleClearHome(String srchplayer) {
		if (homeList.containsKey(srchplayer)) {
			Home warp = homeList.get(srchplayer);
			homeList.remove(srchplayer);
			WarpDataSource.deleteWarp(warp);
			localedata.put("TARGET", srchplayer);
			HomeLogger.info(LocaleManager.getString("admin.delete.done", localedata, true));
			localedata.clear();
		} else {
			localedata.put("TARGET", srchplayer);
			HomeLogger.info(LocaleManager.getString("admin.delete.fail", localedata, true));
			localedata.clear();
		}
	}

	public void privatize(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			warp.publicAll = 0;
			WarpDataSource.publicizeWarp(warp, 0);
			//player.sendMessage(ChatColor.AQUA + "You have privatized your home");
			player.sendMessage(LocaleManager.getString("home.privatize"));
			if (HomePermissions.invite(player)) {
				player.sendMessage(LocaleManager.getString("home.invite.others"));
				player.sendMessage(LocaleManager.getString("usage.invite"));
			}
		} else {
			player.sendMessage(LocaleManager.getString("home.privatize.fail"));
		}
	}

	public void publicize(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			warp.publicAll = 1;
			WarpDataSource.publicizeWarp(warp, 1);
			player.sendMessage(LocaleManager.getString("home.publicize"));
		} else {
			player.sendMessage(LocaleManager.getString("home.publicize.fail"));
		}
	}

	public void invite(Player player, String inviteeName) {
		if (homeList.containsKey(player.getName())) {
			// TODO match player stuff
			Home warp = homeList.get(player.getName());
			if (warp.playerIsInvited(inviteeName)) {
				localedata.put("TARGET", inviteeName);
				player.sendMessage(LocaleManager.getString("home.invite.already", localedata));
				localedata.clear();
			} else if (warp.playerIsCreator(inviteeName)) {
				player.sendMessage(LocaleManager.getString("home.invite.yours"));
			} else {
				warp.invite(inviteeName);
				WarpDataSource.updatePermissions(warp);
				localedata.put("TARGET", inviteeName);
				player.sendMessage(LocaleManager.getString("home.invite.success", localedata));
				localedata.clear();
				if (warp.publicAll == 1) {
					player.sendMessage(LocaleManager.getString("home.stillpublic"));
				}
				for (Player match : server.getOnlinePlayers()) {
					if (match.getName().equalsIgnoreCase(inviteeName)) {
						localedata.put("SOURCE", player.getName());
						match.sendMessage(LocaleManager.getString("home.invited", localedata));
						match.sendMessage(LocaleManager.getString("usage.invited", localedata));
						localedata.clear();		
					}
				}	
			}
		} else {
			player.sendMessage(LocaleManager.getString("home.invite.fail"));
		}
	}

	public void uninvite(Player player, String inviteeName) {
		if (homeList.containsKey(player.getName())) {
			// TODO player match stuff
			Home warp = homeList.get(player.getName());
			if (!warp.playerIsInvited(inviteeName)) {
				localedata.put("TARGET", inviteeName);
				player.sendMessage(LocaleManager.getString("home.uninvite.notinvited", localedata));
				localedata.clear();
			} else if (warp.playerIsCreator(inviteeName)) {
				player.sendMessage(LocaleManager.getString("home.uninvite.yours"));
			} else {
				warp.uninvite(inviteeName);
				WarpDataSource.updatePermissions(warp);
				localedata.put("TARGET", inviteeName);
				player.sendMessage(LocaleManager.getString("home.uninvite.success", localedata));
				localedata.clear();	
				if (warp.publicAll == 1) {
					player.sendMessage(LocaleManager.getString("home.stillpublic"));
				}
				for (Player match : server.getOnlinePlayers()) {
					if (match.getName().equalsIgnoreCase(inviteeName)) {
						localedata.put("SOURCE", player.getName());
						match.sendMessage(LocaleManager.getString("home.uninvited", localedata));
						localedata.clear();	
					}
				}	
			}
		} else {
			player.sendMessage(LocaleManager.getString("home.uninvite.fail"));
		}
	}

	public boolean homeExists(String name) {
		return homeList.containsKey(name);
	}

	public void list(Player player) {
		ArrayList<Home> results = homesInvitedTo(player);

		if (results.size() == 0) {
			player.sendMessage(LocaleManager.getString("home.list.none"));
		} else {
			player.sendMessage(LocaleManager.getString("home.list"));
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
	}

	public void listall(Player player) {
		ArrayList<Home> results = allHomes();

		if (results.size() == 0) {
			player.sendMessage(LocaleManager.getString("home.listall.none"));
		} else {
			player.sendMessage(LocaleManager.getString("home.listall"));
			player.sendMessage(results.toString().replace("[", "").replace("]", ""));
		}
	}

	public void ilist(Player player) {
		if (homeList.containsKey(player.getName())) {
			Home warp = homeList.get(player.getName());
			if (warp.permissions.size() == 0) {
				player.sendMessage(LocaleManager.getString("home.ilist.none"));
			} else {
				player.sendMessage(LocaleManager.getString("home.ilist"));
				player.sendMessage(ChatColor.AQUA + warp.invitees());
			}
		} else {
			player.sendMessage(LocaleManager.getString("error.youhavenohome"));
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
			pointCompass(player, location);
		}
	}

	public void pointCompass(Player player, Location location) {
		if (HomeSettings.compassPointer) {
			player.setCompassTarget(location);
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
