package me.taylorkelly.myhome.utils;

import me.taylorkelly.help.Help;
import me.taylorkelly.myhome.locale.LocaleManager;

import org.bukkit.plugin.Plugin;

/**
 *
 * @author taylor
 */
public class HomeHelp {
	public static void initialize(Plugin plugin) {
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Help");
		if (test != null) {
			Help helpPlugin = ((Help) test);
			helpPlugin.registerCommand("home", LocaleManager.getString("help.home"), plugin, true, "myhome.home.basic.home");
			helpPlugin.registerCommand("home set", LocaleManager.getString("help.homeset"), plugin, true, "myhome.home.basic.set");
			helpPlugin.registerCommand("home [player]", LocaleManager.getString("help.homeplayer"), plugin, "myhome.home.soc.others");
			helpPlugin.registerCommand("home delete", LocaleManager.getString("help.homedelete"), plugin, "myhome.home.basic.delete");
			helpPlugin.registerCommand("home invite [player]", LocaleManager.getString("help.homeinvite"), plugin, "myhome.home.soc.invite");
			helpPlugin.registerCommand("home uninvite [player]", LocaleManager.getString("help.homeuninvite"), plugin, "myhome.home.soc.uninvite");
			helpPlugin.registerCommand("home list", LocaleManager.getString("help.homelist"), plugin, "myhome.home.soc.list");
			helpPlugin.registerCommand("home ilist", LocaleManager.getString("help.homeilist"), plugin, "myhome.home.soc.list");
			helpPlugin.registerCommand("home public", LocaleManager.getString("help.homepublic"), plugin, "myhome.home.soc.public");
			helpPlugin.registerCommand("home private", LocaleManager.getString("help.homeprivate"), plugin, "myhome.home.soc.private");
			HomeLogger.info("Help plugin support enabled.");
		} else {
			HomeLogger.warning("Help plugin not detected. Only providing help via /home help.");
		}
	}
}
