package me.taison;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class CommandAPI {

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


    public void initializeCommands() {
        this.packageNames.forEach(packageName -> {

            for (Class<? extends AbstractCommand> clazz : new Reflections(getClass().getPackage().getName() + packageName).getSubTypesOf(AbstractCommand.class)) {
                try {
                    AbstractCommand playerCommand = clazz.getDeclaredConstructor(String.class, String.class, String.class, String[].class)
                            .newInstance(
                                    clazz.getDeclaredAnnotation(ICommandInfo.class).command(),
                                    clazz.getDeclaredAnnotation(ICommandInfo.class).description(),
                                    clazz.getDeclaredAnnotation(ICommandInfo.class).usage(),
                                    clazz.getDeclaredAnnotation(ICommandInfo.class).aliases()
                            );

                    try {
                        Field field = this.javaPlugin.getServer().getClass().getDeclaredField("commandMap");
                        field.setAccessible(true);
                        CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());
                        commandMap.register(this.javaPlugin.getName(), playerCommand);
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }

                    this.javaPlugin.getCommand(playerCommand.getCommandInfo().command()).setExecutor(playerCommand);
                } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        });

    }



    public List<String> getPackageNames() {
        return this.packageNames;
    }


    public static CommandAPI getInstance(JavaPlugin plugin) {
        return new CommandAPI(plugin);
    }
}

//CommandAPI.getInstance(Bukkit.getPluginManager, this)
//          .addPackageName;
//          .addPackageName;
//          .addPackageName;
//          .initializeCommands();