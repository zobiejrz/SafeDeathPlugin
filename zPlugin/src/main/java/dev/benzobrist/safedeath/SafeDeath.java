package dev.benzobrist.safedeath;

import org.bukkit.plugin.java.JavaPlugin;

public final class SafeDeath extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup login

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
