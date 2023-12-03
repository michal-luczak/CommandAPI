package me.taison.api;

import lombok.SneakyThrows;
import me.taison.CommandAPI;
import me.taison.api.annotations.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public abstract class AbstractCommandExecutor implements CommandExecutor {

    private final @NotNull List<Class<? extends CommandSender>> requiredSenderTypes;

    protected AbstractCommandExecutor() {
        this.requiredSenderTypes = getValueFromAnnotation(RequiredSenderTypes.class,
                requiredSenderTypes -> List.of(requiredSenderTypes.value()),
                Collections.emptyList()
        );
    }

    public boolean onCommandExecute(CommandSender sender, Command command, String label, String[] args) {
        if (FALSE.equals(isSenderTypeCorrect(sender))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.ILLEGAL_SENDER_MESSAGE));
            return true;
        }
        try {
            return onCommand(sender, command, label, args);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', CommandAPI.COMMAND_EXCEPTION_MESSAGE));
            ex.printStackTrace();
        }
        return true;
    }

    private Boolean isSenderTypeCorrect(CommandSender sender) {
        if (!requiredSenderTypes.contains(sender.getClass()) && !requiredSenderTypes.isEmpty()) {
            return FALSE;
        }
        return TRUE;
    }

    private <T extends Annotation, E> E getValueFromAnnotation(
            Class<T> annotationClass,
            Function<T, E> mapper,
            E alternativeValue
    ) {
        return Optional.ofNullable(getClass().getDeclaredAnnotation(annotationClass))
                .map(mapper)
                .orElse(alternativeValue);
    }
}
