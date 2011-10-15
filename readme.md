MyHome - A home warp plugin for Bukkit and Minecraft 
===========


Uses Permissions 2, Permissions 3, PermissionsEx or GroupManager plugins to manage permissions
Supports iConony 4, 5 & 6+, BOSEconomy 6 & 7, EssentialsEco or MultiCurrency for charging users to use MyHome

For simple set-up, give your users 'myhome.home.*' and your admins 'myhome.*'

Commands & Permissions:

    Basic Commands:
    /home - (myhome.home.basic.home) - Takes you to your home
    /home set - (myhome.home.basic.set) - Sets your home to your current position
    /home delete - (myhome.home.basic.delete) - Deletes your home
    /home help - Display help

    Social Commands:
    /home <name> - (myhome.home.soc.others) - Visit <name>'s home
    /home list - (myhome.home.soc.list) - Displays whose homes you can visit
    /home ilist - (myhome.home.soc.list) - Displays who can visit your home
    /home invite <name> - (myhome.home.soc.invite) - Invites <name> to your house
    /home uninvite <name> - (myhome.home.soc.uninvite) - Uninvites <name> to your house
    /home public - (myhome.home.soc.public) - Makes your house public
    /home private - (myhome.home.soc.private) - Makes your house private

    Admin Commands:
    /home listall - (myhome.admin.home.list) - Allow admins to list all homes
    /home clear [playername] - (myhome.admin.home.delete) - Allow an admin to delete playername's home.
    /home convert - (myhome.admin.convert) - Converts the homes from very old homes.txt into the db
    /home reload - (myhome.admin.reload) - Reload MyHome's configuration - Do not use this for swapping to MySQL

Permissions:

    Economy Permissions:
    (myhome.econ.free.*) - Allow /sethome and /home usage for free
    (myhome.econ.free.sethome) - Allow /sethome usage for free
    (myhome.econ.free.home) - Allow /home usage for free

    Bypassing Timers/Limits Permisions:
    (myhome.bypass.*) - Bypass all limits (cooldowns, warmups and bed usage etc)
    (myhome.bypass.cooldown) - Permission to bypass /home cooldowns
    (myhome.bypass.warmup) - Permission to bypass /home warmup
    (myhome.bypass.sethomecool) - Permission to bypass /sethome cooldown
    (myhome.bypass.bedsethome) - Permission to use /sethome when bed usage is forced.
    (myhome.bypass.dmgaborting) - Do not abort a /home warmup when receiving or dealing damage

    Admin Permissions:
    (myhome.admin.*) - Has access to all admin commands in MyHome
    (myhome.admin.home.any) - Admin can /home to anyone's home.

Per user/group settings:
These depend on your permissions plugin supporting them. See the documentation for your chosen permissions plugin for how to use these!

    (myhome.timer.cooldown) - The cooldown timer to use after a player uses /home (in seconds)
    (myhome.timer.warmup) - The warmup timer to use before a player is sent /home (in seconds)
    (myhome.timer.sethome) - The cooldown timer to use between allowing /sethome (in seconds)
    (myhome.costs.home) - The cost to use /home
    (myhome.costs.sethome) - The cost to use /sethome

Some help for this feature might be found in this post

Configuration Options:

    coolDown - Default: 0 - Global timer: The number of seconds between uses of /home
    coolDownNotify - default: false - Whether or not players will be notified after they've cooled down
    warmUp - Default : 0 - Global timer: The number of seconds a player has to wait before being sent /home
    warmUpNotify - default: true - Whether players should be told when they've warmed up.
    abortOnDamage - default: 0 - Warmup Aborting: 0: No aborting of /home on damage, 1: Abort for PVP damage only, 2: Abort for PVE damage only, 3: Abort for both PVP and PVE
    coolDownSetHome - default: 0 - Global Timer: Time in seconds between uses of /sethome
    timerByPerms - defaut: false - Should cooldown/warmup timers be dictated by settings in a permissions plugin.
    additionalTime - default: false - Should group/user timers be IN ADDITION to the global timers.

    compassPointer - default: true - Should the compass point to a player's /home

    downloadLibs - default: true - Should MyHome attempt to download any libraries
    sqliteLib - default: true - Should MyHome attempt to download the SQLite library (downloadLibs must be true)
    mysqlLib - default: true - Should MyHome attempt to download the MySQL library (downloadLibs must be true)

    allowSetHome - default: false - Should /sethome usage be enabled (/home set is not disabled)
    respawnToHome - default: false - Whether or not players will respawn to their homes (false means to global spawn)
    homesArePublic - default: false -Should home warps be made public by default
    bedsCanSethome - default: 0 - Can using a bed do /sethome - 0 = Disabled, 1 = Using a bed will /sethome automatically, 2 = /sethome is disabled and can only be set by using a bed.
    bedsDuringDay - default: false - Whether beds can be used to /sethome during the day without sleeping in them. Must be enabbled for Skylands and bedsCanSethome must not be 0
    loadChunks - default: false - Force sending of the chunk which people teleport to - Not recommended with other chunk loaders.

    eConomyEnabled - default: false - Whether or not to hook into an eConomy plugin.
    setHomeCost - default : 0 - Global: How much to charge the player for using /home set.
    homeCost - default: 0 - Global: How much to charge a player for using /home
    costByPerms - default: false - Should costs be dictated by settings in a permissions plugin - Per user/group costs
    additionalCosts - default: false - Should group/user costs be IN ADDITION to the global costs

    usemySQL - default: false - MySQL usage -- true = use MySQL database / false = use SQLite
    mySQLconn - default: jdbc:mysql://localhost:3306/minecraft - MySQL Connection (only if using MySQL)
    mySQLuname - default: root - MySQL Username (only if using MySQL)
    mySQLpass - default: password - MySQL Password (only if using MySQL)

