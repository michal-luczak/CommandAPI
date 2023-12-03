package me.taison;

import me.taison.api.AbstractCommandExecutor;
import me.taison.api.annotations.GetCommand;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public final class CommandAPI {

    private static CommandAPI INSTANCE;

    public static String ILLEGAL_SENDER_MESSAGE = "&4Aby użyć tej komendy musisz być graczem!";
    public static String COMMAND_EXCEPTION_MESSAGE = "&4Jeżeli widzisz ten komunikat, skontaktuj się z administratorem!";

    public static synchronized CommandAPI getInstance(JavaPlugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new CommandAPI(plugin);
        }
        return INSTANCE;
    }

    private final JavaPlugin javaPlugin;

    private CommandAPI(JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
    }

    public void loadAndRegisterCommands() {
        javaPlugin.getDataFolder().mkdir();
        File file = new File(javaPlugin.getDataFolder(), "commands.yml");
        YamlConfiguration commandFile = loadCommandFile(file);
        Set<TaiCommand> commandList = loadCommandsFromFile(commandFile);
        addCommandsFromDefinedExecutors(commandList);
        registerCommands(commandList);
    }

    private void addCommandsFromDefinedExecutors(Set<TaiCommand> commandSet) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().setUrls(ClasspathHelper.forClass(javaPlugin.getClass()));
        Reflections reflections = new Reflections(configurationBuilder);
        Set<TaiCommand> definedCommands = reflections.getSubTypesOf(AbstractCommandExecutor.class)
                .stream()
                .filter(clazz -> clazz.isAnnotationPresent(GetCommand.class))
                .filter(clazz -> javaPlugin.getCommand(clazz.getDeclaredAnnotation(GetCommand.class).value()) == null)
                .map(clazz -> TaiCommand.builder().name(clazz.getDeclaredAnnotation(GetCommand.class).value()).build())
                .collect(Collectors.toSet());
        commandSet.addAll(definedCommands);
    }

    private void registerCommands(Set<TaiCommand> commandList) {
        try {
            Field field = javaPlugin.getServer()
                    .getClass()
                    .getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(javaPlugin.getServer());
            commandList.forEach(command -> commandMap.register(javaPlugin.getName(), command));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<TaiCommand> loadCommandsFromFile(YamlConfiguration commandFile) {
        Set<String> keys = commandFile.getKeys(true);
        return keys.stream().map(key -> {
            ConfigurationSection configurationSection = commandFile.getConfigurationSection(key);
            Objects.requireNonNull(configurationSection);
            String usage = configurationSection.getString("usage");
            String description = configurationSection.getString("description");
            String permission = configurationSection.getString("permission");
            String permissionMessage = configurationSection.getString("permissionMessage");
            List<String> aliases = configurationSection.getStringList("aliases");
            List<List<String>> argumentsMap = (List<List<String>>) configurationSection.getList("argumentsMap");
            return TaiCommand.builder()
                    .name(key)
                    .usageMessage(usage)
                    .description(description)
                    .permission(permission)
                    .permissionMessage(permissionMessage)
                    .argumentsMap(argumentsMap)
                    .aliases(aliases)
                    .build();
        }).collect(Collectors.toSet());
    }

    private YamlConfiguration loadCommandFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
