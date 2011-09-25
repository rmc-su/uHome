package me.taylorkelly.myhome;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import com.nijikokun.register.payment.Methods;

public class MHPluginListener extends ServerListener {
	private Methods Methods = null;
	private Plugin plugin;
	
	public MHPluginListener(Plugin plugin) {
		if(HomeSettings.eConomyEnabled) {
			this.Methods = new Methods();
			this.plugin = plugin;
		}
	}
	
	@Override
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

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if(HomeSettings.eConomyEnabled) {
			if(HomeEconomy.hookedEconomy()) {
				HomeEconomy.hasUnhooked(event.getPlugin());
			}
		}
	}
}