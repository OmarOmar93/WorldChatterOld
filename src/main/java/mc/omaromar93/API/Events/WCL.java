package mc.omaromar93.API.Events;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public class WCL {

    JavaPlugin main;

    public WCL(JavaPlugin plugin) {
        this.main = plugin;

    }

    private final List<WorldChatterListener> listeners = new ArrayList<>();

    public void addListener(WorldChatterListener toAdd) {
        listeners.add(toAdd);
    }

    public List<WorldChatterListener> getlisteners() {
        return listeners;
    }

}