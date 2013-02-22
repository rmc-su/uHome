package uk.co.ks07.uhome.locale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.bukkit.ChatColor;

import uk.co.ks07.uhome.Home;
import uk.co.ks07.uhome.HomeConfig;

public class LocaleManager {

    private static final String LOCALE_BUNDLE = "uk.co.ks07.uhome.locale.uhome.uhome";
    private static ResourceBundle locResBundle = null;
    private static final DecimalFormat decLocFormatter = new DecimalFormat("#0.#");

    public static void init(File customLocale, Logger log) {
        String locale = HomeConfig.locale.toLowerCase();

        if ("file".equals(locale) || "custom".equals(locale)) {
            try {
                locResBundle = new PropertyResourceBundle(new FileInputStream(customLocale));
                log.info("Using custom localization from customlocale.properties.");
            } catch (FileNotFoundException e) {
                // Failed to load custom locale file so fallback to en.
                locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale("en"));
                log.warning("The locale file customlocale.properties does not exist! Falling back to using English (en).");
            } catch (IOException e) {
                // IO Error occured while loading custom locale file.
                locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale("en"));
                log.warning("An error occured while reading customlocale.properties! Falling back to using English (en).");
            }
        } else {
            try {
                locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale(locale));
                log.info("Using localization: " + locResBundle.getString("locale.name") + " (" + locale + ") by " + locResBundle.getString("locale.author"));
            } catch (MissingResourceException e) {
                // Failed to load requested locale file so fallback to en
                locResBundle = ResourceBundle.getBundle(LOCALE_BUNDLE, new Locale("en"));
                log.warning("Failed to find locale " + locale + ". Falling back to using English (en).");
            }
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
                    output = output.replaceAll("(?i)\\Q{{" + ekey + "}}\\E", Matcher.quoteReplacement(evalue));
                }
            }

            if (home != null) {
                output = output.replaceAll("(?i)\\Q{{HOME}}\\E", Matcher.quoteReplacement(home.name));
                output = output.replaceAll("(?i)\\Q{{OWNER}}\\E", Matcher.quoteReplacement(home.owner));
                output = output.replaceAll("(?i)\\Q{{WORLD}}\\E", Matcher.quoteReplacement(home.world));
                output = output.replaceAll("(?i)\\Q{{X}}\\E", decLocFormatter.format(home.x));
                output = output.replaceAll("(?i)\\Q{{Y}}\\E", decLocFormatter.format(home.y));
                output = output.replaceAll("(?i)\\Q{{Z}}\\E", decLocFormatter.format(home.z));
                if (home.hasInvitees()) {
                    output = output.replaceAll("(?i)\\Q{{INVITED}}\\E", Matcher.quoteReplacement(home.inviteesToString()));
                }
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
