package uk.co.ks07.uhome;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HomeConfig {
    private static final String HOME_LIMIT_SECTION = "homeLimits";
    private static final String INV_LIMIT_SECTION = "homeInvLimits";
    private static final String WARMUP_SECTION = "warmups";
    private static final String COOLDOWN_SECTION = "cooldowns";

    public static File dataDir;
    public static String locale;
    public static boolean useColors;
    public static boolean enableInvite;
    public static boolean enableDefaultPerms;
    public static boolean downloadLibs;
    public static boolean sqliteLib;
    public static boolean mysqlLib;
    public static boolean timerByPerms;
    public static boolean additionalTime;
    public static int defaultCoolDown;
    public static boolean coolDownNotify;
    public static int defaultWarmUp;
    public static boolean warmUpNotify;
    public static int abortOnDamage;
    public static boolean abortOnMove;
    public static boolean respawnToHome;
    public static boolean enableSethome;
    public static int bedsCanSethome;
    public static boolean bedsDuringDay;
    public static boolean loadChunks;
    public static boolean usemySQL;
    public static String mySQLuname;
    public static String mySQLpass;
    public static String mySQLconn;
    public static int defaultLimit;
    public static boolean debugLog;
    public static int defaultInvLimit;
    public static boolean enableEcon;
    public static int warpCost;
    public static int setCost;
    public static boolean enableATime;
    public static boolean enableUnlock;
    public static boolean notifyOnSend;

    // Other classes should check the list via isHomeRespawnWorld()
    private static List<String> respawnToHomeWorlds;

    // Dynamic limit permissions
    public static Map<String, Integer> permLimits;
    public static Map<String, Integer> permInvLimits;
    public static Map<String, Integer> permWarmUps;
    public static Map<String, Integer> permCoolDowns;

    public static void initialize(uHome plugin, FileConfiguration config, File pluginDir, Logger log) {
        try {
            dataDir = pluginDir;
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }

            ConfigurationSection settings = config.getConfigurationSection("settings");
            ConfigurationSection timers = config.getConfigurationSection("timers");
            ConfigurationSection mysql = settings.getConfigurationSection("mysql");
            ConfigurationSection dlLibs = settings.getConfigurationSection("downloadLibs");
            ConfigurationSection defaults = config.getConfigurationSection("defaults");
            ConfigurationSection economy = config.getConfigurationSection("economy");

            ConfigurationSection homeLimits = fillLimitDefaults(config);
            ConfigurationSection homeInvLimits = fillInvLimitDefaults(config);
            ConfigurationSection warmups = fillWarmupDefaults(timers);
            ConfigurationSection cooldowns = fillCooldownDefaults(timers);

            locale = settings.getString("locale", "en");
            useColors = settings.getBoolean("useColors", true);
            respawnToHome = settings.getBoolean("respawnToHome", true);
            bedsCanSethome = settings.getInt("bedsCanSetHome", 0);
            bedsDuringDay = settings.getBoolean("bedsDuringDay", false);
            loadChunks = settings.getBoolean("loadChunks", true);
            enableInvite = settings.getBoolean("enableInvite", true);
            enableDefaultPerms = settings.getBoolean("enableDefaultPerms", true);
            enableSethome = settings.getBoolean("enableSethome", false);
            debugLog = settings.getBoolean("debugLog", false);
            enableATime = settings.getBoolean("recordLastAccess", false);
            enableUnlock = settings.getBoolean("enableUnlock", false);
            notifyOnSend = settings.getBoolean("notifyOnSend", true);

            respawnToHomeWorlds = settings.getStringList("respawnToHomeWorlds");

            downloadLibs = dlLibs.getBoolean("enable", true);
            mysqlLib = dlLibs.getBoolean("mysqlLib", true);
            sqliteLib = dlLibs.getBoolean("sqliteLib", true);

            usemySQL = mysql.getBoolean("enable", false);
            mySQLconn = mysql.getString("connection", "jdbc:mysql://localhost:3306/minecraft");
            mySQLuname = mysql.getString("username", "minecraft");
            mySQLpass = mysql.getString("password", "password");

            defaultLimit = defaults.getInt("homeLimit", 3);
            defaultInvLimit = defaults.getInt("homeInvLimit", 1);
            defaultWarmUp = defaults.getInt("warmup", 0);
            defaultCoolDown = defaults.getInt("cooldown", 0);

            coolDownNotify = timers.getBoolean("cooldownNotify", false);
            warmUpNotify = timers.getBoolean("warmupNotify", true);
            additionalTime = timers.getBoolean("additionalTime", false);
            abortOnDamage = timers.getInt("abortOnDamage", 0);
            abortOnMove = timers.getBoolean("abortOnMovement", false);
            timerByPerms = timers.getBoolean("timerByPerms", false);

            enableEcon = economy.getBoolean("enable", false);
            warpCost = economy.getInt("warpCost", 0);
            setCost = economy.getInt("setCost", 0);

            if (bedsCanSethome > 2 || bedsCanSethome < 0) {
                log.warning("bedsCanSetHome was set to an invalid value. Valid values are 0, 1 or 2. Presuming 0.");
                bedsCanSethome = 0;
            }

            if (abortOnDamage > 3 || abortOnDamage < 0) {
                log.warning("abortOnDamage was set to an invalid value. Valid values are 0, 1, 2 or 3. Presuming 0.");
                abortOnDamage = 0;
            }

            // Initialize permissions handler ready for the registration of nodes.
            SuperPermsManager.initialize(plugin);

            // Begin filling maps for custom variable nodes.
            int count = 0;
            permLimits = new LinkedHashMap<String, Integer>();

            for (Map.Entry<String, Object> obj : homeLimits.getValues(false).entrySet()) {
                if (obj.getValue() instanceof Integer) {
                    count++;
                    String permNode = "uhome.limit." + obj.getKey().substring(obj.getKey().lastIndexOf(".") + 1);
                    Integer limitValue = (Integer) obj.getValue();

                    log.info("Loaded permission node: " + permNode + " with order " + Integer.toString(count));
                    permLimits.put(permNode, limitValue);
                    SuperPermsManager.registerPermission(permNode);
                } else {
                    log.warning("Ignoring an invalid limit value in homeLimits for key: " + obj.getKey());
                }
            }
            log.info("Loaded " + Integer.toString(count) + " permission based home limits.");

            // If we're not using invites, don't bother creating the limit map.
            if (enableInvite) {
                count = 0;
                permInvLimits = new LinkedHashMap<String, Integer>();

                for (Map.Entry<String, Object> obj : homeInvLimits.getValues(false).entrySet()) {
                    if (obj.getValue() instanceof Integer) {
                        count++;
                        String permNode = "uhome.invlimit." + obj.getKey().substring(obj.getKey().lastIndexOf(".") + 1);
                        Integer limitValue = (Integer) obj.getValue();

                        log.info("Loaded permission node: " + permNode + " with order " + Integer.toString(count));
                        permInvLimits.put(permNode, limitValue);
                        SuperPermsManager.registerPermission(permNode);
                    } else {
                        log.warning("Ignoring an invalid limit value in homeInvLimits for key: " + obj.getKey());
                    }
                }

                log.info("Loaded " + Integer.toString(count) + " permission based invite limits.");
            }

            // If we're not using timer perms, don't bother populating the maps.
            if (timerByPerms) {
                count = 0;
                permWarmUps = new LinkedHashMap<String, Integer>();

                for (Map.Entry<String, Object> obj : warmups.getValues(false).entrySet()) {
                    if (obj.getValue() instanceof Integer) {
                        count++;
                        String permNode = "uhome.warmup." + obj.getKey().substring(obj.getKey().lastIndexOf(".") + 1);
                        Integer limitValue = (Integer) obj.getValue();

                        log.info("Loaded permission node: " + permNode + " with order " + Integer.toString(count));
                        permWarmUps.put(permNode, limitValue);
                        SuperPermsManager.registerPermission(permNode);
                    } else {
                        log.warning("Ignoring an invalid time value in warmups for key: " + obj.getKey());
                    }
                }

                log.info("Loaded " + Integer.toString(count) + " permission based warmup times.");

                count = 0;
                permCoolDowns = new LinkedHashMap<String, Integer>();

                for (Map.Entry<String, Object> obj : cooldowns.getValues(false).entrySet()) {
                    if (obj.getValue() instanceof Integer) {
                        count++;
                        String permNode = "uhome.cooldown." + obj.getKey().substring(obj.getKey().lastIndexOf(".") + 1);
                        Integer limitValue = (Integer) obj.getValue();

                        log.info("Loaded permission node: " + permNode + " with order " + Integer.toString(count));
                        permCoolDowns.put(permNode, limitValue);
                        SuperPermsManager.registerPermission(permNode);
                    } else {
                        log.warning("Ignoring an invalid time value in cooldowns for key: " + obj.getKey());
                    }
                }

                log.info("Loaded " + Integer.toString(count) + " permission based cooldown times.");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Unable to load config", ex);
        }
    }

    public static boolean isHomeRespawnWorld(String world) {
        if (respawnToHomeWorlds != null) {
            // If the list is empty, no world restriction. If not empty, true if listed.
            return respawnToHomeWorlds.isEmpty() || respawnToHomeWorlds.contains(world);
        } else {
            // If no list present, no world restriction.
            return true;
        }
    }

    private static ConfigurationSection fillLimitDefaults(ConfigurationSection parent) {
        ConfigurationSection ret;

        if (! parent.isConfigurationSection(HOME_LIMIT_SECTION)) {
            parent.set(HOME_LIMIT_SECTION, null);
            ret = parent.createSection(HOME_LIMIT_SECTION);

            ret.addDefault("a", 30);
            ret.addDefault("b", 20);
            ret.addDefault("c", 15);
            ret.addDefault("d", 10);
            ret.addDefault("e", 5);
        } else {
            ret = parent.getConfigurationSection(HOME_LIMIT_SECTION);
        }

        return ret;
    }

    private static ConfigurationSection fillInvLimitDefaults(ConfigurationSection parent) {
        ConfigurationSection ret;

        if (! parent.isConfigurationSection(INV_LIMIT_SECTION)) {
            parent.set(INV_LIMIT_SECTION, null);
            ret = parent.createSection(INV_LIMIT_SECTION);

            ret.addDefault("a", 10);
            ret.addDefault("b", 8);
            ret.addDefault("c", 6);
            ret.addDefault("d", 4);
            ret.addDefault("e", 2);
        } else {
            ret = parent.getConfigurationSection(INV_LIMIT_SECTION);
        }

        return ret;
    }

    private static ConfigurationSection fillCooldownDefaults(ConfigurationSection parent) {
        ConfigurationSection ret;

        if (! parent.isConfigurationSection(COOLDOWN_SECTION)) {
            parent.set(COOLDOWN_SECTION, null);
            ret = parent.createSection(COOLDOWN_SECTION);

            ret.addDefault("a", 0);
            ret.addDefault("b", 5);
            ret.addDefault("c", 10);
            ret.addDefault("d", 15);
            ret.addDefault("e", 20);
        } else {
            ret = parent.getConfigurationSection(COOLDOWN_SECTION);
        }

        return ret;
    }

    private static ConfigurationSection fillWarmupDefaults(ConfigurationSection parent) {
        ConfigurationSection ret;

        if (! parent.isConfigurationSection(WARMUP_SECTION)) {
            parent.set(WARMUP_SECTION, null);
            ret = parent.createSection(WARMUP_SECTION);

            ret.addDefault("a", 0);
            ret.addDefault("b", 5);
            ret.addDefault("c", 10);
            ret.addDefault("d", 15);
            ret.addDefault("e", 20);
        } else {
            ret = parent.getConfigurationSection(WARMUP_SECTION);
        }

        return ret;
    }
}
