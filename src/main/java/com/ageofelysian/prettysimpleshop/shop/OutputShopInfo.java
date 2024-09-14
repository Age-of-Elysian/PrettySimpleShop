package com.ageofelysian.prettysimpleshop.shop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class OutputShopInfo extends ShopInfo {
    private final double revenue;

    public OutputShopInfo(Block block, ItemStack item, double price, double revenue) {
        super(block, item, price);
        this.revenue = revenue;
    }

    public double getRevenue() {
        return revenue;
    }
}
