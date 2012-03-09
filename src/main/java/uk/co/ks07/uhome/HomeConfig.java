package uk.co.ks07.uhome;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HomeConfig {

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
    
    // Dynamic limit permissions
    public static Map<String, Integer> permLimits;
    public static Map<String, Integer> permInvLimits;
    public static Map<String, Integer> permWarmUps;
    public static Map<String, Integer> permCoolDowns;

    public static void initialize(FileConfiguration config, File pluginDir, Logger log) {
        try {
            dataDir = pluginDir;
            if (!dataDir.exists()) {
                dataDir.mkdir();
            }

            ConfigurationSection settings = config.getConfigurationSection("settings");
            ConfigurationSection homeLimits = config.getConfigurationSection("homeLimits");
            ConfigurationSection homeInvLimits = config.getConfigurationSection("homeInvLimits");
            ConfigurationSection timers = config.getConfigurationSection("timers");
            ConfigurationSection cooldowns = timers.getConfigurationSection("cooldowns");
            ConfigurationSection warmups = timers.getConfigurationSection("warmups");
            ConfigurationSection mysql = settings.getConfigurationSection("mysql");
            ConfigurationSection dlLibs = settings.getConfigurationSection("downloadLibs");
            ConfigurationSection defaults = config.getConfigurationSection("defaults");

            locale = settings.getString("locale", "en");
            useColors = settings.getBoolean("useColors", true);
            respawnToHome = settings.getBoolean("respawnToHome", true);
            bedsCanSethome = settings.getInt("bedsCanSetHome", 0);
            bedsDuringDay = settings.getBoolean("bedsDuringDay", false);
            loadChunks = settings.getBoolean("loadChunks", false);
            enableInvite = settings.getBoolean("enableInvite", true);
            enableDefaultPerms = settings.getBoolean("enableDefaultPerms", true);
            enableSethome = settings.getBoolean("enableSethome", false);
            debugLog = settings.getBoolean("debugLog", false);

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
}
