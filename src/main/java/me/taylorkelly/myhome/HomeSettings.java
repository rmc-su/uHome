package me.taylorkelly.myhome;

import java.io.File;

public class HomeSettings {
	private static final String settingsFile = "MyHome.settings";
	public static File dataDir;
	public static boolean compassPointer;
	
	public static int coolDown;
	public static boolean coolDownNotify;
	
	public static int warmUp;
	public static boolean warmUpNotify;
	public static int abortOnDamage;
	
	public static boolean respawnToHome;
	//public static boolean adminsObeyWarmsCools;
	public static boolean allowSetHome;
	public static int coolDownSetHome;
	public static boolean timerByPerms;
	public static boolean additionalTime;
	public static boolean homesArePublic;
	public static int bedsCanSethome;
	public static boolean bedsDuringDay;
	//public static boolean oneHomeAllWorlds;
	public static boolean loadChunks; 

	public static boolean eConomyEnabled;
	public static int setHomeCost;
	public static int homeCost;
	public static boolean costByPerms;
	public static boolean additionalCosts;
	
	public static boolean usemySQL;
	public static String mySQLuname;
	public static String mySQLpass;
	public static String mySQLconn;

	public static void initialize(File dataFolder) {
		if(!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		File configFile  = new File(dataFolder, settingsFile);
		PropertiesFile file = new PropertiesFile(configFile);
		dataDir = dataFolder;

		compassPointer = file.getBoolean("compassPointer", true, "Whether or not users' compasses point to home");

		coolDown = file.getInt("coolDown", 0, "Global: The number of seconds between when users can go to a home");
		warmUp = file.getInt("warmUp", 0, "Global: The number of seconds after a user uses a home command before it takes them");
		coolDownNotify = file.getBoolean("coolDownNotify", false, "Whether or not players will be notified after they've cooled down");
		warmUpNotify = file.getBoolean("warmUpNotify", true, "Whether or not players will be notified after they've warmed up");
		coolDownSetHome = file.getInt("coolDownSetHome", 0, "Global: The number of seconds between each use of /home set");
		timerByPerms = file.getBoolean("timerByPerms", false, "Should cooldown/warmup timers be dictated by settings in a permissions plugin - Per user/group");
		additionalTime = file.getBoolean("additionalTime", false, "Should group/user timers be IN ADDITION to the global timers");
		abortOnDamage = file.getInt("abortOnDamage", 0, "Global: 0: No aborting, 1: Abort for PVP only, 2: Abort for PVE only, 3: Abort for both pvp/pve damage");
		
		allowSetHome = file.getBoolean("allowSetHome", false, "Whether MyHome should also watch for /sethome - This may cause conflicts with Essentials");
		//		adminsObeyWarmsCools = file.getBoolean("adminsObeyWarmsCools", true, "Whether or not admins obey the WarmUp + CoolDown times (false means they don't)");
		respawnToHome = file.getBoolean("respawnToHome", true, "Whether or not players will respawn to their homes (false means to global spawn)");
		homesArePublic = file.getBoolean("homesArePublic", false, "Should home warps be made public by default");
		bedsCanSethome = file.getInt("bedsCanSethome", 0, "0 = Disabled, 1 = Using a bed will /sethome automatically, 2 = /sethome is disabled and can only be set by using a bed ");
		bedsDuringDay = file.getBoolean("bedsDuringDay", false, "Whether beds can be used to /sethome during the day without sleeping in them. bedsCanSethome enables using beds.");
		//oneHomeAllWorlds = file.getBoolean("oneHomeAllWorlds", true, "Only allow one home for all worlds on the server - False = one home per world");
		loadChunks = file.getBoolean("loadChunks", false, "Force sending of the chunk which people teleport to - default: false - Not recommended with other chunk loaders");

		// Economy
		eConomyEnabled = file.getBoolean("eConomyEnabled", false, "Whether or not to hook into an eConomy plugin");
		setHomeCost = file.getInt("setHomeCost", 0, "Global: How much to charge the player for using /home set");
		homeCost = file.getInt("homeCost", 0, "Global: How much to charge a player for using /home");
		costByPerms = file.getBoolean("costByPerms", false, "Should costs be dictated by settings in a permissions plugin - Per user/group costs");
		additionalCosts = file.getBoolean("additionalCosts", false, "Should group/user costs be IN ADDITION to the global costs");

		// MySQL
		usemySQL = file.getBoolean("usemySQL", false, "MySQL usage --  true = use MySQL database / false = use SQLite");
		mySQLconn = file.getString("mySQLconn", "jdbc:mysql://localhost:3306/minecraft", "MySQL Connection (only if using MySQL)");
		mySQLuname = file.getString("mySQLuname", "root", "MySQL Username (only if using MySQL)");
		mySQLpass = file.getString("mySQLpass", "password", "MySQL Password (only if using MySQL)");

		file.save();
	}
}
