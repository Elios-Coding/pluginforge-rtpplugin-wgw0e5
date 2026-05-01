package com.pluginforge.rtpplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RTPCommand implements CommandExecutor {
    private final RTPPlugin plugin;
    private final RTPManager rtpManager;
    private final ActivationManager activationManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public RTPCommand(RTPPlugin plugin, RTPManager rtpManager, ActivationManager activationManager) {
        this.plugin = plugin;
        this.rtpManager = rtpManager;
        this.activationManager = activationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use /rtp.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("activate")) {
            if (activationManager.isActivated()) {
                player.sendMessage("Plugin is already activated.");
                return true;
            }
            activationManager.beginActivation(player);
            player.sendMessage("Type your server IP in chat to activate the plugin");
            return true;
        }

        if (!activationManager.isActivated()) {
            player.sendMessage("Plugin not activated. Use /rtp activate");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("rtp.admin")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }
            RTPAdminGUI.openMenu(player);
            return true;
        }

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage("You do not have permission to use /rtp.");
            return true;
        }

        long cooldownSeconds = plugin.getConfig().getLong("cooldown", 60L);
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null) {
            long elapsed = (now - last) / 1000L;
            long remaining = cooldownSeconds - elapsed;
            if (remaining > 0) {
                player.sendMessage("You must wait " + remaining + " seconds before using /rtp again.");
                return true;
            }
        }

        int min = plugin.getConfig().getInt("default-radius.min", 500);
        int max = plugin.getConfig().getInt("default-radius.max", 1500);
        cooldowns.put(player.getUniqueId(), now);
        rtpManager.teleportPlayer(player, min, max);
        return true;
    }
}
