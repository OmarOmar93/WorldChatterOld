package mc.omaromar93.classes;

import com.google.common.net.InetAddresses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("ALL")
public class Others extends Thread {

    WorldChatter main;
    public Others(WorldChatter plugin) {
        this.main = plugin;
    }

    private final HashMap<String, String[]> words = new HashMap<>();
    private final HashMap<String, Integer> customwords = new HashMap<>();

    private int largestWordLength = 0;
    private FileConfiguration Config = null;
    private FileConfiguration BroadCastConfig = null;

    private FileConfiguration MessageConfig = null;

    public FileConfiguration getConfig(){
        return this.Config;
    }

    public FileConfiguration getBroadCastConfig(){
        return this.BroadCastConfig;
    }

    public FileConfiguration getMessageConfig(){
        return this.MessageConfig;
    }

    public void run() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Running the Side-Features in it's own thread..");
    }

    void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://docs.google.com/spreadsheets/d/1hIEi2YG3ydav1E06Bzf2mQbGZ12kh2fe4ISgLg_UBuM/export?format=csv").openConnection().getInputStream()));
            String line;
            int counter = 0;
            while ((line = reader.readLine()) != null) {
                counter++;
                String[] content;
                try {
                    content = line.split(",");
                    if (content[0].contains("whatsapp")) {
                        continue;
                    }
                    if (content.length == 0) {
                        continue;
                    }
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if (content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }

                    if (word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Loaded " + counter + " words to filter out");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadCustomConfigs() {
        customwords.clear();
        for (Object o : Config.getList("CustomSwearWords")) {
            customwords.put(o.toString().toLowerCase(), 1);
        }
    }

    ArrayList<String> badWordsFound(String input) {
        if (input == null) {
            return new ArrayList<>();
        }
        String input2 = input;
        input = input.replaceAll("1", "i");
        input = input.replaceAll("!", "i");
        input = input.replaceAll("3", "e");
        input = input.replaceAll("4", "a");
        input = input.replaceAll("@", "a");
        input = input.replaceAll("5", "s");
        input = input.replaceAll("7", "t");
        input = input.replaceAll("0", "o");
        input = input.replaceAll("9", "g");
        input = ChatColor.stripColor(input);
        input2 = ChatColor.stripColor(input2);

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");
        for (int start = 0; start < input.length(); start++) {
            for (int offset = 1; offset < (input.length() + 1 - start) && offset < largestWordLength; offset++) {
                String wordToCheck = input.substring(start, start + offset);
                if (words.containsKey(wordToCheck)) {
                    badWords.add(wordToCheck);
                } else if (customwords.containsKey(wordToCheck)) {
                    badWords.add(wordToCheck);
                } else if (customwords.containsKey(input2) && !badWords.contains(input2)) {
                    badWords.add(input2);
                    break;
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
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Created WorldChatter Config!");
        }
        Config = new YamlConfiguration();
        try {
            Config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    void createcustombroadcastconfig() {
        File broadcastfile = new File(this.main.getDataFolder(), "AutoBroadCast.yml");
        if (!broadcastfile.exists()) {
            broadcastfile.getParentFile().mkdirs();
            this.main.saveResource("AutoBroadCast.yml", false);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Created WorldChatter Auto-BroadCast Config!");
        }
        BroadCastConfig = new YamlConfiguration();
        try {
            BroadCastConfig.load(broadcastfile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    void createcustommessagesconfig() {
        File broadcastfile = new File(this.main.getDataFolder(), "SystemMessages.yml");
        if (!broadcastfile.exists()) {
            broadcastfile.getParentFile().mkdirs();
            this.main.saveResource("SystemMessages.yml", false);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Created WorldChatter System-Messages Config!");
        }
        MessageConfig = new YamlConfiguration();
        try {
            MessageConfig.load(broadcastfile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}