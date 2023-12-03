package me.taison;

import me.taison.api.AbstractCommandExecutor;
import me.taison.api.annotations.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.File;
import java.io.IOException;
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
        YamlConfiguration commandFile = getCommandFile(file);
        Set<TaiCommand> commandList = loadCommandsFromFile(commandFile);
        addCommandsFromDefinedExecutors(commandList);
        registerCommands(commandList);
        commandList.forEach(command -> saveCommandsToFile(commandFile, file, command));
    }

    private void saveCommandsToFile(YamlConfiguration commandsConfig, File commandsFile, TaiCommand command) {
        ConfigurationSection configurationSection = commandsConfig.getConfigurationSection(command.getName());
        if (!Objects.isNull(configurationSection)) {
            setIfNull(configurationSection, "usage", command.getUsage());
            setIfNull(configurationSection, "description", command.getDescription());
            setIfNull(configurationSection, "permission", command.getPermission());
            setIfNull(configurationSection, "permissionMessage", command.permissionMessage());
            setIfNull(configurationSection, "aliases", command.getAliases());
            setIfNull(configurationSection, "argumentsMap", command.getArgumentsMap());
        } else {
            commandsConfig.createSection(command.getName(), command.toMap());
        }
        try {
            commandsConfig.save(commandsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIfNull(ConfigurationSection configurationSection, String key, Object value) {
        if (Objects.isNull(configurationSection.get(key))) {
            configurationSection.set(key, value);
        }
    }

    private void addCommandsFromDefinedExecutors(Set<TaiCommand> commandSet) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().setUrls(ClasspathHelper.forClass(javaPlugin.getClass()));
        Reflections reflections = new Reflections(configurationBuilder);
        Set<TaiCommand> definedCommands = reflections.getSubTypesOf(AbstractCommandExecutor.class)
                .stream()
                .filter(clazz -> clazz.isAnnotationPresent(CommandExecutor.class))
                .filter(clazz -> javaPlugin.getCommand(clazz.getDeclaredAnnotation(CommandExecutor.class).value()) == null)
                .map(clazz -> TaiCommand.builder().name(clazz.getDeclaredAnnotation(CommandExecutor.class).value()).build())
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
        Set<String> keys = commandFile.getKeys(false);
        return keys.stream().map(key -> {
            System.out.println(key);
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

    private YamlConfiguration getCommandFile(File file) {
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
