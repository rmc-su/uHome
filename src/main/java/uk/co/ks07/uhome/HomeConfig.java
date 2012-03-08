package uk.co.ks07.uhome;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HomeConfig {

    public static File dataDir;
    public static String locale;
    public static boolean useColors;
    public static boolean enableInvite;
    public static boolean enableDenyPerm;
    public static boolean downloadLibs;
    public static boolean sqliteLib;
    public static boolean mysqlLib;
    public static boolean timerByPerms;
    public static boolean additionalTime;
    public static int defaultCoolDown;
    public static boolean coolDownNotify;
    public static int defaultWarmUp;
    public static int[] warmUps;
    public static int[] coolDowns;
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
    public static int[] limits;
    public static int defaultLimit;
    public static boolean debugLog;
    public static int[] invlimits;
    public static int defaultInvLimit;

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

            limits = new int[5];
            coolDowns = new int[5];
            warmUps = new int[5];
            invlimits = new int[5];

            locale = settings.getString("locale", "en");
            useColors = settings.getBoolean("useColors", true);
            respawnToHome = settings.getBoolean("respawnToHome", true);
            bedsCanSethome = settings.getInt("bedsCanSetHome", 0);
            bedsDuringDay = settings.getBoolean("bedsDuringDay", false);
            loadChunks = settings.getBoolean("loadChunks", false);
            enableInvite = settings.getBoolean("enableInvite", true);
            enableDenyPerm = settings.getBoolean("enableDenyPerm", false);
            enableSethome = settings.getBoolean("enableSethome", false);
            debugLog = settings.getBoolean("debugLog", false);

            downloadLibs = dlLibs.getBoolean("enable", true);
            mysqlLib = dlLibs.getBoolean("mysqlLib", true);
            sqliteLib = dlLibs.getBoolean("sqliteLib", true);


            usemySQL = mysql.getBoolean("enable", false);
            mySQLconn = mysql.getString("connection", "jdbc:mysql://localhost:3306/minecraft");
            mySQLuname = mysql.getString("username", "minecraft");
            mySQLpass = mysql.getString("password", "password");

            limits[0] = homeLimits.getInt("limitA", 0);
            limits[1] = homeLimits.getInt("limitB", 20);
            limits[2] = homeLimits.getInt("limitC", 15);
            limits[3] = homeLimits.getInt("limitD", 10);
            limits[4] = homeLimits.getInt("limitE", 5);
            defaultLimit = homeLimits.getInt("default", 3);

            invlimits[0] = homeInvLimits.getInt("invlimitA", 10);
            invlimits[1] = homeInvLimits.getInt("invlimitB", 8);
            invlimits[2] = homeInvLimits.getInt("invlimitC", 6);
            invlimits[3] = homeInvLimits.getInt("invlimitD", 4);
            invlimits[4] = homeInvLimits.getInt("invlimitE", 2);
            defaultInvLimit = homeInvLimits.getInt("default", 1);

            coolDownNotify = timers.getBoolean("cooldownNotify", false);
            warmUpNotify = timers.getBoolean("warmupNotify", true);
            timerByPerms = timers.getBoolean("timerByPerms", false);
            additionalTime = timers.getBoolean("additionalTime", false);
            abortOnDamage = timers.getInt("abortOnDamage", 0);
            abortOnMove = timers.getBoolean("abortOnMovement", false);


            coolDowns[0] = cooldowns.getInt("cooldownA", 0);
            coolDowns[1] = cooldowns.getInt("cooldownB", 5);
            coolDowns[2] = cooldowns.getInt("cooldownC", 10);
            coolDowns[3] = cooldowns.getInt("cooldownD", 15);
            coolDowns[4] = cooldowns.getInt("cooldownE", 20);
            defaultCoolDown = cooldowns.getInt("default", 0);

            warmUps[0] = warmups.getInt("warmupA", 0);
            warmUps[1] = warmups.getInt("warmupB", 5);
            warmUps[2] = warmups.getInt("warmupC", 10);
            warmUps[3] = warmups.getInt("warmupD", 15);
            warmUps[4] = warmups.getInt("warmupE", 20);
            defaultWarmUp = warmups.getInt("default", 0);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Unable to load config", ex);
        }
    }
}
