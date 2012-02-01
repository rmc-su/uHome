package uk.co.ks07.uhome;

import java.io.File;
import java.util.HashMap;
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
        public static HashMap<String, Integer> warmUps;
        public static HashMap<String, Integer> coolDowns;
	public static boolean warmUpNotify;
	public static int abortOnDamage;
	public static boolean abortOnMove;

	public static boolean respawnToHome;
	public static boolean allowSetHome;
	public static int bedsCanSethome;
        public static boolean bedsDuringDay;

        public static boolean loadChunks;

	public static boolean usemySQL;
	public static String mySQLuname;
	public static String mySQLpass;
	public static String mySQLconn;

        public static HashMap<String, Integer> limits;
        public static int defaultLimit;

        public static void initialize(FileConfiguration config, File pluginDir, Logger log) {
            try {
                dataDir = pluginDir;
                if (!dataDir.exists()) {
                    dataDir.mkdir();
                }

                ConfigurationSection settings = config.getConfigurationSection("settings");
                ConfigurationSection homeLimits = config.getConfigurationSection("homeLimits");
                ConfigurationSection timers = config.getConfigurationSection("timers");
                ConfigurationSection cooldowns = timers.getConfigurationSection("cooldowns");
                ConfigurationSection warmups = timers.getConfigurationSection("warmups");
                ConfigurationSection mysql = settings.getConfigurationSection("mysql");
                ConfigurationSection dlLibs = settings.getConfigurationSection("downloadLibs");

                limits = new HashMap<String, Integer>();
                coolDowns = new HashMap<String, Integer>();
                warmUps = new HashMap<String, Integer>();

                locale = settings.getString("locale", "en");
                useColors = settings.getBoolean("useColors", true);
                respawnToHome = settings.getBoolean("respawnToHome", true);
                bedsCanSethome = settings.getInt("bedsCanSetHome", 0);
                bedsDuringDay = settings.getBoolean("bedsDuringDay", false);
                loadChunks = settings.getBoolean("loadChunks", false);
                enableInvite = settings.getBoolean("enableInvite", true);
                enableDenyPerm = settings.getBoolean("enableDenyPerm", false);

                downloadLibs = dlLibs.getBoolean("enable", true);
                mysqlLib = dlLibs.getBoolean("mysqlLib", true);
                sqliteLib = dlLibs.getBoolean("sqliteLib", true);


                usemySQL = mysql.getBoolean("enable", false);
                mySQLconn = mysql.getString("connection", "jdbc:mysql://localhost:3306/minecraft");
                mySQLuname = mysql.getString("username", "minecraft");
                mySQLpass = mysql.getString("password", "password");

                limits.put("a", homeLimits.getInt("limitA", 0));
                limits.put("b", homeLimits.getInt("limitB", 20));
                limits.put("c", homeLimits.getInt("limitC", 15));
                limits.put("d", homeLimits.getInt("limitD", 10));
                limits.put("e", homeLimits.getInt("limitE", 5));
                defaultLimit = homeLimits.getInt("default", 3);

                coolDownNotify = timers.getBoolean("cooldownNotify", false);
                warmUpNotify = timers.getBoolean("warmupNotify", true);
                timerByPerms = timers.getBoolean("timerByPerms", false);
                additionalTime = timers.getBoolean("additionalTime", false);
                abortOnDamage = timers.getInt("abortOnDamage", 0);
                abortOnMove = timers.getBoolean("abortOnMovement", false);


                coolDowns.put("a", cooldowns.getInt("cooldownA", 0));
                coolDowns.put("b", cooldowns.getInt("cooldownB", 5));
                coolDowns.put("c", cooldowns.getInt("cooldownC", 10));
                defaultCoolDown = cooldowns.getInt("default", 0);

                warmUps.put("a", warmups.getInt("warmupA", 0));
                warmUps.put("b", warmups.getInt("warmupB", 5));
                warmUps.put("c", warmups.getInt("warmupC", 10));
                defaultWarmUp = warmups.getInt("default", 0);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Unable to load config", ex);
            }
        }
}