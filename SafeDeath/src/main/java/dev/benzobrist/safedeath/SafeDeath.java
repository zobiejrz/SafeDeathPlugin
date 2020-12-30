package dev.benzobrist.safedeath;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;

public final class SafeDeath extends JavaPlugin {

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

    /**
     * Makes a log file with a printout of the inventory of a player
     * @param p     The player whose inventory is being saved
     */
    public void makeInventoryLogFile(Player p) {
        getLogger().info("Logging inventory of " + p.getName());

        String playerName = p.getName();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String fileName = String.format("%s %d.log", playerName, timestamp.getTime());

        File logFile = new File(getDataFolder(), String.format("./Inventory Logs/%s", fileName));

        try {
            PrintStream stream = new PrintStream(logFile);
            ItemStack[] rawInv = p.getInventory().getContents();
            ItemStack[] inv = getNonNullInventory(this, rawInv);

            stream.println("Inventory of " + playerName);
            for (ItemStack i : inv) {
                stream.println(i.toString());
            }

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info("Done logging inventory of " + p.getName());
    }

    @Override
    public void onEnable() {
        // Plugin startup login
        createConfigFile(); // Set up config data
        getLogger().info("Isaac was here");
        // Register Listeners
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void createConfigFile() {
        // Make the config File
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


        // Make empty inventory log folder
        File dir = new File(getDataFolder(), "./Inventory Logs/");
        dir.mkdirs();
    }
}
