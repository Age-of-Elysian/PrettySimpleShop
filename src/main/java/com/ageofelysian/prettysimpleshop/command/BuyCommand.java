package com.ageofelysian.prettysimpleshop.command;

import com.ageofelysian.prettysimpleshop.ConfigManager;
import com.ageofelysian.prettysimpleshop.PrettySimpleShop;
import com.ageofelysian.prettysimpleshop.shop.OutputShopInfo;
import com.ageofelysian.prettysimpleshop.shop.ShopListener;
import com.ageofelysian.prettysimpleshop.event.ShopTransactionEvent;
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

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class BuyCommand implements CommandExecutor {
    private final PrettySimpleShop plugin;
    private final ShopListener shopListener;
    private final ConfigManager config;

    public BuyCommand(PrettySimpleShop plugin, ShopListener shopListener) {
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

        buyCommand(player, quantity);
        return true;
    }

    public void buyCommand(Player player, int amount) {
        if (!(shopListener.getSelectedShop(player) instanceof OutputShopInfo shopInfo)) {
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

        if (plugin.getEconomy().getBalance(player) < amount * shopInfo.getPrice()) {
            config.sendComponent(player, "noMoney");
            return;
        }

        ItemStack output = ShopUtil.performTransaction(shopInfo, amount);

        if (output == null) {
            config.sendComponent(player, "shopModified");
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, output.getAmount() * shopInfo.getPrice());

        Bukkit.getPluginManager().callEvent(new ShopTransactionEvent(player, shopInfo, output.getAmount()));

        config.sendComponent(
                player,
                "buyCompleted",
                component("item", output.asOne().displayName()),
                unparsed("amount", Integer.toString(output.getAmount())),
                unparsed("price", plugin.getEconomy().format(output.getAmount() * shopInfo.getPrice()))
        );

        while (output.getAmount() > 0) {
            int quantity = Math.min(output.getAmount(), output.getMaxStackSize());

            player.getWorld().dropItem(player.getLocation(), output.asQuantity(quantity), item -> {
                item.setOwner(player.getUniqueId());
                item.setPickupDelay(0);
            });

            output.subtract(quantity);
        }
    }
}
