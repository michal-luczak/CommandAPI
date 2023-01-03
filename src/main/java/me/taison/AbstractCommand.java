package me.taison;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public abstract class AbstractCommand extends Command implements CommandExecutor {

    private final ICommandInfo commandInfo;

    public ICommandInfo getCommandInfo() {
        return commandInfo;
    }

    public AbstractCommand(String command, String description, String usage, String[] aliases) {
        super(command, description, usage, Arrays.asList(aliases));
        commandInfo = getClass().getDeclaredAnnotation(ICommandInfo.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(commandInfo.isRequirePermission() && !sender.hasPermission(commandInfo.permission())) {
            //TODO messages
            return true;
        }
        if (commandInfo.isRequirePlayer() && !(sender instanceof Player)) {
            //TODO messages
            return true;
        }
        try{
            this.execute(sender, label, args);
        }
        catch (Exception ex){
            //TODO messages
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);

}
