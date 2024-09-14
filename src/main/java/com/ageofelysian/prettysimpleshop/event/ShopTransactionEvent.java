package com.ageofelysian.prettysimpleshop.event;

import com.ageofelysian.prettysimpleshop.shop.ShopInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShopTransactionEvent extends Event implements Cancellable {
    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private boolean cancelled = false;
    private final Player player;
    private final ShopInfo shopInfo;
    private final int amount;

    public ShopTransactionEvent(Player player, ShopInfo shopInfo, int amount) {
        this.player = player;
        this.shopInfo = shopInfo;
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public ShopInfo getShopInfo() {
        return shopInfo;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
