package com.robomwm.prettysimpleshop.event;

import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShopBoughtEvent extends Event {
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private final Player player;
    private final ShopInfo shopInfo;

    public ShopBoughtEvent(Player player, ShopInfo shopInfo) {
        this.player = player;
        this.shopInfo = shopInfo;
    }

    public Player getPlayer() {
        return player;
    }

    public ShopInfo getShopInfo() {
        return shopInfo;
    }
}
