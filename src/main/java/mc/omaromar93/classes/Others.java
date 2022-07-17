package mc.omaromar93.classes;

import com.google.common.net.InetAddresses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
@SuppressWarnings("ALL")
public class Others {

    WorldChatter main;

    public Others(WorldChatter plugin) {
        this.main = plugin;
    }

    private final HashMap<String, String[]> words = new HashMap<>();
    private final HashMap<String, Integer> customwords = new HashMap<>();

    private int largestWordLength = 0;
    private FileConfiguration config;
    private FileConfiguration broadCastconfig;

    private FileConfiguration messageconfig;

    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getBroadCastConfig() {
        return this.broadCastconfig;
    }

    public FileConfiguration getMessageConfig() {
        return this.messageconfig;
    }

    void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(new BufferedReader(new InputStreamReader((this.main.getResource("swearwords.txt")))));
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null) {
                counter++;
                String[] content;
                try {
                    content = line.split(",");
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if (content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }

                    if (word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);

                } catch (Exception ignore) {
                }
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + ""+ counter + ChatColor.GREEN + " words have been loaded to be filtered");
        } catch (IOException ignore) {
        }
    }

    void loadCustomConfigs() {
        customwords.clear();
        for (Object o : Objects.requireNonNull(config.getList("CustomSwearWords"))) {
            customwords.put(o.toString().toLowerCase(), 1);
        }
    }

    ArrayList<String> badWordsFound(String input) {
        if (input == null) {
            return new ArrayList<>();
        }
        String input2 = input;
        input = input.replace("1", "i");
        input = input.replace("!", "i");
        input = input.replace("3", "e");
        input = input.replace("4", "a");
        input = input.replace("@", "a");
        input = input.replace("5", "s");
        input = input.replace("7", "t");
        input = input.replace("0", "o");
        input = input.replace("9", "g");
        input = ChatColor.stripColor(input);
        input2 = ChatColor.stripColor(input2);

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");
        for (int start = 0; start < input.length(); start++) {
            for (int offset = 1; offset < (input.length() + 1 - start) && offset < largestWordLength; offset++) {
                String wordToCheck = input.substring(start, start + offset);
                String wordToCheck2 = input2.substring(start, start + offset);
                if (words.containsKey(wordToCheck)) {
                    badWords.add(wordToCheck);
                }
                if (customwords.containsKey(wordToCheck2) && !badWords.contains(wordToCheck2)) {
                    badWords.add(wordToCheck2);
                } else if (customwords.containsKey(wordToCheck)) {
                    badWords.add(wordToCheck);
                }
            }
        }
        return badWords;
    }

    boolean isUrl(String str) {
        String[] parts2 = str.split(" ");
        boolean bool = false;
        for (String item : parts2) {
            String item2 = ChatColor.stripColor(item);
            try {
                new URL(item2).toURI();
                bool = true;
            } catch (Exception e) {
                bool = false;
            }
        }
        return bool;
    }

    boolean isIP(String str) {
        String[] parts2 = str.split(" ");
        boolean bool = false;
        for (String item : parts2) {
            String item2 = ChatColor.stripColor(item);
            if (InetAddresses.isMappedIPv4Address(item2)) bool = true;
            if (InetAddresses.isUriInetAddress(item2)) bool = true;
            if (InetAddresses.isInetAddress(item2)) bool = true;
        }
        return bool;
    }

    String getUrl(String str) {
        String[] parts2 = str.split(" ");
        for (String item : parts2) {
            String item2 = ChatColor.stripColor(item);
            try {
                new URL(item2).toURI();
                return item2;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public String getIP(String str) {
        String[] parts2 = str.split(" ");
        for (String item : parts2) {
            String item2 = ChatColor.stripColor(item);
            if (InetAddresses.isMappedIPv4Address(item2)) return item2;
            if (InetAddresses.isUriInetAddress(item2)) return item2;
            if (InetAddresses.isInetAddress(item2)) return item2;
        }
        return null;
    }

    void createCustomConfig() {
        File configFile = new File(this.main.getDataFolder(), "WorldChatter.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            this.main.saveResource("WorldChatter.yml", false);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "A WorldChatter Config was created!");
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    void createcustommessagesconfig() {
        File broadcastfile = new File(this.main.getDataFolder(), "SystemMessages.yml");
        if (!broadcastfile.exists()) {
            broadcastfile.getParentFile().mkdirs();
            this.main.saveResource("SystemMessages.yml", false);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Configured the WorldChatter System-Messages!");
        }
        messageconfig = new YamlConfiguration();
        try {
            messageconfig.load(broadcastfile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}