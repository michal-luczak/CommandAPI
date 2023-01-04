package me.taison;

import me.taison.adnotations.Aliases;
import me.taison.adnotations.Command;
import me.taison.adnotations.Permission;
import me.taison.adnotations.argumentsmap.ArgumentsMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandAPI {

    public static String ILLEGAL_SENDER_MESSAGE = "&4Aby użyć tej komendy musisz być graczem!";
    public static String COMMAND_EXCEPTION_MESSAGE = "&4Jeżeli widzisz ten komunikat, skontaktuj się z administratorem!";
    public static String PERMISSION_MESSAGE = "&4Nie posiadasz uprawnień!";

    private final JavaPlugin javaPlugin;
    private final List<String> packageNames;


    public CommandAPI(JavaPlugin javaPlugin) {
        this.packageNames = new ArrayList<>();
        this.javaPlugin = javaPlugin;
    }

    public CommandAPI addPackageName(String packageName) {
        this.packageNames.add(packageName);
        return this;
    }


    public void registerCommands() {
        this.packageNames.forEach(packageName -> {
            for (Class<? extends AbstractCommand> clazz : new Reflections(this.javaPlugin.getClass().getPackage().getName() + packageName).getSubTypesOf(AbstractCommand.class)) {
                try {
                    AbstractCommand playerCommand = clazz.getDeclaredConstructor().newInstance();

                    Command commandInfo = playerCommand.getCommandInfo();
                    Aliases aliases = playerCommand.getAliases();
                    Permission permission = playerCommand.getPermission();
                    ArgumentsMap argumentsMap = playerCommand.getArgumentsMap();

                    Field field = this.javaPlugin.getServer().getClass().getDeclaredField("commandMap");
                    field.setAccessible(true);
                    CommandMap commandMap = (CommandMap) field.get(this.javaPlugin.getServer());
                    org.bukkit.command.Command command = new org.bukkit.command.Command(commandInfo.name()) {
                        @Override
                        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                            playerCommand.onCommand(sender, commandLabel, args);
                            return false;
                        }

                        @Override
                        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {

                            List<String> arguments;

                            if (args.length <= argumentsMap.arguments().length) {
                                arguments = new ArrayList<>();
                                Arrays.stream(argumentsMap.arguments()[args.length - 1].arguments())
                                        .filter(argument -> {
                                            return argument.toLowerCase().startsWith(args[args.length - 1]);
                                        })
                                        .forEach(arguments::add);

                                Collections.sort(arguments);
                                return arguments;
                            }

                            return null;
                        }
                    };

                    command.setDescription(commandInfo.description())
                            .setUsage(commandInfo.usage());

                    if (aliases != null) {
                        command.setAliases(Arrays.asList(aliases.aliases()));
                    }
                    if (permission != null) {
                        command.setPermission(permission.permission());
                        command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', PERMISSION_MESSAGE));
                    }
                    commandMap.register(this.javaPlugin.getName(), command);

                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public static CommandAPI getInstance(JavaPlugin plugin) {
        return new CommandAPI(plugin);
    }
}
