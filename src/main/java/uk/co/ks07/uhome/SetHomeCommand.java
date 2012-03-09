package uk.co.ks07.uhome;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.locale.LocaleManager;

public class SetHomeCommand implements CommandExecutor {

    private uHome plugin;
    private HomeList homeList;

    public SetHomeCommand(uHome uH, HomeList hL) {
        this.plugin = uH;
        this.homeList = hL;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (HomeConfig.enableSethome) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                    if (HomeConfig.bedsCanSethome == 2 && !SuperPermsManager.hasPermission(player, SuperPermsManager.bypassBed)) {
                        player.sendMessage(LocaleManager.getString("usage.sleep"));
                        return true;
                    }

                    if (args.length == 1) {
                        homeList.addHome(player, plugin, args[0], plugin.getLogger());
                    } else if (args.length == 0) {
                        homeList.addHome(player, plugin, uHome.DEFAULT_HOME, plugin.getLogger());
                    } else {
                        return false;
                    }
                }

            }
        }
        return true;
    }
}
