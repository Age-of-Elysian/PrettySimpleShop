package com.ageofelysian.prettysimpleshop.feature;

import com.ageofelysian.prettysimpleshop.PrettySimpleShop;
import com.ageofelysian.prettysimpleshop.event.ShopSelectEvent;
import com.ageofelysian.prettysimpleshop.shop.InputShopInfo;
import com.ageofelysian.prettysimpleshop.shop.OutputShopInfo;
import com.ageofelysian.prettysimpleshop.shop.ShopInfo;
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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 3/13/2018.
 *
 * @author RoboMWM
 */
public class BuyConversation implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, ShopInfo> selected = new ConcurrentHashMap<>();

    public BuyConversation(PrettySimpleShop plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopSelectWithIntent(ShopSelectEvent event) {
        selected.put(event.getPlayer().getUniqueId(), event.getShopInfo());
    }

    @EventHandler(ignoreCancelled = true)
    private void onCommand(PlayerCommandPreprocessEvent event) {
        selected.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        selected.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onChangeWorlds(PlayerChangedWorldEvent event) {
        selected.remove(event.getPlayer().getUniqueId());
    }

    // Must use the deprecated event because
    // of other legacy plugins
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    private void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        ShopInfo shopInfo = selected.remove(player.getUniqueId());

        if (shopInfo == null) {
            return;
        }

        try {
            int amount = Integer.parseInt(event.getMessage());

            if (amount <= 0) {
                return;
            }

            event.setCancelled(true);

            if (shopInfo instanceof OutputShopInfo) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.performCommand("prettysimpleshop:psbuy " + amount));
            } else if (shopInfo instanceof InputShopInfo) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.performCommand("prettysimpleshop:pssell " + amount));
            }

        } catch (Throwable ignored) {
        }
    }
}
