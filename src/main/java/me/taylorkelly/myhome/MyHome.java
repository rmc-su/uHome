package me.taylorkelly.myhome;

import me.taylorkelly.myhome.commands.MyHomeCommand;
import me.taylorkelly.myhome.data.HomeEconomy;
import me.taylorkelly.myhome.data.HomeList;
import me.taylorkelly.myhome.listeners.MHEntityListener;
import me.taylorkelly.myhome.listeners.MHPlayerListener;
import me.taylorkelly.myhome.listeners.MHPluginListener;
import me.taylorkelly.myhome.locale.LocaleManager;
import me.taylorkelly.myhome.permissions.HomePermissions;
import me.taylorkelly.myhome.sql.ConnectionManager;
import me.taylorkelly.myhome.utils.HomeHelp;
import me.taylorkelly.myhome.utils.HomeLogger;
import me.taylorkelly.myhome.utils.MHUtils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.configuration.file.FileConfiguration;

public class MyHome extends JavaPlugin {
	
	private MHPlayerListener playerListener;
	private MHEntityListener entityListener;
	private MHPluginListener pluginListener;
	private HomeList homeList;
	public String name;
	public String version;
	public PluginManager pm;
	private MHUtils utils;
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
		this.utils = new MHUtils(this);
		this.config = this.getConfig();
		
		try {
			this.config.options().copyDefaults(true);
			this.saveConfig();
			HomeSettings.initialize(config, getDataFolder());
		} catch (Exception e) {
			HomeLogger.severe("Failed to load configuration!");
		}

		utils.startupChecks();
	
		homeList = new HomeList(getServer());
		LocaleManager.init();
		HomePermissions.initialize(this);
		HomeHelp.initialize(this);
		HomeEconomy.init(this);
		
		MyHomeCommand cmdmgr = new MyHomeCommand(this, homeList);
		getCommand("myhome").setExecutor(cmdmgr);
		getCommand("home").setExecutor(cmdmgr);
		getCommand("sethome").setExecutor(cmdmgr);
		
		this.playerListener = new MHPlayerListener(homeList, this);
		this.entityListener = new MHEntityListener(this);
		this.pluginListener = new MHPluginListener(this);
		registerEvents();

		HomeLogger.info(name + " " + version + " enabled");
	}
	
	private void registerEvents() {
		pm.registerEvents(pluginListener, this);
		pm.registerEvents(playerListener, this);
		pm.registerEvents(entityListener, this);
	}

	public void disablePlugin() {
		pm.disablePlugin(this);
	}
}
