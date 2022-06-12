package mc.omaromar93.classes;


import mc.omaromar93.API.Events.WCL;
import mc.omaromar93.API.Events.WorldChatterListener;
import mc.omaromar93.API.enums.BlockType;
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
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public final class WorldChatter extends JavaPlugin implements Listener, WorldChatterListener {

    Others others;
    WCL wcl;

    private Boolean lockChat = false;
    private Boolean placeholderapisuppport = false;
    private final Random rand = SecureRandom.getInstanceStrong();
    PluginUpdater updater;

    public WorldChatter() throws NoSuchAlgorithmException {
        // for random to work
    }

    @Override
    public void onDisable() {
        others.stop();
        updater.stop();
    }

    @Override
    public void onEnable() {
        this.others = new Others(this);
        this.updater = new PluginUpdater(this);
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("worldchatter")).setExecutor(this);
        others.start();
        others.setName("Side-Features");
        updater.start();
        updater.setName("Updater");
        others.createCustomConfig();
        others.createcustombroadcastconfig();
        others.createcustommessagesconfig();
        others.loadConfigs();
        others.loadCustomConfigs();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded Custom words from Config file");
        this.wcl = new WCL(this);
        wcl.addListener(this);
        if (updater.isPluginUpdated(Bukkit.getConsoleSender()))
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You can download the latest version from spigot!");
        if (others.getConfig().getBoolean("AutoSave")) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                others.createCustomConfig();
                others.createcustombroadcastconfig();
                others.createcustommessagesconfig();
                others.loadCustomConfigs();
            }, 0L, 20L);
        }
        startBroadcastTimer(getServer().getScheduler());
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "PlaceholderAPI " + ChatColor.YELLOW + "is not installed in ur server! " + ChatColor.GRAY + "(Using the default expressions)");
        } else {
            placeholderapisuppport = true;
            Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "PlaceholderAPI " + ChatColor.YELLOW + "is installed in ur server! " + ChatColor.GRAY + "(Using PlaceholderAPI's expressions)");
        }
    }

    private void startBroadcastTimer(BukkitScheduler scheduler) {
        scheduler.scheduleSyncDelayedTask(this, () -> {
            String msg = Objects.requireNonNull(others.getBroadCastConfig().getList("Texts")).get(rand.nextInt(Objects.requireNonNull(others.getBroadCastConfig().getList("Texts")).size())).toString();
            msg = msg.replace("%server players%", String.valueOf(getServer().getOnlinePlayers().size()));
            String finalMsg = msg;
            Objects.requireNonNull(others.getBroadCastConfig().getList("Worlds")).forEach(world -> Objects.requireNonNull(getServer().getWorld(world.toString())).getPlayers().forEach(player -> player.sendMessage(finalMsg.replace("%world players%", String.valueOf(Objects.requireNonNull(getServer().getWorld(world.toString())).getPlayers().size())))));
            startBroadcastTimer(scheduler);
        }, others.getBroadCastConfig().getInt("Time") * 20L);
    }

    ArrayList<Player> seconds = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent chat) {
        if (!Objects.requireNonNull(others.getBroadCastConfig().getList("BlackListWorlds")).contains(chat.getPlayer().getWorld().getName())) {
            chat.getRecipients().clear();
            Set<Player> receivers = new HashSet<>(chat.getPlayer().getWorld().getPlayers());
            chat.getRecipients().addAll(receivers);
            if (!others.getConfig().getBoolean("ChatLock") || Boolean.TRUE.equals(!lockChat)) {
                String msg = chat.getMessage();
                if (others.getConfig().isInt("AntiSpam") && (!(seconds.contains(chat.getPlayer()))) && others.getConfig().getInt("AntiSpam") >= 0) {
                    if (others.getConfig().getBoolean("ColoredText")) {
                        msg = ChatColor.translateAlternateColorCodes('&', chat.getMessage());
                    }
                    if (others.getConfig().getBoolean("AntiSwear")) {
                        ArrayList<String> badWords = others.badWordsFound(msg);
                        if (!badWords.isEmpty()) {
                            this.wcl.detectswear(badWords, chat.getPlayer());
                            chat.setCancelled(true);
                            return;
                        }
                    }
                    if (others.getConfig().getBoolean("AntiURL") && others.isUrl(msg)) {
                        this.wcl.detectmessage(others.getUrl(msg), chat.getPlayer(), BlockType.URL);
                        chat.setCancelled(true);
                        return;
                    }
                    if (others.getConfig().getBoolean("AntiIP") && others.isIP(msg)) {
                        this.wcl.detectmessage(others.getIP(msg), chat.getPlayer(), BlockType.IP);
                        chat.setCancelled(true);
                        return;
                    }
                    String finalMsg = msg;
                    if (others.getConfig().getBoolean("ChatFormatting")) {
                        if (Boolean.FALSE.equals(placeholderapisuppport)) {
                            chat.setFormat(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getConfig().getString("FormatStyle"))
                                    .replace("%player%", chat.getPlayer().getName())
                                    .replace("%world%", chat.getPlayer().getWorld().getName())));
                        } else {
                            String textmessage = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getConfig().getString("FormatStyle")));
                            String apimesssage = PlaceholderAPI.setPlaceholders(chat.getPlayer(), textmessage);
                            chat.setFormat(apimesssage.replace("%", "^") + "%2$s");
                        }
                    }
                    chat.setMessage(finalMsg);
                    seconds.add(chat.getPlayer());
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> seconds.remove(chat.getPlayer()), others.getConfig().getInt("AntiSpam") * 20L);
                } else {
                    this.wcl.detectmessage(msg, chat.getPlayer(), BlockType.SPAM);
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
                    player.sendMessage(ChatColor.YELLOW + "Reloading WorldChatter Config...");
                    others.createCustomConfig();
                    others.createcustombroadcastconfig();
                    others.createcustommessagesconfig();
                    others.loadCustomConfigs();
                    this.wcl.reloadconfig(sender);
                }
                if (args[0].equalsIgnoreCase("update"))
                    if (updater.isPluginUpdated(player)) {
                        player.sendMessage(ChatColor.YELLOW + "Update Found, Check the console for more details!");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "No Updates were found, Check the console for more details!");
                    }
                if (args[0].equalsIgnoreCase("lock"))
                    if (!others.getConfig().getBoolean("ChatLock")) {
                        sender.sendMessage(ChatColor.RED + "Cannot lock the chat! " + ChatColor.YELLOW + "because, the \"ChatLock\" feature is disabled\nYou can enable it in the config");
                    } else {
                        lockChat = !lockChat;
                        this.wcl.chattoogle(player, lockChat);
                    }
            }
        } else if (!(sender instanceof Player) &&
                args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Reloading WorldChatter Configs...");
                others.createCustomConfig();
                others.createcustombroadcastconfig();
                others.createcustommessagesconfig();
                others.loadCustomConfigs();
                this.wcl.reloadconfig(sender);
            }
            if (args[0].equalsIgnoreCase("update") &&
                    updater.isPluginUpdated(Bukkit.getConsoleSender()))
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "You can download the latest version from spigot!");
            if (args[0].equalsIgnoreCase("lock"))
                if (!others.getConfig().getBoolean("ChatLock")) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Cannot lock the chat! " + ChatColor.YELLOW + "because, the \"ChatLock\" feature is disabled\nYou can enable it in the config");
                } else {
                    lockChat = !lockChat;
                    this.wcl.chattoogle(Bukkit.getConsoleSender(), lockChat);
                }
        }
        return true;
    }

    @Override
    public void onMessageDetected(Object detectedmessage, Player player, BlockType type) {
        if (type.equals(BlockType.IP))
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("IPMessage")).replace("%player%", player.getName()).replace("%message%", detectedmessage.toString())));
        if (type.equals(BlockType.URL))
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("URLMessage")).replace("%player%", player.getName()).replace("%message%", detectedmessage.toString())));
    }

    @Override
    public void onMessageSwear(ArrayList<String> badwords, Player player) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(others.getMessageConfig().getString("SwearDetected")).replace("%player%", player.getName()).replace("%words%", String.join(", ", badwords)).replace("%words_size%", String.valueOf(badwords.size()))));
    }

    @Override
    public void onChatLockToggle(CommandSender sender, Boolean isLocked) {
        if (Boolean.TRUE.equals(isLocked))
            sender.sendMessage(ChatColor.GREEN + "The Chat is now " + ChatColor.YELLOW + "Locked!");
        else
            sender.sendMessage(ChatColor.GREEN + "The Chat is now " + ChatColor.YELLOW + "Unlocked!");
    }

    @Override
    public void onUpdateCheck(String oldversion, String newversion, CommandSender sender) {
        // Do nothing to prevent the listener error
    }

    @Override
    public void onConfigReload(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Reloaded WorldChatter Config!");
    }

    public WCL getWcl() {
        return this.wcl;

    }

}