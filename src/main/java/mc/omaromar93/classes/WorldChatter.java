package mc.omaromar93.classes;


import mc.omaromar93.api.Events.WCL;
import mc.omaromar93.api.Events.WorldChatterListener;
import mc.omaromar93.api.enums.BlockType;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class WorldChatter extends JavaPlugin implements Listener, WorldChatterListener {

    Others others;
    WCL wcl;

    private Boolean lockChat = false;
    private Boolean placeholderapisuppport = false;
    PluginUpdater updater;
    String papi = "PlaceholderAPI";
    String playerexpression = "%player%";
    String chatlock = "ChatLock";
    String antispam = "AntiSpam";

    @Override
    public void onEnable() {
        this.wcl = new WCL(this);
        wcl.addListener(this);
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("worldchatter")).setExecutor(this);
        this.others = new Others(this);
        this.updater = new PluginUpdater(this);
        new Thread("WorldChatter Side-Features Thread") {
            @Override
            public void run() {
                others.createCustomConfig();
                others.createcustommessagesconfig();
                others.loadConfigs();
                others.loadCustomConfigs();
                if (updater.isPluginUpdated(Bukkit.getConsoleSender()))
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "From spigot, you can download the most recent version.");
                if (Bukkit.getPluginManager().getPlugin(papi) == null) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Your server does not have "+ChatColor.BLUE+ papi+ChatColor.YELLOW+" installed! " + ChatColor.GRAY + "(Utilizing the built-in expressions)");
                } else {
                    placeholderapisuppport = true;
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Your server does have "+ChatColor.BLUE+ papi+ChatColor.YELLOW+" installed! " + ChatColor.GRAY + "(Using the expressions of PlaceholderAPI)");
                }
            }
        }.start();
    }

    ArrayList<Player> seconds = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent chat) {
        if (!Objects.requireNonNull(others.getConfig().getList("BlackListWorlds")).contains(chat.getPlayer().getWorld().getName())) {
            chat.getRecipients().clear();
            Set<Player> receivers = new HashSet<>(chat.getPlayer().getWorld().getPlayers());
            chat.getRecipients().addAll(receivers);
            if (!others.getConfig().getBoolean(chatlock) || Boolean.TRUE.equals(!lockChat)) {
                String msg = chat.getMessage();
                if (others.getConfig().isInt(antispam) && (!(seconds.contains(chat.getPlayer()))) && others.getConfig().getInt(antispam) >= 0) {
                    if (others.getConfig().getBoolean("ColoredText")) {
                        msg = ChatColor.translateAlternateColorCodes('&', chat.getMessage());
                    }
                    if (others.getConfig().getBoolean("AntiSwear")) {
                        ArrayList<String> badWords = others.badWordsFound(msg);
                        if (!badWords.isEmpty()) {
                            for (WorldChatterListener hl : wcl.getlisteners())
                                hl.onMessageSwear(badWords, chat.getPlayer());
                            chat.setCancelled(true);
                            return;
                        }
                    }
                    if (others.getConfig().getBoolean("AntiURL") && others.isUrl(msg)) {
                        for (WorldChatterListener hl : wcl.getlisteners())
                            hl.onMessageDetected(others.getUrl(msg), chat.getPlayer(), BlockType.URL);
                        chat.setCancelled(true);
                        return;
                    }
                    if (others.getConfig().getBoolean("AntiIP") && others.isIP(msg)) {
                        for (WorldChatterListener hl : wcl.getlisteners())
                            hl.onMessageDetected(others.getIP(msg), chat.getPlayer(), BlockType.IP);
                        chat.setCancelled(true);
                        return;
                    }
                    String finalMsg = msg;
                    if (others.getConfig().getBoolean("ChatFormatting")) {
                        if (Boolean.FALSE.equals(placeholderapisuppport)) {
                            chat.setFormat(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getConfig().getString("FormatStyle"))
                                    .replace(playerexpression, chat.getPlayer().getName())
                                    .replace("%world%", chat.getPlayer().getWorld().getName())));
                        } else {
                            String textmessage = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getConfig().getString("FormatStyle")));
                            String apimesssage = PlaceholderAPI.setPlaceholders(chat.getPlayer(), textmessage);
                            chat.setFormat(apimesssage.replace("%", "^") + "%2$s");
                        }
                    }
                    chat.setMessage(finalMsg);
                    seconds.add(chat.getPlayer());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> seconds.remove(chat.getPlayer()), others.getConfig().getInt(antispam) * 20L);
                } else {
                    for (WorldChatterListener hl : wcl.getlisteners())
                        hl.onMessageDetected(msg, chat.getPlayer(), BlockType.SPAM);
                    chat.setCancelled(true);
                }
            } else {
                chat.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player && sender.isOp()) {
            Player player = (Player) sender;
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    player.sendMessage(ChatColor.YELLOW + "Reloading the WorldChatter Configuration...");
                    others.createCustomConfig();
                    others.createcustommessagesconfig();
                    others.loadCustomConfigs();
                    for (WorldChatterListener hl : wcl.getlisteners())
                        hl.onConfigReload(sender);
                } else if (args[0].equalsIgnoreCase("update")) {
                    if (updater.isPluginUpdated(player)) {
                        player.sendMessage(ChatColor.YELLOW + "Update discovered, For more information, check the console!");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "There were no updates found; please consult the console for more information!");
                    }
                } else if (args[0].equalsIgnoreCase("lock")) {
                    if (!others.getConfig().getBoolean(chatlock)) {
                        sender.sendMessage(ChatColor.RED + "The chat cannot be locked! " + ChatColor.YELLOW + "Because the \"ChatLock\" feature is disabled, it is possible to enable it in the configuration.");
                    } else {
                        lockChat = !lockChat;
                        for (WorldChatterListener hl : wcl.getlisteners())
                            hl.onChatLockToggle(player, lockChat);
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(ChatColor.GREEN + "You have (Help/update/lock/reload) ez.");
                } else {
                    player.sendMessage(ChatColor.RED + "I'm not sure what that is.");
                }
            }
        } else if (!(sender instanceof Player) &&
                args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Reloading the WorldChatter Configuration...");
                others.createCustomConfig();
                others.createcustommessagesconfig();
                others.loadCustomConfigs();
                for (WorldChatterListener hl : wcl.getlisteners())
                    hl.onConfigReload(sender);
            } else if (args[0].equalsIgnoreCase("update")) {
                if (updater.isPluginUpdated(Bukkit.getConsoleSender())) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "From spigot, you can download the most recent version.");
                }
            } else if (args[0].equalsIgnoreCase("lock")) {
                if (!others.getConfig().getBoolean(chatlock)) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The chat cannot be locked! " + ChatColor.YELLOW + "Because the \"ChatLock\" feature is disabled, it is possible to enable it in the configuration.");
                } else {
                    lockChat = !lockChat;
                    for (WorldChatterListener hl : wcl.getlisteners())
                        hl.onChatLockToggle(Bukkit.getConsoleSender(), lockChat);
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "You have (Help/update/lock/reload) ez.");
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "I'm not sure what that is.");
            }
        }
        return true;
    }

    @Override
    public void onMessageDetected(Object detectedmessage, Player player, BlockType type) {
        if (type.equals(BlockType.IP))
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("IPMessage")).replace(playerexpression, player.getName()).replace("%message%", detectedmessage.toString())));
        if (type.equals(BlockType.URL))
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("URLMessage")).replace(playerexpression, player.getName()).replace("%message%", detectedmessage.toString())));
    }

    @Override
    public void onMessageSwear(ArrayList<String> badwords, Player player) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("SwearDetected")).replace(playerexpression, player.getName()).replace("%words%", String.join(", ", badwords)).replace("%words_size%", String.valueOf(badwords.size()))));
    }

    @Override
    public void onChatLockToggle(CommandSender sender, Boolean isLocked) {
        if (Boolean.TRUE.equals(isLocked))
            sender.sendMessage(ChatColor.YELLOW + "The chat is now " + ChatColor.RED + "closed!");
        else
            sender.sendMessage(ChatColor.YELLOW + "The chat is now " + ChatColor.GREEN + "opened!");
    }

    @Override
    public void onUpdateCheck(String oldversion, String newversion, CommandSender sender) {
        // nothing
    }

    @Override
    public void onConfigReload(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "The WorldChatter Config has been reloaded!");
    }

    public WCL getWcl() {
        return this.wcl;
    }
}