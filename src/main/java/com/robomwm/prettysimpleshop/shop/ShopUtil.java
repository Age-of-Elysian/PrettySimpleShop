package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created on 2/6/2018.
 * Provides convenience methods to access and operate upon a shop, given a location
 * <p>
 * We do not store Shop objects since we store everything inside the Chests' inventoryName
 * <p>
 * You have no idea how much time I wasted trying to figure out stupid DoubleChests just to find out they store a stupid copy of a Chest that you can't update so yea thanks Bukkit/spigot
 *
 * @author RoboMWM
 */
public class ShopUtil {
    private static final String PRICE_KEY = "Price:";
    private static final String SALES_KEY = "Sales:";

    /**
     * Returns a copy of the item sold in this shop.
     * The total quantity available is stored in ItemStack#getAmount
     * <p>
     * Will return null if !item#isSimilar to another in the container.
     *
     * @param container
     * @return the item sold, or null if either no item exists or multiple types of items are present in the container
     */
    public static ItemStack getItemStack(Container container) {
        Inventory inventory = container.getInventory();
        ItemStack item = null;
        for (ItemStack itemStack : inventory) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            if (item == null) {
                item = itemStack.clone();
            } else if (!item.isSimilar(itemStack)) {
                return null;
            } else {
                item.setAmount(item.getAmount() + itemStack.getAmount());
            }
        }
        return item;
    }

    public static boolean isShop(Container container) {
        return isShopFormat(getName(container));
    }

    public static boolean isShopFormat(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String[] split = name.split(" ");
        return split.length == 5 && split[4].startsWith("§§");
    }

    public static void setPrice(Container container, double newPrice) {
        String name = getName(container);
        PrettySimpleShop.debug("setPrice:" + name + ";");

        String[] split = null;

        if (name != null) {
            split = name.split(" ");
        }

        // turn it into a shop no matter what
        if (split == null || !split[0].equals(PRICE_KEY)) {
            setName(container, PRICE_KEY + " " + newPrice + " " + SALES_KEY + " 0 §§");
            return;
        }

        // otherwise, change the price portion of the string
        split[1] = Double.toString(newPrice);

        setName(container, String.join(" ", split));
    }

    public static ShopInfo getShopInfo(Container container) {
        if (!isShop(container)) {
            return null;
        }

        return new ShopInfo(getLocation(container), getItemStack(container), getPrice(container), getRevenue(container, false));
    }

    public static double getPrice(Container container) {
        String name = getName(container);

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");

        if (split.length < 2 || !split[0].equals(PRICE_KEY)) {
            return -1;
        }

        return Double.parseDouble(split[1]);
    }

    public static double getRevenue(Container container, boolean reset) {
        String name = getName(container);

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");
        PrettySimpleShop.debug("getRevenue:" + String.join(" ", split));

        if (split.length < 5 || split[4].length() < 2 || !split[4].startsWith("§§")) {
            return -1;
        }

        if (split[4].length() < 3) {
            return 0;
        }

        double revenue = Double.parseDouble(split[4].substring(2));

        if (reset) {
            split[4] = "§§";
            setName(container, String.join(" ", split));
        }

        return revenue;
    }

    /**
     * Get the container at a given location.
     * In the case of double chests, the
     * left chest has a priority.
     *
     * @param location of container
     * @return the block's state as a Container, or null if not a Nameable Container.
     */
    public static Container getContainer(Location location) {
        BlockState state = location.getBlock().getState(false);
        if (state instanceof Container container) {
            return container;
        }
        return null;
    }


    /**
     * Get the location of a container.
     * Double chests return the exact middle
     * between the two chests.
     *
     * @param container whose location is returned
     * @return location of the container
     */
    public static Location getLocation(Container container) {
        return container.getInventory().getLocation();
    }

    private static String getName(Container container) {
        if (container == null) {
            return null;
        }

        if (container.getInventory().getHolder(false) instanceof DoubleChest doubleChest) {
            String left = ((Container) doubleChest.getLeftSide(false)).getCustomName();
            String right = ((Container) doubleChest.getRightSide(false)).getCustomName();

            // left side takes precedence, but we'll override if the right side is a shop and the left side isn't
            if (!isShopFormat(left) && isShopFormat(right)) {
                return right;
            }

            return left;
        }

        return container.getCustomName();
    }

    private static void setName(Container container, String name) {
        if (container == null) {
            return;
        }

        if (container.getInventory().getHolder(false) instanceof DoubleChest doubleChest) {
            ((Chest) doubleChest.getLeftSide(false)).setCustomName(name);
            ((Chest) doubleChest.getRightSide(false)).setCustomName(name);
            return;
        }

        container.setCustomName(name);
    }

    /**
     * Remove items from the shop & performs the transaction.
     *
     * @param input - requested item
     * @param price - price at time of selection
     * @return item sold
     */
    public static ItemStack performTransaction(Container container, ItemStack input, double price) {
        ShopInfo shopInfo = getShopInfo(container);

        if (shopInfo == null) {
            return null;
        }

        //Verify price
        PrettySimpleShop.debug(shopInfo.getPrice() + " " + price);
        if (shopInfo.getPrice() != price) {
            return null;
        }
        PrettySimpleShop.debug("price validated");
        //Verify item type
        ItemStack output = shopInfo.getItem();
        if (input == null || !input.isSimilar(output)) {
            return null;
        }
        PrettySimpleShop.debug(output.toString() + input);
        PrettySimpleShop.debug("item validated");
        //Verify stock - cap to max stock remaining
        //We use and return the shopItem since this is already a cloned ItemStack (instead of also cloning item)
        //(This is why we're modifying `shopItem` to the request amount, unless it is larger.
        if (input.getAmount() < output.getAmount()) {
            output.setAmount(input.getAmount());
        }

        //Update statistics/revenue first, otherwise will overwrite inventory changes
        String[] split = getName(container).split(" ");
        split[3] = Long.toString(Long.parseLong(split[3]) + output.getAmount());
        PrettySimpleShop.debug("rev" + shopInfo.getRevenue());
        double revenue = shopInfo.getRevenue() + output.getAmount() * price;
        // trailing + to avoid color
        split[4] = "§§+" + revenue;
        setName(container, String.join(" ", split));

        Inventory inventory = container.getInventory();
        inventory.removeItem(output);

        return output;
    }

    public static Component getItemName(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                return meta.displayName();
            }

            if (meta instanceof BookMeta bookMeta) {
                return bookMeta.title();
            }
        }

        return Component.translatable(item);
    }
}
