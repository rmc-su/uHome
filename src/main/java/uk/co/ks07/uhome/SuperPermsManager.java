package uk.co.ks07.uhome;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class SuperPermsManager {
    private static uHome plugin;

    public static void initialize(uHome plugin) {
        SuperPermsManager.plugin = plugin;
    }

    // Home owners
    public static final String ownWarp = "uhome.own.warp";
    public static final String ownSet = "uhome.own.set";
    public static final String ownList = "uhome.own.list";
    public static final String ownListInvites = "uhome.own.listinvites";
    public static final String ownDelete = "uhome.own.delete";
    public static final String ownInvite = "uhome.own.invite";
    public static final String ownUninvite = "uhome.own.uninvite";
    public static final String ownUnlock = "uhome.own.unlock";
    // Home admins
    public static final String adminWarp = "uhome.admin.warp";
    public static final String adminSet = "uhome.admin.set";
    public static final String adminList = "uhome.admin.list";
    public static final String adminListInvites = "uhome.admin.listinvites";
    public static final String adminDelete = "uhome.admin.delete";
    public static final String adminInfo = "uhome.admin.info";
    public static final String adminReload = "uhome.admin.reload";
    public static final String adminSend = "uhome.admin.send";
    public static final String adminSilent = "uhome.admin.createsilent";
    // Limit bypasses
    public static final String bypassLimit = "uhome.bypass.limit";
    public static final String bypassBed = "uhome.bypass.bed";
    public static final String bypassWarmup = "uhome.bypass.warmup";
    public static final String bypassWarmupDamage = "uhome.bypass.warmup.damage";
    public static final String bypassWarmupMovement = "uhome.bypass.warmup.movement";
    public static final String bypassCooldown = "uhome.bypass.cooldown";
    public static final String allowCrossWorld = "uhome.crossworld";
    public static final String bypassInvLimit = "uhome.bypass.invlimit";
    public static final String bypassEcon = "uhome.bypass.economy";

    public static boolean hasPermission(Player player, String permission) {
        boolean ret = player.hasPermission(permission);

        if (HomeConfig.debugLog) {
            if (ret) {
                plugin.getLogger().info(player.getName() + " returned true for the node " + permission);
            } else {
                plugin.getLogger().info(player.getName() + " returned false for the node " + permission);
            }
        }

        return ret;
    }

    public static int getHomeLimit(Player player) {
        if (hasPermission(player, bypassLimit)) {
            return -1;
        } else {
            for (Map.Entry<String, Integer> permEntry : HomeConfig.permLimits.entrySet()) {
                if (hasPermission(player, permEntry.getKey())) {
                    return permEntry.getValue();
                }
            }

            return HomeConfig.defaultLimit;
        }
    }

    public static int getHomeCooldown(Player player) {
        for (Map.Entry<String, Integer> permEntry : HomeConfig.permCoolDowns.entrySet()) {
            if (hasPermission(player, permEntry.getKey())) {
                return permEntry.getValue();
            }
        }

        return HomeConfig.defaultCoolDown;
    }

    public static int getWarmup(Player player) {
        for (Map.Entry<String, Integer> permEntry : HomeConfig.permWarmUps.entrySet()) {
            if (hasPermission(player, permEntry.getKey())) {
                return permEntry.getValue();
            }
        }

        return HomeConfig.defaultWarmUp;
    }

    public static int getInviteLimit(Player player) {
        if (hasPermission(player, bypassInvLimit)) {
            return -1;
        } else {
            for (Map.Entry<String, Integer> permEntry : HomeConfig.permInvLimits.entrySet()) {
                if (hasPermission(player, permEntry.getKey())) {
                    return permEntry.getValue();
                }
            }

            return HomeConfig.defaultInvLimit;
        }
    }

    public static void registerPermission(String permNode) {
        if (plugin.pm.getPermission(permNode) == null) {
            plugin.pm.addPermission(new Permission(permNode));
        }
    }
}
