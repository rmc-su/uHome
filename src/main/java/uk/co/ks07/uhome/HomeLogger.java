package uk.co.ks07.uhome;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class HomeLogger {

    public static final Logger log = Bukkit.getLogger();

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[uHome] " + string, ex);

    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[uHome] " + string);
    }

    public static void info(String string) {
        log.log(Level.INFO, "[uHome] " + string);
    }

    public static void warning(String string) {
        log.log(Level.WARNING, "[uHome] " + string);
    }
}
