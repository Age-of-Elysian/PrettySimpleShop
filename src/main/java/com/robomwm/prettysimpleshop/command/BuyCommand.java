package com.robomwm.prettysimpleshop.command;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import com.robomwm.prettysimpleshop.event.ShopBoughtEvent;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopInfo;
import com.robomwm.prettysimpleshop.shop.ShopListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class BuyCommand implements CommandExecutor, Listener {
    private final ShopListener shopListener;
    private final ConfigManager config;
    private final ShopAPI shopAPI;
    private final Economy economy;

    public BuyCommand(PrettySimpleShop plugin, ShopListener shopListener, Economy economy) {
        this.shopListener = shopListener;
        this.config = plugin.getConfigManager();
        this.shopAPI = plugin.getShopAPI();
        this.economy = economy;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player) || args.length < 1) {
            return false;
        }

        if (args.length == 4) //Selecting a shop via clicking
        {
            World world = player.getServer().getWorld(args[0]);
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

            if (!shopListener.selectShop(player, location.getBlock(), true))
                config.sendComponent(player, "noShopThere");
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

        buyCommand(player, quantity, args.length == 2 && args[1].equalsIgnoreCase("confirm"));
        return true;
    }

    public boolean buyCommand(Player player, int amount, boolean confirm) {
        ShopInfo shopInfo = shopListener.getSelectedShop(player);
        if (shopInfo == null) {
            config.sendComponent(player, "noShopSelected");
            return false;
        }

        if (shopInfo.getPrice() < 0) {
            config.sendComponent(player, "noPrice");
            return false;
        }

        if (economy.getBalance(player) < amount * shopInfo.getPrice()) {
            config.sendComponent(player, "noMoney");
            return false;
        }

        ItemStack itemStack = shopInfo.getItem();
        itemStack.setAmount(amount);

        itemStack = shopAPI.performTransaction(shopAPI.getContainer(shopInfo.getLocation()), itemStack, shopInfo.getPrice());
        if (itemStack == null) {
            config.sendComponent(player, "shopModified");
            return false;
        }

        economy.withdrawPlayer(player, itemStack.getAmount() * shopInfo.getPrice());

        config.sendComponent(
                player,
                "transactionCompleted",
                component("item", itemStack.displayName()),
                unparsed("amount", Integer.toString(itemStack.getAmount())),
                unparsed("price", economy.format(itemStack.getAmount() * shopInfo.getPrice()))
        );

        player.getServer().getPluginManager().callEvent(new ShopBoughtEvent(player, shopInfo, itemStack.getAmount()));

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(itemStack);

        for (ItemStack stack : overflow.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack, item -> item.setOwner(player.getUniqueId()));
        }

        return true;
    }
}
