package me.taylorkelly.myhome;

import me.taylorkelly.myhome.timers.WarmUp;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class MHEntityListener extends EntityListener {
	private Plugin plugin;
	public MHEntityListener(Plugin plugin) {
		this.plugin = plugin;
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || HomeSettings.abortOnDamage == 0)
			return;
		
		final LivingEntity victim = (LivingEntity) event.getEntity();
		final Entity aggressor =((EntityDamageByEntityEvent)event).getDamager();	

		if(HomeSettings.abortOnDamage == 3) {
			if(victim instanceof Player) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin);
			}
			if(aggressor instanceof Player) {
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin);
			}
		} else if(HomeSettings.abortOnDamage == 2) {
			if(victim instanceof Player && ((aggressor instanceof LivingEntity) && !(aggressor instanceof Player))) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin);
			}
			if(aggressor instanceof Player && ((victim instanceof LivingEntity) && !(victim instanceof Player))) {
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin);
			}			
		} else if(HomeSettings.abortOnDamage == 1) {
			if(victim instanceof Player && aggressor instanceof Player) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin);
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin);
			}			
		}
	}
}
