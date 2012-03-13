package uk.co.ks07.uhome;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.ks07.uhome.HomeList.ExitStatus;

import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

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
                        HomeCommand.setHome(player, args[0], this.plugin, this.homeList);
                    } else if (args.length == 0) {
                        HomeCommand.setHome(player, uHome.DEFAULT_HOME, this.plugin, this.homeList);
                    } else {
                        return false;
                    }
                }

            }
        }
        return true;
    }
}
