package me.taison;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.taison.api.AbstractCommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CommandManager implements Consumer<Class<? extends AbstractCommandExecutor>> {

    private final JavaPlugin plugin;
    private final YamlConfiguration commandsConfig;
    private final File commandsFile;

    @Override
    public void accept(Class<? extends AbstractCommandExecutor> clazz) {
        try {
            Constructor<? extends AbstractCommandExecutor> declaredConstructor = clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            AbstractCommandExecutor commandExecutor = declaredConstructor.newInstance();
            TaiCommand taiCommand = TaiCommand.builder()
                    .name(commandExecutor.getCommandName())
                    .usageMessage(commandExecutor.getCommandUsage())
                    .description(commandExecutor.getCommandDescription())
                    .permission(commandExecutor.getPermission())
                    .permissionMessage(commandExecutor.getPermissionMessage())
                    .argumentsMap(commandExecutor.getArgumentsMap())
                    .aliases(commandExecutor.getAliases())
                    .onExecute(commandExecutor::onCommandExecute)
                    .build();
            try {
                saveCommandToConfig(taiCommand, commandExecutor.getArgumentsMap());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Field field = plugin.getServer()
                    .getClass()
                    .getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(plugin.getServer());
            commandMap.register(plugin.getName(), taiCommand);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {

    }

    private void saveCommandToConfig(TaiCommand command, Map<Integer, List<String>> argumentsMap) throws IOException {
        ConfigurationSection configurationSection = commandsConfig.getConfigurationSection(command.getName());
        if (!Objects.isNull(configurationSection)) {
            setIfNull(configurationSection, "usage", command.getUsage());
            setIfNull(configurationSection, "description", command.getDescription());
            setIfNull(configurationSection, "permission", command.getPermission());
            setIfNull(configurationSection, "permissionMessage", command.permissionMessage());
            setIfNull(configurationSection, "aliases", command.getAliases());
            configurationSection.createSection("argumentsMap", argumentsMap);
        } else {
            commandsConfig.createSection(command.getName(), command.toMap());
        }
        commandsConfig.save(commandsFile);
    }

    private void setIfNull(ConfigurationSection configurationSection, String key, Object value) {
        if (Objects.isNull(configurationSection.get(key))) {
            configurationSection.set(key, value);
        }
    }
}
