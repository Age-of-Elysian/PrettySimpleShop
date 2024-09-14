package com.ageofelysian.prettysimpleshop.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
            if (itemStack == null || itemStack.getType().isEmpty()) {
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

    public static Location getMiddleLocation(Container container) {
        if (container == null) {
            return null;
        }

        return container.getInventory().getLocation();
    }

    public static Container getPriorityChest(DoubleChest doubleChest) {
        Container left = (Container) doubleChest.getLeftSide(false);
        Container right = (Container) doubleChest.getRightSide(false);

        // left side takes precedence, but we'll override if the right side is a shop and the left side isn't
        if (!isShop(left.getPersistentDataContainer()) && isShop(right.getPersistentDataContainer())) {
            return right;
        }

        return left;
    }

    public static Container getPriorityContainer(Container container) {
        if (container == null) {
            return null;
        }

        if (container.getInventory().getHolder(false) instanceof DoubleChest doubleChest) {
            return getPriorityChest(doubleChest);
        }

        return container;
    }

    public static PersistentDataContainer getPriorityData(Container container) {
        return getPriorityContainer(container).getPersistentDataContainer();
    }

    private static final NamespacedKey PRICE_KEY = new NamespacedKey("prettysimpleshop", "price");
    private static final NamespacedKey REVENUE_KEY = new NamespacedKey("prettysimpleshop", "revenue");
    private static final NamespacedKey DEPOSIT_KEY = new NamespacedKey("prettysimpleshop", "deposit");

    public static double getPrice(PersistentDataContainer data) {
        return data.getOrDefault(PRICE_KEY, PersistentDataType.DOUBLE, -1.0);
    }

    public static void setPrice(PersistentDataContainer data, double price) {
        data.set(PRICE_KEY, PersistentDataType.DOUBLE, price);
    }

    public static double getRevenue(PersistentDataContainer data) {
        return data.getOrDefault(REVENUE_KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public static void setRevenue(PersistentDataContainer data, double revenue) {
        data.set(REVENUE_KEY, PersistentDataType.DOUBLE, revenue);
    }

    public static double getDeposit(PersistentDataContainer data) {
        return data.getOrDefault(DEPOSIT_KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public static void setDeposit(PersistentDataContainer data, double deposit) {
        data.set(DEPOSIT_KEY, PersistentDataType.DOUBLE, deposit);
    }

    public static boolean isShop(PersistentDataContainer data) {
        return data != null && data.has(PRICE_KEY);
    }

    public static ShopInfo getShopInfo(Container container) {
        Container priority = getPriorityContainer(container);
        PersistentDataContainer data = priority.getPersistentDataContainer();

        if (!isShop(data)) {
            return null;
        }

        // If shop is input
        if (data.has(DEPOSIT_KEY)) {
            return new InputShopInfo(priority.getBlock(), getItemStack(container), getPrice(data), getDeposit(data));
        }

        return new OutputShopInfo(priority.getBlock(), getItemStack(container), getPrice(data), getRevenue(data));
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

    public static ItemStack performTransaction(OutputShopInfo original, int amount) {
        if (!(original.getBlock().getState(false) instanceof Container container)) {
            return null;
        }

        if (!(getShopInfo(container) instanceof OutputShopInfo current)) {
            return null;
        }

        // Check for price changes
        if (original.getPrice() != current.getPrice()) {
            return null;
        }

        // Check for item changes
        if (!original.getItem().isSimilar(current.getItem())) {
            return null;
        }

        ItemStack result = current.getItem();

        // Don't buy more than available
        if (amount < result.getAmount()) {
            result.setAmount(amount);
        }

        PersistentDataContainer data = getPriorityData(container);

        double revenue = current.getRevenue() + result.getAmount() * current.getPrice();
        setRevenue(data, revenue);

        Inventory inventory = container.getInventory();
        inventory.removeItem(result);

        return result;
    }

    public static int performTransaction(InputShopInfo original, int amount) {
        if (!(original.getBlock().getState(false) instanceof Container container)) {
            return -1;
        }

        if (!(getShopInfo(container) instanceof InputShopInfo current)) {
            return -1;
        }

        // Check for price changes
        if (original.getPrice() != current.getPrice()) {
            return -1;
        }

        // Check for item changes
        if (!original.getItem().isSimilar(current.getItem())) {
            return -1;
        }

        PersistentDataContainer data = getPriorityData(container);
        int priced = Math.min(amount, (int) (current.getDeposit() / current.getPrice()));

        ItemStack overflow = container.getInventory().addItem(current.getItem().asQuantity(priced)).get(0);

        int result;

        if (overflow == null) {
            result = priced;
        } else {
            result = priced - overflow.getAmount();
        }

        double deposit = current.getDeposit() - result * current.getPrice();
        setDeposit(data, deposit);

        return result;
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
