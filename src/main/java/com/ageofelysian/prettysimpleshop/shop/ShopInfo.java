package com.ageofelysian.prettysimpleshop.shop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 * @implNote Used to store info about a selected shop to compare to when performing a transaction
 */
public abstract class ShopInfo {
    private final Block block;
    private final ItemStack item;
    private final double price;

    public ShopInfo(Block block, ItemStack item, double price) {
        this.block = block;
        this.item = item;
        this.price = price;
    }

    public Block getBlock() {
        return block;
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
