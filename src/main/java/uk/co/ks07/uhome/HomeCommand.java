package uk.co.ks07.uhome;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private uHome plugin;
    private HomeList homeList;

    public HomeCommand(uHome uH, HomeList hL) {
        this.plugin = uH;
        this.homeList = hL;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String[] split = args;
		String commandName = command.getName().toLowerCase();
		if (sender instanceof Player) {
			Player player = (Player) sender;

                        // Workaround for ticket 8.
                        if (HomeConfig.enableDenyPerm && SuperPermsManager.hasPermission(player, SuperPermsManager.denyPerm)) {
                            return true;
                        }

			/**
			 * /sethome support
			 */
			if (commandName.equals("sethome") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
				if(HomeConfig.bedsCanSethome == 2 && !SuperPermsManager.hasPermission(player, SuperPermsManager.bypassBed)) {
					player.sendMessage(ChatColor.RED + "You can only set a home by sleeping in a bed");
					return true;
				}

                                if (split.length == 1) {
                                        homeList.addHome(player, plugin, split[0]);
                                        return true;
                                }

                                homeList.addHome(player, plugin);
				return true;
				/**
				 * Start of /home
				 */
			} else if (commandName.equals("home")) {
				/**
				 * /home
				 */
				if (split.length == 0 && SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
					if (homeList.playerHasHome(player)) {
						homeList.sendPlayerHome(player, plugin);
					} else {
						player.sendMessage(ChatColor.RED + "You have no home :(");
						if(HomeConfig.bedsCanSethome == 2) {
							player.sendMessage("You need to sleep in a bed to set your default home");
						} else {
							player.sendMessage("Use: " + ChatColor.RED + "/home set" + ChatColor.WHITE + " to set a home");
						}
					}
					/**
					 *  /home reload
					 */
				} else if(split.length == 1 && split[0].equalsIgnoreCase("reload") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminReload)) {
					HomeConfig.initialize(plugin.config, plugin.getDataFolder());
					player.sendMessage("[uHome] Reloading config");
					/**
					 *  /home limit
					 */
				} else if(split.length == 1 && split[0].equalsIgnoreCase("limit") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
					player.sendMessage("You can set up to " + homeList.playerGetLimit(player) + " homes.");
					/**
					 *  /home limit [player]
					 */
				} else if(split.length == 2 && split[0].equalsIgnoreCase("limit") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
                                        Player target = plugin.getServer().getPlayer(split[1]);

                                        if (target != null) {
                                            player.sendMessage(target.getName() + " can set up to " + homeList.playerGetLimit(target) + " homes.");
                                        } else {
                                            player.sendMessage("Player not found.");
                                        }
					/**
					 * /home set
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("set") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
					if(HomeConfig.bedsCanSethome == 2 && !SuperPermsManager.hasPermission(player, SuperPermsManager.bypassBed)) {
						player.sendMessage("You can only set your default home by sleeping in a bed");
						return true;
					} else {
						homeList.addHome(player, plugin);
					}
					/**
					 * /home set [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("set") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
					homeList.addHome(player, plugin, split[1]);
					/**
					 * /home set [player] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("set") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminSet)) {
					homeList.adminAddHome(player, split[1], split[2]);
					/**
					 * /home delete
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("delete") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
					homeList.deleteHome(player);
					/**
					 * /home list [player]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("list") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
					homeList.listOther(player, split[1]);
					/**
					 * /home invites
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("invites") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
					homeList.listInvitedTo(player);
					/**
					 * /home requests
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("requests") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
					homeList.listRequests(player);
					/**
					 * /home list
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("list") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
					homeList.list(player);
                                        /**
                                         * /home info [owner] [name]
                                         */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("info") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminInfo)) {
					Location homeLoc = homeList.getHomeLocation(split[1], split[2]);
                                        player.sendMessage("Warp details: "+split[1]+" : "+split[2]+" "+homeLoc.toString());
					/**
					 *  /home delete [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("delete") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
					homeList.deleteHome(player, split[1]);
					/**
					 * /home delete [owner] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("delete") && SuperPermsManager.hasPermission(player, SuperPermsManager.adminDelete)) {
					homeList.deleteHome(split[1], split[2], player);
					/**
					 * /home invite [player] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("invite") && HomeConfig.enableInvite && SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
                                        String targetPlayer = split[1];
                                        String targetHome = split[2];

                                        if (homeList.homeExists(player.getName(), targetHome)) {
                                                homeList.invitePlayer(player, targetPlayer, targetHome);
                                        } else {
                                                player.sendMessage("The home " + targetHome + " doesn't exist!");
                                        }
					/**
					 * /home uninvite [player] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("uninvite") && HomeConfig.enableInvite && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUninvite)) {
                                        String targetPlayer = split[1];
                                        String targetHome = split[2];

                                        if (homeList.homeExists(player.getName(), targetHome)) {
                                                homeList.uninvitePlayer(player, targetPlayer, targetHome);
                                        } else {
                                                player.sendMessage("The home " + targetHome + " doesn't exist!");
                                        }

					/**
					 * /home help
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("help")) {
					ArrayList<String> messages = new ArrayList<String>();
					messages.add(ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " -----");
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
						messages.add(ChatColor.RED + "/home" + ChatColor.WHITE + "  -  Go home young chap!");
					}
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
						messages.add(ChatColor.RED + "/home set" + ChatColor.WHITE + "  -  Sets your home to your current position");
					}
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
						messages.add(ChatColor.RED + "/home delete" + ChatColor.WHITE + "  -  Deletes your current home");
					}
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
						messages.add(ChatColor.RED + "/home [player]" + ChatColor.WHITE + "  -  Go to " + ChatColor.GRAY + "[player]" + ChatColor.WHITE
								+ "'s house (if allowed)");
					}
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
						messages.add(ChatColor.RED + "/home list" + ChatColor.WHITE + "  -  List the homes that you are invited to");
					}
					if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
						messages.add(ChatColor.RED + "/home delete [playername]" + ChatColor.WHITE + "  -  Clear playername's home");
					}

					for (String message : messages) {
						player.sendMessage(message);
					}
                                        return false;
                                        /**
					 * /home warp [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("warp") && SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
					String target = split[1];

                                        if (homeList.homeExists(player.getName(), target)) {
                                                homeList.warpTo(target, player, plugin);
                                        } else {
                                                player.sendMessage("The home " + target + " doesn't exist!");
                                        }
                                        /**
					 * /home [name] OR /home [player]
					 */
				} else if (split.length == 1 && (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp) || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp))) {
					String target = split[0];

                                        if (homeList.homeExists(player.getName(), target)) {
                                                homeList.warpTo(target, player, plugin);
                                        } else if (homeList.homeExists("home", target) && homeList.playerCanWarp(player, target, "home")) {
                                                homeList.warpTo(target, "home", player, plugin);
                                        } else {
                                                player.sendMessage("The home " + target + " doesn't exist!");
                                        }
                                        /**
					 * /home [player] [name]
					 */
				} else if (split.length == 2) {
					String targetOwner = split[0];
                                        String target = split[1];

                                        if (homeList.homeExists(targetOwner, target)) {
                                                if (homeList.playerCanWarp(player, targetOwner, target)) {
                                                    homeList.warpTo(targetOwner, target, player, plugin);
                                                } else {
                                                    player.sendMessage("You aren't invited to "+targetOwner+"'s home '"+target+"'!");
                                                }
                                        } else {
                                                player.sendMessage("The home " + target + " doesn't exist!");
                                        }
				} else {
					return false;
				}
				return true;
			}
		} else {
                        if (commandName.equals("home")) {
                                /**
                                 * /home list [player]
                                 */
                                if (split.length == 2 && split[0].equalsIgnoreCase("list")) {
                                        String target = split[1];

                                        if(homeList.getPlayerList(target) == null) {
                                            sender.sendMessage("[uHome] The player " + target + " has no homes!");
                                        } else {
                                            sender.sendMessage("[uHome] That player has the following homes ("+homeList.getPlayerWarpNo(target) +"):");
                                            sender.sendMessage("[uHome] "+homeList.getPlayerList(target));
                                        }
                                /**
                                 * /home reload
                                 */
                                } else if (split.length == 1 && split[0].equalsIgnoreCase("reload")) {
                                        HomeConfig.initialize(plugin.config, plugin.getDataFolder());
					sender.sendMessage("[uHome] Reloading config");
                                /**
                                 * /home info [owner] [name]
                                 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("info")) {
					Location homeLoc = homeList.getHomeLocation(split[1], split[2]);
                                        sender.sendMessage("[uHome] Warp details: "+split[1]+" : "+split[2]+
                                                        homeLoc.toString());
                                /**
                                 * /home delete [owner] [name]
                                 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("delete")) {
					homeList.deleteHome(split[1], split[2]);
                                        sender.sendMessage("[uHome] Deleted warp "+split[1]+" : "+split[2]);
                                } else {
                                        return false;
                                }
                                return true;
                        }
                }
		return false;
	}
}
