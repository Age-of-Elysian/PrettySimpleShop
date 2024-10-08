package com.ageofelysian.prettysimpleshop.event;

import com.ageofelysian.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopOpenEvent extends Event {
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

    public ShopOpenEvent(Player player, ShopInfo shopInfo) {
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
