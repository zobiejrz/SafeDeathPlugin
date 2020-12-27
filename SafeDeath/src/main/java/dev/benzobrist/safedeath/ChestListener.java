package dev.benzobrist.safedeath;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static dev.benzobrist.safedeath.SafeDeath.getNonNullInventory;

public class ChestListener implements Listener {
    private SafeDeath plugin;

    ChestListener(SafeDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void catchChestOpen(InventoryOpenEvent event) {

        if(event.getInventory().getType().equals(InventoryType.CHEST)) {

            plugin.getLogger().info("Intercepted chest opening from " + event.getPlayer().getName());

            Block target = event.getPlayer().getTargetBlock(null, 16);
            BlockState targetState = target.getState();
            TileState targetTileState = (TileState) targetState;
            PersistentDataContainer container = targetTileState.getPersistentDataContainer();

            if (container.has(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING)) {
                String ownerUUID = container.get(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING);
                boolean isOwner = (ownerUUID == event.getPlayer().getUniqueId().toString());

                if (plugin.getConfig().getBoolean("protectedGraves") && !isOwner) {
                    plugin.getLogger().info(event.getPlayer().getName() + " attempted opening someone else's grave - cancelled");
                    event.setCancelled(true);
                }

                plugin.getLogger().info(event.getPlayer().getName() + " opening grave");
            }

        }

    }

    @EventHandler
    public void leave(InventoryCloseEvent event) {
        if(event.getInventory().getType().equals(InventoryType.CHEST)) {

            plugin.getLogger().info("Intercepted chest closing from " + event.getPlayer().getName());

            Block target = event.getPlayer().getTargetBlock(null, 16);
            BlockState targetState = target.getState();
            TileState targetTileState = (TileState) targetState;
            PersistentDataContainer container = targetTileState.getPersistentDataContainer();

            if (container.has(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING)) {
                String ownerUUID = container.get(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING);
                boolean isOwner = (ownerUUID == event.getPlayer().getUniqueId().toString());

                ItemStack[] rawItems = event.getInventory().getContents();
                ItemStack[] inv = getNonNullInventory(plugin, rawItems);

                if (plugin.getConfig().getBoolean("disappearingGraves") && inv.length < 1) {
                    Chest targetChest = (Chest) target.getState();
                    targetChest.getInventory().clear(); // Clear Chest
                    target.setType(Material.AIR); // Set to air
                }
            }
        }
    }
}
