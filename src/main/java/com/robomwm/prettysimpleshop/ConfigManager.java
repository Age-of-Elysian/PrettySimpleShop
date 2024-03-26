package com.robomwm.prettysimpleshop;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created on 2/16/2017.
 *
 * @author RoboMWM
 */
public class ConfigManager {
    private Configuration config;

    public ConfigManager(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
    }

    public boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public void sendComponent(Audience audience, String key, TagResolver... tagResolvers) {
        audience.sendMessage(getComponent(key, tagResolvers));
    }

    public Component getComponent(String key, TagResolver... tagResolvers) {
        String input = config.getString("messages." + key);

        if (input != null) {
            return MiniMessage.miniMessage().deserialize(input, tagResolvers);
        }

        return Component.text("Missing message: " + key, NamedTextColor.RED);
    }

    public boolean isWhitelistedWorld(World world) {
        List<String> worlds = config.getStringList("worldWhitelist");
        return worlds.isEmpty() || worlds.contains(world.getName());
    }
}
