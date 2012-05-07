package uk.co.ks07.uhome;

import uk.co.ks07.uhome.storage.WarpDataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.HomeList.ExitStatus;
import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.timers.HomeCoolDown;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

public class HomeCommand implements CommandExecutor {

    private uHome plugin;
    private HomeList homeList;

    private final HomeCoolDown homeCoolDown = HomeCoolDown.getInstance();
    private static final int PAGINATION_SIZE = 8;
    private static final Pattern integerPattern = Pattern.compile("^\\d+$");

    public HomeCommand(uHome uH, HomeList hL) {
        this.plugin = uH;
        this.homeList = hL;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            switch (args.length) {
                case 0:
                    if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
                        // /home
                        this.goToHome(player);
                    }
                    break;
                case 1:
                    if ("set".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                        // /home set
                        this.setHome(player, uHome.DEFAULT_HOME);
                    } else if ("list".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
                        // /home list
                        this.showHomeList(player);
                    } else if ("delete".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
                        // /home delete
                        this.deleteHome(player, uHome.DEFAULT_HOME);
                    } else if ("help".equalsIgnoreCase(args[0])) {
                        // /home help
                        this.showHelp(player, 1);
                    } else if ("limit".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                        // /home limit
                        this.showHomeLimit(player);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home invites
                        this.showInviteList(player, 1);
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home requests
                        this.showRequestList(player, 1);
                    } else if (HomeConfig.enableUnlock && "unlock".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUnlock)) {
                        // /home unlock
                        this.unlockHome(player, uHome.DEFAULT_HOME);
                    } else if (HomeConfig.enableUnlock && "lock".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUnlock)) {
                        // /home lock
                        this.lockHome(player, uHome.DEFAULT_HOME);
                    } else if ("reload".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminReload)) {
                        // /home reload
                        this.reloadSettings(player);
                    } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp) || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
                        // /home (player|name)
                        this.goToUnknownTarget(player, args[0]);
                    }
                    break;
                case 2:
                    if ("set".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                        // /home set (name)
                        this.setHome(player, args[1]);
                    } else if ("delete".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
                        // /home delete (name)
                        this.deleteHome(player, args[1]);
                    } else if ("list".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
                        // /home list (player)
                        this.showHomeList(player, args[1]);
                    } else if ("limit".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
                        // /home limit (player)
                        this.showOtherLimit(player, args[1]);
                    } else if ("warp".equalsIgnoreCase(args[0]) && (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp) || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp))) {
                        // /home warp (player|name)
                        this.goToUnknownTarget(player, args[1]);
                    } else if (HomeConfig.enableInvite && "invite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
                        // /home invite (player)
                        this.inviteToHome(player, args[1], uHome.DEFAULT_HOME);
                    } else if (HomeConfig.enableInvite && "uninvite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUninvite)) {
                        // /home uninvite (player)
                        this.uninviteFromHome(player, args[1], uHome.DEFAULT_HOME);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && isPageNo(args[1]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home invites (page)
                        this.showInviteList(player, getPageNo(args[1]));
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && isPageNo(args[1]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home requests (page)
                        this.showRequestList(player, getPageNo(args[1]));
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home invites (player)
                        this.showInviteList(sender, args[1], 1);
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home requests (player)
                        this.showRequestList(sender, args[1], 1);
                    } else if (HomeConfig.enableUnlock && "unlock".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUnlock)) {
                        // /home unlock (home)
                        this.unlockHome(player, args[1]);
                    } else if (HomeConfig.enableUnlock && "lock".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUnlock)) {
                        // /home lock (home)
                        this.lockHome(player, args[1]);
                    } else if ("help".equalsIgnoreCase(args[0]) && isPageNo(args[1])) {
                        // /home help (page)
                        this.showHelp(player, getPageNo(args[1]));
                    } else if (HomeConfig.enableInvite || HomeConfig.enableUnlock || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
                        // /home (player) (name)
                        this.goToOtherHome(player, args[1], args[0]);
                    }
                    break;
                case 3:
                    if (HomeConfig.enableInvite && "invite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
                        // /home invite (player) (name) 
                        this.inviteToHome(player, args[1], args[2]);
                    } else if (HomeConfig.enableInvite && "uninvite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUninvite)) {
                        // /home uninvite (player) (name)
                        this.uninviteFromHome(player, args[1], args[2]);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && "from".equalsIgnoreCase(args[1]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home invites from (player)
                        this.showInviteList(sender, args[1], 1);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && isPageNo(args[2]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home invites (player) (page)
                        this.showInviteList(sender, args[1], getPageNo(args[2]));
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && isPageNo(args[2]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home requests (player) (page)
                        this.showRequestList(player, args[1], getPageNo(args[2]));
                    } else if ("set".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminSet)) {
                        // /home set (player) (name)
                        this.setOtherHome(player, args[2], args[1]);
                    } else if ("info".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminInfo)) {
                        // /home info (player) (name)
                        this.showHomeInfo(player, args[2], args[1]);
                    } else if ("delete".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminDelete)) {
                        // /home delete (player) (name)
                        this.deleteOtherHome(player, args[1], args[2]);
                    }
                    break;
                default:
                    return false;
            }
        } else {
            // User is not in game.
            switch (args.length) {
                case 1:
                    if ("reload".equalsIgnoreCase(args[0])) {
                        // /home reload
                        this.reloadSettings(sender);
                    } else if ("help".equalsIgnoreCase(args[0])) {
                        // /home help
                        this.showAllHelp(sender);
                    } else if ("debug".equalsIgnoreCase(args[0])) {
                        // /home debug
                        this.toggleDebug();
                    } else if ("dump".equalsIgnoreCase(args[0])) {
                        // /home dump
                        this.dumpSQL(sender);
                    }
                    break;
                case 2:
                    if ("list".equalsIgnoreCase(args[0])) {
                        // /home list (player)
                        this.showHomeList(sender, args[1]);
                    } else if ("limit".equalsIgnoreCase(args[0])) {
                        // /home limit (player)
                        this.showOtherLimit(sender, args[1]);
                    } else if (HomeConfig.enableATime && "purge".equalsIgnoreCase(args[0])) {
                        // /home purge (days)
                        this.showOtherLimit(sender, args[1]);
                    } else if (HomeConfig.enableInvite) {
                        // /home invites|requests (player)
                        if ("invites".equalsIgnoreCase(args[0])) {
                            this.showInviteList(sender, args[1], 1);
                        } else if ("requests".equalsIgnoreCase(args[0])) {
                            this.showRequestList(sender, args[1], 1);
                        }
                    }
                    break;
                case 3:
                    if ("info".equalsIgnoreCase(args[0])) {
                        // /home info (player) (name)
                        this.showHomeInfo(sender, args[2], args[1]);
                    } else if ("delete".equalsIgnoreCase(args[0])) {
                        // /home delete (player) (name)
                        this.deleteOtherHome(sender, args[1], args[2]);
                    } else if (HomeConfig.enableInvite && isPageNo(args[2])) {
                        // /home invites|requests (player) (page)
                        if ("invites".equalsIgnoreCase(args[0])) {
                            this.showInviteList(sender, args[1], getPageNo(args[2]));
                        } else if ("requests".equalsIgnoreCase(args[0])) {
                            this.showRequestList(sender, args[1], getPageNo(args[2]));
                        }
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public void unlockHome(Player player, String name) {
        HashMap<String, String> params = new HashMap<String, String>(1);
        params.put("HOME", name);

        if (this.homeList.homeExists(player.getName(), name)) {
            if (!this.homeList.toggleHomeLock(player.getName(), name)) {
                this.homeList.toggleHomeLock(player.getName(), name);
            }
            player.sendMessage(LocaleManager.getString("own.lock.unlocked", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.lock.notexists", params));
        }
    }

    public void lockHome(Player player, String name) {
        HashMap<String, String> params = new HashMap<String, String>(1);
        params.put("HOME", name);

        if (this.homeList.homeExists(player.getName(), name)) {
            if (this.homeList.toggleHomeLock(player.getName(), name)) {
                this.homeList.toggleHomeLock(player.getName(), name);
            }
            player.sendMessage(LocaleManager.getString("own.lock.locked", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.lock.notexists", params));
        }
    }

    public void dumpSQL(CommandSender user) {
        user.sendMessage("Exporting homes to home_export.sql");
        File file = new File(plugin.getDataFolder(), "home_export.sql");
        WarpDataSource.dumpTableSQL(file, plugin.getLogger());
        user.sendMessage("Home export complete.");
    }

    public void setHome(Player player, String name) {
        HomeCommand.setHome(player, name, this.plugin, this.homeList);
    }

    public static void setHome(Player player, String name, uHome plugin, HomeList homeList) {
        if (HomeConfig.bedsCanSethome == 2 && !SuperPermsManager.hasPermission(player, SuperPermsManager.bypassBed)) {
            player.sendMessage(LocaleManager.getString("usage.sleep"));
            return;
        }
                    
        ExitStatus es = homeList.addHome(player, plugin, name, plugin.getLogger());

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
                Integer mustDelete = (homeList.getPlayerHomeCount(player.getName()) - SuperPermsManager.getHomeLimit(player)) + 1;
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
                costs.put("CURRENCY", plugin.economy.currencyNamePlural());

                player.sendMessage(LocaleManager.getString("econ.insufficient.set", costs));
                break;
        }
    }

    public void setOtherHome(Player player, String homeName, String owner) {
        ExitStatus es = this.homeList.adminAddHome(player.getLocation(), owner, homeName, plugin.getLogger());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", owner);
        params.put("HOME", homeName);

        switch (es) {
            case SUCCESS:
                player.sendMessage(LocaleManager.getString("admin.set.new", params));
                break;
            case SUCCESS_FIRST:
                player.sendMessage(LocaleManager.getString("admin.set.first", params));
                break;
            case SUCCESS_MOVED:
                player.sendMessage(LocaleManager.getString("admin.set.moved", params));
                break;
        }
    }

    public void deleteHome(Player player, String homeName) {
        ExitStatus es = this.homeList.deleteHome(player.getName(), homeName, plugin.getLogger());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", homeName);

        if (es == ExitStatus.NOT_EXISTS) {
            player.sendMessage(LocaleManager.getString("own.delete.notexists", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.delete.ok", params));
        }
    }

    public void deleteOtherHome(CommandSender sender, String owner, String name) {
        ExitStatus es = this.homeList.deleteHome(owner, name, plugin.getLogger());

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", name);
        params.put("OWNER", owner);

        if (es == ExitStatus.NOT_EXISTS) {
            sender.sendMessage(LocaleManager.getString("admin.delete.notexists", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.delete.ok", params));
        }
    }

    public void goToUnknownTarget(Player player, String target) {
        HashMap<String, String> params;
        ExitStatus es = homeList.warpTo(target, player, plugin);

        // If SUCCESS, the task has been passed on to the warmup handler.
        switch (es) {
            case NEED_COOLDOWN:
                params = new HashMap<String, String>();
                params.put("CD_REMAINING", Integer.toString(this.homeCoolDown.estimateTimeLeft(player)));
                params.put("CD_TOTAL", Integer.toString(this.homeCoolDown.getTimer(player)));

                player.sendMessage(LocaleManager.getString("own.warp.cooldown", params));
                break;
            case NOT_ENOUGH_MONEY:
                params = new HashMap<String, String>();
                params.put("HOME", target);
                params.put("COST", Integer.toString(HomeConfig.warpCost));
                params.put("CURRENCY", this.plugin.economy.currencyNamePlural());

                player.sendMessage(LocaleManager.getString("econ.insufficient.warp", params));
                break;
            case NOT_PERMITTED: case NOT_EXISTS:
                // If no matches are found (or not permitted!?), check player default home.
                String playerName = this.completePlayerName(target);

                if (homeList.playerHasDefaultHome(playerName) && homeList.playerCanWarp(player, playerName, uHome.DEFAULT_HOME)) {
                    es = homeList.warpTo(playerName, uHome.DEFAULT_HOME, player, plugin);

                    // If SUCCESS, the task has been passed on to the warmup handler.
                    switch (es) {
                        case NEED_COOLDOWN:
                            params = new HashMap<String, String>();
                            params.put("CD_REMAINING", Integer.toString(this.homeCoolDown.estimateTimeLeft(player)));
                            params.put("CD_TOTAL", Integer.toString(this.homeCoolDown.getTimer(player)));

                            player.sendMessage(LocaleManager.getString("own.warp.cooldown", params));
                            break;
                        case NOT_ENOUGH_MONEY:
                            params = new HashMap<String, String>();
                            params.put("HOME", target);
                            params.put("COST", Integer.toString(HomeConfig.warpCost));
                            params.put("CURRENCY", this.plugin.economy.currencyNamePlural());

                            player.sendMessage(LocaleManager.getString("econ.insufficient.warp", params));
                            break;
                        case NOT_PERMITTED:
                            params = new HashMap<String, String>();
                            params.put("HOME", uHome.DEFAULT_HOME);
                            params.put("OWNER", playerName);

                            player.sendMessage(LocaleManager.getString("other.warp.notinvited", params));
                            break;
                    }
                } else {
                    // Assume the player entered a name of a warp, rather than a player when responding.
                    params = new HashMap<String, String>();
                    params.put("HOME", target);

                    player.sendMessage(LocaleManager.getString("own.warp.notexists", params));

                    if (HomeConfig.bedsCanSethome == 2) {
                        player.sendMessage(LocaleManager.getString("usage.sleep"));
                    } else {
                        player.sendMessage(LocaleManager.getString("usage.set"));
                    }
                }
                break;
        }
    }

    public void goToHome(Player player) {
        ExitStatus es = this.homeList.sendPlayerHome(player, this.plugin);
        HashMap<String, String> params;

        // If SUCCESS, the task has been passed on to the warmup handler.
        switch (es) {
            case NEED_COOLDOWN:
                params = new HashMap<String, String>();
                params.put("CD_REMAINING", Integer.toString(this.homeCoolDown.estimateTimeLeft(player)));
                params.put("CD_TOTAL", Integer.toString(this.homeCoolDown.getTimer(player)));

                player.sendMessage(LocaleManager.getString("own.warp.cooldown", params));
                break;
            case NOT_EXISTS:
                params = new HashMap<String, String>();
                params.put("HOME", uHome.DEFAULT_HOME);

                player.sendMessage(LocaleManager.getString("own.warp.notexists", params));

                if (HomeConfig.bedsCanSethome == 2) {
                    player.sendMessage(LocaleManager.getString("usage.sleep"));
                } else {
                    player.sendMessage(LocaleManager.getString("usage.set"));
                }
                break;
            case NOT_ENOUGH_MONEY:
                params = new HashMap<String, String>();
                params.put("HOME", uHome.DEFAULT_HOME);
                params.put("COST", Integer.toString(HomeConfig.warpCost));
                params.put("CURRENCY", this.plugin.economy.currencyNamePlural());

                player.sendMessage(LocaleManager.getString("econ.insufficient.warp", params));
                break;
        }
    }

    public void goToOtherHome(Player player, String targetHome, String targetOwner) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", targetHome);
        params.put("OWNER", targetOwner);
        
        ExitStatus es = this.homeList.warpTo(targetOwner, targetHome, player, this.plugin);

        // If SUCCESS, the task has been passed on to the warmup handler.
        switch (es) {
            case NEED_COOLDOWN:
                params.put("CD_REMAINING", Integer.toString(this.homeCoolDown.estimateTimeLeft(player)));
                params.put("CD_TOTAL", Integer.toString(this.homeCoolDown.getTimer(player)));

                player.sendMessage(LocaleManager.getString("own.warp.cooldown", params));
                break;
            case NOT_PERMITTED:
                player.sendMessage(LocaleManager.getString("other.warp.notinvited", params));
                break;
            case NOT_ENOUGH_MONEY:
                params.put("COST", Integer.toString(HomeConfig.warpCost));
                params.put("CURRENCY", this.plugin.economy.currencyNamePlural());

                player.sendMessage(LocaleManager.getString("econ.insufficient.warp", params));
                break;
            case NOT_EXISTS:
                player.sendMessage(LocaleManager.getString("other.warp.notexists", params));
                break;
        }
    }

    public void inviteToHome(Player player, String targetPlayer, String targetHome) {
        Player tPlayer = plugin.getServer().getPlayer(targetPlayer);
        
        if (tPlayer != null) {
            targetPlayer = tPlayer.getName();
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", targetHome);
        params.put("INVITED", targetPlayer);
        
        if (homeList.homeExists(player.getName(), targetHome)) {
            ExitStatus result = homeList.invitePlayer(player.getName(), targetPlayer, targetHome);
            switch (result) {
                case SUCCESS:
                    player.sendMessage(LocaleManager.getString("own.invite.ok", params));
                    break;
                case AT_LIMIT:
                    params.put("LIMIT", Integer.toString(SuperPermsManager.getInviteLimit(player)));
                    player.sendMessage(LocaleManager.getString("own.invite.atlimit", params));
                    break;
                case DUPLICATE:
                    player.sendMessage(LocaleManager.getString("own.invite.already", params));
                    break;
            }
        } else {
            player.sendMessage(LocaleManager.getString("own.invite.notexists", params));
        }
    }

    public void uninviteFromHome(Player player, String targetPlayer, String targetHome) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", targetHome);
        params.put("INVITED", targetPlayer);
        
        if (homeList.homeExists(player.getName(), targetHome)) {
            if (homeList.uninvitePlayer(player.getName(), targetPlayer, targetHome)) {
                player.sendMessage(LocaleManager.getString("own.uninvite.ok", params));
            } else {
                player.sendMessage(LocaleManager.getString("own.uninvite.notinvited", params));
            }
        } else {
            player.sendMessage(LocaleManager.getString("own.uninvite.notexists", params));
        }
    }

    public void showHomeInfo(CommandSender user, String targetHome, String targetOwner) {
        if (homeList.homeExists(targetOwner, targetHome)) {
            Home home = homeList.getNamedHome(targetOwner, targetHome);
            
            user.sendMessage(LocaleManager.getString("admin.info.output", null, home));
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("HOME", targetHome);
            params.put("OWNER", targetOwner);

            user.sendMessage(LocaleManager.getString("admin.info.notexists", params));
        }
    }

    public void showHomeList(Player player) {
        Home[] hList = this.homeList.getPlayerHomes(player.getName());

        if (hList == null) {
            player.sendMessage(LocaleManager.getString("own.list.nohomes"));
        } else {
            player.sendMessage(LocaleManager.getString("own.list.ok"));
            
            boolean hasNewlines = LocaleManager.getString("own.list.separator").contains("{{NEWLINE}}");

            if (hasNewlines) {
                String[] playerMessages = new String[hList.length];
                String[] tmpSplit = LocaleManager.getString("own.list.separator").split("\\{\\{NEWLINE\\}\\}", 2);
                String[] separators = new String[2];

                separators[0] = tmpSplit[0];
                if (tmpSplit.length == 2) {
                    separators[1] = tmpSplit[1];
                } else {
                    separators[1] = "";
                }

                int count = 0;
                // Initialise the first line.
                playerMessages[count] = "";
                for (Home h : hList) {
                    playerMessages[count].concat(LocaleManager.getString("own.list.item", null, h));

                    if (count < (hList.length - 1)) {
                        playerMessages[count].concat(separators[0]);
                        playerMessages[count + 1] = separators[1];
                    }

                    count++;
                }

                player.sendMessage(playerMessages);
            } else {
                StringBuilder playerMessage = new StringBuilder();

                int count = 0;
                for (Home h : hList) {
                    playerMessage.append(LocaleManager.getString("own.list.item", null, h));

                    if (count < (hList.length - 1)) {
                        playerMessage.append(LocaleManager.getString("own.list.separator"));
                    }

                    count++;
                }


                player.sendMessage(playerMessage.toString());
            }
        }
    }

    public void showHomeList(CommandSender sender, String targetPlayer) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", targetPlayer);

        Home[] hList = this.homeList.getPlayerHomes(targetPlayer);

        if (hList == null) {
            sender.sendMessage(LocaleManager.getString("admin.list.nohomes", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.list.ok", params));

            boolean hasNewlines = LocaleManager.getString("admin.list.separator").contains("{{NEWLINE}}");

            if (hasNewlines) {
                String[] playerMessages = new String[hList.length];
                String[] tmpSplit = LocaleManager.getString("admin.list.separator").split("\\{\\{NEWLINE\\}\\}", 2);
                String[] separators = new String[2];

                separators[0] = tmpSplit[0];
                if (tmpSplit.length == 2) {
                    separators[1] = tmpSplit[1];
                } else {
                    separators[1] = "";
                }

                int count = 0;
                // Initialise the first line.
                playerMessages[count] = "";
                for (Home h : hList) {
                    playerMessages[count].concat(LocaleManager.getString("admin.list.item", null, h));

                    if (count < (hList.length - 1)) {
                        playerMessages[count].concat(separators[0]);
                        playerMessages[count + 1] = separators[1];
                    }

                    count++;
                }

                sender.sendMessage(playerMessages);
            } else {
                StringBuilder playerMessage = new StringBuilder();

                int count = 0;
                for (Home h : hList) {
                    playerMessage.append(LocaleManager.getString("admin.list.item", null, h));

                    if (count < (hList.length - 1)) {
                        playerMessage.append(LocaleManager.getString("admin.list.separator"));
                    }

                    count++;
                }


                sender.sendMessage(playerMessage.toString());
            }
        }
    }
    
    public void showInviteList(Player player, int page) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("INVITED", player.getName());

        Home[] iList = this.homeList.getInvitedToList(player.getName());

        if (iList == null || iList.length == 0) {
            player.sendMessage(LocaleManager.getString("own.invites.none", params));
        } else {
            ArrayList<String> messages = new ArrayList<String>(iList.length);
            for (Home home : iList) {
                messages.add(LocaleManager.getString("own.invites.output", params, home));
            }

            player.sendMessage(LocaleManager.getString("own.invites.ok", params));
            sendPaginated(null, messages, page, player);
        }
    }

    public void showInviteListFrom(Player player, String from) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("INVITED", player.getName());
        params.put("OWNER", HomeList.getOnlinePlayerCapitalisation(from));

        Collection<Home> iList = this.homeList.getInvitedToList(player.getName(), from);

        if (iList == null || iList.isEmpty()) {
            player.sendMessage(LocaleManager.getString("own.invitesfrom.none", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.invitesfrom.ok", params));
            player.sendMessage(iList.toString().replace("[", "").replace("]", ""));
        }
    }

    public void showInviteList(CommandSender sender, String player, int page) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("INVITED", HomeList.getOnlinePlayerCapitalisation(player));

        Home[] iList = this.homeList.getInvitedToList(player);

        if (iList == null) {
            sender.sendMessage(LocaleManager.getString("admin.invites.none", params));
        } else {
            ArrayList<String> messages = new ArrayList<String>(iList.length);
            for (Home home : iList) {
                messages.add(LocaleManager.getString("admin.invites.output", params, home));
            }

            sender.sendMessage(LocaleManager.getString("admin.invites.ok", params));
            sendPaginated(null, messages, page, sender);
        }
    }

    public void showRequestList(Player player, int page) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", player.getName());

        Collection<Home> results = this.homeList.getRequestList(player.getName());

        if (results == null || results.isEmpty()) {
            player.sendMessage(LocaleManager.getString("own.requests.none", params));
        } else {
            ArrayList<String> messages = new ArrayList<String>(results.size());
            for (Home home : results) {
                messages.add(LocaleManager.getString("own.requests.output", params, home));
            }

            player.sendMessage(LocaleManager.getString("own.requests.ok", params));
            sendPaginated(null, messages, page, player);
        }
    }

    public void showRequestList(CommandSender sender, String player, int page) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", HomeList.getOnlinePlayerCapitalisation(player));

        Collection<Home> results = this.homeList.getRequestList(player);

        if (results == null || results.isEmpty()) {
            sender.sendMessage(LocaleManager.getString("admin.requests.none", params));
        } else {
            ArrayList<String> messages = new ArrayList<String>(results.size());
            for (Home home : results) {
                messages.add(LocaleManager.getString("admin.requests.output", params, home));
            }

            sender.sendMessage(LocaleManager.getString("admin.requests.ok", params));
            sendPaginated(null, messages, page, sender);
        }
    }

    public void showHomeLimit(Player player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("LIMIT", Integer.toString(SuperPermsManager.getHomeLimit(player)));
        
        player.sendMessage(LocaleManager.getString("own.limit.ok", params));
    }

    public void showOtherLimit(CommandSender sender, String targetPlayer) {
        Player target = plugin.getServer().getPlayer(targetPlayer);

        if (target != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("LIMIT", Integer.toString(SuperPermsManager.getHomeLimit(target)));
            params.put("OWNER", targetPlayer);

            sender.sendMessage(LocaleManager.getString("admin.limit.ok", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.limit.noplayer"));
        }
    }

    public void reloadSettings(CommandSender user) {
        user.sendMessage(LocaleManager.getString("admin.reload.ok"));
        HomeConfig.initialize(plugin.getConfig(), plugin.getDataFolder(), plugin.getLogger());
    }

    public void toggleDebug() {
        // Console command only - only console users can see the effects!
        if (HomeConfig.debugLog) {
            this.plugin.getLogger().info("Debug logging disabled.");
            HomeConfig.debugLog = false;
        } else {
            this.plugin.getLogger().info("Debug logging enabled.");
            HomeConfig.debugLog = true;
        }
    }

    private static final int PURGE_TIMEOUT_MILLIS = 30000; // 30 seconds.
    private long purgeCommandTime = 0;
    private int purgeCommandDays = Integer.MAX_VALUE;

    public void purgeHomes(CommandSender user, String argument) {
        if (purgeCommandTime > (System.currentTimeMillis() - PURGE_TIMEOUT_MILLIS) && "confirm".equals(argument)) {
            user.sendMessage("Now purging homes last accessed over " + Integer.toString(purgeCommandDays) + " days ago. This may take a long time, and may lag the server...");
            
            int purgeTime = daysToSeconds(purgeCommandDays);
            
            int purged = this.homeList.cleanupHomes(purgeTime);

            if (purged > 0) {
                user.sendMessage("Deleted " + Integer.toString(purged) + " homes last accessed over " + Integer.toString(purgeCommandDays) + " days ago.");
            } else {
                user.sendMessage("No homes were last accessed more than " + purgeCommandDays + " days ago.");
            }
        } else {
            try {
                this.purgeCommandDays = Integer.parseInt(argument);
                this.purgeCommandTime = System.currentTimeMillis();
                user.sendMessage("Ready to purge all homes last accessed more than " + Integer.toString(this.purgeCommandDays) + " days ago.");
                user.sendMessage(ChatColor.RED + "This will PERMANENTLY DELETE any homes found that match this criteria. We STRONGLY suggest you backup your database before continuing!");
                user.sendMessage("To continue, please do `home purge confirm` within the next 30 seconds. This may cause lag depending on the number of homes.");
            } catch (NumberFormatException ex) {
                user.sendMessage(ChatColor.RED + "Invalid number of days specified.");
            }
        }
    }

    private static int daysToSeconds(int days) {
        // 86,400 seconds in one day.
        return days * 86400;
    }

    public void showHelp(Player player, int page) {
        ArrayList<String> messages = new ArrayList<String>(18);
        String header = ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP [page]" + ChatColor.RED + " -----";

        if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
            messages.add(ChatColor.RED + "/home" + ChatColor.WHITE + " -  Warp to your default home.");
            messages.add(ChatColor.RED + "/home [name]" + ChatColor.WHITE + " -  Warp to the named home.");
            messages.add(ChatColor.RED + "/home warp [name]" + ChatColor.WHITE + " -  Warp to a named home with a conflicting name.");
        }
        if (HomeConfig.enableInvite || SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
            messages.add(ChatColor.RED + "/home [player]" + ChatColor.WHITE + " -  Warp to a player's home (if allowed).");
            messages.add(ChatColor.RED + "/home [player] [name]" + ChatColor.WHITE + " -  Warp to a player's named home (if allowed).");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
            messages.add(ChatColor.RED + "/home set" + ChatColor.WHITE + " -  Set your default home to your current position.");
            messages.add(ChatColor.RED + "/home set [name]" + ChatColor.WHITE + " -  Set a named home to your current position.");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownDelete)) {
            messages.add(ChatColor.RED + "/home delete" + ChatColor.WHITE + " -  Delete your default home");
            messages.add(ChatColor.RED + "/home delete [name]" + ChatColor.WHITE + " -  Delete the named home");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminDelete)) {
            messages.add(ChatColor.RED + "/home delete [player]" + ChatColor.WHITE + " -  Delete a player's default home.");
            messages.add(ChatColor.RED + "/home delete [player] [name]" + ChatColor.WHITE + " -  Delete a player's named home.");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
            messages.add(ChatColor.RED + "/home list" + ChatColor.WHITE + " -  List your homes.");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
            messages.add(ChatColor.RED + "/home list [player]" + ChatColor.WHITE + " -  List a player's homes.");
        }
        if (HomeConfig.enableInvite && SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
            if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
                messages.add(ChatColor.RED + "/home invite [player] [name]" + ChatColor.WHITE + " -  Invite a player to the named home.");
            }
            if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownUninvite)) {
                messages.add(ChatColor.RED + "/home uninvite [player] [name]" + ChatColor.WHITE + " -  Uninvite a player from the named home.");
            }
            messages.add(ChatColor.RED + "/home invites" + ChatColor.WHITE + " -  List the invites you have received.");
            messages.add(ChatColor.RED + "/home requests" + ChatColor.WHITE + " -  List the invites you have sent.");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
            messages.add(ChatColor.RED + "/home limit" + ChatColor.WHITE + " -  Show your max homes.");
        }
        if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminList)) {
            messages.add(ChatColor.RED + "/home limit [player]" + ChatColor.WHITE + " -  Show a player's max homes.");
        }

        sendPaginated(header, messages, page, player);
    }

    public void showAllHelp(CommandSender sender) {
        // Send all help, no permissions checks.
        sender.sendMessage(ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " -----");
        sender.sendMessage(ChatColor.RED + "/home" + ChatColor.WHITE + " -  Warp to your default home.");
        sender.sendMessage(ChatColor.RED + "/home [name]" + ChatColor.WHITE + " -  Warp to the named home.");
        sender.sendMessage(ChatColor.RED + "/home warp [name]" + ChatColor.WHITE + " -  Warp to a named home with a conflicting name.");
        sender.sendMessage(ChatColor.RED + "/home [player]" + ChatColor.WHITE + " -  Warp to a player's home (if allowed).");
        sender.sendMessage(ChatColor.RED + "/home [player] [name]" + ChatColor.WHITE + " -  Warp to a player's named home (if allowed).");
        sender.sendMessage(ChatColor.RED + "/home set" + ChatColor.WHITE + " -  Set your default home to your current position.");
        sender.sendMessage(ChatColor.RED + "/home set [name]" + ChatColor.WHITE + " -  Set a named home to your current position.");
        sender.sendMessage(ChatColor.RED + "/home delete" + ChatColor.WHITE + " -  Delete your default home");
        sender.sendMessage(ChatColor.RED + "/home delete [name]" + ChatColor.WHITE + " -  Delete the named home");
        sender.sendMessage(ChatColor.RED + "/home delete [player]" + ChatColor.WHITE + " -  Delete a player's default home.");
        sender.sendMessage(ChatColor.RED + "/home delete [player] [name]" + ChatColor.WHITE + " -  Delete a player's named home.");
        sender.sendMessage(ChatColor.RED + "/home list" + ChatColor.WHITE + " -  List your homes.");
        sender.sendMessage(ChatColor.RED + "/home list [player]" + ChatColor.WHITE + " -  List a player's homes.");
        if (HomeConfig.enableInvite) {
            sender.sendMessage(ChatColor.RED + "/home invite [player] [name]" + ChatColor.WHITE + " -  Invite a player to the named home.");
            sender.sendMessage(ChatColor.RED + "/home uninvite [player] [name]" + ChatColor.WHITE + " -  Uninvite a player from the named home.");
            sender.sendMessage(ChatColor.RED + "/home invites" + ChatColor.WHITE + " -  List the invites you have received.");
            sender.sendMessage(ChatColor.RED + "/home requests" + ChatColor.WHITE + " -  List the invites you have sent.");
        }
        sender.sendMessage(ChatColor.RED + "/home limit" + ChatColor.WHITE + " -  Show your max homes.");
        sender.sendMessage(ChatColor.RED + "/home limit [player]" + ChatColor.WHITE + " -  Show a player's max homes.");
    }

    private String completePlayerName(String partial) {
        Player player = this.plugin.getServer().getPlayer(partial);

        if (player != null) {
            return player.getName();
        } else {
            return partial;
        }
    }

    private static boolean isPageNo(String input) {
        return (input.length() < 3) && integerPattern.matcher(input).matches();
    }

    private static int getPageNo(String input) {
        int ret = 1;
        
        try {
            ret = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            // Presume 1.
        } finally {
            return ret;
        }
    }

    private static void sendPaginated(String header, ArrayList<String> messages, int printPage, CommandSender receiver) {
        int pageLen;

        if (header == null || header.isEmpty()) {
            pageLen = PAGINATION_SIZE;
        } else {
            pageLen = PAGINATION_SIZE - 1;
            receiver.sendMessage(header);
        }

        int atPageRemains = messages.size() - ((printPage - 1) * pageLen);
        int startIndex = (printPage - 1) * pageLen;
        int endIndex;
        
        if (atPageRemains < pageLen) {
            endIndex = startIndex + atPageRemains;
        } else {
            endIndex = startIndex + pageLen;
        }

        for (int i = startIndex; i < endIndex; i++) {
            receiver.sendMessage(messages.get(i));
        }
    }
}
