package dev.benzobrist.safedeath;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public final class SafeDeath extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup login
        createFiles(); // Set up config data

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createFiles() {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            saveResource("config.yml", false);
        }

        FileConfiguration config = new YamlConfiguration();

        try {
            config.load(configFile);
        }
        catch(IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
