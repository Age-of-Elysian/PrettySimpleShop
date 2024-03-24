package com.robomwm.prettysimpleshop;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created on 2/16/2017.
 *
 * @author RoboMWM
 */
public class ConfigManager {
    private final FileConfiguration config;
    private final boolean debug;
    private ConfigurationSection messageSection;
    private final Set<World> whitelistedWorlds = new HashSet<>();
    private final Set<Material> shopBlocks = new HashSet<>();


    public ConfigManager(JavaPlugin plugin) {
        config = plugin.getConfig();

        config.addDefault("showOffItemsFeature", Map.of("enabled", true, "showItemsName", true));

        config.addDefault("useWorldWhitelist", false);

        List<String> whitelist = List.of("mall");
        config.addDefault("worldWhitelist", whitelist);

        List<String> shopBlockList = new ArrayList<>();
        shopBlockList.add("CHEST");
        shopBlockList.add("TRAPPED_CHEST");
        shopBlockList.add("BARREL");
        for (Material material : Tag.SHULKER_BOXES.getValues())
            shopBlockList.add(material.name());
        config.addDefault("shopBlocks", shopBlockList);

        messageSection = config.getConfigurationSection("messages");
        if (messageSection == null)
            messageSection = config.createSection("messages");
        messageSection.addDefault("noPrice", "<red>This shop is not open for sale yet! <yellow>If you are the owner, use /setprice <price> to open this shop!");
        messageSection.addDefault("noStock", "<red>This shop is out of stock!");
        messageSection.addDefault("noMoney", "<red>Transaction canceled: Insufficient /money. Try again with a smaller quantity?");
        messageSection.addDefault("noShopSelected", "<red>Select a shop via left-clicking its chest.");
        messageSection.addDefault("shopModified", "<red>Transaction canceled: Shop was modified. Please try again.");
        messageSection.addDefault("transactionCanceled", "<red>Transaction canceled.");
        messageSection.addDefault("transactionCompletedWindow", "Bought <amount>x <item> for <price>");
        messageSection.addDefault("transactionCompleted", "<green>Bought <yellow><amount>x</yellow> <item> for <yellow><price>");
        messageSection.addDefault("applyPrice", "<green>Open the shop to apply your shiny new price, or use /setprice again to cancel.");
        messageSection.addDefault("setPriceCanceled", "<red>/setprice canceled");
        messageSection.addDefault("priceApplied", "<green>Price updated to <yellow><price>");
        messageSection.addDefault("collectRevenue", "<green>Collected <yellow><revenue></yellow> in sales from this shop");
        messageSection.addDefault("collectDeposit", "<green>Withdrew the <yellow><deposit></yellow> deposit from this shop");
        messageSection.addDefault("tooFar", "<red>You're too far away from this shop");
        messageSection.addDefault("noShopThere", "<red>This shop has been moved or destroyed");
        messageSection.addDefault("buyPrompt", "<green>How many <item> do you want? <yellow>(<available> available, <price> each)");
        messageSection.addDefault("shopCommand", """
                Selling:
                To create a shop, put items of the same type in a chest, and use /setprice to set the price per item.
                Buying:
                Punch a shop to view the item. Hover over it in chat for item details.
                Click the message, /psbuy, or double-punch a shop to buy from a shop
                """);
        messageSection.addDefault("hologramFormat", "<item><newline><amount>x @ <price>");

        config.options().copyDefaults(true);
        plugin.saveConfig();
        debug = config.getBoolean("debug", false);

        if (config.getBoolean("useWorldWhitelist")) {
            for (String worldName : config.getStringList("worldWhitelist")) {
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    plugin.getLogger().warning("World " + worldName + " does not exist.");
                } else {
                    whitelistedWorlds.add(world);
                }
            }
        }

        for (String blockName : config.getStringList("shopBlocks")) {
            Material material = Material.matchMaterial(blockName);
            if (material == null)
                plugin.getLogger().warning(blockName + " is not a valid Material name.");
            else
                shopBlocks.add(material);
        }

        config.options().header("""
                showOffItems spawns a display item above each shop.
                showBuyPrompt prompts the buyer to input the quantity they wish to buy in chat, instead of requiring them to use the /psbuy command.
                shopBlocks contains blocks you allow to be used as shops. Only blocks that are a Nameable Container can be used as a shop.
                Valid Material names can be found here https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
                Block types that are Containers are listed as subinterfaces here (some are not Nameable, you can check by clicking the subinterface of interest) https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Container.html
                """);

        plugin.saveConfig();
    }

    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public boolean isDebug() {
        return debug;
    }

    public void sendComponent(Audience audience, String key, TagResolver... tagResolvers) {
        audience.sendMessage(getComponent(key, tagResolvers));
    }

    public Component getComponent(String key, TagResolver... tagResolvers) {
        String input = messageSection.getString(key);

        if (input != null) {
            return MiniMessage.miniMessage().deserialize(input, tagResolvers);
        }

        return Component.text("Missing message: " + key, NamedTextColor.RED);
    }

    public boolean isWhitelistedWorld(World world) {
        return whitelistedWorlds.isEmpty() || whitelistedWorlds.contains(world);
    }

    public boolean isShopBlock(Material material) {
        return shopBlocks.contains(material);
    }
}
