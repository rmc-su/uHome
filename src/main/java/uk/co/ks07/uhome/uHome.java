package uk.co.ks07.uhome;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;

import uk.co.ks07.uhome.griefcraft.Updater;
import uk.co.ks07.uhome.locale.LocaleManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class uHome extends JavaPlugin {

    private HomeList homeList;
    public String name;
    public String version;
    private Updater updater;
    public PluginManager pm;
    public FileConfiguration config;
    public static final String DEFAULT_HOME = "home";

    @Override
    public void onDisable() {
        ConnectionManager.closeConnection(this.getLogger());
    }

    @Override
    public void onEnable() {
        this.pm = getServer().getPluginManager();
        this.name = this.getDescription().getName();
        this.version = this.getDescription().getVersion();
        this.config = this.getConfig();

        this.getLogger().setLevel(Level.INFO);

        try {
            this.config.options().copyDefaults(true);
            this.saveConfig();
            HomeConfig.initialize(config, getDataFolder(), this.getLogger());
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Could not load config!", ex);
        }

        libCheck();
        boolean needImport = convertOldDB(getDataFolder());
        if (!sqlCheck()) {
            return;
        }

        homeList = new HomeList(getServer(), needImport, this.getLogger());
        LocaleManager.init(this.getLogger());
        HomeHelp.initialize(this);

        this.getCommand("sethome").setExecutor(new SetHomeCommand(this, homeList));
        this.getCommand("home").setExecutor(new HomeCommand(this, homeList));

        this.pm.registerEvents(new UHomeListener(this, this.homeList), this);
    }

    private void libCheck() {
        if (HomeConfig.downloadLibs) {
            updater = new Updater();
            try {
                updater.check();
                updater.update();
            } catch (Exception e) {
                this.getLogger().log(Level.WARNING, "Failed to update libs.");
            }
        }
    }

    private boolean convertOldDB(File df) {
        File oldDatabase = new File(df, "homes.db");
        File newDatabase = new File(df, "uhomes.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            // Create new database file.
            updateFiles(newDatabase);
            oldDatabase.renameTo(new File(df, "homes.db.old"));

            // Return true if importing is required (sqlite only).
            if (!HomeConfig.usemySQL) {
                return true;
            }
        } else if (newDatabase.exists() && oldDatabase.exists()) {
            // We no longer need this file since uhomes.db exists
            oldDatabase.renameTo(new File(df, "homes.db.old"));
        }
        return false;
    }

    private boolean sqlCheck() {
        Connection conn = ConnectionManager.initialize(this.getLogger());
        if (conn == null) {
            this.getLogger().log(Level.SEVERE, "Could not establish SQL connection.");
            pm.disablePlugin(this);
            return false;
        }
        return true;
    }

    private void updateFiles(File newDatabase) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (newDatabase.exists()) {
            newDatabase.delete();
        }
        try {
            newDatabase.createNewFile();
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not create new database file", ex);
        }
    }
    /**
     * File copier from xZise
     * @param fromFile
     * @param toFile
     */
//	private static void copyFile(File fromFile, File toFile) {
//		FileInputStream from = null;
//		FileOutputStream to = null;
//		try {
//			from = new FileInputStream(fromFile);
//			to = new FileOutputStream(toFile);
//			byte[] buffer = new byte[4096];
//			int bytesRead;
//
//			while ((bytesRead = from.read(buffer)) != -1) {
//				to.write(buffer, 0, bytesRead);
//			}
//		} catch (IOException ex) {
//		} finally {
//			if (from != null) {
//				try {
//					from.close();
//				} catch (IOException e) {
//				}
//			}
//			if (to != null) {
//				try {
//					to.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//	}
}
