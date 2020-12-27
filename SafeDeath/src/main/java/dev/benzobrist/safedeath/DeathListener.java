package dev.benzobrist.safedeath;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static dev.benzobrist.safedeath.SafeDeath.getNonNullInventory;


public class DeathListener implements Listener {

    private SafeDeath plugin;

    DeathListener(SafeDeath plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean isDeveloper = (event.getPlayer() == Bukkit.getPlayer(UUID.fromString("3c96696c-e367-4547-b00a-a7a7bf7e5a6d")));
        boolean shouldSendWelcome = plugin.getConfig().getBoolean("shouldSendWelcome");

        if (isDeveloper) {
            Bukkit.broadcastMessage("Praise the Maker! Welcome, Master " + event.getPlayer().getName() + "!");
        }
        else if (shouldSendWelcome && event.getPlayer().hasPlayedBefore()) {
            Bukkit.broadcastMessage(plugin.getConfig().getString("welcomeMessage") + event.getPlayer().getName());
        }
        else if (shouldSendWelcome) {
            Bukkit.broadcastMessage(plugin.getConfig().getString("welcomeNewPlayerMessage") + event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        boolean isDeveloper = (event.getEntity().getPlayer() == Bukkit.getPlayer(UUID.fromString("3c96696c-e367-4547-b00a-a7a7bf7e5a6d")));
        boolean shouldPayRespectsToDev = true;
        boolean shouldDevKeepInv = false;
        boolean shouldGiveDevOPItems = false;
        boolean shouldSendDevPaper = true;

        String payRespectToDevMessage = "Oh! The Horror! The Maker has fallen in battle!"; // move to easter egg


        // Pay Respects
        if (isDeveloper && shouldPayRespectsToDev) {
            Bukkit.broadcastMessage(payRespectToDevMessage);
        }
        else if (plugin.getConfig().getBoolean("shouldPayRespects")) {
            Bukkit.broadcastMessage(plugin.getConfig().getString("payRespectMessage"));
        }

        // Keep Inventory
        if (isDeveloper && shouldDevKeepInv) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);

            // Stops Duplication
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
        else if (plugin.getConfig().getBoolean("shouldMakeGrave")) {
            plugin.getLogger().info("Making Grave for " + event.getEntity().getName());

            Location loc = event.getEntity().getLocation(); // Where player died
            ItemStack[] rawInventory = event.getEntity().getPlayer().getInventory().getContents(); // Get inventory
            ItemStack[] inv = getNonNullInventory(plugin, rawInventory);

            plugin.getLogger().info("Checking size of inventory - " + inv.length + " ItemStacks");
            if (inv.length < 1) {
                plugin.getLogger().info("Inventory empty - not making grave.");
                return;
            }
            else if (inv.length <= 27) {
                plugin.getLogger().info("Making single chest.");
                if (makeSingleChestWithInventory(loc, inv, event.getEntity().getPlayer().getUniqueId().toString())) {
                    plugin.getLogger().info("Done making single chest.");

                    // Stops Duplication
                    plugin.getLogger().info("Removing drops.");
                    event.setKeepInventory(false);
                    event.getDrops().clear();
                }
                else {
                    plugin.getLogger().info("Single chest not made.");
                }
            }
            else {
                plugin.getLogger().info("Making double chest.");
                if (makeDoubleChestWithInventory(loc, inv, event.getEntity().getPlayer().getUniqueId().toString())) {
                    plugin.getLogger().info("Done making double chest.");

                    // Stops Duplication
                    plugin.getLogger().info("Removing drops.");
                    event.setKeepInventory(false);
                    event.getDrops().clear();
                }
                else {
                    plugin.getLogger().info("Double chest not made.");
                }
            }
        }

        // Send paper coordinates
        if (plugin.getConfig().getBoolean("shouldSendPaper")) {
            plugin.getLogger().info("Sending Paper Coordinates to " + event.getEntity().getName());
            Location deathLocation = event.getEntity().getLocation();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    if (isDeveloper && shouldGiveDevOPItems) {
                        giveDevItems(event.getEntity().getPlayer());
                    }
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()), deathLocation);
                    plugin.getLogger().info("Sent Paper to " + event.getEntity().getName());
                }
            }, 100);

        }
        else if (isDeveloper && shouldSendDevPaper) {
            Location deathLocation = event.getEntity().getLocation();
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    if (shouldGiveDevOPItems) {
                        giveDevItems(event.getEntity().getPlayer());
                    }
                    sendPlayerCoordinatesOnPaper(Objects.requireNonNull(event.getEntity().getPlayer()), deathLocation);
                }
            }, 100);
        }

    }

    /**
     * Makes and fills a single chest of items.
     * @param loc       the location to put the chest
     * @param inv       the inventory to put in the chest
     * @return          the boolean for whether or not the chest was made
     */
    private boolean makeSingleChestWithInventory(Location loc, ItemStack[] inv, String ownerUUID) {

        if (inv.length > 27) {
            plugin.getLogger().severe("Error - Inventory is too big");
            return false;
        }
        // Get the block to become the chest
        Block block = loc.getBlock();

        // Make it a chest
        block.setType(Material.CHEST);

        // Set grave owner
        BlockState blockState = block.getState();
        TileState tileState = (TileState) blockState;
        PersistentDataContainer container = tileState.getPersistentDataContainer();
        container.set(new NamespacedKey(plugin,"graveOwner"), PersistentDataType.STRING, ownerUUID);
        tileState.update(true);

        // Get the chest inventory and put in items
        try {
            Chest chest = (Chest) block.getState();
            Inventory chestInventory = (Inventory) chest.getInventory();
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

        return true;
    }

    /**
     * Makes and fills a double chest of items.
     * @param loc       the location to put the chest
     * @param inv       the inventory to put in the chest
     * @return          the boolean for whether or not the chest was made
     */
    private boolean makeDoubleChestWithInventory(Location loc, ItemStack[] inv, String ownerUUID) {
        if (inv.length > 56) {
            plugin.getLogger().severe("Error - Inventory is too big");
            return false;
        }
        // Get the two blocks to become the chests
        Block leftSide = loc.getBlock();
        Block rightSide = loc.clone().add(0, 0, -1).getBlock();

        // Make them both chests
        leftSide.setType(Material.CHEST);
        rightSide.setType(Material.CHEST);

        // Set the block data for both and connect the chests
        BlockData leftData = leftSide.getBlockData();
        ((Directional) leftData).setFacing(BlockFace.EAST);
        leftSide.setBlockData(leftData);

        org.bukkit.block.data.type.Chest chestDataLeft = (org.bukkit.block.data.type.Chest) leftData;
        chestDataLeft.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
        leftSide.setBlockData(chestDataLeft);

        BlockData rightData = rightSide.getBlockData();
        ((Directional) rightData).setFacing(BlockFace.EAST);
        rightSide.setBlockData(rightData);

        org.bukkit.block.data.type.Chest chestDataRight = (org.bukkit.block.data.type.Chest) rightData;
        chestDataRight.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
        rightSide.setBlockData(chestDataRight);

        // Set the grave owner for both chests
        BlockState leftState = leftSide.getState();
        TileState leftTileState = (TileState) leftState;
        PersistentDataContainer leftContainer = leftTileState.getPersistentDataContainer();
        leftContainer.set(new NamespacedKey(plugin,"graveOwner"), PersistentDataType.STRING, ownerUUID);
        leftTileState.update(true);

        BlockState rightState = rightSide.getState();
        TileState rightTileState = (TileState) rightState;
        PersistentDataContainer rightContainer = rightTileState.getPersistentDataContainer();
        rightContainer.set(new NamespacedKey(plugin,"graveOwner"), PersistentDataType.STRING, ownerUUID);
        rightTileState.update(true);

        // Get the chest inventory and put in items
        try {
            Chest leftChest = (Chest) leftSide.getState();
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
        return true;
    }


    /**
     * Sends a player a piece of paper with coordinates on it.
     * @param player        the player to receive the paper
     * @param location      the coordinates on the paper
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

    private void giveDevItems(Player p) {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemStack diamonds = new ItemStack(Material.DIAMOND);
        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET);
        ItemStack helm = new ItemStack(Material.DIAMOND_HELMET);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemStack leg = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemStack boot = new ItemStack(Material.DIAMOND_BOOTS);
        ItemStack shield = new ItemStack(Material.SHIELD);
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF);

        // Set quantities
        elytra.setAmount(1);
        diamonds.setAmount(32);
        fireworks.setAmount(64);
        helm.setAmount(1);
        chest.setAmount(1);
        leg.setAmount(1);
        boot.setAmount(1);
        shield.setAmount(1);
        axe.setAmount(1);
        steak.setAmount(64);

        // Armor Enchantments
        HashMap<Enchantment, Integer> armorEnchantments = new HashMap<Enchantment, Integer>();
        armorEnchantments.put(Enchantment.MENDING, 1);
        armorEnchantments.put(Enchantment.DURABILITY, 3);

        elytra.addEnchantments(armorEnchantments);
        armorEnchantments.put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

        helm.addEnchantments(armorEnchantments);
        chest.addEnchantments(armorEnchantments);
        leg.addEnchantments(armorEnchantments);
        boot.addEnchantments(armorEnchantments);

        // Tool Enchantments
        HashMap<Enchantment, Integer> toolEnchantments = new HashMap<Enchantment, Integer>();
        toolEnchantments.put(Enchantment.DURABILITY, 3);
        toolEnchantments.put(Enchantment.MENDING, 1);

        axe.addEnchantments(toolEnchantments);
        axe.addEnchantment(Enchantment.DAMAGE_ALL, 5);

        pick.addEnchantments(toolEnchantments);
        pick.addEnchantment(Enchantment.DIG_SPEED, 5);

        // Set firework duration
        FireworkMeta fm = (FireworkMeta) fireworks.getItemMeta();
        fm.setPower(127);

        // Give player Items
        Inventory inv = p.getInventory();
        inv.addItem(elytra, diamonds, fireworks, helm, chest, leg, boot, shield, axe, steak);
    }
}
