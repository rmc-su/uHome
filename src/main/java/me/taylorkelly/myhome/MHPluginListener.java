package me.taylorkelly.myhome;

import com.nijikokun.register.payment.Methods;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;

public class MHPluginListener extends ServerListener {
	private Methods Methods = null;

	public MHPluginListener() {
		if(HomeSettings.eConomyEnabled) {
			this.Methods = new Methods();
		}
	}

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if(HomeSettings.eConomyEnabled) {
			//Economy support
			if(!this.Methods.hasMethod()){
				if(this.Methods.setMethod(event.getPlugin())){
					HomeEconomy.economy = this.Methods.getMethod();
					HomeLogger.info("Connected to " + HomeEconomy.economy.getName() + " v" + HomeEconomy.economy.getVersion() + " for economy support.");
				}
			}
		}
	}

	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if(HomeSettings.eConomyEnabled) {
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