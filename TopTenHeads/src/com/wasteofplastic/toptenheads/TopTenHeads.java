package com.wasteofplastic.toptenheads;


import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class TopTenHeads extends JavaPlugin {
    private TopTenHeadsCommand commandListener;

    @Override
    public void onEnable() {
        // Enable the plugin
        PluginManager manager = getServer().getPluginManager();
        // Check for ASkyBlock
        Plugin asb = manager.getPlugin("ASkyBlock");

        if (asb == null) {
            getLogger().severe("ASkyBlock not loaded. Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            getLogger().info(asb.getDescription().getVersion());
            // Load config
            saveDefaultConfig();
            // Register command
            commandListener = new TopTenHeadsCommand(this);
            getCommand("placetopten").setExecutor(commandListener);
            getCommand("removetopten").setExecutor(commandListener);
            // Load from config
            getServer().getScheduler().runTaskLater(this, new Runnable() {

                public void run() {
                    reload();	
                }}, 20L);
        }

    }

    @Override
    public void onDisable() {
        //getLogger().info("DEBUG: disabling");
        if (commandListener == null) {
            return;
        }
        // Save any top ten lists
        List<LevelListener> topTen = commandListener.getTopTen();
        List<String> serialize = new ArrayList<String>();
        for (LevelListener panel : topTen) {
            //getLogger().info("DEBUG: serializing");
            serialize.add(Util.getStringLocation(panel.getTopTenLocation()) + ":" + panel.getDirection().toString());
        }
        getConfig().set("panels", serialize);
        saveConfig();
    }

    public void reload() {
        // Load any panels
        List<String> serialize = getConfig().getStringList("panels");
        for (String panel : serialize) {
            try {
                String direction = panel.substring(panel.lastIndexOf(':')+1);
                //getLogger().info("DEBUG: direction = " + direction);
                BlockFace dir = BlockFace.valueOf(direction);
                String location = panel.substring(0, panel.lastIndexOf(':'));
                //getLogger().info("DEBUG: location = " + location);
                Location loc = Util.getLocationString(location);
                //getLogger().info("DEBUG: loc = " + loc);
                LevelListener newTopTen = new LevelListener(this, loc, dir);
                commandListener.addTopTen(newTopTen);
                getServer().getPluginManager().registerEvents(newTopTen, this);
                //getLogger().info("DEBUG: new topten panel at " + loc + " heading " + dir);
            } catch(Exception e) {
                getLogger().severe("Problem loading panel " + panel + " skipping...");
                e.printStackTrace();
            }
        }
    }

}
