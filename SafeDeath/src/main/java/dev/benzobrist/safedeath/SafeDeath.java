package dev.benzobrist.safedeath;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class SafeDeath extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup login
        createFiles(); // Set up config data

        // Register Listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Removes null ItemStacks from an inventory
     * @param inv       the inventory to process
     * @return          an ItemStack[] with no null items
     */
    static ItemStack[] getNonNullInventory(SafeDeath plugin, ItemStack[] inv) {
        plugin.getLogger().info("Making Non Null Inventory");
        int size = 0;
        for (ItemStack i : inv) {
            if (i != null) {
                size += 1;
            }
        }

        int index = 0;
        ItemStack[] output = new ItemStack[size];
        for (ItemStack i : inv) {
            if (i != null) {
                output[index] = i;
                index += 1;
            }
        }

        plugin.getLogger().info("Made non null inventory " + size + " ItemStacks big");
        return output;
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
