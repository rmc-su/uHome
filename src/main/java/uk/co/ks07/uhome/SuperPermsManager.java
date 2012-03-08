package uk.co.ks07.uhome;

import java.util.Map;
import org.bukkit.entity.Player;

public class SuperPermsManager {
    public static uHome plugin;

    public static void initialize(uHome plugin) {
        SuperPermsManager.plugin = plugin;
    }

    // Deny permission (default override)
    public static final String denyPerm = "uhome.deny";
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
    // Home limit variables
    public static final String limitA = "uhome.limit.a";
    public static final String limitB = "uhome.limit.b";
    public static final String limitC = "uhome.limit.c";
    public static final String limitD = "uhome.limit.d";
    public static final String limitE = "uhome.limit.e";
    // Home invite limit variables
    public static final String invlimitA = "uhome.invlimit.a";
    public static final String invlimitB = "uhome.invlimit.b";
    public static final String invlimitC = "uhome.invlimit.c";
    public static final String invlimitD = "uhome.invlimit.d";
    public static final String invlimitE = "uhome.invlimit.e";
    // Warmup timer variables
    public static final String warmupA = "uhome.warmup.a";
    public static final String warmupB = "uhome.warmup.b";
    public static final String warmupC = "uhome.warmup.c";
    public static final String warmupD = "uhome.warmup.d";
    public static final String warmupE = "uhome.warmup.e";
    // Cooldown timer variables
    public static final String cooldownA = "uhome.cooldown.a";
    public static final String cooldownB = "uhome.cooldown.b";
    public static final String cooldownC = "uhome.cooldown.c";
    public static final String cooldownD = "uhome.cooldown.d";
    public static final String cooldownE = "uhome.cooldown.e";

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
                plugin.getLogger().severe("Looking for an unsupported dynamic limit! Please report this.");
                return -1;
        }
    }

    private enum LimitType {
        HOME,
        INVITE,
        WARMUP,
        COOLDOWN
    }
}
