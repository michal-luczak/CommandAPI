import me.taison.api.AbstractCommandExecutor;
import me.taison.api.annotations.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandExecutor("test")
class ExampleCommandExecutor extends AbstractCommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Define what this command would suppose to do
        return true;
    }
}
