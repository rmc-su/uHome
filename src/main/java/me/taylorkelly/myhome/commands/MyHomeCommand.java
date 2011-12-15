package me.taylorkelly.myhome.commands;

import java.util.ArrayList;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.MyHome;
import me.taylorkelly.myhome.data.HomeList;
import me.taylorkelly.myhome.locale.LocaleManager;
import me.taylorkelly.myhome.permissions.HomePermissions;
import me.taylorkelly.myhome.utils.Converter;
import me.taylorkelly.myhome.utils.HomeLogger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class MyHomeCommand implements CommandExecutor {
	private final MyHome plugin;
	private HomeList homeList;
	private boolean warning = false;
	
	public MyHomeCommand(MyHome plugin, HomeList homeList) {
		this.plugin = plugin;
		this.homeList = homeList;
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
					homeList.addHome(player, plugin);
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
						homeList.sendPlayerHome(player, plugin);
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
					HomeSettings.initialize(plugin.getDataFolder());
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
						Converter.convert(player, plugin.getServer(), homeList);
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
						homeList.addHome(player, plugin);
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
					Player invitee = plugin.getServer().getPlayer(split[1]);
					String inviteeName = (invitee == null) ? split[1] : invitee.getName();

					homeList.invite(player, inviteeName);
					/**
					 * /home uninvite <player>
					 */
				} else if (split.length == 2 && split[0].equalsIgnoreCase("uninvite") && HomePermissions.uninvite(player)) {
					Player invitee = plugin.getServer().getPlayer(split[1]);
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
					homeList.warpTo(playerName, player, plugin);
				} else {
					return false;
				}
				return true;
			}
		} else if ((commandName.equals("home") || commandName.equals("myhome") || commandName.equals("mh"))) {
			if (split.length == 2 && split[0].equalsIgnoreCase("clear")) {
					homeList.consoleClearHome(split[1]);
			} else if(split.length == 1 && split[0].equalsIgnoreCase("reload")) {
				HomeSettings.initialize(plugin.getDataFolder());
				HomeLogger.info(LocaleManager.getString("admin.reload"));
			} else if (split.length == 1 && split[0].equalsIgnoreCase("convert")) {
				if (!warning) {
					HomeLogger.warning(LocaleManager.getString("admin.convert"));
					HomeLogger.warning(LocaleManager.getString("admin.convert2"));
					HomeLogger.warning(LocaleManager.getString("usage.convert"));
					warning = true;
				} else {
					Converter.convert(plugin.getServer(), homeList);
					warning = false;
				}
			} else {
				HomeLogger.info("--------------------------");
				HomeLogger.info("MyHome Console Commands:");
				HomeLogger.info("--------------------------");
				HomeLogger.info("myhome clear <playername> -- Clear <playername>'s home.");	
				HomeLogger.info("myhome reload -- Reload the MyHome config");
				HomeLogger.info("myhome convert -- Convert an old homes.txt");
				HomeLogger.info("--------------------------");
			}
			return true;
		}
		return false;
	}

}
