package uk.co.ks07.uhome;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.co.ks07.uhome.HomeList.ExitStatus;
import uk.co.ks07.uhome.timers.SetHomeCoolDown;

public class HomeCommand implements CommandExecutor {

    private uHome plugin;
    private HomeList homeList;

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
                    } else if (HomeConfig.enableInvite) {
                        // /home invites|requests
                        if ("invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                            this.showInviteList(player);
                        } else if ("requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownListInvites)) {
                            this.showRequestList(player);
                        }
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
                    } else if (HomeConfig.enableInvite) {
                        // /home invites|requests (player)
                        if ("invites".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                            this.showInviteList(sender, args[1]);
                        } else if ("requests".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.adminListInvites)) {
                            this.showRequestList(sender, args[1]);
                        }
                    } else if (SuperPermsManager.hasPermission(player, SuperPermsManager.adminWarp)) {
                        // /home (player) (name)
                        this.goToOtherHome(player, args[1], args[0]);
                    }
                    break;
                case 3:
                    if (HomeConfig.enableInvite) {
                        // /home invite|uninvite (player) (name)
                        if ("invite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownInvite)) {
                            this.inviteToHome(player, args[1], args[2]);
                        } else if ("uninvite".equalsIgnoreCase(args[0]) && SuperPermsManager.hasPermission(player, SuperPermsManager.ownUninvite)) {
                            this.uninviteFromHome(player, args[1], args[2]);
                        }
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
                player.sendMessage(ChatColor.AQUA + "Welcome to your new home :).");
                break;
            case SUCCESS_FIRST:
                player.sendMessage(ChatColor.AQUA + "Welcome to your first home!");
                break;
            case SUCCESS_MOVED:
                player.sendMessage(ChatColor.AQUA + "Succesfully moved your home.");
                break;
            case AT_LIMIT:
                player.sendMessage(ChatColor.RED + "You have too many homes! You must delete one before you can set a new home!");
                break;
            case NEED_COOLDOWN:
                player.sendMessage(ChatColor.RED + "You need to wait "
                    + SetHomeCoolDown.getInstance().estimateTimeLeft(player) + " more seconds of the "
                    + SetHomeCoolDown.getInstance().getTimer(player) + " second cooldown before you can edit your homes.");
        }
    }

    public void setOtherHome(Player player, String homeName, String owner) {
        ExitStatus es = this.homeList.adminAddHome(player.getLocation(), owner, homeName, plugin.getLogger());
        
        switch (es) {
            case SUCCESS:
                player.sendMessage(ChatColor.AQUA + "Created new home for " + owner);
                break;
            case SUCCESS_FIRST:
                player.sendMessage(ChatColor.AQUA + "Created first home for " + owner);
                break;
            case SUCCESS_MOVED:
                player.sendMessage(ChatColor.AQUA + "Succesfully moved home for " + owner);
                break;
        }
    }

    public void deleteHome(Player player, String homeName) {
        ExitStatus es = this.homeList.deleteHome(player.getName(), homeName, plugin.getLogger());

        if (es == ExitStatus.NOT_EXISTS) {
            player.sendMessage(ChatColor.RED + "You don't have a home called '" + homeName + "'!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have deleted your home '" + homeName + "'.");
        }
    }

    public void deleteOtherHome(CommandSender sender, String owner, String name) {
        ExitStatus es = this.homeList.deleteHome(owner, name, plugin.getLogger());

        if (es == ExitStatus.NOT_EXISTS) {
            sender.sendMessage(ChatColor.RED + "There is no home '" + name + "' for " + owner + "!");
        } else {
            sender.sendMessage(ChatColor.AQUA + "You have deleted " + owner + "'s home '" + name + "'.");
        }
    }

    public void goToUnknownTarget(Player player, String target) {
        if (homeList.homeExists(player.getName(), target)) {
            homeList.warpTo(target, player, plugin);
        } else if (homeList.homeExists("home", target) && homeList.playerCanWarp(player, target, "home")) {
            homeList.warpTo(target, "home", player, plugin);
        } else {
            player.sendMessage("The home " + target + " doesn't exist!");
        }
    }

    public void goToHome(Player user) {
        if (this.homeList.playerHasDefaultHome(user.getName())) {
            this.homeList.sendPlayerHome(user, this.plugin);
        } else {
            user.sendMessage(ChatColor.RED + "You have no home :(");
            if (HomeConfig.bedsCanSethome == 2) {
                user.sendMessage("You need to sleep in a bed to set your default home");
            } else {
                user.sendMessage("Use: " + ChatColor.RED + "/home set" + ChatColor.WHITE + " to set a home");
            }
        }
    }

    public void goToOtherHome(Player user, String targetHome, String targetOwner) {
        if (this.homeList.homeExists(targetOwner, targetHome)) {
            if (this.homeList.playerCanWarp(user, targetOwner, targetHome)) {
                this.homeList.warpTo(targetOwner, targetHome, user, this.plugin);
            } else {
                user.sendMessage("You aren't invited to " + targetOwner + "'s home '" + targetHome + "'!");
            }
        } else {
            user.sendMessage("The home " + targetHome + " doesn't exist!");
        }
    }

    public void inviteToHome(Player player, String targetPlayer, String targetHome) {
        if (homeList.homeExists(player.getName(), targetHome)) {
            if (homeList.invitePlayer(player.getName(), targetPlayer, targetHome)) {
                player.sendMessage("Invited " + player + " to your home " + targetHome);
            } else {
                player.sendMessage(player + " was already invited to your home!");
            }
        } else {
            player.sendMessage("The home " + targetHome + " doesn't exist!");
        }
    }

    public void uninviteFromHome(Player player, String targetPlayer, String targetHome) {
        if (homeList.homeExists(player.getName(), targetHome)) {
            if (homeList.uninvitePlayer(player.getName(), targetPlayer, targetHome)) {
                player.sendMessage("Uninvited " + player + " from your home " + targetHome);
            } else {
                player.sendMessage(player + " wasn't invited to your home!");
            }
        } else {
            player.sendMessage("The home " + targetHome + " doesn't exist!");
        }
    }

    public void showHomeInfo(CommandSender user, String targetHome, String targetOwner) {
        Location homeLoc = homeList.getHomeLocation(targetOwner, targetHome);
        user.sendMessage("Home details: " + targetOwner + " : " + targetHome + " " + homeLoc.toString());
    }

    public void showHomeList(Player player) {
        String hList = this.homeList.getPlayerList(player.getName());

        if (hList == null) {
            player.sendMessage(ChatColor.RED + "You have no homes!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have the following homes:");
            player.sendMessage(hList);
        }
    }

    public void showHomeList(CommandSender sender, String targetPlayer) {
        String hList = this.homeList.getPlayerList(targetPlayer.toLowerCase());

        if (hList == null) {
            sender.sendMessage(ChatColor.RED + "That player has no homes.");
        } else {
            sender.sendMessage(ChatColor.AQUA + "That player has the following homes:");
            sender.sendMessage(hList);
        }
    }
    
    public void showInviteList(Player player) {
        String iList = this.homeList.getInvitedToList(player.getName());

        if (iList == null) {
            player.sendMessage(ChatColor.RED + "You have no invites!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have been invited to the following homes:");
            player.sendMessage(iList);
        }
    }

    public void showInviteList(CommandSender sender, String player) {
        String iList = this.homeList.getInvitedToList(player);

        if (iList == null) {
            sender.sendMessage(ChatColor.RED + "That player has no invites!");
        } else {
            sender.sendMessage(ChatColor.AQUA + "That player has invites to the following homes:");
            sender.sendMessage(iList);
        }
    }

    public void showRequestList(Player player) {
        String results[] = this.homeList.getRequestList(player.getName());

        if (results == null) {
            player.sendMessage(ChatColor.RED + "You haven't invited anyone!");
        } else {
            player.sendMessage(ChatColor.AQUA + "You have invited others to the following homes:");
            for (String s : results) {
                player.sendMessage(s);
            }
        }
    }

    public void showRequestList(CommandSender sender, String player) {
        String results[] = this.homeList.getRequestList(player);

        if (results == null) {
            sender.sendMessage(ChatColor.RED + "That player hasn't invited anyone!");
        } else {
            sender.sendMessage(ChatColor.AQUA + "That player has invited others to the following homes:");
            for (String s : results) {
                sender.sendMessage(s);
            }
        }
    }

    public void showHomeLimit(Player player) {
        player.sendMessage("You can set up to " + homeList.playerGetLimit(player) + " homes.");
    }

    public void showOtherLimit(CommandSender sender, String targetPlayer) {
        Player target = plugin.getServer().getPlayer(targetPlayer);

        if (target != null) {
            sender.sendMessage(target.getName() + " can set up to " + homeList.playerGetLimit(target) + " homes.");
        } else {
            sender.sendMessage("Player not found.");
        }
    }

    public void reloadSettings(CommandSender user) {
        user.sendMessage("[uHome] Reloading config.");
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
