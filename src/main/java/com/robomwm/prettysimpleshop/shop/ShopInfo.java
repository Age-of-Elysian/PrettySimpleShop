package com.robomwm.prettysimpleshop.shop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 * @implNote Used to store info about a selected shop to compare to when performing a transaction
 */
public class ShopInfo {
    private final Location location;
    private final ItemStack item;
    private final double price;
    private final double revenue;

    public ShopInfo(Location location, ItemStack item, double price, double revenue) {
        this.location = location;
        this.item = item;
        this.price = price;
        this.revenue = revenue;
    }

    public ShopInfo(ShopInfo shopInfo, int amount) {
        this.location = shopInfo.location.clone();
        this.item = shopInfo.item.asQuantity(amount);
        this.price = shopInfo.price;
        this.revenue = shopInfo.revenue;
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

    public double getRevenue() {
        return revenue;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (super.equals(other))
            return true;
        ShopInfo otherShopInfo = (ShopInfo) other;
        return otherShopInfo.location.equals(location) && (otherShopInfo.item == item || otherShopInfo.item.isSimilar(item)) && otherShopInfo.price == price;
    }
}
