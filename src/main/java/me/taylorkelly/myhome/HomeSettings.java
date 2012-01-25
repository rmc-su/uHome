package me.taylorkelly.myhome;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import me.taylorkelly.myhome.utils.HomeLogger;

public class HomeSettings {
	public static File dataDir;
	
	public static boolean compassPointer;
	
	public static String locale;
	public static boolean useColors;

	public static boolean downloadLibs;
	public static boolean sqliteLib;
	public static boolean mysqlLib;

	public static boolean enableBypassPerms;

	public static boolean timerByPerms;
	public static boolean additionalTime;
	public static int coolDown;
	public static boolean coolDownNotify;
	public static int coolDownSetHome;
	public static int warmUp;
	public static boolean warmUpNotify;
	public static int abortOnDamage;
	public static boolean abortOnMove;

	public static boolean respawnToHome;
	public static boolean allowSetHome;
	public static boolean homesArePublic;
	public static int bedsCanSethome;
	public static boolean bedsDuringDay;
	public static boolean opPermissions;
	//public static boolean oneHomeAllWorlds;

	public static boolean loadChunks; 

	public static boolean eConomyEnabled;
	public static boolean enableFreePerms;
	public static int setHomeCost;
	public static int homeCost;
	public static boolean costByPerms;
	public static boolean additionalCosts;

	public static boolean usemySQL;
	public static String mySQLuname;
	public static String mySQLpass;
	public static String mySQLdb;
	public static String mySQLtable;
	public static String mySQLhost;
	public static int mySQLport;
	
	public static boolean applyDistanceLimits;
	public static boolean enableDistanceBypass;
	public static boolean distanceByPerms;
	public static boolean additionalDistance;
	public static int maxDistance;

	public static void initialize(FileConfiguration config, File dataFolder) {
		dataDir = dataFolder;
		try {
			ConfigurationSection confsettings = config.getConfigurationSection("settings");
			ConfigurationSection conftimers = config.getConfigurationSection("timers");
			ConfigurationSection confeconomy = config.getConfigurationSection("economy");
			ConfigurationSection confdatabase = config.getConfigurationSection("mysql");
			ConfigurationSection conflocale = config.getConfigurationSection("locale");
			ConfigurationSection conflibs = config.getConfigurationSection("libraries");
			ConfigurationSection confdist = config.getConfigurationSection("distances");

			// Locale
			locale = conflocale.getString("locale", "en");
			useColors = conflocale.getBoolean("useColors", true);
			
			// Timers
			coolDown = conftimers.getInt("globalCoolDown", 0);
			warmUp = conftimers.getInt("globalWarmUp", 0);
			coolDownSetHome = conftimers.getInt("globalCoolDownSetHome", 0);
			coolDownNotify = conftimers.getBoolean("coolDownNotify", false);
			warmUpNotify = conftimers.getBoolean("warmUpNotify", true);
			timerByPerms = conftimers.getBoolean("timerByPerms", false);
			additionalTime = conftimers.getBoolean("additionalTime", false);
			abortOnDamage = conftimers.getInt("abortOnDamage", 0);
			abortOnMove = conftimers.getBoolean("abortOnMove", false);

			// Settings
			compassPointer = confsettings.getBoolean("compassPointer", true);
			allowSetHome = confsettings.getBoolean("allowSetHome", true);
			respawnToHome = confsettings.getBoolean("respawnToHome", true);
			homesArePublic = confsettings.getBoolean("homesArePublic", false);
			bedsCanSethome = confsettings.getInt("bedsCanSethome", 0);
			bedsDuringDay = confsettings.getBoolean("bedsDuringDay", false);
			enableBypassPerms = confsettings.getBoolean("enableBypassPerms", true);
			loadChunks = confsettings.getBoolean("loadChunks", false);
			opPermissions = confsettings.getBoolean("opPermissions", true);

			// Economy
			eConomyEnabled = confeconomy.getBoolean("enabled", false);
			enableFreePerms = confeconomy.getBoolean("enableFreePerms", true);
			setHomeCost = confeconomy.getInt("globalSetHomeCost", 0);
			homeCost = confeconomy.getInt("globalHomeCost", 0);
			costByPerms = confeconomy.getBoolean("costByPerms", false);
			additionalCosts = confeconomy.getBoolean("additionalCosts", false);

			// Libs
			downloadLibs = conflibs.getBoolean("downloadLibs", true);
			mysqlLib = conflibs.getBoolean("mysqlLib", true);
			sqliteLib = conflibs.getBoolean("sqliteLib", true);

			// Database
			usemySQL = confdatabase.getBoolean("enabled", false);
			mySQLhost = confdatabase.getString("host", "'localhost'");
			mySQLport = confdatabase.getInt("port", 3306);
			mySQLuname = confdatabase.getString("username", "'root'");
			mySQLpass = confdatabase.getString("password", "'password'");
			mySQLdb = confdatabase.getString("database", "'minecraft'");
			mySQLtable = confdatabase.getString("table", "'homeTable'");
			
			// Distances
			applyDistanceLimits = confdist.getBoolean("applyDistanceLimits", false);
			enableDistanceBypass = confdist.getBoolean("enableDistanceBypassPerm", false);
			distanceByPerms = confdist.getBoolean("distanceByPerms", true);
			additionalDistance = confdist.getBoolean("additionalDistance", false);
			maxDistance = confdist.getInt("globalMaxDistance", 0);
			
		} catch (Exception e) {
			HomeLogger.severe("Failed to process configuration", e);
		}
	}
}
