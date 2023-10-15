package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;


/**
 * Created on 8/4/2019.
 *
 * @author RoboMWM
 */
public class ActionBarItemDetails implements Listener {
    private final ShopAPI shopAPI;
    private final ConfigManager config;
    private final Economy economy;

    public ActionBarItemDetails(PrettySimpleShop plugin, ShopAPI shopAPI, Economy economy) {
        config = plugin.getConfigManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.shopAPI = shopAPI;
        this.economy = economy;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendShopDetails(player);
            }
        }, 5L, 5L);
    }

    public boolean sendShopDetails(Player player) {
        if (!config.isWhitelistedWorld(player.getWorld()))
            return false;

        Block block = player.getTargetBlockExact(7);

        if (!shopAPI.isShop(block, false))
            return false;

        Container shopBlock = (Container) block.getState();

        ItemStack item = shopAPI.getItemStack(shopBlock);

        if (item == null)
            return false;

        String textToSend = config.getString("saleInfo", PrettySimpleShop.getItemName(item), economy.format(shopAPI.getPrice(shopBlock)), Integer.toString(item.getAmount()));
        player.sendActionBar(textToSend);

        return true;
    }
}
