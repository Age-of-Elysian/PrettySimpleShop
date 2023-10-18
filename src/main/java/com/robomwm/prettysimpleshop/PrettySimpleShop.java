package com.robomwm.prettysimpleshop;

import com.robomwm.prettysimpleshop.command.BuyCommand;
import com.robomwm.prettysimpleshop.command.HelpCommand;
import com.robomwm.prettysimpleshop.command.PriceCommand;
import com.robomwm.prettysimpleshop.feature.BuyConversation;
import com.robomwm.prettysimpleshop.feature.ShowoffItem;
import com.robomwm.prettysimpleshop.shop.ShopAPI;
import com.robomwm.prettysimpleshop.shop.ShopListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

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
        shopAPI = new ShopAPI(config.getString("shopName"), config.getString("price"), config.getString("sales"));

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            economy = getEconomy();
            if (economy == null) {
                getLogger().severe("No economy plugin was found. Disabling.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            ShopListener shopListener = new ShopListener(this, shopAPI, economy, config);
            if (config.getBoolean("showOffItemsFeature.enabled")) {
                showoffItem = new ShowoffItem(this, economy, shopAPI, config.getBoolean("showOffItemsFeature.showItemsName"));
            }
            getCommand("shop").setExecutor(new HelpCommand(this));
            getCommand("setprice").setExecutor(new PriceCommand(shopListener));
            getCommand("buy").setExecutor(new BuyCommand(this, shopListener, economy));
            new BuyConversation(this);
        });
    }

    public void onDisable() {
        if (showoffItem != null) {
            showoffItem.despawnAll();
        }
    }

    public ShopAPI getShopAPI() {
        return shopAPI;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public static void debug(Object message) {
        if (debug) {
            System.out.println("[PrettySimpleShop debug] " + message);
        }
    }

    private Economy getEconomy() {
        if (economy != null) {
            return economy;
        }

        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return null;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return null;
        }

        economy = rsp.getProvider();
        return economy;
    }
}
