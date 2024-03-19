package com.robomwm.prettysimpleshop.shop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class OutputShopInfo extends ShopInfo {
    private final double revenue;

    public OutputShopInfo(Location location, ItemStack item, double price, double revenue) {
        super(location, item, price);
        this.revenue = revenue;
    }

    public double getRevenue() {
        return revenue;
    }
}
