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
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.InventoryBlockStartEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShowoffItem implements Listener {
    private PrettySimpleShop plugin;
    private ShopAPI shopAPI;
    private YamlConfiguration cache;
    private File cacheFile;
    private Set<ItemDisplay> spawnedItems = new HashSet<>();
    private ConfigManager config;
    private boolean showItemName;

    public ShowoffItem(PrettySimpleShop plugin, ShopAPI shopAPI, boolean showItemName) {
        this.plugin = plugin;
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.showItemName = showItemName;
        cacheFile = new File(plugin.getDataFolder(), "chunksContainingShops.data");
        cache = YamlConfiguration.loadConfiguration(cacheFile);
        for (World world : plugin.getServer().getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                loadShopItemsInChunk(chunk);
    }

    private void saveCache() {
        try {
            cache.save(cacheFile);
        } catch (Throwable rock) {
            plugin.getLogger().warning("Unable to save cache file: " + rock.getMessage());
        }
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
        if (!cache.contains(getChunkName(chunk)))
            return;
        Collection<BlockState> snapshot = chunk.getTileEntities(block -> config.isShopBlock(block.getType()), false);
        boolean noShops = true;
        for (BlockState state : snapshot) {
            Container container = shopAPI.getContainer(state.getLocation());
            if (container == null || !shopAPI.isShop(container, false))
                continue;
            ItemStack item = shopAPI.getItemStack(container);
            if (item == null)
                continue;
            if (spawnItem(new ShopInfo(shopAPI.getLocation(container), item, plugin.getShopAPI().getPrice(container))))
                noShops = false; //Shops exist in this chunk
        }
        if (noShops)
            removeCachedChunk(chunk);
    }

    //despawn items when a shop chest becomes a doublechest
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onDoubleChest(BlockPlaceEvent event) {
        if (!config.isWhitelistedWorld(event.getBlock().getWorld()))
            return;
        if (!config.isShopBlock(event.getBlock().getType()))
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                InventoryHolder holder = ((Container) event.getBlock().getState()).getInventory().getHolder();

                if (holder instanceof DoubleChest doubleChest) {
                    despawnItem(((Chest) (doubleChest.getLeftSide())).getLocation().add(0.5, 1.25, 0.5));
                    despawnItem(((Chest) (doubleChest.getRightSide())).getLocation().add(0.5, 1.25, 0.5));
                }
            }
        }.runTask(plugin);
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
        despawnItem(event.getShopInfo().getLocation().add(0.5, 1.25, 0.5));
    }

    private boolean spawnItem(ShopInfo shopInfo) {
        Location location = shopInfo.getLocation().add(0.5, 1.25, 0.5);
        ItemStack itemStack = shopInfo.getItem();
        despawnItem(location);
        if (itemStack == null)
            return false;
        itemStack.setAmount(1);

        var item = location.getWorld().spawn(location, ItemDisplay.class, displayItem -> {
            displayItem.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
            displayItem.setItemStack(itemStack);
            displayItem.setBillboard(Display.Billboard.VERTICAL);
            displayItem.setPersistent(false);
        });

        if (showItemName) {
            var text = location.getWorld().spawn(location, TextDisplay.class, displayText -> {
                displayText.setText(PrettySimpleShop.getItemName(itemStack));
                displayText.setBillboard(Display.Billboard.VERTICAL);
                displayText.setTransformation(new Transformation(
                        new Vector3f(0f, 0.45f, 0f),
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

        cacheChunk(location.getChunk());

        return true;
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

    private void cacheChunk(Chunk chunk) {
        if (cache.contains(getChunkName(chunk)))
            return;
        cache.set(getChunkName(chunk), true);
        saveCache();
    }

    private void removeCachedChunk(Chunk chunk) {
        cache.set(getChunkName(chunk), null);
        saveCache();
    }

    private String getChunkName(Chunk chunk) {
        return chunk.getWorld().getName() + chunk.getX() + "," + chunk.getZ();
    }

    public void despawnAll() {
        for (var itemDisplay : spawnedItems) {
            despawnItem(itemDisplay.getLocation());
        }

        spawnedItems.clear();
    }
}
