package dev.benzobrist.safedeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static dev.benzobrist.safedeath.SafeDeath.getNonNullInventory;

public class ChestListener implements Listener {
    private SafeDeath plugin;

    ChestListener(SafeDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void catchChestOpen(InventoryOpenEvent event) {
        try {
            if (event.getInventory().getType().equals(InventoryType.CHEST)) {

                plugin.getLogger().info("Intercepted chest opening from " + event.getPlayer().getName());

                Block target = event.getPlayer().getTargetBlock(null, 16);
//                plugin.getLogger().info("1");

                BlockState targetState = target.getState();
//                plugin.getLogger().info("2");

                TileState targetTileState = (TileState) targetState;
//                plugin.getLogger().info("3");

                PersistentDataContainer container = targetTileState.getPersistentDataContainer();
//                plugin.getLogger().info("4");


                if (container.has(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING)) {
                    String ownerUUID = container.get(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING);
                    boolean isOwner = (event.getPlayer() == Bukkit.getPlayer(UUID.fromString(ownerUUID)));

                    if (plugin.getConfig().getBoolean("protectedGraves") && !isOwner) {
                        plugin.getLogger().info(event.getPlayer().getName() + " attempted opening someone else's grave - cancelled");
                        event.setCancelled(true);
                    }

                    plugin.getLogger().info(event.getPlayer().getName() + " opening grave");
                }

            }
        }
        catch (ClassCastException e) {
            plugin.getLogger().severe("ERROR - Player target is not a chest - " + e.toString());
        }
        catch(Exception e) {
            plugin.getLogger().severe("ERROR - " + e.toString());

        }

    }

    @EventHandler
    public void leave(InventoryCloseEvent event) {
        try {
            if (event.getInventory().getType().equals(InventoryType.CHEST)) {

                plugin.getLogger().info("Intercepted chest closing from " + event.getPlayer().getName());

                Block target = event.getPlayer().getTargetBlock(null, 16);
//                plugin.getLogger().info("1");

                BlockState targetState = target.getState();
//                plugin.getLogger().info("2");

                TileState targetTileState = (TileState) targetState;
//                plugin.getLogger().info("3");

                PersistentDataContainer container = targetTileState.getPersistentDataContainer();
//                plugin.getLogger().info("4");


                if (container.has(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING)) {

                    ItemStack[] rawItems = event.getInventory().getContents();
                    ItemStack[] inv = getNonNullInventory(plugin, rawItems);

                    if (plugin.getConfig().getBoolean("disappearingGraves") && inv.length < 1) {
                        plugin.getLogger().info("Chest Empty - Removing");
                        Chest targetChest = (Chest) target.getState();
                        targetChest.getInventory().clear(); // Clear Chest
                        target.setType(Material.AIR); // Set to air
                    }
                }
            }
        }
        catch (ClassCastException e) {
            plugin.getLogger().severe("ERROR - Player target is not a chest - " + e.toString());
        }
        catch(Exception e) {
            plugin.getLogger().severe("ERROR - " + e.toString());
        }
    }
}
