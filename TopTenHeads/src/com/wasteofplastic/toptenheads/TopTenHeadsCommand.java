package com.wasteofplastic.toptenheads;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;
import org.bukkit.util.BlockIterator;

import com.wasteofplastic.askyblock.events.IslandLevelEvent;

public class TopTenHeadsCommand implements CommandExecutor {
    private TopTenHeads plugin;
    private List<LevelListener> topTen;
    /**
     * @param plugin
     */
    public TopTenHeadsCommand(TopTenHeads plugin) {
        this.plugin = plugin;
        topTen = new ArrayList<LevelListener>();
    }

    public boolean onCommand(CommandSender sender, Command arg1, String label, String[] arg3) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Command must be used in-game while looking at a row of signs.");
            return false;
        }
        Player player = (Player)sender;
        if (!player.isOp() && !player.hasPermission("askyblock.mod.topten")) {
            sender.sendMessage(ChatColor.RED + "You must be op or have askyblock.mod.topten permission to use this command");
            return true;
        }
        // Deal with global commands first
        if (label.equalsIgnoreCase("placetopten")) {
            if (arg3.length == 1) {
                if (arg3[0].equalsIgnoreCase("help")) {
                    sender.sendMessage(ChatColor.RED + "TopTenHeads Help");
                    sender.sendMessage(ChatColor.GOLD + "To place or remove a top ten panel, build a row of signs and look at the left-most sign, then type a command");
                    sender.sendMessage(ChatColor.WHITE + "/placetopten " + ChatColor.GOLD + "- place a top ten panel.");
                    sender.sendMessage(ChatColor.WHITE + "/removetopten " + ChatColor.GOLD + "- remove a top ten panel");
                    sender.sendMessage(ChatColor.WHITE + "/removetopten all" + ChatColor.GOLD + "- removal all top ten panels"); 
                    return true;
                }
            }
            // Get the block that the player is looking at
            // Find out whether the player is looking at a warp sign
            // Look at what the player was looking at
            BlockIterator iter = new BlockIterator(player, 10);
            Block lastBlock = iter.next();
            while (iter.hasNext()) {
                lastBlock = iter.next();
                if (lastBlock.getType() == Material.AIR)
                    continue;
                break;
            }
            //plugin.getLogger().info("DEBUG: " + lastBlock);
            if (lastBlock.getType() != Material.WALL_SIGN && lastBlock.getType() != Material.SIGN_POST) {
                sender.sendMessage(ChatColor.RED + "You must be looking at a sign to start");
                return true;
            }
            // Get the direction that the sign is facing
            Sign sign = (Sign)lastBlock.getState().getData();
            BlockFace facing = sign.getFacing();
            BlockFace right = null;
            // Get the direction to the right
            switch (facing) {
            case EAST:
                right = BlockFace.NORTH;
                break;
            case NORTH:
                right = BlockFace.WEST;
                break;
            case SOUTH:
                right = BlockFace.EAST;
                break;
            case WEST:
                right = BlockFace.SOUTH;
                break;
            default:
                break;

            }
            if (right == null) {
                sender.sendMessage(ChatColor.RED + "Sign needs to face north, south, east or west.");
                return true; 
            }
            // Check if this panel already exists
            for (LevelListener panel : topTen) {
                if (panel.getTopTenLocation().equals(lastBlock.getLocation())) {
                    panel.setDirection(right);
                    sender.sendMessage(ChatColor.RED + "Top ten board is already active");
                    return true;
                }
            }
            LevelListener newTopTen = new LevelListener(plugin, lastBlock.getLocation(), right);
            topTen.add(newTopTen);
            plugin.getServer().getPluginManager().registerEvents(newTopTen, plugin);
            sender.sendMessage(ChatColor.GREEN + "Top ten board created");
        } else if (label.equalsIgnoreCase("removetopten")) {
            if (arg3.length == 1 && arg3[0].equalsIgnoreCase("all")) {
                if (topTen.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "There are no active top ten panels");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Removing " + topTen.size() + " top ten panels");
                for (LevelListener panel : topTen) {
                    // Unregister
                    IslandLevelEvent.getHandlerList().unregister(panel);
                    // Clear
                    panel.removeTopTen();
                    // Remove from the list
                    topTen.remove(panel);
                }
                return true;
            }
            if (arg3.length == 1 && arg3[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.RED + "TopTenHeads Help");
                sender.sendMessage(ChatColor.GOLD + "To place or remove a top ten panel, build a row of signs and look at the left-most sign, then type a command");
                sender.sendMessage(ChatColor.WHITE + "/placetopten " + ChatColor.GOLD + "- place a top ten panel.");
                sender.sendMessage(ChatColor.WHITE + "/removetopten " + ChatColor.GOLD + "- remove a top ten panel");
                sender.sendMessage(ChatColor.WHITE + "/removetopten all" + ChatColor.GOLD + "- removal all top ten panels"); 
                return true;
            }
            // Look at what the player was looking at
            BlockIterator iter = new BlockIterator(player, 10);
            Block lastBlock = iter.next();
            while (iter.hasNext()) {
                lastBlock = iter.next();
                if (lastBlock.getType() == Material.AIR)
                    continue;
                break;
            }
            //plugin.getLogger().info("DEBUG: " + lastBlock);
            if (lastBlock.getType() != Material.WALL_SIGN && lastBlock.getType() != Material.SIGN_POST) {
                sender.sendMessage(ChatColor.RED + "You must be looking at the #1 top ten sign");
                return true;
            }
            // Check if this panel  exists
            for (LevelListener panel : topTen) {
                if (panel.getTopTenLocation().equals(lastBlock.getLocation())) {
                    // Unregister
                    IslandLevelEvent.getHandlerList().unregister(panel);
                    // Clear
                    panel.removeTopTen();
                    // Remove from the list
                    topTen.remove(panel);
                    sender.sendMessage(ChatColor.GREEN + "Top ten board removed");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.RED + "Could not find a Top Ten board in that position");
            return true;
        }
        return false;
    }


    /**
     * @return the topTen
     */
    public List<LevelListener> getTopTen() {
        return topTen;
    }


    /**
     * @param topTen the topTen to set
     */
    public void setTopTen(List<LevelListener> topTen) {
        this.topTen = topTen;
    }

    /**
     * Add one panel to the list
     * @param topTen
     */
    public void addTopTen(LevelListener topTen) {
        this.topTen.add(topTen);
    }

}
