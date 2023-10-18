package com.robomwm.prettysimpleshop.command;

import com.robomwm.prettysimpleshop.PrettySimpleShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 2/8/2018.
 *
 * @author RoboMWM
 */
public class HelpCommand implements CommandExecutor {
    private final PrettySimpleShop plugin;

    public HelpCommand(PrettySimpleShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getConfigManager().sendComponent(sender, "shopCommand");
        return true;
    }
}
