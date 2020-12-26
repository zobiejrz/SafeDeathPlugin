package dev.benzobrist.safedeath;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Objects;
import java.util.UUID;


public class PlayerListener implements Listener {

    private SafeDeath plugin;

    PlayerListener(SafeDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean isDeveloper = (event.getPlayer() == Bukkit.getPlayer(UUID.fromString("3c96696c-e367-4547-b00a-a7a7bf7e5a6d")));

        if (isDeveloper) {
            Bukkit.broadcastMessage("Praise the Maker! Welcome, Master " + event.getPlayer().getName() + "!");
        }
        else if (event.getPlayer().hasPlayedBefore()) {
            Bukkit.broadcastMessage("Welcome back " + event.getPlayer().getName());
        }
        else {
            Bukkit.broadcastMessage("Welcome to the server, " + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        boolean isDeveloper = (event.getEntity().getPlayer() == Bukkit.getPlayer(UUID.fromString("3c96696c-e367-4547-b00a-a7a7bf7e5a6d")));

        boolean shouldPayRespects = true; // move to config
        boolean shouldPayRespectsToDev = true;

        boolean shouldDevKeepInv = false; // move to easter egg

        boolean shouldSendPaper = true; // move to config
        boolean shouldSendDevPaper = false; // move to easter egg

        boolean shouldMakeGrave = true; // move to config

        String payRespectMessage = "f"; // move to config
        String payRespectToDevMessage = "Oh! The Horror! The Maker has fallen in battle!"; // move to easter egg


        // Pay Respects
        if (isDeveloper && shouldPayRespectsToDev) {
            Bukkit.broadcastMessage(payRespectToDevMessage);
        }
        else if (shouldPayRespects) {
            Bukkit.broadcastMessage(payRespectMessage);
        }

        // Keep Inventory
        if (isDeveloper && shouldDevKeepInv) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);

            // Stops Duplication
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
        else if (shouldMakeGrave) {
            plugin.getLogger().info("Making Grave for " + event.getEntity().getName());

            Location loc = event.getEntity().getLocation(); // Where player died
            ItemStack[] inv = event.getEntity().getPlayer().getInventory().getContents(); // Get inventory

            Block leftSide = loc.getBlock();
            Block rightSide = loc.clone().add(0, 0, -1).getBlock();

            leftSide.setType(Material.CHEST);
            rightSide.setType(Material.CHEST);

            BlockData leftData = leftSide.getBlockData();
            ((Directional) leftData).setFacing(BlockFace.EAST);
            leftSide.setBlockData(leftData);

            org.bukkit.block.data.type.Chest chestDataLeft = (org.bukkit.block.data.type.Chest) leftData;
            chestDataLeft.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
            leftSide.setBlockData(chestDataLeft);

            Chest leftChest = (Chest) leftSide.getState();

            BlockData rightData = rightSide.getBlockData();
            ((Directional) rightData).setFacing(BlockFace.EAST);
            rightSide.setBlockData(rightData);

            org.bukkit.block.data.type.Chest chestDataRight = (org.bukkit.block.data.type.Chest) rightData;
            chestDataRight.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
            rightSide.setBlockData(chestDataRight);

            World world = loc.getWorld();

            try {
                DoubleChestInventory chestInventory = (DoubleChestInventory) leftChest.getInventory();
                for (ItemStack i : inv) { // Add new drops to block
//                    plugin.getLogger().info("Added an item - " + i);
                    if (i != null) {
                        chestInventory.addItem(i);
                    }
                }
            }
            catch(Exception e) {
                plugin.getLogger().severe("ERROR - " + e.toString());
            }
            plugin.getLogger().info("Done.");

            // Stops Duplication
            event.setKeepInventory(false);
            event.getDrops().clear();
        }

        // Send paper coordinates
        if (shouldSendPaper) {
            plugin.getLogger().info("Sending Paper Coordinates to " + event.getEntity().getName());
            Location deathLocation = event.getEntity().getLocation();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()), deathLocation);
                }
            }, 100);
            plugin.getLogger().info("Done.");
        }
        else if (isDeveloper && shouldSendDevPaper) {
            Location deathLocation = event.getEntity().getLocation();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()), deathLocation);
                }
            }, 100);
        }

    }

    /**
    * @param player    the player to receive their own coordinates on paper
    * @return
    */
    private void sendPlayerCoordinatesOnPaper(org.bukkit.entity.Player player, Location location) {
        // Make the message
        String dimension = location.getWorld().getName();

        Double x = location.getX(); // This gets the component as a Double
        String xStr = String.format("%.2f", x); // This formats to two decimal spots

        Double y = location.getY();
        String yStr = String.format("%.2f", y);

        Double z = location.getZ();
        String zStr = String.format("%.2f", z);

        String message = String.format("%s %s %s %s", dimension, xStr, yStr, zStr);

        // Set the message to be the name of the paper
        ItemStack paper = new ItemStack(Material.PAPER); // Make a new Item
        ItemMeta im = paper.getItemMeta();               // Grab the meta data
        im.setDisplayName(message);                      // Make the change
        paper.setItemMeta(im);                           // Save the new meta data

        // Put in inventory
        player.getInventory().addItem(paper);
    }
}
