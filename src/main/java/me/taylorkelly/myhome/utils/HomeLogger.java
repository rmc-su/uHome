package me.taylorkelly.myhome.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HomeLogger {

    public static final Logger log = Logger.getLogger("Minecraft");

    public static void severe(String string, Exception ex) {
        log.log(Level.SEVERE, "[MyHome] " + string, ex);

    }

    public static void severe(String string) {
        log.log(Level.SEVERE, "[MyHome] " + string);
    }

    public static void info(String string) {
        log.log(Level.INFO, "[MyHome] " + string);
    }

    public static void warning(String string) {
        log.log(Level.WARNING, "[MyHome] " + string);
    }
}