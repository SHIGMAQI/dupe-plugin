package dev.shigmaqi.dupe;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DupePlugin extends JavaPlugin {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> lastCooldownMessage = new HashMap<>();
    private int cooldownSeconds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cooldownSeconds = getConfig().getInt("cooldown-seconds", 5);

        if (this.getCommand("dupe") != null) {
            this.getCommand("dupe").setExecutor(this);
            getLogger().info("DupePlugin enabled with " + cooldownSeconds + "s cooldown.");
            getLogger().info("Created by: https://github.com/SHIGMAQI");
        } else {
            getLogger().severe("Command /dupe not found in plugin.yml!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Cooldown check
        if (cooldowns.containsKey(uuid)) {
            long lastUsed = cooldowns.get(uuid);
            long timePassed = now - lastUsed;
            long cooldownMillis = cooldownSeconds * 1000L;

            if (timePassed < cooldownMillis) {
                // Only show the cooldown message once per second
                long lastMsg = lastCooldownMessage.getOrDefault(uuid, 0L);
                if (now - lastMsg >= 1000) {
                    long secondsLeft = (cooldownMillis - timePassed) / 1000;
                    player.sendMessage("§c[DupePlugin] You must wait " + secondsLeft + " more second(s)!");
                    lastCooldownMessage.put(uuid, now);
                }
                return true;
            }
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getAmount() == 0) {
            player.sendMessage("§c[DupePlugin] You must be holding an item to dupe.");
            return true;
        }

        ItemStack dupedItem = itemInHand.clone();
        boolean added = player.getInventory().addItem(dupedItem).isEmpty();

        if (!added) {
            player.getWorld().dropItemNaturally(player.getLocation(), dupedItem);
        }

        cooldowns.put(uuid, now);
        lastCooldownMessage.remove(uuid); // clear message timer
        player.sendMessage("§a[DupePlugin] Duplicated " + itemInHand.getAmount() + "x " + itemInHand.getType().name() + "!");
        return true;
    }
}
