package uk.co.ks07.uhome;

import org.bukkit.entity.Player;

public class SuperPermsManager {
    // Home owners
    public static final String ownWarp     = "uhome.own.warp";
    public static final String ownSet      = "uhome.own.set";
    public static final String ownList     = "uhome.own.list";
    public static final String ownDelete   = "uhome.own.delete";
    public static final String ownInvite   = "uhome.own.invite";
    public static final String ownUninvite   = "uhome.own.uninvite";

    // Home admins
    public static final String adminWarp   = "uhome.admin.warp";
    public static final String adminSet    = "uhome.admin.set";
    public static final String adminList   = "uhome.admin.list";
    public static final String adminDelete = "uhome.admin.delete";
    public static final String adminInfo   = "uhome.admin.info";
    public static final String adminReload = "uhome.admin.reload";

    // Limit bypasses
    public static final String bypassLimit           = "uhome.bypass.limit";
    public static final String bypassBed             = "uhome.bypass.bed";
    public static final String bypassWarmup          = "uhome.bypass.warmup";
    public static final String bypassWarmupDamage    = "uhome.bypass.warmup.damage";
    public static final String bypassWarmupMovement  = "uhome.bypass.warmup.movement";
    public static final String bypassCooldown        = "uhome.bypass.cooldown";

    // Home limit variables
    public static final String limitA      = "uhome.limit.a";
    public static final String limitB      = "uhome.limit.b";
    public static final String limitC      = "uhome.limit.c";
    public static final String limitD      = "uhome.limit.d";
    public static final String limitE      = "uhome.limit.e";

    // Warmup timer variables
    public static final String warmupA     = "uhome.warmup.a";
    public static final String warmupB     = "uhome.warmup.b";
    public static final String warmupC     = "uhome.warmup.c";

    // Cooldown timer variables
    public static final String cooldownA   = "uhome.cooldown.a";
    public static final String cooldownB   = "uhome.cooldown.b";
    public static final String cooldownC   = "uhome.cooldown.c";

    public static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}
