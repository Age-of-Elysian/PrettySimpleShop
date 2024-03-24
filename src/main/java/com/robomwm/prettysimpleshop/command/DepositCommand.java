package com.robomwm.prettysimpleshop.command;

import com.robomwm.prettysimpleshop.shop.ShopListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DepositCommand implements CommandExecutor {
    private final ShopListener shopListener;

    public DepositCommand(ShopListener shopListener) {
        this.shopListener = shopListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length < 1) {
            shopListener.depositCommand(player, null);
            return false;
        }

        double price;

        try {
            price = Double.parseDouble(args[0]);
        } catch (Throwable rock) {
            shopListener.depositCommand(player, null);
            return false;
        }

        shopListener.depositCommand(player, price);
        return true;
    }
}
