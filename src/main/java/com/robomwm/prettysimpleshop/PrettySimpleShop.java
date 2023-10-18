package com.robomwm.prettysimpleshop;

import com.robomwm.prettysimpleshop.command.BuyCommand;
import com.robomwm.prettysimpleshop.command.HelpCommand;
import com.robomwm.prettysimpleshop.command.PriceCommand;
import com.robomwm.prettysimpleshop.feature.BuyConversation;
import com.robomwm.prettysimpleshop.feature.DestroyShopOnBreak;
import com.robomwm.prettysimpleshop.feature.ShowoffItem;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopListener;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 2/4/2018.
 *
 * @author RoboMWM
 */
public class PrettySimpleShop extends JavaPlugin {
    private Economy economy;
    private static boolean debug;
    private ShopAPI shopAPI;
    private ConfigManager config;
    private ShowoffItem showoffItem = null;

    public void onEnable() {
        config = new ConfigManager(this);
        debug = config.isDebug();
        shopAPI = new ShopAPI(config.getString("shopName"), config.getString("price"), config.getString("sales"), config);

        PrettySimpleShop plugin = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                economy = getEconomy();
                if (economy == null) {
                    getLogger().severe("No economy plugin was found. Disabling.");
                    getServer().getPluginManager().disablePlugin(plugin);
                    return;
                }
                ShopListener shopListener = new ShopListener(plugin, shopAPI, economy, config);
                if (config.getBoolean("showOffItemsFeature.enabled"))
                    showoffItem = new ShowoffItem(plugin, economy, shopAPI, config.getBoolean("showOffItemsFeature.showItemsName"));
                if (config.getBoolean("deleteShopWhenBroken"))
                    new DestroyShopOnBreak(plugin);
                getCommand("shop").setExecutor(new HelpCommand(plugin));
                getCommand("setprice").setExecutor(new PriceCommand(shopListener));
                getCommand("buy").setExecutor(new BuyCommand(plugin, shopListener, economy));
                if (config.getBoolean("useBuyPrompt"))
                    new BuyConversation(plugin);
            }
        }.runTask(this);
    }

    public void onDisable() {
        if (showoffItem != null)
            showoffItem.despawnAll();
    }

    public static Component getItemName(ItemStack item) {
        if (item.hasItemMeta()) {
            var meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                return meta.displayName();
            }
        }

        return Component.translatable(item.translationKey());
    }

    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public static void debug(Object message) {
        if (debug)
            System.out.println("[PrettySimpleShop debug] " + message);
    }

    private Economy getEconomy() {
        if (economy != null)
            return economy;
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        economy = rsp.getProvider();
        return economy;
    }


}
