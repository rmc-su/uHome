package me.taylorkelly.myhome.locale;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.Iterator;

import org.bukkit.ChatColor;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.utils.HomeLogger;

public class LocaleManager {
	private static final String LOCALE_BUNDLE = "me.taylorkelly.myhome.locale.myhome";
	private static ResourceBundle locResBundle = null;

	public static void init() {
		String locale = HomeSettings.locale.toLowerCase();
		try {
			locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale(locale));
			HomeLogger.info("Using localization: " + locResBundle.getString("locale.name") + " (" + locale + ")");
		} catch (MissingResourceException e) {
			// Failed to load requested locale file so fallback to en_us
			locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale("en"));
			HomeLogger.warning("Failed to find locale " + locale + ". Falling back to using English (en).");
		}
	}

	public static String getString(String key) {
		return getString(key, null);
	}

	public static String getString(String key, Map<String, String> params) {
		return getString(key, params, false);
	}
	
	public static String getString(String key, Map<String, String> params, Boolean console) {
		try {
			String output = locResBundle.getString(key);

			if (params != null) {
				for(Map.Entry<String, String> e : params.entrySet()) {
					String ekey = e.getKey().toUpperCase();
					String evalue = e.getValue();
					output = output.replaceAll("(?i)\\Q{{" + ekey + "}}\\E", evalue);
				}
			}
			
			if(HomeSettings.useColors) {
				output = addColors(output);
			} else {
				output = stripColors(output);
			}
			
			if(console) {
				output = stripColors(output);
			}
			
			return output;
		} catch (MissingResourceException e) {
			return "Missing locale string: " + key;
		}
	}
	
	private static String addColors(String input) {
		input = input.replaceAll("(?i)\\Q{{BLACK}}\\E", ChatColor.BLACK.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_BLUE}}\\E", ChatColor.DARK_BLUE.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_GREEN}}\\E", ChatColor.DARK_GREEN.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_AQUA}}\\E", ChatColor.DARK_AQUA.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_RED}}\\E", ChatColor.DARK_RED.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_PURPLE}}\\E", ChatColor.DARK_PURPLE.toString());
		input = input.replaceAll("(?i)\\Q{{GOLD}}\\E", ChatColor.GOLD.toString());
		input = input.replaceAll("(?i)\\Q{{GRAY}}\\E", ChatColor.GRAY.toString());
		input = input.replaceAll("(?i)\\Q{{DARK_GRAY}}\\E", ChatColor.DARK_GRAY.toString());
		input = input.replaceAll("(?i)\\Q{{BLUE}}\\E", ChatColor.BLUE.toString());
		input = input.replaceAll("(?i)\\Q{{GREEN}}\\E", ChatColor.GREEN.toString());
		input = input.replaceAll("(?i)\\Q{{AQUA}}\\E", ChatColor.AQUA.toString());
		input = input.replaceAll("(?i)\\Q{{RED}}\\E", ChatColor.RED.toString());
		input = input.replaceAll("(?i)\\Q{{LIGHT_PURPLE}}\\E", ChatColor.LIGHT_PURPLE.toString());
		input = input.replaceAll("(?i)\\Q{{YELLOW}}\\E", ChatColor.YELLOW.toString());
		input = input.replaceAll("(?i)\\Q{{WHITE}}\\E", ChatColor.WHITE.toString());
		
		return input;
	}
	
	private static String stripColors(String input) {
		input = input.replaceAll("(?i)\\Q{{BLACK}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_BLUE}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_GREEN}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_AQUA}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_RED}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_PURPLE}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{GOLD}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{GRAY}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{DARK_GRAY}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{BLUE}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{GREEN}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{AQUA}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{RED}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{LIGHT_PURPLE}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{YELLOW}}\\E", "");
		input = input.replaceAll("(?i)\\Q{{WHITE}}\\E", "");
		
		return input;
	}
}