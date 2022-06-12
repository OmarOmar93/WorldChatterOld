package mc.omaromar93.API.Events;

import mc.omaromar93.API.enums.BlockType;
import mc.omaromar93.classes.WorldChatter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class WCL {

    WorldChatter main;

    public WCL(WorldChatter plugin) {
        this.main = plugin;

    }

    private final List<WorldChatterListener> listeners = new ArrayList<>();

    public void addListener(WorldChatterListener toAdd) {
        listeners.add(toAdd);
    }

    public void detectmessage(Object detectedmessage, Player player, BlockType type) {
        for (WorldChatterListener hl : listeners)
            hl.onMessageDetected(detectedmessage, player, type);
    }

    public void detectswear(ArrayList<String> badwords, Player player) {
        for (WorldChatterListener hl : listeners)
            hl.onMessageSwear(badwords, player);
    }

    public void chattoogle(CommandSender sender, Boolean bool) {
        for (WorldChatterListener hl : listeners)
            hl.onChatLockToggle(sender, bool);
    }


    public void updateevent(String oldversion, String newversion, CommandSender player) {
        for (WorldChatterListener hl : listeners)
            hl.onUpdateCheck(oldversion, newversion, player);
    }

    public void reloadconfig(CommandSender player) {
        for (WorldChatterListener hl : listeners)
            hl.onConfigReload(player);
    }
}