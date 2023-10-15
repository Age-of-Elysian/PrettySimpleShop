package com.robomwm.prettysimpleshop.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created on 2/9/2018.
 *
 * @author RoboMWM
 */
public class ShopPricedEvent extends Event {
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
    private final Location location;
    private final double newPrice;

    public ShopPricedEvent(Player player, Location location, double newPrice) {
        this.player = player;
        this.location = location;
        this.newPrice = newPrice;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public double getNewPrice() {
        return newPrice;
    }
}
