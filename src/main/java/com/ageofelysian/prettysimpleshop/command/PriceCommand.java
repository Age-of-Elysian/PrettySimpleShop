package com.ageofelysian.prettysimpleshop.command;

import com.ageofelysian.prettysimpleshop.shop.ShopListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class PriceCommand implements CommandExecutor {
    private final ShopListener shopListener;

    public PriceCommand(ShopListener shopListener) {
        this.shopListener = shopListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            shopListener.priceCommand(player, null);
            return false;
        }

        double price;

        try {
            price = Double.parseDouble(args[0]);
        } catch (Throwable rock) {
            shopListener.priceCommand(player, null);
            return false;
        }

        shopListener.priceCommand(player, price);
        return true;
    }
}
