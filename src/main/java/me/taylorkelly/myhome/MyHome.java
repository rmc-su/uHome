package me.taylorkelly.myhome;

import java.util.ArrayList;

import me.taylorkelly.myhome.data.HomeEconomy;
import me.taylorkelly.myhome.data.HomeList;
import me.taylorkelly.myhome.listeners.MHEntityListener;
import me.taylorkelly.myhome.listeners.MHPlayerListener;
import me.taylorkelly.myhome.listeners.MHPluginListener;
import me.taylorkelly.myhome.locale.LocaleManager;
import me.taylorkelly.myhome.permissions.HomePermissions;
import me.taylorkelly.myhome.sql.ConnectionManager;
import me.taylorkelly.myhome.utils.Converter;
import me.taylorkelly.myhome.utils.HomeHelp;
import me.taylorkelly.myhome.utils.HomeLogger;
import me.taylorkelly.myhome.utils.MHUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class MyHome extends JavaPlugin {
	private MHPlayerListener playerListener;
	private MHEntityListener entityListener;
	private MHPluginListener pluginListener;
	private HomeList homeList;
	private boolean warning = false;
	public String name;
	public String version;
	public PluginManager pm;
	private MHUtils utils;

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
		this.utils = new MHUtils(this);
		
		HomeSettings.initialize(getDataFolder());

		utils.startupChecks();
	
		homeList = new HomeList(getServer());
		LocaleManager.init();
		HomePermissions.initialize(this);
		HomeHelp.initialize(this);
		HomeEconomy.init(this);
		
		this.playerListener = new MHPlayerListener(homeList, this);
		this.entityListener = new MHEntityListener(this);
		this.pluginListener = new MHPluginListener(this);
		
		registerEvents();

		HomeLogger.info(name + " " + version + " enabled");
	}
	
	private void registerEvents() {
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
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
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String[] split = args;
		String commandName = command.getName().toLowerCase();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			/**
			 * /Sethome support
			 */
			if (commandName.equals("sethome") && HomePermissions.set(player)) {
				if(HomeSettings.bedsCanSethome == 2 && !HomePermissions.bedBypass(player) ) { 
					player.sendMessage(LocaleManager.getString("error.sleepinbed"));
					return true;
				}
				// Check for /sethome if enabled in the configuration
				if (HomeSettings.allowSetHome) { 
					homeList.addHome(player, this);
				} else {
					player.sendMessage(LocaleManager.getString("usage.sethome"));
				}
				return true;
				/**
				 * Start of /home
				 */
			} else if (commandName.equals("home") || commandName.equals("myhome") || commandName.equals("mh")) {
				/**
				 * /home
				 */
				if (split.length == 0 && HomePermissions.home(player)) {
					if (homeList.playerHasHome(player)) {
						homeList.sendPlayerHome(player, this);
					} else {
						player.sendMessage(LocaleManager.getString("error.youhavenohome"));
						if(HomeSettings.bedsCanSethome == 2) { 
							player.sendMessage(LocaleManager.getString("error.sleepinbed"));
						} else {       
							player.sendMessage(LocaleManager.getString("usage.sethome"));
						}
					}
					/**
					 *  /home reload
					 */
				} else if(split.length == 1 && split[0].equalsIgnoreCase("reload") && HomePermissions.adminReloadConfig(player)) {
					HomeSettings.initialize(getDataFolder());
					player.sendMessage(LocaleManager.getString("admin.reload"));
					/**
					 * /home convert
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("convert") && HomePermissions.adminConvert(player)) {
					if (!warning) {
						player.sendMessage(LocaleManager.getString("admin.convert"));
						player.sendMessage(LocaleManager.getString("admin.convert2"));
						player.sendMessage(LocaleManager.getString("usage.convert"));
						warning = true;
					} else {
						Converter.convert(player, getServer(), homeList);
						warning = false;
					}
					/**
					 * /home set
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("set") && HomePermissions.set(player)) {
					if(HomeSettings.bedsCanSethome == 2 && !HomePermissions.bedBypass(player)) { 
						player.sendMessage(LocaleManager.getString("error.sleepinbed"));
						return true;
					} else {
						homeList.addHome(player, this);
					}
					/**
					 * /home delete
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("delete") && HomePermissions.delete(player)) {
					homeList.deleteHome(player);
					/**
					 * /home list
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("list") && HomePermissions.list(player)) {
					homeList.list(player);
					/**
					 * /home ilist
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("ilist") && HomePermissions.list(player)) {
					homeList.ilist(player);
					/**
					 * /home listall
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("listall") && HomePermissions.adminListHome(player)) {
					homeList.listall(player);
					/**
					 * /home private
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("private") && HomePermissions.canPrivate(player)) {
					homeList.privatize(player);
					/**
					 * /home public
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("public") && HomePermissions.canPublic(player)) {
					homeList.publicize(player);
					/**
					 * /home point
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("point") && HomeSettings.compassPointer) {
					homeList.orientPlayer(player);
					/**
					 *  /home delete [player]
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("clear") && HomePermissions.adminDeleteHome(player)) {
					homeList.clearHome(split[1], player);
					/**
					 * /home invite <player>
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("invite") && HomePermissions.invite(player)) {
					Player invitee = getServer().getPlayer(split[1]);
					String inviteeName = (invitee == null) ? split[1] : invitee.getName();

					homeList.invite(player, inviteeName);
					/**
					 * /home uninvite <player>
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("uninvite") && HomePermissions.uninvite(player)) {
					Player invitee = getServer().getPlayer(split[1]);
					String inviteeName = (invitee == null) ? split[1] : invitee.getName();

					homeList.uninvite(player, inviteeName);
					/**
					 * /home <name>
					 */
				} else if (split.length == 1 && split[0].equalsIgnoreCase("help")) {
					ArrayList<String> messages = new ArrayList<String>();
					messages.add(ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " -----");
					if (HomePermissions.home(player)) {
						messages.add(ChatColor.RED + "/home" + ChatColor.WHITE + "  -  " + LocaleManager.getString("help.home"));
					}
					if (HomePermissions.set(player)) {
						messages.add(ChatColor.RED + "/home set" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeset"));
					}
					if (HomePermissions.delete(player)) {
						messages.add(ChatColor.RED + "/home delete" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homedelete"));
					}
					if (HomePermissions.homeOthers(player)) {
						messages.add(ChatColor.RED + "/home [player]" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeplayer"));
					}
					if (HomePermissions.list(player)) {
						messages.add(ChatColor.RED + "/home list" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homelist"));
						messages.add(ChatColor.RED + "/home ilist" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeilist"));
					}
					if (HomePermissions.invite(player)) {
						messages.add(ChatColor.RED + "/home invite [player]" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeinvite"));
					}
					if (HomePermissions.uninvite(player)) {
						messages.add(ChatColor.RED + "/home uninvite [player]" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeuninvite"));
					}
					if (HomePermissions.canPublic(player)) {
						messages.add(ChatColor.RED + "/home public" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homepublic"));
					}
					if (HomePermissions.canPrivate(player)) {
						messages.add(ChatColor.RED + "/home private" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.homeprivate"));
					}
					if (HomeSettings.compassPointer) {
						messages.add(ChatColor.RED + "/home point" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.point"));
					}
					if(HomePermissions.adminConvert(player)) {
						messages.add(ChatColor.RED + "/home convert" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.convert"));
					}
					if(HomePermissions.adminDeleteHome(player)) {
						messages.add(ChatColor.RED + "/home clear [playername]" + ChatColor.WHITE + "  -  "  + LocaleManager.getString("help.admin.clear"));
					}
					if(HomePermissions.adminListHome(player)) {
						messages.add(ChatColor.RED + "/home listall" + ChatColor.WHITE + "  - "  + LocaleManager.getString("help.admin.listall"));
					}

					for (String message : messages) {
						player.sendMessage(message);
					}
				} else if (split.length == 1 && HomePermissions.homeOthers(player)) {
					String playerName = split[0];
					homeList.warpTo(playerName, player, this);
				} else {
					return false;
				}
				return true;
			}
		} else if (commandName.equals("home") || commandName.equals("myhome") || commandName.equals("mh") && (split.length == 2 && split[0].equalsIgnoreCase("clear"))) {
			homeList.consoleClearHome(split[1]);
			return true;
		}
		return false;
	}

	public void disablePlugin() {
		pm.disablePlugin(this);
	}
}
