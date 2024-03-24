package com.robomwm.prettysimpleshop.shop;

import org.bukkit.block.Container;

public class LegacyUtil {
    // Price: 123 Sold: 3 §§+0

    public static boolean isLegacyFormat(Container container) {
        String name = container.getCustomName();

        if (name == null || name.isEmpty()) {
            return false;
        }

        String[] split = name.split(" ");

        if (!split[0].equals("Price:")) {
            return false;
        }

        return split.length == 5 && split[4].startsWith("§§");
    }

    public static double getPrice(Container container) {
        String name = container.getCustomName();

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");

        if (split.length != 5 || !split[0].equals("Price:")) {
            return -1;
        }

        return Double.parseDouble(split[1]);
    }

    public static double getSold(Container container) {
        String[] split = container.getCustomName().split(" ");
        return Long.parseLong(split[3]);
    }

    public static double getRevenue(Container container) {
        String name = container.getCustomName();

        if (name == null || name.isEmpty()) {
            return -1;
        }

        String[] split = name.split(" ");

        if (split.length != 5 || !split[4].startsWith("§§")) {
            return -1;
        }

        if (split[4].length() < 3) {
            return 0;
        }

        return Double.parseDouble(split[4].substring(2));
    }
}
