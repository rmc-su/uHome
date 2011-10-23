uHome - A multiple private home warp plugin for Bukkit and Minecraft.
===========


Uses Bukkit's superperms for all permissions - works with all modern permissions systems!

For simple set-up, give your users 'uhome.own' and your admins 'uhome.admin'. A 'uhome.*' superperms node is included for ease of use.

Commands & Permissions:

    Basic Commands:
    /home - (uhome.own.warp) - Takes you to your "home" home.
    /home set - (uhome.own.set) - Sets your "home" home to your current position.
    /home delete - (uhome.own.delete) - Deletes your home "home".
    /home help - Display help

    Multihome Commands:
    /home <name> - (uhome.own.warp) - Takes you to your home with the name <name>.
    /home list - (uhome.own.list) - Displays the homes you own.
    /home set <name> - (uhome.own.set) - Sets a home called <name> to your current position.
    /home delete <name> - (uhome.own.delete) - Deletes your home named <name>.

    Admin Commands:
    /home list <player> - (uhome.admin.list) - Lists a player's homes.
    /home delete <player> <name> - (uhome.admin.delete) - Delete's a player's home.
    /home reload - (uhome.admin.reload) - Reload uHome's configuration - Do not use this for swapping to MySQL
    /home <player> <name> - (uhome.admin.warp) - Takes you to the home of <player> called <name>.
    /home set <player> <name> - (uhome.admin.set) - Sets another player's home to your location.

Permissions:

    Variable Permissions:
    (uhome.limit.[a-e]) - Gives the player the home limit reflected by the config. E.g. if limitB=10, and the player has the permission "uhome.limit.b", the player can make up to 10 homes.
    (uhome.cooldown.[a-c]) - Gives the player the cooldown time reflected by the config, as above.
    (uhome.warmup.[a-c]) - Gives the player the warmup time reflected by the config, as above.

    Bypassing Timers/Limits Permisions:
    (uhome.bypass) - Bypass all limits (cooldowns, warmups, bed usage and home limit)
    (uhome.bypass.cooldown) - Permission to bypass /home cooldowns
    (uhome.bypass.warmup) - Permission to bypass /home warmup
    (uhome.bypass.warmup.damage) - Permission to ignore damage when warming-up
    (uhome.bypass.warmup.movement) - Permission to ignore movement when warming-up
    (uhome.bypass.bed) - Permission to use /sethome when bed usage is forced.

    Admin Permissions:
    (uhome.*) - Has access to all commands in uHome
    (uhome.admin.reload) - Is able to reload uHome config.

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

    downloadLibs - default: true - Should uHome attempt to download any libraries
    sqliteLib - default: true - Should uHome attempt to download the SQLite library (downloadLibs must be true)
    mysqlLib - default: true - Should uHome attempt to download the MySQL library (downloadLibs must be true)

    allowSetHome - default: false - Should /sethome usage be enabled (/home set is not disabled)
    respawnToHome - default: false - Whether or not players will respawn to their homes (false means to global spawn)
    bedsCanSethome - default: 0 - Can using a bed do /sethome - 0 = Disabled, 1 = Using a bed will /sethome automatically, 2 = /sethome is disabled and can only be set by using a bed.
    bedsDuringDay - default: false - Whether beds can be used to /sethome during the day without sleeping in them. Must be enabbled for Skylands and bedsCanSethome must not be 0
    loadChunks - default: false - Force sending of the chunk which people teleport to - Not recommended with other chunk loaders.

    usemySQL - default: false - MySQL usage -- true = use MySQL database / false = use SQLite
    mySQLconn - default: jdbc:mysql://localhost:3306/minecraft - MySQL Connection (only if using MySQL)
    mySQLuname - default: root - MySQL Username (only if using MySQL)
    mySQLpass - default: password - MySQL Password (only if using MySQL)

