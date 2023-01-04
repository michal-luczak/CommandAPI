package me.taison;

import me.taison.adnotations.Aliases;
import me.taison.adnotations.Command;
import me.taison.adnotations.Permission;
import me.taison.adnotations.PlayerNotRequired;
import me.taison.adnotations.argumentsmap.ArgumentsMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractCommand {

    private final Command commandInfo;
    private final Aliases aliases;
    private final Permission permission;
    private final PlayerNotRequired playerNotRequired;
    private final ArgumentsMap argumentsMap;



    public Aliases getAliases() {
        return aliases;
    }

    public Permission getPermission() {
        return permission;
    }

    public Command getCommandInfo() {
        return commandInfo;
    }

    public PlayerNotRequired getPlayerNotRequired() {
        return playerNotRequired;
    }

    public ArgumentsMap getArgumentsMap() {
        return argumentsMap;
    }

    public AbstractCommand() {
        commandInfo = getClass().getDeclaredAnnotation(Command.class);
        aliases = getClass().getDeclaredAnnotation(Aliases.class);
        permission = getClass().getDeclaredAnnotation(Permission.class);
        playerNotRequired = getClass().getDeclaredAnnotation(PlayerNotRequired.class);
        argumentsMap = getClass().getDeclaredAnnotation(ArgumentsMap.class);
    }

    public void onCommand(CommandSender sender, String label, String[] args) {
        if (playerNotRequired == null && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.ILLEGAL_SENDER_MESSAGE));
            return;
        }
        try{
            this.execute(sender, label, args);
        }
        catch (Exception ex){
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.COMMAND_EXCEPTION_MESSAGE));
            ex.printStackTrace();
        }
    }

    public abstract void execute(CommandSender sender, String commandLabel, String[] args);

}
