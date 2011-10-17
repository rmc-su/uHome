package me.taylorkelly.myhome;

import java.util.LinkedHashMap;
import java.util.Map;

import ru.tehkode.permissions.bukkit.*;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.anjocaido.groupmanager.GroupManager;

import me.taylorkelly.myhome.utils.HomeLogger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class HomePermissions {
	private enum PermissionsHandler {
		PERMISSIONSEX, PERMISSIONS3, PERMISSIONS, GROUPMANAGER, PERMBUKKIT, NONE
	}
	private static PermissionsHandler handler;
	private static Plugin permissionPlugin;
	private static PluginManager pm;

	public static void initialize(Server server) {
		Plugin permissionsEx = server.getPluginManager().getPlugin("PermissionsEx");
		Plugin groupManager = server.getPluginManager().getPlugin("GroupManager");
		Plugin permissions = server.getPluginManager().getPlugin("Permissions");

		if (permissionsEx != null) {
			permissionPlugin = permissionsEx;
			handler = PermissionsHandler.PERMISSIONSEX;
			String version = permissionsEx.getDescription().getVersion();
			HomeLogger.info("Permissions enabled using: PermissionsEx v" + version);
		} else if (groupManager != null) {
			permissionPlugin = groupManager;
			handler = PermissionsHandler.GROUPMANAGER;
			String version = groupManager.getDescription().getVersion();
			HomeLogger.info("Permissions enabled using: GroupManager v" + version);
		} else if (permissions != null) {
			permissionPlugin = permissions;
			String version = permissions.getDescription().getVersion();
			if(version.contains("3.")) {
				// This shouldn't make any difference according to the Permissions API
				handler = PermissionsHandler.PERMISSIONS3;
			} else {
				handler = PermissionsHandler.PERMISSIONS;
			}
			HomeLogger.info("Permissions enabled using: Permissions v" + version);
		} else {
			handler = PermissionsHandler.PERMBUKKIT;
			HomeLogger.info("Permissions enabled using: PermissionsBukkit");
			HomePermissions.pm = server.getPluginManager();
			registerPermissions();
		}
	}

	public static boolean permission(Player player, String permission, boolean defaultPerm) {
		switch (handler) {
		case PERMISSIONSEX:
			return ((PermissionsEx) permissionPlugin).getPermissionManager().has(player, permission);
		case PERMISSIONS3:
			return ((Permissions) permissionPlugin).getHandler().has(player, permission);
		case PERMISSIONS:
			return ((Permissions) permissionPlugin).getHandler().has(player, permission);
		case GROUPMANAGER:
			return ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, permission);
		case PERMBUKKIT:
			return player.hasPermission(permission);
		case NONE:
			return defaultPerm;
		default:
			return defaultPerm;
		}
	}

	private static void registerPermissions() {
		registerAdminPerms();
		registerUserPerms();
		registerEcoPerms();
		registerBypassPerms();
		overallPerm();
	}
	
	public static int integer(Player player, String permission, int defaultPerm) {
		String world = player.getWorld().getName();
		String playername = player.getName();
		switch (handler) {
		case PERMISSIONSEX:
			return ((PermissionsEx) permissionPlugin).getPermissionManager().getUser(playername).getOptionInteger(permission, world, defaultPerm);
		case PERMISSIONS3:
			return ((Permissions) permissionPlugin).getHandler().getPermissionInteger(world, playername, permission);
		case PERMISSIONS:
			return ((Permissions) permissionPlugin).getHandler().getPermissionInteger(world, playername, permission);
		case GROUPMANAGER:
			return ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).getPermissionInteger(playername, permission);
		case PERMBUKKIT:
			return defaultPerm;
		case NONE:
			return defaultPerm;
		default:
			return defaultPerm;
		}
	}
	// --------------------------------------------
	// Admin Permissions
	public static boolean adminReloadConfig(Player player) {
		return permission(player, "myhome.admin.reload", player.isOp());
	}
	public static boolean adminDeleteHome(Player player) {
		return permission(player, "myhome.admin.home.delete", player.isOp());
	}
	public static boolean adminAnyHome(Player player) {
		return permission(player, "myhome.admin.home.any", player.isOp());
	}
	public static boolean adminConvert(Player player) {
		return permission(player, "myhome.admin.convert", player.isOp());
	}
	public static boolean adminListHome(Player player) {
		return permission(player, "myhome.admin.home.list", player.isOp());
	}

	private static void registerAdminPerms() {
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.reload", "Admin: Reload settings", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.home.delete", "Admin: Delete homes of users", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.home.any", "Admin: Teleport to any user's /home", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.convert", "Admin: Convert from old homes.txt", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.home.list", "Admin: See a full list of /homes", PermissionDefault.OP));
		Map<String, Boolean> adminmap = new LinkedHashMap<String, Boolean>();
		adminmap.put("myhome.admin.home.delete", true);
		adminmap.put("myhome.admin.home.any", true);
		adminmap.put("myhome.admin.convert", true);
		adminmap.put("myhome.admin.home.list", true);
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.admin.*", "Admin: All admin commands", PermissionDefault.OP, adminmap));
	}
	
	// --------------------------------------------
	// User permissions
	public static boolean home(Player player) {
		return permission(player, "myhome.home.basic.home", true);
	}
	public static boolean set(Player player) {
		return permission(player, "myhome.home.basic.set", true);
	}
	public static boolean delete(Player player) {
		return permission(player, "myhome.home.basic.delete", true);
	}
	public static boolean list(Player player) {
		return permission(player, "myhome.home.soc.list", true);
	}
	public static boolean homeOthers(Player player) {
		return permission(player, "myhome.home.soc.others", true);
	}
	public static boolean invite(Player player) {
		return permission(player, "myhome.home.soc.invite", true);
	}
	public static boolean uninvite(Player player) {
		return permission(player, "myhome.home.soc.uninvite", true);
	}
	public static boolean canPublic(Player player) {
		return permission(player, "myhome.home.soc.public", true);
	}
	public static boolean canPrivate(Player player) {
		return permission(player, "myhome.home.soc.private", true);
	}
	
	private static void registerUserPerms() {
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.basic.home", "Usage of /home", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.basic.set", "Usage of /sethome", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.basic.delete", "Usage of /home delete", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.list", "Can see a list of homes", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.others", "Can /home to other homes if invited", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.invite", "Can invite to your /home", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.uninvite", "Can uninvite people from your /home", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.public", "Allow anyone to use your /home", PermissionDefault.TRUE));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.private", "Disallow anyone to use your /home", PermissionDefault.TRUE));
		Map<String, Boolean> userbasicmap = new LinkedHashMap<String, Boolean>();
		Map<String, Boolean> usersocmap = new LinkedHashMap<String, Boolean>();
		Map<String, Boolean> userallmap = new LinkedHashMap<String, Boolean>();
		userbasicmap.put("myhome.home.basic.home", true);
		userbasicmap.put("myhome.home.basic.set", true);
		userbasicmap.put("myhome.home.basic.delete", true);
		usersocmap.put("myhome.home.soc.list", true);
		usersocmap.put("myhome.home.soc.others", true);
		usersocmap.put("myhome.home.soc.invite", true);
		usersocmap.put("myhome.home.soc.uninvite", true);
		usersocmap.put("myhome.home.soc.public", true);
		usersocmap.put("myhome.home.soc.private", true);
		userallmap.putAll(userbasicmap);
		userallmap.putAll(usersocmap);
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.basic.*", "Basic /home commands", PermissionDefault.TRUE, userbasicmap));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.soc.*", "Social /home commands", PermissionDefault.TRUE, usersocmap));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.*", "All user /home commands", PermissionDefault.TRUE, userallmap));
	
	}
	
	// ------------------------------------
	// Economy permissions
	public static boolean setHomeFree(Player player) {
		if(!HomeSettings.enableFreePerms) return false;
		
		return permission(player, "myhome.econ.free.sethome", true);
	}
	
	public static boolean homeFree(Player player) {
		if(!HomeSettings.enableFreePerms) return false;
		
		return permission(player, "myhome.econ.free.home", true);
	}
	
	private static void registerEcoPerms() {
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.econ.free.sethome", "Free usage of /sethome", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.econ.free.home", "Free usage of /home", PermissionDefault.OP));
		Map<String, Boolean> econmap = new LinkedHashMap<String, Boolean>();
		econmap.put("myhome.econ.free.sethome", true);
		econmap.put("myhome.econ.free.home", true);
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.home.free.*", "Free usage of commands", PermissionDefault.OP, econmap));
	}
	
	// -----------------------------------
	// Bypass Permissions
	public static boolean bedBypass(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permission(player, "myhome.bypass.bedsethome", true);
	}
	public static boolean bypassCooling(Player player) { 
		if(!HomeSettings.enableBypassPerms) return false;

		return permission(player, "myhome.bypass.cooldown", player.isOp());
	}
	public static boolean bypassWarming(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permission(player, "myhome.bypass.warmup", player.isOp());
	}
	public static boolean bypassSHCooling(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permission(player, "myhome.bypass.sethomecool", player.isOp());
	}
	public static boolean bypassWarmupDmgAbort(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permission(player, "myhome.bypass.dmgaborting", player.isOp());
	}
	
	public static boolean bypassWarmupMoveAbort(Player player) {
		if(!HomeSettings.enableBypassPerms) return false;
		
		return permission(player, "myhome.bypass.moveaborting", player.isOp());
	}

	private static void registerBypassPerms() {
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.bedsethome", "Bypass: can use /sethome when beds are enforced", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.cooldown", "Bypass: Do not have to wait for /home cooldown timers", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.warmup", "Bypass: Do not wait for /home to warm up", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.sethomecooldown", "Bypass: Do not have to wait for cooldown to use /sethome", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.dmgaborting", "Bypass: Do not abort /home if damaged", PermissionDefault.OP));
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.moveaborting", "Bypass: Do not abort /home if moving", PermissionDefault.OP));
		Map<String, Boolean> bypassmap = new LinkedHashMap<String, Boolean>();
		bypassmap.put("myhome.bypass.bedsethome", true);
		bypassmap.put("myhome.bypass.cooldown", true);
		bypassmap.put("myhome.bypass.warmup", true);
		bypassmap.put("myhome.bypass.sethomecooldown", true);
		bypassmap.put("myhome.bypass.dmgaborting", true);
		bypassmap.put("myhome.bypass.moveaborting", true);
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.bypass.*", "Bypass all timers and restrictions", PermissionDefault.OP, bypassmap));
	}
	
	public static void overallPerm() {
		Map<String, Boolean> fullmap = null;
		fullmap.put("myhome.econ.free.*", true);
		fullmap.put("myhome.bypass.*", true);
		fullmap.put("myhome.home.soc.*", true);
		fullmap.put("myhome.home.basic.*", true);
		pm.addPermission(new org.bukkit.permissions.Permission("myhome.*", "Full access", PermissionDefault.OP, fullmap));
	}
}