package uk.co.ks07.uhome;

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
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassLimit)) {
            return -1;
        } else {
            int playerMaxWarps;

            if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitA)) {
                playerMaxWarps = HomeConfig.limits[0];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitB)) {
                playerMaxWarps = HomeConfig.limits[1];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitC)) {
                playerMaxWarps = HomeConfig.limits[2];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitD)) {
                playerMaxWarps = HomeConfig.limits[3];
            } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.limitE)) {
                playerMaxWarps = HomeConfig.limits[4];
            } else {
                playerMaxWarps = HomeConfig.defaultLimit;
            }

            return playerMaxWarps;
        }
    }

    public static int getHomeCooldown(Player player) {
        int ret;

        if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownA)) {
            ret = HomeConfig.coolDowns[0];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownB)) {
            ret = HomeConfig.coolDowns[1];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownC)) {
            ret = HomeConfig.coolDowns[2];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownD)) {
            ret = HomeConfig.coolDowns[3];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.cooldownE)) {
            ret = HomeConfig.coolDowns[4];
        } else {
            ret = HomeConfig.defaultCoolDown;
        }

        return ret;
    }

    public static int getWarmup(Player player) {
        int ret;

        if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupA)) {
            ret = HomeConfig.warmUps[0];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupB)) {
            ret = HomeConfig.warmUps[1];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupC)) {
            ret = HomeConfig.warmUps[2];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupD)) {
            ret = HomeConfig.warmUps[3];
        } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.warmupE)) {
            ret = HomeConfig.warmUps[4];
        } else {
            ret = HomeConfig.defaultWarmUp;
        }

        return ret;
    }

    public static int getInviteLimit(Player player) {
        if (player != null) {
            if (SuperPermsManager.hasPermission(player, SuperPermsManager.bypassInvLimit)) {
                return -1;
            } else {
                int playerMaxWarps;

                if (SuperPermsManager.hasPermission(player, SuperPermsManager.invlimitA)) {
                    playerMaxWarps = HomeConfig.invlimits[0];
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.invlimitB)) {
                    playerMaxWarps = HomeConfig.invlimits[1];
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.invlimitC)) {
                    playerMaxWarps = HomeConfig.invlimits[2];
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.invlimitD)) {
                    playerMaxWarps = HomeConfig.invlimits[3];
                } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.invlimitE)) {
                    playerMaxWarps = HomeConfig.invlimits[4];
                } else {
                    playerMaxWarps = HomeConfig.defaultInvLimit;
                }

                return playerMaxWarps;
            }
        } else {
            return -1;
        }
    }
}
