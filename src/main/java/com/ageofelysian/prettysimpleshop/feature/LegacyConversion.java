package com.ageofelysian.prettysimpleshop.feature;

import com.ageofelysian.prettysimpleshop.ConfigManager;
import com.ageofelysian.prettysimpleshop.PrettySimpleShop;
import com.ageofelysian.prettysimpleshop.shop.LegacyUtil;
import com.ageofelysian.prettysimpleshop.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;

public class LegacyConversion implements Listener {
    private final ConfigManager config;

    public LegacyConversion(PrettySimpleShop plugin) {
        this.config = plugin.getConfigManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                migrateLegacyShops(chunk);
            }
        }
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        if (event.isNewChunk() || !config.isWhitelistedWorld(event.getWorld())) {
            return;
        }

        migrateLegacyShops(event.getChunk());
    }

    private void migrateLegacyShops(Chunk chunk) {
        BlockState[] states = chunk.getTileEntities(false);

        for (BlockState state : states) {
            if (!(state instanceof Container container)) {
                continue;
            }

            if (!LegacyUtil.isLegacyFormat(container)) {
                continue;
            }

            double price = LegacyUtil.getPrice(container);
            double revenue = LegacyUtil.getRevenue(container);

            PersistentDataContainer data = container.getPersistentDataContainer();

            ShopUtil.setPrice(data, price);
            ShopUtil.setRevenue(data, revenue);

            container.setCustomName(null);
        }
    }
}
