package com.robomwm.prettysimpleshop.shop;

import com.robomwm.prettysimpleshop.ConfigManager;
import com.robomwm.prettysimpleshop.PrettySimpleShop;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
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
public class ShopAPI {
    private final String shopKey;
    private final String priceKey;
    private final String salesKey;

    public ShopAPI(String shopKey, String priceKey, String salesKey) {
        this.shopKey = shopKey;
        this.priceKey = priceKey;
        this.salesKey = salesKey;
    }

    /**
     * Returns a copy of the item sold in this shop.
     * The total quantity available is stored in ItemStack#getAmount
     * <p>
     * Will return null if !item#isSimilar to another in the container.
     *
     * @param container
     * @return the item sold, or null if either no item exists or multiple types of items are present in the container
     */
    public ItemStack getItemStack(Container container) {
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

    public boolean isShop(Container container) {
        return isShop(container, true);
    }

    public boolean isShop(Container container, boolean includeNew) {
        return isShopFormat(getName(container), includeNew);
    }

    public boolean isShopFormat(String name, boolean includeNew) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String[] split = name.split(" ");

        if (split.length == 1 && split[0].equalsIgnoreCase(shopKey) && includeNew) {
            return true;
        }

        return split.length == 5 && split[4].startsWith("\u00A7\u00A7");
    }

    public void setPrice(Container container, double newPrice) {
        String name = getName(container);
        PrettySimpleShop.debug("setPrice:" + name + ";");

        String[] split = null;

        if (name != null) {
            split = name.split(" ");
        }

        // turn it into a shop no matter what
        if (split == null || !split[0].equals(priceKey)) {
            setName(container, priceKey + " " + newPrice + " " + salesKey + " 0 \u00A7\u00A7");
            return;
        }

        // otherwise, change the price portion of the string
        split[1] = Double.toString(newPrice);

        setName(container, String.join(" ", split));
    }

    public ShopInfo getShopInfo(Container container) {
        return new ShopInfo(getLocation(container), getItemStack(container), getPrice(container));
    }

    public double getPrice(Container container) {
        String name = getName(container);

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");

        if (split.length < 2 || !split[0].equals(priceKey)) {
            return -1;
        }

        return Double.parseDouble(split[1]);
    }

    public double getRevenue(Container container, boolean reset) {
        String name = getName(container);

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");
        PrettySimpleShop.debug("getRevenue:" + String.join(" ", split));

        if (split.length < 5 || split[4].length() < 2 || !split[4].startsWith("\u00A7\u00A7")) {
            return -1;
        }

        if (split[4].length() < 3) {
            return 0;
        }

        double revenue = Double.parseDouble(split[4].substring(2));

        if (reset) {
            split[4] = "\u00A7\u00A7";
            setName(container, String.join(" ", split));
        }

        return revenue;
    }

    /**
     * In case what qualifies as "similar" must be modified in the future...
     *
     * @param item1 cannot be null, else will always return false
     * @param item2
     * @return
     */
    private boolean isSimilar(ItemStack item1, ItemStack item2) {
        return item1 != null && item1.isSimilar(item2);
    }

    /**
     * If the block at the given location is a nameable container, its state will be returned as a Container.
     *
     * @param location
     * @return the block's state as a Container, or null if not a Nameable Container.
     */
    public Container getContainer(Location location) {
        BlockState state = location.getBlock().getState(false);
        if (state instanceof Container container) {
            return container;
        }
        return null;
    }


    /**
     * Returns the location of a shop
     * DoubleChest conveniently returns the middle between the two chests.
     *
     * @param container
     * @return
     */
    public Location getLocation(Container container) {
        return container.getInventory().getLocation();
    }

    private String getName(Container container) {
        if (container == null) {
            return null;
        }

        if (container.getInventory().getHolder(false) instanceof DoubleChest doubleChest) {
            String left = ((Container) doubleChest.getLeftSide(false)).getCustomName();
            String right = ((Container) doubleChest.getRightSide(false)).getCustomName();

            // left side takes precedence, but we'll override if the right side is a shop and the left side isn't
            if (!isShopFormat(left, false) && isShopFormat(right, true)) {
                return right;
            }

            return left;
        }

        return container.getCustomName();
    }

    private void setName(Container container, String name) {
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
     * Removes items from the shop - performs the transaction
     * <p>
     * There may be a demand for a "dry run" transaction to see what the result would be before actually executing it
     * Such a thing would also be useful to verify if items can be delivered, and cancel (instead of attempting to reverse) the transaction if the delivery is expected to fail.
     *
     * @param requestedItem
     * @param price
     * @return amount sold
     */
    public ItemStack performTransaction(Container container, ItemStack requestedItem, double price) {
        //Verify price
        PrettySimpleShop.debug(getPrice(container) + " " + price);
        if (getPrice(container) != price) {
            return null;
        }
        PrettySimpleShop.debug("price validated");
        //Verify item type
        ItemStack shopItem = getItemStack(container);
        if (!isSimilar(requestedItem, shopItem)) {
            return null;
        }
        PrettySimpleShop.debug(shopItem.toString() + requestedItem);
        PrettySimpleShop.debug("item validated");
        //Verify stock - cap to max stock remaining
        //We use and return the shopItem since this is already a cloned ItemStack (instead of also cloning item)
        //(This is why we're modifying `shopItem` to the request amount, unless it is larger.
        if (requestedItem.getAmount() < shopItem.getAmount()) {
            shopItem.setAmount(requestedItem.getAmount());
        }

        //Update statistics/revenue first, otherwise will overwrite inventory changes
        String[] split = getName(container).split(" ");
        split[3] = Long.toString(Long.parseLong(split[3]) + shopItem.getAmount());
        double revenue = getRevenue(container, false);
        PrettySimpleShop.debug("rev" + revenue);
        revenue += shopItem.getAmount() * price;
        split[4] = "\u00A7\u00A7" + revenue;
        setName(container, String.join(" ", split));

        Inventory inventory = container.getInventory();
        inventory.removeItem(shopItem);

        return shopItem;
    }

    public Component getItemName(ItemStack item) {
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
