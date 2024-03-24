package com.robomwm.prettysimpleshop;

import com.robomwm.prettysimpleshop.command.*;
import com.robomwm.prettysimpleshop.feature.BuyConversation;
import com.robomwm.prettysimpleshop.feature.LegacyConversion;
import com.robomwm.prettysimpleshop.feature.ShowoffItem;
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
    private ConfigManager config;
    private ShowoffItem showoffItem = null;

    public void onEnable() {
        config = new ConfigManager(this);
        debug = config.isDebug();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            economy = getEconomy();
            if (economy == null) {
                getLogger().severe("No economy plugin was found. Disabling.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            ShopListener shopListener = new ShopListener(this, economy, config);
            if (config.getBoolean("showOffItemsFeature.enabled")) {
                showoffItem = new ShowoffItem(this, economy, config.getBoolean("showOffItemsFeature.showItemsName"));
            }
            new LegacyConversion(this);
            getCommand("psshop").setExecutor(new HelpCommand(this));
            getCommand("psbuy").setExecutor(new BuyCommand(this, shopListener));
            getCommand("pssell").setExecutor(new SellCommand(this, shopListener));
            getCommand("setprice").setExecutor(new PriceCommand(shopListener));
            getCommand("setdeposit").setExecutor(new DepositCommand(shopListener));
            new BuyConversation(this);
        });
    }

    public void onDisable() {
        if (showoffItem != null) {
            showoffItem.despawnAll();
        }
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public static void debug(Object message) {
        if (debug) {
            System.out.println("[PrettySimpleShop debug] " + message);
        }
    }

    public Economy getEconomy() {
        if (economy != null) {
            return economy;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return null;
        }

        economy = rsp.getProvider();
        return economy;
    }
}
