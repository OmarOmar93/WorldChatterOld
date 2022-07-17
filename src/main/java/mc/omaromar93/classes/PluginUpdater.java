package mc.omaromar93.classes;

import mc.omaromar93.api.Events.WCL;
import mc.omaromar93.api.Events.WorldChatterListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
public class PluginUpdater {

    WorldChatter main;
    WCL wcl;


    public PluginUpdater(WorldChatter plugin) {
        this.main = plugin;
        this.wcl = plugin.getWcl();
    }

    public boolean isPluginUpdated(CommandSender sender) {
        String update;
        try {
            update = getUrlAsString("https://api.spigotmc.org/legacy/update.php?resource=101226");
            int update2 = Integer.parseInt(update.replace(".", ""));
            int updateold = Integer.parseInt(this.main.getDescription().getVersion().replace(".", ""));
            if (update2 > updateold) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "WorldChatter has released a new update! " + ChatColor.GRAY + "-> " + ChatColor.GREEN + update);
                for (WorldChatterListener hl : wcl.getlisteners())
                    hl.onUpdateCheck(update, this.main.getDescription().getVersion(), sender);
                return true;
            }
            if (update2 < updateold) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You appear to be using an early-access version of WorldChatter " + ChatColor.DARK_GRAY + "Perhaps the API hasn't been updated yet.");
                for (WorldChatterListener hl : wcl.getlisteners())
                    hl.onUpdateCheck(update, this.main.getDescription().getVersion(), sender);
                return true;
            }
            for (WorldChatterListener hl : wcl.getlisteners())
                hl.onUpdateCheck(update, this.main.getDescription().getVersion(), sender);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "You're using the most recent version of WorldChatter!");
            return false;
        } catch (Exception ignore) {
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