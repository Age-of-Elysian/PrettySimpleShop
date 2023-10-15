package com.robomwm.prettysimpleshop.event;

import com.robomwm.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Created on 2/11/2018.
 * <p>
 * Convenience event to determine when a player broke a shop
 *
 * @author RoboMWM
 */
public class ShopBreakEvent extends Event {
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
    private final BlockBreakEvent baseEvent;

    public ShopBreakEvent(Player player, ShopInfo shopInfo, BlockBreakEvent baseEvent) {
        this.player = player;
        this.shopInfo = shopInfo;
        this.baseEvent = baseEvent;
    }

    public Player getPlayer() {
        return player;
    }

    public ShopInfo getShopInfo() {
        return shopInfo;
    }

    public BlockBreakEvent getBaseEvent() {
        return baseEvent;
    }
}
