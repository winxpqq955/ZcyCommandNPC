package me.messageofdeath.commandnpc.Utilities;

import me.messageofdeath.commandnpc.CommandNPC;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;

public enum ZcyHub {
    INSTANCE;
    private final static String MAGIC = "zcyhub:v1";
    private JavaPlugin plugin;
    public void init() {
        plugin = CommandNPC.getInstance();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, MAGIC);
    }

    public void shutdown() {
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, MAGIC);
    }

    public void sendPlayer2Any(final Player player, final String groupName) {
        player.sendPluginMessage(plugin, MAGIC, String.format("%s,%s",player.getName(), groupName).getBytes(StandardCharsets.UTF_8));
    }
}
