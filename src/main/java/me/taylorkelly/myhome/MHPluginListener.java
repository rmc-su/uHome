package me.taylorkelly.myhome;

import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

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
			if(HomeEconomy.checkRegister(this.plugin)) {
				//Economy support
				if(!this.Methods.hasMethod()){
					if(this.Methods.setMethod(plugin.getServer().getPluginManager())){
						HomeEconomy.economy = this.Methods.getMethod();
						HomeLogger.info("Connected to " + HomeEconomy.economy.getName() + " v" + HomeEconomy.economy.getVersion() + " for economy support.");
					}
				}
			}
		}
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if(HomeSettings.eConomyEnabled) {
			if(HomeEconomy.checkRegister(this.plugin)) {
				if (this.Methods != null && this.Methods.hasMethod()) {
					Boolean check = this.Methods.checkDisabled(event.getPlugin());
					if(check) {
						this.Methods = null;
						HomeLogger.info("Payment method was disabled. No longer accepting payments.");
					}
				}
			}
		}
	}
}