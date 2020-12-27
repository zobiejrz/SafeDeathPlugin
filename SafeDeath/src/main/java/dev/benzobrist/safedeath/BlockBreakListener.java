package dev.benzobrist.safedeath;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static dev.benzobrist.safedeath.SafeDeath.getNonNullInventory;

public class BlockBreakListener implements Listener {

    private SafeDeath plugin;

    BlockBreakListener(SafeDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check to see if the block is a grave

        if (event.getBlock().getType() != Material.CHEST) {
            return;
        }
        plugin.getLogger().info("Intercepting chest break event");

        Block target = event.getBlock();
        BlockState targetState = target.getState();
        TileState targetTileState = (TileState) targetState;
        PersistentDataContainer targetContainer = targetTileState.getPersistentDataContainer();

        String ownerUUID = "";
        if (targetContainer.has(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING)) {
            ownerUUID = targetContainer.get(new NamespacedKey(plugin, "graveOwner"), PersistentDataType.STRING);
            plugin.getLogger().info("Chest break was grave");
        }
        else {
            return; // it's not a grave so no additional handling needed
        }

        boolean isOwner = (event.getPlayer() == Bukkit.getPlayer(UUID.fromString(ownerUUID)));
        event.setCancelled(true); // We know it's a grave so it's definitely cancelled

        // Perform check to cancel event
        if (plugin.getConfig().getBoolean("protectedGraves") && !isOwner) {
            plugin.getLogger().info(event.getPlayer().getName() + " tried breaking someone else's grave - cancelled event");
            return;
        }

        // Perform check to make grave disappear on breaking
        if (plugin.getConfig().getBoolean("disappearingGraves")) {
            plugin.getLogger().info(event.getPlayer().getName() + " is breaking their grave");
            Chest targetChest = (Chest) target.getState();
            Inventory inv = targetChest.getInventory();
            ItemStack[] rawItems = inv.getContents();
            ItemStack[] items = getNonNullInventory(plugin, rawItems);

            Player p = event.getPlayer();
            World w = p.getWorld();

            plugin.getLogger().info("Dropping " + event.getPlayer().getName() + "'s items");

            for (ItemStack i : items) {
                w.dropItem(p.getLocation(), i);
            }

            plugin.getLogger().info("Replacing " + event.getPlayer().getName() + "'s grave with air");
            targetChest.getInventory().clear();
            target.setType(Material.AIR);
        }


    }

}
