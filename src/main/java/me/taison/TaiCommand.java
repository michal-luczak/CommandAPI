package me.taison;

import lombok.AccessLevel;
import lombok.Builder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Builder(access = AccessLevel.PACKAGE)
final class TaiCommand extends Command {

    private @NotNull String name;
    private @NotNull String description;
    private @NotNull String usageMessage;
    private @NotNull List<String> aliases;
    private @NotNull String permission;
    private @NotNull String permissionMessage;
    private final @NotNull CommandExecutor onExecute;
    private final @NotNull List<List<String>> argumentsMap;

    private TaiCommand(
            @NotNull String name,
            @NotNull String description,
            @NotNull String usageMessage,
            @NotNull List<String> aliases,
            @NotNull String permission,
            @NotNull String permissionMessage,
            @NotNull CommandExecutor onExecute,
            @NotNull List<List<String>> argumentsMap
    ) {
        super(name, description, usageMessage, aliases);
        super.setPermission(permission);
        super.permissionMessage(Component.text(permissionMessage));
        this.name = name;
        this.description = description;
        this.usageMessage = usageMessage;
        this.aliases = aliases;
        this.permission = permission;
        this.permissionMessage = permissionMessage;
        this.onExecute = onExecute;
        this.argumentsMap = argumentsMap;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return onExecute.onCommand(sender, this, commandLabel, args);
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> arguments = Collections.emptyList();
        if (args.length <= argumentsMap.size()) {
            arguments = argumentsMap.get(args.length - 1)
                    .stream()
                    .filter(argument -> argument.toLowerCase().startsWith(args[args.length - 1]))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return arguments;
    }

    Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("usage", usageMessage);
        map.put("description", description);
        map.put("permission", permission);
        map.put("permissionMessage", permissionMessage);
        map.put("aliases", aliases);
        map.put("argumentsMap", argumentsMap);
        return map;
    }
}
