package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import net.kyori.adventure.text.Component;
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

    public ShopInfo(Location location, ItemStack item, double price) {
        this.location = location;
        this.item = item;
        this.price = price;
    }

    public ShopInfo(ShopInfo shopInfo, int amount) {
        this.location = shopInfo.location.clone();
        this.item = shopInfo.item.clone();
        this.item.setAmount(amount);
        this.price = shopInfo.price;
    }

    public Location getLocation() {
        return location.clone();
    }

    public double getPrice() {
        return price;
    }

    public ItemStack getItem() {
        if (item != null)
            return item.clone();
        return null;
    }

    public Component getItemName() {
        return PrettySimpleShop.getItemName(item);
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
