package com.ageofelysian.prettysimpleshop.command;

import com.ageofelysian.prettysimpleshop.ConfigManager;
import com.ageofelysian.prettysimpleshop.PrettySimpleShop;
import com.ageofelysian.prettysimpleshop.event.ShopTransactionEvent;
import com.ageofelysian.prettysimpleshop.shop.InputShopInfo;
import com.ageofelysian.prettysimpleshop.shop.ShopListener;
import com.ageofelysian.prettysimpleshop.shop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

public class SellCommand implements CommandExecutor {
    private final PrettySimpleShop plugin;
    private final ShopListener shopListener;
    private final ConfigManager config;

    public SellCommand(PrettySimpleShop plugin, ShopListener shopListener) {
        this.plugin = plugin;
        this.shopListener = shopListener;
        this.config = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player) || args.length < 1) {
            return false;
        }

        // selecting a shop via clicking
        if (args.length == 4) {
            World world = Bukkit.getWorld(args[0]);

            if (world == null || player.getWorld() != world) {
                config.sendComponent(player, "tooFar");
                return false;
            }

            Location location;

            try {
                location = new Location(world, Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3]));
            } catch (Throwable rock) {
                return false;
            }

            // TODO: make distance configurable
            if (!location.isChunkLoaded() || location.distance(player.getLocation()) > 20) {
                config.sendComponent(player, "tooFar");
                return false;
            }

            if (!shopListener.selectShop(player, location.getBlock())) {
                config.sendComponent(player, "noShopThere");
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            config.sendComponent(player, "transactionCanceled");
            return true;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(args[0]);
        } catch (Throwable rock) {
            return false;
        }

        sellCommand(player, quantity);
        return true;
    }

    public void sellCommand(Player player, int amount) {
        if (!(shopListener.getSelectedShop(player) instanceof InputShopInfo shopInfo)) {
            config.sendComponent(player, "noShopSelected");
            return;
        }

        // TODO: make configurable
        if (!shopInfo.getBlock().getLocation().isChunkLoaded() || shopInfo.getBlock().getLocation().distance(player.getLocation()) > 20) {
            config.sendComponent(player, "tooFar");
            return;
        }

        if (shopInfo.getPrice() < 0) {
            config.sendComponent(player, "noPrice");
            return;
        }

        if (shopInfo.getDeposit() < shopInfo.getPrice()) {
            config.sendComponent(player,"outOfDeposit");
            return;
        }

        int containing = 0;

        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType().isAir()) {
                continue;
            }

            if (item.isSimilar(shopInfo.getItem())) {
                containing += item.getAmount();
            }
        }

        amount = Math.min(amount, containing);

        if (amount == 0) {
            config.sendComponent(player, "noStockPlayer");
            return;
        }

        int output = ShopUtil.performTransaction(shopInfo, amount);

        if (output == -1) {
            config.sendComponent(player, "shopModified");
            return;
        }

        if (output == 0) {
            config.sendComponent(player, "shopFull");
            return;
        }

        player.getInventory().removeItemAnySlot(shopInfo.getItem().asQuantity(output));
        plugin.getEconomy().depositPlayer(player, output * shopInfo.getPrice());

        Bukkit.getPluginManager().callEvent(new ShopTransactionEvent(player, shopInfo, output));

        config.sendComponent(
                player,
                "sellCompleted",
                component("item", shopInfo.getItem().asOne().displayName()),
                unparsed("amount", Integer.toString(output)),
                unparsed("price", plugin.getEconomy().format(output * shopInfo.getPrice()))
        );
    }
}
