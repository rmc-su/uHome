package uk.co.ks07.uhome;

import uk.co.ks07.uhome.timers.WarmUp;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Animals;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class UHEntityListener extends EntityListener {
	private Plugin plugin;
	public UHEntityListener(Plugin plugin) {
		this.plugin = plugin;
	}

	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled() || !(event instanceof EntityDamageByEntityEvent) || !(event.getEntity() instanceof LivingEntity) || HomeConfig.abortOnDamage == 0)
			return;
		
		final LivingEntity victim = (LivingEntity) event.getEntity();
		final Entity aggressor =((EntityDamageByEntityEvent)event).getDamager();	

		if(HomeConfig.abortOnDamage == 3) {
			if(victim instanceof Player) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
			}
			if(aggressor instanceof Player) {
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
			}
		} else if(HomeConfig.abortOnDamage == 2) {
			if(victim instanceof Player && (((aggressor instanceof Monster) || (aggressor instanceof Animals)) && !(aggressor instanceof Player))) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
			}
			if(aggressor instanceof Player && (((victim instanceof Monster) || (victim instanceof Animals)) && !(victim instanceof Player))) {
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
			}			
		} else if(HomeConfig.abortOnDamage == 1) {
			if(victim instanceof Player && aggressor instanceof Player) {
				Player vplayer = (Player) event.getEntity();
				WarmUp.cancelWarming(vplayer, plugin, WarmUp.Reason.DAMAGE);
				Player aplayer = (Player) ((EntityDamageByEntityEvent)event).getDamager();
				WarmUp.cancelWarming(aplayer, plugin, WarmUp.Reason.DAMAGE);
			}			
		}
	}
}
