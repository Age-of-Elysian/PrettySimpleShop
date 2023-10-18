package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
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
    private final Set<Player> buyPrompt = ConcurrentHashMap.newKeySet(); //thread safe????????

    public BuyConversation(PrettySimpleShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopSelectWithIntent(ShopSelectEvent event) {
        Player player = event.getPlayer();
        buyPrompt.remove(player);
        if (!event.hasIntentToBuy())
            return;
        buyPrompt.add(player);
    }

    @EventHandler(ignoreCancelled = true)
    private void onCommand(PlayerCommandPreprocessEvent event) {
        buyPrompt.remove(event.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        buyPrompt.remove(event.getPlayer());
    }

    @EventHandler
    private void onChangeWorlds(PlayerChangedWorldEvent event) {
        buyPrompt.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncChatEvent event) {
        if (!buyPrompt.remove(event.getPlayer()))
            return;
        try {
            String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
            int amount = Integer.parseInt(message);
            if (amount <= 0)
                return;
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().performCommand("buy " + amount); //TODO: call directly
                }
            }.runTask(plugin);
        } catch (Throwable ignored) {
        }
    }
}
