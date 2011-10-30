package me.taylorkelly.myhome.permissions;

import me.taylorkelly.myhome.HomeSettings;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HomePermissions {
	private transient static PermissionsHandler permissionsHandler;

	public static void initialize(Plugin plugin) {
		permissionsHandler = new PermissionsHandler(plugin);
	}
	
	public static int integer(Player player, String node, int defaultInt) {
		return permissionsHandler.getInteger(player, node, defaultInt);
	}

	// --------------------------------------------
	// Admin Permissions
	public static boolean adminReloadConfig(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.admin.reload", player.isOp());
	}
	public static boolean adminDeleteHome(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.admin.home.delete", player.isOp());
	}
	public static boolean adminAnyHome(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.admin.home.any", player.isOp());
	}
	public static boolean adminConvert(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.admin.convert", player.isOp());
	}
	public static boolean adminListHome(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.admin.home.list", player.isOp());
	}

	// --------------------------------------------
	// User permissions
	public static boolean home(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.basic.home", true);
	}
	public static boolean set(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.basic.set", true);
	}
	public static boolean delete(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.basic.delete", true);
	}
	public static boolean list(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.list", true);
	}
	public static boolean homeOthers(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.others", true);
	}
	public static boolean invite(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.invite", true);
	}
	public static boolean uninvite(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.uninvite", true);
	}
	public static boolean canPublic(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.public", true);
	}
	public static boolean canPrivate(Player player) {
		return permissionsHandler.hasPermission(player, "myhome.home.soc.private", true);
	}
	
	// ------------------------------------
	// Economy permissions
	public static boolean setHomeFree(Player player) {
		if(!HomeSettings.enableFreePerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.econ.free.sethome", true);
	}
	
	public static boolean homeFree(Player player) {
		if(!HomeSettings.enableFreePerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.econ.free.home", true);
	}
	
	// -----------------------------------
	// Bypass Permissions
	public static boolean bedBypass(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.bypass.bedsethome", true);
	}
	public static boolean bypassCooling(Player player) { 
		if(!HomeSettings.enableBypassPerms) return false;

		return permissionsHandler.hasPermission(player, "myhome.bypass.cooldown", player.isOp());
	}
	public static boolean bypassWarming(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.bypass.warmup", player.isOp());
	}
	public static boolean bypassSHCooling(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.bypass.sethomecool", player.isOp());
	}
	public static boolean bypassWarmupDmgAbort(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.bypass.dmgaborting", player.isOp());
	}
	
	public static boolean bypassWarmupMoveAbort(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permissionsHandler.hasPermission(player, "myhome.bypass.moveaborting", player.isOp());
	}
}