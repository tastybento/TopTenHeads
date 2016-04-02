package com.wasteofplastic.skyplus;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.events.IslandLevelEvent;

public class LevelListener implements Listener {

    private TopTenHeads plugin;
    private ASkyBlockAPI api = ASkyBlockAPI.getInstance();
    private int minLevel;
    private int maxLevel;
    private Location topTenLocation;
    private BlockFace direction;

    /**
     * @param plugin
     * @param topTenLocation
     * @param direction
     */
    public LevelListener(TopTenHeads plugin, Location topTenLocation, BlockFace direction) {
        this.plugin = plugin;
        this.topTenLocation = topTenLocation;
        //plugin.getLogger().info("DEBUG: direction = " + direction);
        this.direction = direction;
        minLevel = 0;
        maxLevel = 0;
        displayTopTen();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewLevel(final IslandLevelEvent event) {
        //plugin.getLogger().info("Island level event");
        Map<UUID, Integer> topTen = api.getTopTen();
        topTen = sortByValue(topTen);
        int count = 0;
        for (Entry<UUID, Integer> entry : topTen.entrySet()) {
            if (entry.getValue() > maxLevel) {
                maxLevel = entry.getValue();
            }
            if (entry.getValue() < minLevel) {
                minLevel = entry.getValue();
            }
            if (count++ == 10) {
                break;
            }
        }
        if (count == 10 && event.getLevel() < minLevel) {
            // If the count is not yet 10, then anyone can be in the top ten
            return;
        }
        // TODO: Player is in topTen - check if this is just a repetition
        displayTopTen();

    }

    private void displayTopTen() {
        Map<UUID, Integer> topTen = api.getTopTen();
        topTen = sortByValue(topTen);
        // Sort by rank
        
        // Display the top ten
        int i = 0;
        Block b = topTenLocation.getBlock();
        for (Entry<UUID, Integer> entry : topTen.entrySet()) {
            i++;
            //plugin.getLogger().info("DEBUG: " + i);
            // Get the block and move
            BlockFace directionFacing;
            if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                Sign sign = (Sign)b.getState();
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
                directionFacing = s.getFacing();
                sign.setLine(0, "#" + i);
                String name = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
                sign.setLine(1, name);
                sign.setLine(2, "Level:" + entry.getValue());
                sign.update();
                // Place head
                BlockFace opp = directionFacing.getOppositeFace();
                Block attachToBlock = b.getRelative(BlockFace.UP).getRelative(opp);
                attachToBlock.setType(Material.SKULL);
                Skull skull = (Skull)attachToBlock.getState();
                skull.setRotation(directionFacing);
                skull.setSkullType(SkullType.PLAYER);
                skull.setOwner(name);
                skull.update();
            }


            // Move to the next block
            b = b.getRelative(direction);
            if (i == 10) {
                break;
            }
        }
        // Less than 10 in the top ten
        if (i < 10) {
            for (int j = i+1; j < 11; j++) {
                BlockFace directionFacing;
                if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                    Sign sign = (Sign)b.getState();
                    org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
                    directionFacing = s.getFacing();
                    sign.setLine(0, "#" + j);
                    sign.setLine(1, "");
                    sign.setLine(2, "");
                    sign.update();
                    // Place head
                    BlockFace opp = directionFacing.getOppositeFace();
                    Block attachToBlock = b.getRelative(BlockFace.UP).getRelative(opp);
                    attachToBlock.setType(Material.AIR);
                }
                b = b.getRelative(direction);
            }
        }

    }


    public static <Key, Value extends Comparable<? super Value>> LinkedHashMap<Key, Value> sortByValue(Map<Key, Value> map) {
        List<Map.Entry<Key, Value>> list = new LinkedList<Map.Entry<Key, Value>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Key, Value>>() {
            public int compare(Map.Entry<Key, Value> o1, Map.Entry<Key, Value> o2) {
                // Switch these two if you want ascending
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        LinkedHashMap<Key, Value> result = new LinkedHashMap<Key, Value>();
        for (Map.Entry<Key, Value> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if (result.size() > 20)
                break;
        }
        return result;
    }

    public void removeTopTen() {
        // Remove the top ten
        Block b = topTenLocation.getBlock();
        for (int j = 0; j < 10; j++) {
            BlockFace directionFacing;
            if (b.getType() == Material.WALL_SIGN || b.getType() == Material.SIGN_POST) {
                Sign sign = (Sign)b.getState();
                org.bukkit.material.Sign s = (org.bukkit.material.Sign) sign.getData();
                directionFacing = s.getFacing();
                sign.setLine(0, "");
                sign.setLine(1, "");
                sign.setLine(2, "");
                sign.update();
                // Place head
                BlockFace opp = directionFacing.getOppositeFace();
                Block attachToBlock = b.getRelative(BlockFace.UP).getRelative(opp);
                attachToBlock.setType(Material.AIR);
            }
            b = b.getRelative(direction);
        }

    }

    /**
     * @return the topTenLocation
     */
    public Location getTopTenLocation() {
        return topTenLocation;
    }

    /**
     * @return the direction
     */
    public BlockFace getDirection() {
        return direction;
    }

    /**
     * @param topTenLocation the topTenLocation to set
     */
    public void setTopTenLocation(Location topTenLocation) {
        this.topTenLocation = topTenLocation;
    }

    /**
     * @param direction the direction to set
     */
    public void setDirection(BlockFace direction) {
        this.direction = direction;
    }
}
