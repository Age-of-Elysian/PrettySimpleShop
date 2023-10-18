package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/13/2018.
 * <p>
 * I was gonna do something like an assistant or cashier or something
 * but that's all too ambiguous so here's a generic name
 *
 * @author RoboMWM
 */
public class BuyConversation implements Listener {
    private final JavaPlugin plugin;
    private final Set<UUID> buyPrompt = ConcurrentHashMap.newKeySet();

    public BuyConversation(PrettySimpleShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncChatEvent event) {
        if (!buyPrompt.remove(event.getPlayer().getUniqueId())) {
            return;
        }

        try {
            String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
            int amount = Integer.parseInt(message);

            if (amount <= 0) {
                return;
            }

            event.setCancelled(true);

            // TODO: run directly
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> event.getPlayer().performCommand("buy " + amount));
        } catch (Throwable ignored) {
        }
    }
}
