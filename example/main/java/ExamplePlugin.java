import me.taison.CommandAPI;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

class ExamplePlugin extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        CommandAPI commandAPI = CommandAPI.getInstance(this);
        commandAPI.loadAndRegisterCommands();
    }
}
