package com.robomwm.prettysimpleshop.feature;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import com.robomwm.prettysimpleshop.event.ShopCloseEvent;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import com.robomwm.prettysimpleshop.event.ShopTransactionEvent;
import com.robomwm.prettysimpleshop.shop.ShopInfo;
import com.robomwm.prettysimpleshop.shop.ShopUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShowoffItem implements Listener {
    private final PrettySimpleShop plugin;
    private final Economy economy;
    private final Set<ItemDisplay> spawnedItems = new HashSet<>();
    private final ConfigManager config;
    private final boolean showItemName;

    public ShowoffItem(PrettySimpleShop plugin, Economy economy, boolean showItemName) {
        this.plugin = plugin;
        this.economy = economy;
        config = plugin.getConfigManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.showItemName = showItemName;

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                loadShopItemsInChunk(chunk);
            }
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk() || !config.isWhitelistedWorld(event.getWorld())) {
            return;
        }

        loadShopItemsInChunk(event.getChunk());
    }

    private void loadShopItemsInChunk(Chunk chunk) {
        Collection<BlockState> states = chunk.getTileEntities(block -> config.isShopBlock(block.getType()), false);

        for (BlockState state : states) {
            if (!(state instanceof Container container)) {
                continue;
            }

            ShopInfo shopInfo = ShopUtil.getShopInfo(container);

            if (shopInfo == null || shopInfo.getItem() == null) {
                continue;
            }

            spawnItem(shopInfo);
        }
    }

    // despawn items when a shop chest becomes a double chest
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDoubleChest(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST) {
            return;
        }

        if (!config.isWhitelistedWorld(block.getWorld())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!(block.getState(false) instanceof Container container)) {
                return;
            }

            InventoryHolder holder = container.getInventory().getHolder(false);

            if (holder instanceof DoubleChest doubleChest) {
                despawnItem(((Chest) (doubleChest.getLeftSide(false))).getLocation().add(0.5, 1.15, 0.5));
                despawnItem(((Chest) (doubleChest.getRightSide(false))).getLocation().add(0.5, 1.15, 0.5));
            }
        });
    }

    @EventHandler
    public void onDespawn(EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof ItemDisplay itemDisplay) {
            spawnedItems.remove(itemDisplay);
        }
    }

    @EventHandler
    private void onShopBought(ShopTransactionEvent event) {
        ShopInfo shopInfo = ShopUtil.getShopInfo(ShopUtil.getContainer(event.getShopInfo().getLocation()));
        spawnItem(shopInfo);
    }

    @EventHandler
    private void onShopSelect(ShopSelectEvent event) {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopClose(ShopCloseEvent event) {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopBreak(ShopBreakEvent event) {
        despawnItem(event.getShopInfo().getLocation().add(0.5, 1.15, 0.5));
    }

    private void spawnItem(ShopInfo shopInfo) {
        Location location = shopInfo.getLocation().add(0.5, 1.15, 0.5);
        despawnItem(location);

        ItemStack itemStack = shopInfo.getItem();

        if (itemStack == null) {
            return;
        }

        var item = location.getWorld().spawn(location, ItemDisplay.class, displayItem -> {
            displayItem.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            displayItem.setItemStack(itemStack.asOne());
            displayItem.setBillboard(Display.Billboard.VERTICAL);
            displayItem.setPersistent(false);
        });

        if (showItemName) {
            var text = location.getWorld().spawn(location, TextDisplay.class, displayText -> {
                displayText.text(
                        config.getComponent(
                                "hologramFormat",
                                component("item", ShopUtil.getItemName(itemStack)),
                                unparsed("amount", Integer.toString(itemStack.getAmount())),
                                unparsed("price", economy.format(shopInfo.getPrice()))
                        )
                );

                displayText.setBillboard(Display.Billboard.VERTICAL);
                displayText.setTransformation(new Transformation(
                        new Vector3f(0f, 0.4f, 0f),
                        new AxisAngle4f(0f, 0f, 0f, 1f),
                        new Vector3f(0.5f, 0.5f, 0.5f),
                        new AxisAngle4f(0f, 0f, 0f, 1f)
                ));
                displayText.setLineWidth(75);
                displayText.setPersistent(false);
            });

            item.addPassenger(text);
        }

        spawnedItems.add(item);
    }

    private void despawnItem(Location location) {
        PrettySimpleShop.debug("Checking for item at " + location);

        for (var itemDisplay : location.getNearbyEntitiesByType(ItemDisplay.class, 0.001f)) {
            itemDisplay.getPassengers().forEach(Entity::remove);
            itemDisplay.remove();

            PrettySimpleShop.debug("removed item at " + location);
        }
    }

    public void despawnAll() {
        for (var itemDisplay : spawnedItems) {
            despawnItem(itemDisplay.getLocation());
        }

        spawnedItems.clear();
    }
}
