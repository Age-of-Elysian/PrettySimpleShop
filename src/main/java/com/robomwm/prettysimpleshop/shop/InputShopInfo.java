package com.robomwm.prettysimpleshop.shop;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class InputShopInfo extends ShopInfo {
    private final double deposit;

    public InputShopInfo(Block block, ItemStack item, double price, double deposit) {
        super(block, item, price);
        this.deposit = deposit;
    }

    public double getDeposit() {
        return deposit;
    }
}
