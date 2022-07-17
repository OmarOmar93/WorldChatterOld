package mc.omaromar93.api.Events;

import mc.omaromar93.api.enums.BlockType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public interface WorldChatterListener {
    /**
     * Detects the message if it contains IP or URL
     *
     * @param detectedmessage detected message
     * @param player the author of the message
     * @param type checks if its ip or url
     */
    void onMessageDetected(Object detectedmessage, Player player, BlockType type);

    /**
     * Detects the message if it contains a swear word
     *
     * @param badwords amount of swear words found
     * @param player the author of the message
     */
    void onMessageSwear(ArrayList<String> badwords, Player player);

    /**
     * Fires when the chat is locked or unlocked
     *
     * @param sender the author of the command
     * @param isLocked checks if it's locked or not
     */
    void onChatLockToggle(CommandSender sender, Boolean isLocked);

    /**
     * Fires when the user checked if there is an update or not
     *
     * @param oldversion the current plugin version
     * @param newversion the latest avaliable plugin version
     * @param sender the author of the command
     */
    void onUpdateCheck(String oldversion, String newversion, CommandSender sender);

    /**
     * Fires when the plugin config gets reloaded
     */
    void onConfigReload(CommandSender sender);
}
