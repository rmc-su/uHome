package uk.co.ks07.uhome;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;

import uk.co.ks07.uhome.griefcraft.Updater;
import uk.co.ks07.uhome.locale.LocaleManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class uHome extends JavaPlugin {
	private UHPlayerListener playerListener;
	private UHEntityListener entityListener;
	private HomeList homeList;
	public String name;
	public String version;
	private Updater updater;
	public PluginManager pm;
        public FileConfiguration config;

	@Override
	public void onDisable() {
		ConnectionManager.closeConnection();
		HomeLogger.info(name + " " + version + " disabled");
	}

	@Override
	public void onEnable() {
		this.pm = getServer().getPluginManager();
		this.name = this.getDescription().getName();
		this.version = this.getDescription().getVersion();
                this.config = this.getConfig();
                
                try {
                    this.config.options().copyDefaults(true);
                    this.saveConfig();
                    HomeConfig.initialize(config, getDataFolder());
                } catch (Exception ex) {
                    HomeLogger.severe("Could not load config!", ex);
                }

		libCheck();
		boolean needImport = convertOldDB(getDataFolder());
		if(!sqlCheck()) { return; }
		
		homeList = new HomeList(getServer(), needImport);
		LocaleManager.init();
		HomeHelp.initialize(this);
		
		playerListener = new UHPlayerListener(homeList, this);
		entityListener = new UHEntityListener(this);

                this.getCommand("sethome").setExecutor(new SetHomeCommand(this, homeList));
                this.getCommand("home").setExecutor(new HomeCommand(this, homeList));
		
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		
		if(HomeConfig.respawnToHome) { 
			// Dont need this if we're not handling respawning.
			pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Priority.Highest, this);
		}
		if(HomeConfig.loadChunks) {
			// We dont need to register for teleporting if we dont want to load chunks.
			pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
		}
		if(HomeConfig.abortOnDamage != 0) {
			// We dont need this if we're not aborting warmups for combat.
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
		}
		if(HomeConfig.abortOnMove) { 
			// We dont need this if we're not aborting if they move during warmup.
			pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
		}
		if(HomeConfig.bedsDuringDay && HomeConfig.bedsCanSethome != 0) {
			// We don't need this if the beds cannot be used during the day
			pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		} else if(!HomeConfig.bedsDuringDay && HomeConfig.bedsCanSethome != 0) {
			// We don't need this if the beds dont sethome
			pm.registerEvent(Event.Type.PLAYER_BED_LEAVE, playerListener, Priority.Monitor, this);
		}

		HomeLogger.info(name + " " + version + " enabled");
	}


	private void libCheck(){
		if(HomeConfig.downloadLibs){ 
			updater = new Updater();
			try {
				updater.check();
				updater.update();
			} catch (Exception e) {
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
		Connection conn = ConnectionManager.initialize();
		if (conn == null) {
			HomeLogger.severe("Could not establish SQL connection. Disabling uHome");
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
			HomeLogger.severe("Could not create new database file", ex);
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