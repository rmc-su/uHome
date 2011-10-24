package me.taylorkelly.myhome.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.MyHome;
import me.taylorkelly.myhome.griefcraft.Updater;
import me.taylorkelly.myhome.sql.ConnectionManager;

public class MHUtils {
	private MyHome plugin;
	private Updater updater;
	
	public MHUtils(MyHome plugin) {
		this.plugin = plugin;
	}
	
	public void startupChecks() {
		libCheck();
		convertOldDB(plugin.getDataFolder());
		if(!sqlCheck()) { return; }
	}
	
	public void libCheck(){
		if(HomeSettings.downloadLibs){ 
			updater = new Updater();
			try {
				updater.check();
				updater.update();
			} catch (Exception e) {
			}
		}
	}

	public void convertOldDB(File df) {
		File newDatabase = new File(df, "homes.db");
		File oldDatabase = new File("homes-warps.db");
		if (!newDatabase.exists() && oldDatabase.exists()) {
			updateFiles(oldDatabase, newDatabase);
			oldDatabase.renameTo(new File("homes-warps.db.old"));
		} else if (newDatabase.exists() && oldDatabase.exists()) {
			// We no longer need this file since homes.db exists
			oldDatabase.renameTo(new File("homes-warps.db.old"));
		}
	}

	public boolean sqlCheck() {
		Connection conn = ConnectionManager.initialize();
		if (conn == null) {
			HomeLogger.severe("Could not establish SQL connection. Disabling MyHome");
			plugin.disablePlugin();
			return false;
		} 
		return true;
	}

	public void updateFiles(File oldDatabase, File newDatabase) {
		if (!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		if (newDatabase.exists()) {
			newDatabase.delete();
		}
		try {
			newDatabase.createNewFile();
		} catch (IOException ex) {
			HomeLogger.severe("Could not create new database file", ex);
		}
		copyFile(oldDatabase, newDatabase);
	}

	/**
	 * File copier from xZise
	 * @param fromFile
	 * @param toFile
	 */
	private static void copyFile(File fromFile, File toFile) {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead);
			}
		} catch (IOException ex) {
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException e) {
				}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
