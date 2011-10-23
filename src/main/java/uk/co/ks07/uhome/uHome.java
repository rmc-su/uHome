package uk.co.ks07.uhome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import uk.co.ks07.uhome.griefcraft.Updater;
import uk.co.ks07.uhome.locale.LocaleManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class uHome extends JavaPlugin {
	private UHPlayerListener playerListener;
	private UHEntityListener entityListener;
	private HomeList homeList;
	public String name;
	public String version;
	private Updater updater;
	public PluginManager pm;

	@Override
	public void onDisable() {
		ConnectionManager.closeConnection();
		HomeLogger.info(name + " " + version + " disabled");
	}

	@Override
	public void onEnable() {
		this.pm = getServer().getPluginManager();
		this.name = this.getDescription().getName();
		this.version = this.getDescription().getVersion();

		HomeSettings.initialize(getDataFolder());

		libCheck();
		convertOldDB(getDataFolder());
		if(!sqlCheck()) { return; }
		
		homeList = new HomeList(getServer());
		LocaleManager.init();
		HomeHelp.initialize(this);
		
		playerListener = new UHPlayerListener(homeList, this);
		entityListener = new UHEntityListener(this);
		
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		
		if(HomeSettings.respawnToHome) { 
			// Dont need this if we're not handling respawning.
			pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, this);
		}
		if(HomeSettings.loadChunks) {
			// We dont need to register for teleporting if we dont want to load chunks.
			pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
		}
		if(HomeSettings.abortOnDamage != 0) {
			// We dont need this if we're not aborting warmups for combat.
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
		}
		if(HomeSettings.abortOnMove) { 
			// We dont need this if we're not aborting if they move during warmup.
			pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
		}
		if(HomeSettings.bedsDuringDay && HomeSettings.bedsCanSethome != 0) {
			// We don't need this if the beds cannot be used during the day
			pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		} else if(!HomeSettings.bedsDuringDay && HomeSettings.bedsCanSethome != 0) {
			// We don't need this if the beds dont sethome
			pm.registerEvent(Event.Type.PLAYER_BED_LEAVE, playerListener, Priority.Monitor, this);
		}

		HomeLogger.info(name + " " + version + " enabled");
	}


	private void libCheck(){
		if(HomeSettings.downloadLibs){ 
			updater = new Updater();
			try {
				updater.check();
				updater.update();
			} catch (Exception e) {
			}
		}
	}

	private void convertOldDB(File df) {
		File newDatabase = new File(df, "homes.db");
		File oldDatabase = new File("homes-warps.db");
		if (!newDatabase.exists() && oldDatabase.exists()) {
			updateFiles(oldDatabase, newDatabase);
			oldDatabase.renameTo(new File("homes-warps.db.old"));
		} else if (newDatabase.exists() && oldDatabase.exists()) {
			// We no longer need this file since homes.db exists
			oldDatabase.renameTo(new File("homes-warps.db.old"));
		}
	}

	private boolean sqlCheck() {
		Connection conn = ConnectionManager.initialize();
		if (conn == null) {
			HomeLogger.severe("Could not establish SQL connection. Disabling uHome");
			pm.disablePlugin(this);
			return false;
		} 
		return true;
	}

	private void updateFiles(File oldDatabase, File newDatabase) {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		if (newDatabase.exists()) {
			newDatabase.delete();
		}
		try {
			newDatabase.createNewFile();
		} catch (IOException ex) {
			HomeLogger.severe("Could not create new database file", ex);
		}
		copyFile(oldDatabase, newDatabase);
	}

	/**
	 * File copier from xZise
	 * @param fromFile
	 * @param toFile
	 */
	private static void copyFile(File fromFile, File toFile) {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead);
			}
		} catch (IOException ex) {
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String[] split = args;
		String commandName = command.getName().toLowerCase();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			/**
			 * /sethome support
			 */
			if (commandName.equals("sethome") /*&& HomePermissions.set(player)*/) {
				if(HomeSettings.bedsCanSethome == 2 /*&& !HomePermissions.bedBypass(player)*/ ) {
					player.sendMessage(ChatColor.RED + "You can only set a home by sleeping in a bed");
					return true;
				}

                                if (split.length == 1) {
                                        homeList.addHome(player, this, split[0]);
                                        return true;
                                }

                                homeList.addHome(player, this);
				return true;
				/**
				 * Start of /home
				 */
			} else if (commandName.equals("home")) {
				/**
				 * /home
				 */
				if (split.length == 0 && player.hasPermission("uhome.own.warp")) {
					if (homeList.playerHasHome(player)) {
						homeList.sendPlayerHome(player, this);
					} else {
						player.sendMessage(ChatColor.RED + "You have no home :(");
						if(HomeSettings.bedsCanSethome == 2) { 
							player.sendMessage("You need to sleep in a bed to set a home");
						} else {
							player.sendMessage("Use: " + ChatColor.RED + "/home set" + ChatColor.WHITE + " to set a home");
						}
					}
					/**
					 *  /home reload
					 */
				} else if(split.length == 1 && split[0].equalsIgnoreCase("reload") && player.hasPermission("uhome.admin.reload")) {
					HomeSettings.initialize(getDataFolder());
					player.sendMessage("[uHome] Reloading config");
					/**
					 * /home set
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("set") && player.hasPermission("uhome.own.set")) {
					if(HomeSettings.bedsCanSethome == 2 && !player.hasPermission("uhome.bedbypass")) {
						player.sendMessage("You can only set your default home by sleeping in a bed");
						return true;
					} else {
						homeList.addHome(player, this);
					}
					/**
					 * /home set [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("set") && player.hasPermission("uhome.own.set")) {
					homeList.addHome(player, this, split[1]);
					/**
					 * /home set [player] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("set") && player.hasPermission("uhome.admin.set")) {
					homeList.adminAddHome(player, split[1], split[2]);
					/**
					 * /home delete
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("delete") && player.hasPermission("uhome.own.delete")) {
					homeList.deleteHome(player);
					/**
					 * /home list
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("list") && player.hasPermission("uhome.admin.list")) {
					homeList.listOther(player, split[1]);
					/**
					 * /home list [player]
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("list") && player.hasPermission("uhome.own.list")) {
					homeList.list(player);
                                        /**
                                         * /home info [owner] [name]
                                         */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("info")) {
					Location homeLoc = homeList.getHomeLocation(split[1], split[2]);
                                        player.sendMessage("Warp details: "+split[1]+" : "+split[2]+" "+homeLoc.toString());
					/**
					 *  /home delete [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("delete") && player.hasPermission("uhome.own.delete")) {
					homeList.deleteHome(player, split[1]);
					/**
					 * /home delete [owner] [name]
					 */
				} else if (split.length == 3 && split[0].equalsIgnoreCase("delete") && player.hasPermission("uhome.admin.delete")) {
					homeList.deleteHome(split[1], split[2], player);
					/**
					 * /home help
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("help")) {
					ArrayList<String> messages = new ArrayList<String>();
					messages.add(ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " -----");
					if (player.hasPermission("uhome.own.warp")) {
						messages.add(ChatColor.RED + "/home" + ChatColor.WHITE + "  -  Go home young chap!");
					}
					if (player.hasPermission("uhome.own.set")) {
						messages.add(ChatColor.RED + "/home set" + ChatColor.WHITE + "  -  Sets your home to your current position");
					}
					if (player.hasPermission("uhome.own.delete")) {
						messages.add(ChatColor.RED + "/home delete" + ChatColor.WHITE + "  -  Deletes your current home");
					}
					if (player.hasPermission("uhome.admin.warp")) {
						messages.add(ChatColor.RED + "/home [player]" + ChatColor.WHITE + "  -  Go to " + ChatColor.GRAY + "[player]" + ChatColor.WHITE
								+ "'s house (if allowed)");
					}
					if (player.hasPermission("uhome.own.list")) {
						messages.add(ChatColor.RED + "/home list" + ChatColor.WHITE + "  -  List the homes that you are invited to");
					}
					if(player.hasPermission("uhome.admin.delete")) {
						messages.add(ChatColor.RED + "/home delete [playername]" + ChatColor.WHITE + "  -  Clear playername's home");
					}

					for (String message : messages) {
						player.sendMessage(message);
					}
                                        return false;
                                        /**
					 * /home warp [name]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("warp") && player.hasPermission("uhome.own.warp")) {
					String target = split[1];

                                        if (homeList.homeExists(player.getName(), target)) {
                                                homeList.warpTo(target, player, this);
                                        } else {
                                                player.sendMessage("The home " + target + " doesn't exist!");
                                        }
                                        /**
					 * /home [name] OR /home [player]
					 */
				} else if (split.length == 1 && player.hasPermission("uhome.own.warp")) {
					String target = split[0];

                                        if (homeList.homeExists(player.getName(), target)) {
                                                homeList.warpTo(target, player, this);
                                        } else if (homeList.homeExists("home", target) && homeList.playerCanWarp(player, target, "home")) {
                                                homeList.warpTo(target, "home", player, this);
                                        } else {
                                                player.sendMessage("The home " + target + " doesn't exist!");
                                        }
                                        /**
					 * /home [player] [name]
					 */
				} else if (split.length == 2 && player.hasPermission("uhome.admin.warp")) {
					String targetOwner = split[0];
                                        String target = split[1];

                                        if (homeList.homeExists(targetOwner, target)) {
                                            player.sendMessage("Home exists, teleporting...");
                                                homeList.warpTo(targetOwner, target, player, this);
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
                                        HomeSettings.initialize(getDataFolder());
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
