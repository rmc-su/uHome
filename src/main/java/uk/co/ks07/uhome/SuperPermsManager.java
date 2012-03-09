package uk.co.ks07.uhome;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class SuperPermsManager {
    public static uHome plugin;

    public static void initialize(uHome plugin) {
        SuperPermsManager.plugin = plugin;

        // Enable defaults if necessary.
        if (HomeConfig.enableDefaultPerms) {
            plugin.pm.getPermission("uhome.own").setDefault(PermissionDefault.TRUE);
            plugin.pm.getPermission("uhome.admin").setDefault(PermissionDefault.OP);
            plugin.pm.getPermission("uhome.bypass").setDefault(PermissionDefault.OP);
        }
    }

    // Home owners
    public static final String ownWarp = "uhome.own.warp";
    public static final String ownSet = "uhome.own.set";
    public static final String ownList = "uhome.own.list";
    public static final String ownListInvites = "uhome.own.listinvites";
    public static final String ownDelete = "uhome.own.delete";
    public static final String ownInvite = "uhome.own.invite";
    public static final String ownUninvite = "uhome.own.uninvite";
    // Home admins
    public static final String adminWarp = "uhome.admin.warp";
    public static final String adminSet = "uhome.admin.set";
    public static final String adminList = "uhome.admin.list";
    public static final String adminListInvites = "uhome.admin.listinvites";
    public static final String adminDelete = "uhome.admin.delete";
    public static final String adminInfo = "uhome.admin.info";
    public static final String adminReload = "uhome.admin.reload";
    // Limit bypasses
    public static final String bypassLimit = "uhome.bypass.limit";
    public static final String bypassBed = "uhome.bypass.bed";
    public static final String bypassWarmup = "uhome.bypass.warmup";
    public static final String bypassWarmupDamage = "uhome.bypass.warmup.damage";
    public static final String bypassWarmupMovement = "uhome.bypass.warmup.movement";
    public static final String bypassCooldown = "uhome.bypass.cooldown";
    public static final String allowCrossWorld = "uhome.crossworld";
    public static final String bypassInvLimit = "uhome.bypass.invlimit";

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
            return getPermissionLimit(player, LimitType.HOME);
        }
    }

    public static int getHomeCooldown(Player player) {
        return getPermissionLimit(player, LimitType.COOLDOWN);
    }

    public static int getWarmup(Player player) {
        return getPermissionLimit(player, LimitType.WARMUP);
    }

    public static int getInviteLimit(Player player) {
        if (hasPermission(player, bypassInvLimit)) {
            return -1;
        } else {
            return getPermissionLimit(player, LimitType.INVITE);
        }
    }

    public static void registerPermission(String permNode) {
        plugin.pm.addPermission(new Permission(permNode));
    }

    private static int getPermissionLimit(Player player, LimitType type) {
        switch (type) {
            case HOME:
                for (Map.Entry<String, Integer> permEntry : HomeConfig.permLimits.entrySet()) {
                    if (hasPermission(player, permEntry.getKey())) {
                        return permEntry.getValue();
                    }
                }

                return HomeConfig.defaultLimit;
            case INVITE:
                for (Map.Entry<String, Integer> permEntry : HomeConfig.permInvLimits.entrySet()) {
                    if (hasPermission(player, permEntry.getKey())) {
                        return permEntry.getValue();
                    }
                }

                return HomeConfig.defaultInvLimit;
            case WARMUP:
                for (Map.Entry<String, Integer> permEntry : HomeConfig.permWarmUps.entrySet()) {
                    if (hasPermission(player, permEntry.getKey())) {
                        return permEntry.getValue();
                    }
                }

                return HomeConfig.defaultWarmUp;
            case COOLDOWN:
                for (Map.Entry<String, Integer> permEntry : HomeConfig.permCoolDowns.entrySet()) {
                    if (hasPermission(player, permEntry.getKey())) {
                        return permEntry.getValue();
                    }
                }

                return HomeConfig.defaultCoolDown;
            default:
                throw new IllegalArgumentException();
        }
    }

    private enum LimitType {
        HOME,
        INVITE,
        WARMUP,
        COOLDOWN
    }
}
