package uk.co.ks07.uhome.locale;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import uk.co.ks07.uhome.Home;

import uk.co.ks07.uhome.HomeConfig;

public class LocaleManager {

    private static final String LOCALE_BUNDLE = "uk.co.ks07.uhome.locale.uhome.uhome";
    private static ResourceBundle locResBundle = null;

    public static void init(Logger log) {
        String locale = HomeConfig.locale.toLowerCase();
        try {
            locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale(locale));
            log.info("Using localization: " + locResBundle.getString("locale.name") + " (" + locale + ")");
        } catch (MissingResourceException e) {
            // Failed to load requested locale file so fallback to en
            locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale("en"));
            log.warning("Failed to find locale " + locale + ". Falling back to using English (en).");
        }
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, Map<String, String> params) {
        return getString(key, params, null);
    }

    public static String getString(String key, Map<String, String> params, Home home) {
        try {
            String output = locResBundle.getString(key);

            if (params != null) {
                for (Map.Entry<String, String> e : params.entrySet()) {
                    String ekey = e.getKey();
                    String evalue = e.getValue();
                    output = output.replaceAll("(?i)\\Q{{" + ekey + "}}\\E", evalue);
                }
            }
            
            if (home != null) {
                    output = output.replaceAll("(?i)\\Q{{HOME}}\\E", home.name);
                    output = output.replaceAll("(?i)\\Q{{OWNER}}\\E", home.owner);
                    output = output.replaceAll("(?i)\\Q{{WORLD}}\\E", home.world);
            }

            if (HomeConfig.useColors) {
                output = addColors(output);
            } else {
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
