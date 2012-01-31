package uk.co.ks07.uhome;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    private uHome plugin;
    private HomeList homeList;

    public SetHomeCommand(uHome uH, HomeList hL) {
        this.plugin = uH;
        this.homeList = hL;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

                        // Workaround for ticket 8.
                        if (HomeConfig.enableDenyPerm && SuperPermsManager.hasPermission(player, SuperPermsManager.denyPerm)) {
                            return true;
                        }
                        
			if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
				if(HomeConfig.bedsCanSethome == 2 && !SuperPermsManager.hasPermission(player, SuperPermsManager.bypassBed)) {
					player.sendMessage(ChatColor.RED + "You can only set a home by sleeping in a bed");
					return true;
				}

                                if (args.length == 1) {
                                        homeList.addHome(player, plugin, args[0]);
                                        return true;
                                }

                                homeList.addHome(player, plugin, uHome.DEFAULT_HOME);
				return true;
			}
			
		}
		return false;
	}
}
