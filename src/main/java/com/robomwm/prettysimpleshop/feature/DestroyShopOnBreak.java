package com.robomwm.prettysimpleshop.feature;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 11/9/2019.
 *
 * @author RoboMWM
 */
public class DestroyShopOnBreak implements Listener {
    public DestroyShopOnBreak(PrettySimpleShop plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onShopBreak(ShopBreakEvent event) {
        Location location = event.getBaseEvent().getBlock().getLocation();
        location.getWorld().dropItemNaturally(location, new ItemStack(event.getBaseEvent().getBlock().getType()));
        event.getBaseEvent().setDropItems(false);
    }
}
