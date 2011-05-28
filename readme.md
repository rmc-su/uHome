MyHome - A home warp plugin for Bukkit and Minecraft 
===========


Uses Permissions, GroupManager or PermissionsEx plugin to manage permissions (but is not required)

For simple set-up, give your users 'myhome.home.*'
and your moderators 'myhome.*' (includes myhome.admin and myhome.home.*)

**... is Basic**

Many server admins only want the very basics. MyHome delivers on that, and still gives the speed and reliability of using a SQLite database or MySQL database.

By only giving your users permission to myhome.home.basic.*, they will only be able to use the classic commands: /home and /home set.

Don't want your users to be able to jump home whenever they please? Only give them myhome.home.basic.set.

    /home - (myhome.home.basic.home) - Takes you to your home
    /home set - (myhome.home.basic.set) - Sets your home to your current position
    /home delete - (myhome.home.basic.delete) - Deletes your home
    /home help - Display help

**...is Social**

However, sometimes your users will demand more. They'll want to invite their friends to their home, visit other peoples homes, and see whose home they can visit

Give your users myhome.home.soc.*, and they'll be able to do all of that.

    /home <name> - (myhome.home.soc.others) - Visit <name>'s home
    /home list - (myhome.home.soc.list) - Displays whose homes you can visit
    /home ilist - (myhome.home.soc.list) - Displays who can visit your home
    /home invite <name> - (myhome.home.soc.invite) - Invites <name> to your house
    /home uninvite <name> - (myhome.home.soc.uninvite) - Uninvites <name> to your house
    /home public - (myhome.home.soc.invite) - Makes your house public
    /home private - (myhome.home.soc.uninvite) - Makes your house private

**...is Restricting**

You may be a server admin who doesn't want their users always /home'ing out of danger whenever they jolly-well-feel-like-it. MyHome allows you to set 'Warm Ups', 'Cool Downs', or both. You can set this in MyHome.settings. Or... if you don't care about these things, just leave their settings as default, which is off.

Warm Ups are the amount of time that it takes to send a player home after they use /home. So if you have a warm up time of 5 seconds; young Billy will have to evade that Creeper for 5 seconds longer before he's sent home after he hastily runs a /home command.

Cool Downs are the amount of time between which players can use the /home command. In this case, if you have a cool down of 1 minute, and Billy uses /home, he'll be sent home; however, he can't use the /home command again for another 1 minute.

SetHome Cool Downs is the amount of time which must pass between the uses of the /sethome command.

For now, Warm Ups and both Cool Downs are global, but once the Bukkit permissions/groups get more fleshed out, MH will have per user/group Warm Ups and Cool Downs.

Some server administrators may prefer that where players sleep should be considered their home. They have the option to make sleeping in a bed function as /sethome and also to disable the /sethome command to force sleeping. 

**...is Helpful**

As an server administrator, your job is tough. MyHome works to make your job a little easier (within it's domain). As an administrator, you can warp to everyone's home. You can also bypass the cooldown and warmup limits set upon your users if you'd like; check in the MyHome.settings.

Transitioning from hMod? Have a homes.txt full of your users' homes? Use /home convert to import that into the MyHome database

    (myhome.admin) - Is an admin over MyHome.
    /home convert - (myhome.convert) - Converts the homes from homes.txt into the db

