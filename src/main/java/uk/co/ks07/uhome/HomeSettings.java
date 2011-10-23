package uk.co.ks07.uhome;

import java.io.File;
import java.util.HashMap;

public class HomeSettings {
	private static final String settingsFile = "uHome.settings";
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
	public static int defaultCoolDown;
	public static boolean coolDownNotify;
	public static int coolDownSetHome;
	public static int defaultWarmUp;
        public static HashMap<String, Integer> warmUps;
        public static HashMap<String, Integer> coolDowns;
	public static boolean warmUpNotify;
	public static int abortOnDamage;
	public static boolean abortOnMove;
	
	public static boolean respawnToHome;
	public static boolean allowSetHome;
	public static boolean homesArePublic;
	public static int bedsCanSethome;
	public static boolean bedsDuringDay;
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
	public static String mySQLconn;

        public static HashMap<String, Integer> limits;
        public static int defaultLimit;


	public static void initialize(File dataFolder) {
		if(!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		File configFile  = new File(dataFolder, settingsFile);
		PropertiesFile file = new PropertiesFile(configFile);
		dataDir = dataFolder;

                limits = new HashMap<String, Integer>();
                coolDowns = new HashMap<String, Integer>();
                warmUps = new HashMap<String, Integer>();

		compassPointer = file.getBoolean("compassPointer", true, "Whether or not users' compasses point to home");

		locale = file.getString("locale", "en", "Localization: what language to use for uHome");
		useColors = file.getBoolean("useColors", true, "Should messages in uHome use colors?");
		
		enableBypassPerms = file.getBoolean("enableBypassPerms", true, "Enable the bypass permissions? If you use a * permission and dont want bypassing, set to false");
		
		defaultCoolDown = file.getInt("defaultCoolDown", 0, "Global: The default number of seconds between when users can go to a home");
		defaultWarmUp = file.getInt("defaultWarmUp", 0, "Global: The default number of seconds after a user uses a home command before it takes them");
		coolDownNotify = file.getBoolean("coolDownNotify", false, "Whether or not players will be notified after they've cooled down");
		warmUpNotify = file.getBoolean("warmUpNotify", true, "Whether or not players will be notified after they've warmed up");
		coolDownSetHome = file.getInt("coolDownSetHome", 0, "Global: The number of seconds between each use of /home set");
		timerByPerms = file.getBoolean("timerByPerms", false, "Should cooldown/warmup timers be dictated by settings in a permissions plugin - Per user/group");
		additionalTime = file.getBoolean("additionalTime", false, "Should group/user timers be IN ADDITION to the global timers");
		abortOnDamage = file.getInt("abortOnDamage", 0, "Global: 0: No aborting, 1: Abort for PVP only, 2: Abort for PVE only, 3: Abort for both pvp/pve damage");
		abortOnMove = file.getBoolean("abortOnMove", false, "Should /home abort the warmup if the player moves during that time");
		
		respawnToHome = file.getBoolean("respawnToHome", true, "Whether or not players will respawn to their homes (false means to global spawn)");
		bedsCanSethome = file.getInt("bedsCanSethome", 0, "0 = Disabled, 1 = Using a bed will /sethome automatically, 2 = /sethome is disabled and can only be set by using a bed ");
		bedsDuringDay = file.getBoolean("bedsDuringDay", false, "Whether beds can be used to /sethome during the day without sleeping in them. bedsCanSethome enables using beds.");
		loadChunks = file.getBoolean("loadChunks", false, "Force sending of the chunk which people teleport to - default: false - Not recommended with other chunk loaders");
		mysqlLib = file.getBoolean("mysqlLib", true, "Should uHome attempt to download the mysql lib- downloadLibs must be true to use this");
		sqliteLib = file.getBoolean("sqliteLib", true, "Should uHome download the sqlite lib - downloadLibs must be true to use this");
		downloadLibs = file.getBoolean("downloadLibs", true, "Should uHome attempt to download any libraries");
		
		// Economy
//		eConomyEnabled = file.getBoolean("eConomyEnabled", false, "Whether or not to hook into an eConomy plugin");
//		enableFreePerms = file.getBoolean("enableFreePerms", true, "Enable the permissions to allow free usage of /home and /sethome - If you use a * permission and dont want it to be free");
//		setHomeCost = file.getInt("setHomeCost", 0, "Global: How much to charge the player for using /home set");
//		homeCost = file.getInt("homeCost", 0, "Global: How much to charge a player for using /home");
//		costByPerms = file.getBoolean("costByPerms", false, "Should costs be dictated by settings in a permissions plugin - Per user/group costs");
//		additionalCosts = file.getBoolean("additionalCosts", false, "Should group/user costs be IN ADDITION to the global costs");

		// MySQL
		usemySQL = file.getBoolean("usemySQL", false, "MySQL usage --  true = use MySQL database / false = use SQLite");
		mySQLconn = file.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft", "MySQL Connection (only if using MySQL)");
		mySQLuname = file.getString("mySQLuname", "root", "MySQL Username (only if using MySQL)");
		mySQLpass = file.getString("mySQLpass", "password", "MySQL Password (only if using MySQL)");

                // Home Limits
                limits.put("a", file.getInt("limitA", 0, "The home limit A. A takes highest precedence."));
                limits.put("b", file.getInt("limitB", 20, "The home limit B."));
                limits.put("c", file.getInt("limitC", 15, "The home limit C."));
                limits.put("d", file.getInt("limitD", 10, "The home limit D."));
                limits.put("e", file.getInt("limitE", 5, "The home limit E. E takes lowest precedence."));
                defaultLimit = file.getInt("defaultLimit", 3, "The default home limit for players with no permission. Set to 0 for unlimited.");

                // Cooldown & Warmup timers
                coolDowns.put("a", file.getInt("cooldownA", 0, "The cooldown time A. A takes highest precedence."));
                coolDowns.put("b", file.getInt("cooldownB", 5, "The cooldown time B."));
                coolDowns.put("c", file.getInt("cooldownC", 10, "The cooldown time C. C takes lowest precedence."));
                warmUps.put("a", file.getInt("warmupA", 0, "The warmup time A. A takes highest precedence."));
                warmUps.put("b", file.getInt("warmupB", 5, "The warmup time B."));
                warmUps.put("c", file.getInt("warmupC", 10, "The warmup time C. C takes lowest precedence."));

		file.save();
	}
}
