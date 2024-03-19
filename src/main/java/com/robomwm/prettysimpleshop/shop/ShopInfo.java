package com.robomwm.prettysimpleshop.shop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 * @implNote Used to store info about a selected shop to compare to when performing a transaction
 */
public abstract class ShopInfo {
    private final Location location;
    private final ItemStack item;
    private final double price;

    public ShopInfo(Location location, ItemStack item, double price) {
        this.location = location;
        this.item = item;
        this.price = price;
    }

    public Location getLocation() {
        return location.clone();
    }

    public ItemStack getItem() {
        if (item != null) {
            return item.clone();
        }

        return null;
    }

    public double getPrice() {
        return price;
    }
}
