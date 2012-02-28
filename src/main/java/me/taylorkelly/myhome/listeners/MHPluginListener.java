package me.taylorkelly.myhome.listeners;

import me.taylorkelly.myhome.HomeSettings;
import me.taylorkelly.myhome.data.HomeEconomy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import com.nijikokun.register.payment.Methods;

public class MHPluginListener implements Listener {
	private Methods Methods = null;
	private Plugin plugin;
	
	public MHPluginListener(Plugin plugin) {
		if(HomeSettings.eConomyEnabled) {
			this.Methods = new Methods();
			this.plugin = plugin;
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPluginEnable(PluginEnableEvent event) {
		if(HomeSettings.eConomyEnabled) {
			if(!HomeEconomy.hookedEconomy()) {
				Plugin plugin = event.getPlugin();
				String plugname = plugin.getDescription().getName();
				if(plugname.equals("Register") && plugin.getClass().getName().equals("com.nijikokun.register.Register")) {
					// We found register
					HomeEconomy.enableEconomy();
					HomeEconomy.findEconomy();
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) 
	public void onPluginDisable(PluginDisableEvent event) {
		if(HomeSettings.eConomyEnabled) {
			if(HomeEconomy.hookedEconomy()) {
				HomeEconomy.hasUnhooked(event.getPlugin());
			}
		}
	}
}