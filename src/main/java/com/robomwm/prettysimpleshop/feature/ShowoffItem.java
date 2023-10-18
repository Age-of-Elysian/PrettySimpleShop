package com.robomwm.prettysimpleshop.feature;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBoughtEvent;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import com.robomwm.prettysimpleshop.event.ShopOpenCloseEvent;
import com.robomwm.prettysimpleshop.event.ShopSelectEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopInfo;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Display;
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

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShowoffItem implements Listener {
    private final PrettySimpleShop plugin;
    private final Economy economy;
    private final ShopAPI shopAPI;
    private final Set<ItemDisplay> spawnedItems = new HashSet<>();
    private final ConfigManager config;
    private final boolean showItemName;

    public ShowoffItem(PrettySimpleShop plugin, Economy economy, ShopAPI shopAPI, boolean showItemName) {
        this.plugin = plugin;
        this.economy = economy;
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.showItemName = showItemName;
        for (World world : plugin.getServer().getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                loadShopItemsInChunk(chunk);
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        if (!config.isWhitelistedWorld(event.getWorld()))
            return;
        final Chunk chunk = event.getChunk();
        if (event.isNewChunk())
            return;
        loadShopItemsInChunk(chunk);
    }

    private void loadShopItemsInChunk(Chunk chunk) {
        Collection<BlockState> states = chunk.getTileEntities(block -> config.isShopBlock(block.getType()), false);

        for (BlockState state : states) {
            if (!(state instanceof Container container)) {
                continue;
            }

            if (!shopAPI.isShop(container, false)) {
                continue;
            }

            ItemStack item = shopAPI.getItemStack(container);

            if (item == null) {
                continue;
            }

            spawnItem(new ShopInfo(shopAPI.getLocation(container), item, plugin.getShopAPI().getPrice(container)));
        }
    }

    // despawn items when a shop chest becomes a double chest
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDoubleChest(BlockPlaceEvent event) {
        if (!config.isWhitelistedWorld(event.getBlock().getWorld())) {
            return;
        }

        if (!config.isShopBlock(event.getBlock().getType())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            InventoryHolder holder = ((Container) event.getBlock().getState(false)).getInventory().getHolder();

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
    private void onShopBought(ShopBoughtEvent event) {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopSelect(ShopSelectEvent event) {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopOpen(ShopOpenCloseEvent event) {
        spawnItem(event.getShopInfo());
    }

    @EventHandler
    private void onShopBreak(ShopBreakEvent event) {
        despawnItem(event.getShopInfo().getLocation().add(0.5, 1.15, 0.5));
    }

    private void spawnItem(ShopInfo shopInfo) {
        Location location = shopInfo.getLocation().add(0.5, 1.15, 0.5);
        ItemStack itemStack = shopInfo.getItem();
        despawnItem(location);
        if (itemStack == null)
            return;
        itemStack.setAmount(1);

        var item = location.getWorld().spawn(location, ItemDisplay.class, displayItem -> {
            displayItem.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            displayItem.setItemStack(itemStack);
            displayItem.setBillboard(Display.Billboard.VERTICAL);
            displayItem.setPersistent(false);
        });

        if (showItemName) {
            var text = location.getWorld().spawn(location, TextDisplay.class, displayText -> {
                // TODO: make configurable
                displayText.text(PrettySimpleShop.getItemName(itemStack).append(Component.newline()).append(Component.text(shopInfo.getItem().getAmount() + "x @ " + economy.format(shopInfo.getPrice()))));
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
            for (var textDisplay : itemDisplay.getPassengers()) {
                textDisplay.remove();
            }

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
