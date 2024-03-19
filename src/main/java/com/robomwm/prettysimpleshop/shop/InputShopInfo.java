package com.robomwm.prettysimpleshop.shop;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class InputShopInfo extends ShopInfo {
    private final double deposit;

    public InputShopInfo(Location location, ItemStack item, double price, double deposit) {
        super(location, item, price);
        this.deposit = deposit;
    }

    public double getDeposit() {
        return deposit;
    }
}
