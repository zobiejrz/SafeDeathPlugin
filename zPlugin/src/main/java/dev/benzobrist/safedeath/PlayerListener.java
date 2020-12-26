package dev.benzobrist.safedeath;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
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

        boolean shouldDevKeepInv = true; // move to easter egg

        boolean shouldSendPaper = true; // move to config
        boolean shouldSendDevPaper = false; // move to easter egg

        boolean shouldMakeGrave = true; // move to config
        boolean shouldGraveDrop = true; // move to config
        boolean shouldLockGraves = true; // move to config

        String payRespectMessage = "f"; // move to config
        String payRespectToDevMessage = "Oh! The Horror! The Maker has fallen in battle!"; // move to easter egg


        // Pay Respects
        if (isDeveloper && shouldPayRespectsToDev) {
            Bukkit.broadcastMessage(payRespectToDevMessage);
        }
        else if (shouldPayRespects) {
            Bukkit.broadcastMessage(payRespectMessage);
        }

        // Send paper coordinates
        if (shouldSendPaper) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()));
                }
            }, 100);
        }
        else if (isDeveloper && shouldSendDevPaper) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()));
                }
            }, 100);
        }

        // Keep Inventory
        if (isDeveloper && shouldDevKeepInv) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);

            // Stops Duplication
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
        

    }

    /**
    * @param player    the player to receive their own coordinates on paper
    * @return
    */
    private void sendPlayerCoordinatesOnPaper(org.bukkit.entity.Player player) {
        // Make the message
        String dimension = player.getLocation().getWorld().getName();

        Double x = player.getLocation().getX(); // This gets the component as a Double
        String xStr = String.format("%.2f", x); // This formats to two decimal spots

        Double y = player.getLocation().getY();
        String yStr = String.format("%.2f", y);

        Double z = player.getLocation().getZ();
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
