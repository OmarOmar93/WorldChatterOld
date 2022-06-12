package mc.omaromar93.classes;

import mc.omaromar93.API.Events.WCL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
public class PluginUpdater extends Thread {

    WorldChatter main;
    WCL wcl;

    public PluginUpdater(WorldChatter plugin) {
        this.main = plugin;
        this.wcl = plugin.getWcl();

    }

    public void run() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Running the Plugin Updater in it's own thread..");
    }

    public boolean isPluginUpdated(CommandSender sender) {
        String update;
        try {
            update = getUrlAsString("https://api.spigotmc.org/legacy/update.php?resource=101226");
            int update2 = Integer.parseInt(update.replace(".", ""));
            int updateold = Integer.parseInt(this.main.getDescription().getVersion().replace(".", ""));
            if (update2 > updateold) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "New WorldChatter Update available! " + ChatColor.GRAY + "-> " + ChatColor.GREEN + update);
                this.wcl.updateevent(update, this.main.getDescription().getVersion(), sender);
                return true;
            }
            if (update2 < updateold) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Looks like you're using an Early-access version of WorldChatter " + ChatColor.DARK_GRAY + "Or you just changed the code");
                this.wcl.updateevent(update, this.main.getDescription().getVersion(), sender);
                return true;
            }
            this.wcl.updateevent(update, this.main.getDescription().getVersion(), sender);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "You're using the latest WorldChatter!");
            return false;
        } catch (Exception ignore) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error while getting updates.");
        }
        return false;
    }

    public String getUrlAsString(String url) throws IOException {
        URL url2 = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream()));
        String line;
        if ((line = in.readLine()) != null)
            return line;
        in.close();
        return url;
    }
}