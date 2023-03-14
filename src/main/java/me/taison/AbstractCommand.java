package me.taison;

import me.taison.adnotations.Aliases;
import me.taison.adnotations.Command;
import me.taison.adnotations.Permission;
import me.taison.adnotations.PlayerNotRequired;
import me.taison.adnotations.argumentsmap.ArgumentsMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class AbstractCommand {

    private final Command commandInfo;
    private final Aliases aliases;
    private final Permission permission;
    private final PlayerNotRequired playerNotRequired;
    private final ArgumentsMap argumentsMap;
    private final JavaPlugin javaPlugin;
    @Nullable
    private Player player;



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
    public JavaPlugin getPlugin() {
        return javaPlugin;
    }
    @Nullable
    public Player getPlayer() {
        return player;
    }



    public AbstractCommand(JavaPlugin javaPlugin) {
        this.commandInfo = getClass().getDeclaredAnnotation(Command.class);
        this.aliases = getClass().getDeclaredAnnotation(Aliases.class);
        this.permission = getClass().getDeclaredAnnotation(Permission.class);
        this.playerNotRequired = getClass().getDeclaredAnnotation(PlayerNotRequired.class);
        this.argumentsMap = getClass().getDeclaredAnnotation(ArgumentsMap.class);
        this.javaPlugin = javaPlugin;
        this.player = null;
    }



    public void onCommand(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (playerNotRequired == null && Objects.isNull(player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.ILLEGAL_SENDER_MESSAGE));
            return;
        }
        try {
            this.execute(sender, label, args);
        }
        catch (Exception ex) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.COMMAND_EXCEPTION_MESSAGE));
            ex.printStackTrace();
        }
    }

    public abstract void execute(CommandSender sender, String commandLabel, String[] args);
}
