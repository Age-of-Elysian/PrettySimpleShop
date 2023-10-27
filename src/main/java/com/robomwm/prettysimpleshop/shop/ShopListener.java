package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.event.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class ShopListener implements Listener {
    private final Economy economy;
    private final Map<UUID, ShopInfo> selectedShop = new HashMap<>();
    private final Map<UUID, Double> priceSetter = new HashMap<>();
    private final ConfigManager config;


    public ShopListener(JavaPlugin plugin, Economy economy, ConfigManager config) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.config = config;
        this.economy = economy;
    }

    // Cleanup
    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        selectedShop.remove(event.getPlayer().getUniqueId());
        priceSetter.remove(event.getPlayer().getUniqueId());
    }

    // Cleanup
    @EventHandler
    private void onWorldChange(PlayerChangedWorldEvent event) {
        selectedShop.remove(event.getPlayer().getUniqueId());
        priceSetter.remove(event.getPlayer().getUniqueId());
    }

    //We don't watch BlockDamageEvent as player may be in adventure (but uh this event probably doesn't fire in adventure either so... uhm yea... hmmm.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onLeftClickChest(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        if (!config.isWhitelistedWorld(event.getPlayer().getWorld())) {
            return;
        }

        selectShop(player, event.getClickedBlock());
    }

    //Select shop if interact with shop block is denied
    @EventHandler(priority = EventPriority.MONITOR)
    private void onRightClickChest(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.useInteractedBlock() != Event.Result.DENY) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!config.isWhitelistedWorld(event.getPlayer().getWorld())) {
            return;
        }

        selectShop(player, event.getClickedBlock());
    }

    // clear any set price the player may have inadvertently forgotten to remove
    @EventHandler(priority = EventPriority.HIGHEST)
    private void clearSetPrice(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) {
            priceCommand(event.getPlayer(), null);
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        if (event.getClickedBlock() != null && config.isShopBlock(event.getClickedBlock().getType())) {
            return;
        }

        priceCommand(event.getPlayer(), null);
    }

    public boolean selectShop(Player player, Block block) {
        if (!config.isShopBlock(block.getType())) {
            return false;
        }

        Container container = (Container) block.getState(false);

        ShopInfo shopInfo = ShopUtil.getShopInfo(container);

        if (shopInfo == null) {
            return false;
        }

        if (shopInfo.getPrice() < 0) {
            config.sendComponent(player, "noPrice");
            return true;
        }

        if (shopInfo.getItem() == null) {
            config.sendComponent(player, "noStock");
            return true;
        }

        ShopSelectEvent shopSelectEvent = new ShopSelectEvent(player, shopInfo);

        selectedShop.put(player.getUniqueId(), shopInfo);

        config.sendComponent(
                player,
                "buyPrompt",
                component("item", shopInfo.getItem().displayName()),
                unparsed("price", economy.format(shopInfo.getPrice())),
                unparsed("available", Integer.toString(shopInfo.getItem().getAmount()))
        );

        Bukkit.getPluginManager().callEvent(shopSelectEvent);
        return true;
    }

    public ShopInfo getSelectedShop(Player player) {
        return selectedShop.get(player.getUniqueId());
    }

    //Collect revenues
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onOpenInventory(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder(false) instanceof Container container)) {
            return;
        }

        if (priceSetter.containsKey(player.getUniqueId())) {
            double newPrice = priceSetter.remove(player.getUniqueId());
            ShopUtil.setPrice(container, newPrice);
            config.sendComponent(player, "priceApplied", unparsed("price", economy.format(newPrice)));
            Bukkit.getPluginManager().callEvent(new ShopPricedEvent(player, container.getLocation(), newPrice));
        }

        ShopInfo shopInfo = ShopUtil.getShopInfo(container);

        if (shopInfo == null) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ShopOpenEvent(player, shopInfo));

        double revenue = ShopUtil.getRevenue(container, true);

        if (revenue <= 0) {
            return;
        }

        economy.depositPlayer(player, revenue);
        config.sendComponent(player, "collectRevenue", unparsed("revenue", economy.format(revenue)));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBreakShop(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!config.isShopBlock(block.getType())) {
            return;
        }

        Container container = (Container) block.getState(false);

        ShopInfo shopInfo = ShopUtil.getShopInfo(container);

        if (shopInfo == null) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ShopBreakEvent(event.getPlayer(), shopInfo, event));

        double revenue = ShopUtil.getRevenue(container, true);

        if (revenue > 0) {
            Player player = event.getPlayer();
            economy.depositPlayer(player, revenue);
            config.sendComponent(player, "collectRevenue", unparsed("revenue", economy.format(revenue)));
        }

        // reset name so the drop is unnamed
        container.customName(null);
    }

    //Purely for calling the dumb event
    @EventHandler(ignoreCancelled = true)
    private void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof Container container)) {
            return;
        }

        ShopInfo shopInfo = ShopUtil.getShopInfo(container);

        if (shopInfo == null) {
            return;
        }

        Bukkit.getPluginManager().callEvent(new ShopCloseEvent(player, shopInfo));
    }

    //For now we'll just prevent explosions. Might consider dropping stored revenue on explosion later.
    @EventHandler(ignoreCancelled = true)
    private void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> config.isShopBlock(block.getType()) && ShopUtil.isShop((Container) block.getState(false)));
    }

    //Commands cuz well all the data's here so yea

    public void priceCommand(Player player, Double price) {
        if (price == null || price <= 0) {
            if (priceSetter.remove(player.getUniqueId()) != null) {
                config.sendComponent(player, "setPriceCanceled");
            }

            return;
        }

        selectedShop.remove(player.getUniqueId());
        priceSetter.put(player.getUniqueId(), price);
        config.sendComponent(player, "applyPrice");
    }
}
