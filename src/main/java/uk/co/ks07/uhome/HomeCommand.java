package uk.co.ks07.uhome;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.ks07.uhome.HomeList.ExitStatus;
import uk.co.ks07.uhome.locale.LocaleManager;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

public class HomeCommand implements CommandExecutor {

    private uHome plugin;
    private HomeList homeList;

    private final SetHomeCoolDown setHomeCoolDown = SetHomeCoolDown.getInstance();

    public HomeCommand(uHome uH, HomeList hL) {
        this.plugin = uH;
        this.homeList = hL;
    }

    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Workaround for DBO ticket 8.
            if (HomeConfig.enableDenyPerm && SuperPermsManager.hasPermission(player, SuperPermsManager.denyPerm)) {
                return true;
            }

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
                        this.showHelp(player);
                    } else if ("limit".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                        // /home limit
                        this.showHomeLimit(player);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home invites
                        this.showInviteList(player);
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                        // /home requests
                        this.showRequestList(player);
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
                    } else if ("list".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownList)) {
                        // /home list (player)
                        this.showHomeList(player, args[1]);
                    } else if ("limit".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownSet)) {
                        // /home limit (player)
                        this.showOtherLimit(player, args[1]);
                    } else if ("warp".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownWarp)) {
                        // /home warp (player|name)
                        this.goToUnknownTarget(player, args[1]);
                    } else if (HomeConfig.enableInvite && "invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home invites (player)
                        this.showInviteList(sender, args[1]);
                    } else if (HomeConfig.enableInvite && "requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                        // /home requests (player)
                        this.showRequestList(sender, args[1]);
                    } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
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
                    }
                    break;
                case 2:
                    if ("list".equalsIgnoreCase(args[0])) {
                        // /home list (player)
                        this.showHomeList(sender, args[1]);
                    } else if ("limit".equalsIgnoreCase(args[0])) {
                        // /home limit (player)
                        this.showOtherLimit(sender, args[1]);
                    } else if (HomeConfig.enableInvite) {
                        // /home invites|requests (player)
                        if ("invites".equalsIgnoreCase(args[0])) {
                            this.showInviteList(sender, args[1]);
                        } else if ("requests".equalsIgnoreCase(args[0])) {
                            this.showRequestList(sender, args[1]);
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
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public void setHome(Player player, String name) {
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
                player.sendMessage(LocaleManager.getString("own.set.atlimit"));
                break;
            case NEED_COOLDOWN:
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("CD_REMAINING", Integer.toString(this.setHomeCoolDown.estimateTimeLeft(player)));
                params.put("CD_TOTAL", Integer.toString(this.setHomeCoolDown.getTimer(player)));
                
                player.sendMessage(LocaleManager.getString("own.set.cooldown", params));
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
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", target);
        params.put("OWNER", player.getName());

        if (homeList.homeExists(player.getName(), target)) {
            homeList.warpTo(target, player, plugin);
        } else if (homeList.homeExists("home", target) && homeList.playerCanWarp(player, target, "home")) {
            homeList.warpTo(target, "home", player, plugin);
        } else {
            player.sendMessage(LocaleManager.getString("own.warp.notexists", params));
        }
    }

    public void goToHome(Player user) {
        if (this.homeList.playerHasDefaultHome(user.getName())) {
            this.homeList.sendPlayerHome(user, this.plugin);
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("HOME", uHome.DEFAULT_HOME);

            user.sendMessage(LocaleManager.getString("own.warp.notexists", params));
            if (HomeConfig.bedsCanSethome == 2) {
                user.sendMessage(LocaleManager.getString("usage.sleep"));
            } else {
                user.sendMessage(LocaleManager.getString("usage.set"));
            }
        }
    }

    public void goToOtherHome(Player user, String targetHome, String targetOwner) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", targetHome);
        params.put("OWNER", targetOwner);

        if (this.homeList.homeExists(targetOwner, targetHome)) {
            if (this.homeList.playerCanWarp(user, targetOwner, targetHome)) {
                this.homeList.warpTo(targetOwner, targetHome, user, this.plugin);
            } else {
                user.sendMessage(LocaleManager.getString("other.warp.notinvited", params));
            }
        } else {
            user.sendMessage(LocaleManager.getString("other.warp.notexists", params));
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
            if (homeList.invitePlayer(player.getName(), targetPlayer, targetHome)) {
                player.sendMessage(LocaleManager.getString("own.invite.ok", params));
            } else {
                player.sendMessage(LocaleManager.getString("own.invite.already", params));
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
            player.sendMessage("The home " + targetHome + " doesn't exist!");
        }
    }

    public void showHomeInfo(CommandSender user, String targetHome, String targetOwner) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("HOME", targetHome);
        params.put("OWNER", targetOwner);

        if (homeList.homeExists(targetOwner, targetHome)) {
            params.put("LOCATION", homeList.getHomeLocation(targetOwner, targetHome).toString());
            user.sendMessage(LocaleManager.getString("admin.info.ok", params));
        } else {
            user.sendMessage(LocaleManager.getString("admin.info.notexists", params));
        }
    }

    public void showHomeList(Player player) {
        String hList = this.homeList.getPlayerList(player.getName());

        if (hList == null) {
            player.sendMessage(LocaleManager.getString("own.list.nohomes"));
        } else {
            player.sendMessage(LocaleManager.getString("own.list.ok"));
            player.sendMessage(hList);
        }
    }

    public void showHomeList(CommandSender sender, String targetPlayer) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", targetPlayer);

        String hList = this.homeList.getPlayerList(targetPlayer);

        if (hList == null) {
            sender.sendMessage(LocaleManager.getString("admin.list.nohomes", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.list.ok", params));
            sender.sendMessage(hList);
        }
    }
    
    public void showInviteList(Player player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("INVITED", player.getName());

        String iList = this.homeList.getInvitedToList(player.getName());

        if (iList == null) {
            player.sendMessage(LocaleManager.getString("own.invites.none", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.invites.ok", params));
            player.sendMessage(iList);
        }
    }

    public void showInviteList(CommandSender sender, String player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("INVITED", player);

        String iList = this.homeList.getInvitedToList(player);

        if (iList == null) {
            sender.sendMessage(LocaleManager.getString("admin.invites.none", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.invites.ok", params));
            sender.sendMessage(iList);
        }
    }

    public void showRequestList(Player player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", player.getName());

        String results[] = this.homeList.getRequestList(player.getName());

        if (results == null) {
            player.sendMessage(LocaleManager.getString("own.requests.none", params));
        } else {
            player.sendMessage(LocaleManager.getString("own.requests.ok", params));
            for (String s : results) {
                if (s != null) {
                    player.sendMessage(s);
                }
            }
        }
    }

    public void showRequestList(CommandSender sender, String player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("OWNER", player);

        String results[] = this.homeList.getRequestList(player);

        if (results == null) {
            sender.sendMessage(LocaleManager.getString("admin.requests.none", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.requests.ok", params));
            for (String s : results) {
                sender.sendMessage(s);
            }
        }
    }

    public void showHomeLimit(Player player) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("LIMIT", Integer.toString(homeList.playerGetLimit(player)));
        
        player.sendMessage(LocaleManager.getString("own.limit.ok", params));
    }

    public void showOtherLimit(CommandSender sender, String targetPlayer) {
        Player target = plugin.getServer().getPlayer(targetPlayer);

        if (target != null) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("LIMIT", Integer.toString(homeList.playerGetLimit(target)));
            params.put("OWNER", targetPlayer);

            sender.sendMessage(LocaleManager.getString("admin.limit.ok", params));
        } else {
            sender.sendMessage(LocaleManager.getString("admin.limit.noplayer"));
        }
    }

    public void reloadSettings(CommandSender user) {
        user.sendMessage(LocaleManager.getString("admin.reload.ok"));
        HomeConfig.initialize(plugin.config, plugin.getDataFolder(), plugin.getLogger());
    }

    public void showHelp(Player player) {
        ArrayList<String> messages = new ArrayList<String>(19);
        messages.add(ChatColor.RED + "----- " + ChatColor.WHITE + "/HOME HELP" + ChatColor.RED + " -----");

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

        for (String message : messages) {
            player.sendMessage(message);
        }
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
}
