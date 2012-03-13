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
                        this.setHome(player, args[0]);
                    } else if (args.length == 0) {
                        this.setHome(player, uHome.DEFAULT_HOME);
                    } else {
                        return false;
                    }
                }

            }
        }
        return true;
    }

    private void setHome(Player player, String name) {
        ExitStatus es = this.homeList.addHome(player, plugin, name, plugin.getLogger());

        switch (es) {
            case SUCCESS:
                player.sendMessage(LocaleManager.getString("own.set.new"));
                break;
            case SUCCESS_FIRST:
                player.sendMessage(LocaleManager.getString("own.set.first"));
                break;
            case SUCCESS_MOVED:
                player.sendMessage(LocaleManager.getString("own.set.moved"));
                break;
            case AT_LIMIT:
                Integer mustDelete = this.homeList.getPlayerHomeCount(player.getName()) - SuperPermsManager.getHomeLimit(player);
                HashMap<String, String> vars = new HashMap<String, String>();
                vars.put("DELETE", mustDelete.toString());
                vars.put("LIMIT", Integer.toString(SuperPermsManager.getHomeLimit(player)));

                player.sendMessage(LocaleManager.getString("own.set.atlimit", vars));
                break;
            case NEED_COOLDOWN:
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("CD_REMAINING", Integer.toString(SetHomeCoolDown.getInstance().estimateTimeLeft(player)));
                params.put("CD_TOTAL", Integer.toString(SetHomeCoolDown.getInstance().getTimer(player)));

                player.sendMessage(LocaleManager.getString("own.set.cooldown", params));
                break;
            case NOT_ENOUGH_MONEY:
                HashMap<String, String> costs = new HashMap<String, String>();
                costs.put("COST", Integer.toString(HomeConfig.setCost));
                costs.put("CURRENCY", this.plugin.economy.currencyNamePlural());

                player.sendMessage(LocaleManager.getString("econ.insufficient.set", costs));
                break;
        }
    }
}
