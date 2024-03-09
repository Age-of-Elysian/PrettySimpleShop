package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/13/2018.
 *
 * @author RoboMWM
 */
public class BuyConversation implements Listener {
    private final JavaPlugin plugin;
    private final Set<UUID> buyPrompt = ConcurrentHashMap.newKeySet();

    public BuyConversation(PrettySimpleShop plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopSelectWithIntent(ShopSelectEvent event) {
        buyPrompt.add(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    private void onCommand(PlayerCommandPreprocessEvent event) {
        buyPrompt.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        buyPrompt.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onChangeWorlds(PlayerChangedWorldEvent event) {
        buyPrompt.remove(event.getPlayer().getUniqueId());
    }

    // Must use the deprecated event because
    // of other legacy plugins
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!buyPrompt.remove(player.getUniqueId())) {
            return;
        }

        try {
            int amount = Integer.parseInt(event.getMessage());

            if (amount <= 0) {
                return;
            }

            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.performCommand("prettysimpleshop:psbuy " + amount));
        } catch (Throwable ignored) {
        }
    }
}
