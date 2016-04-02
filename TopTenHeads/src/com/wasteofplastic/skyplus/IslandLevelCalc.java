package com.wasteofplastic.skyplus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.events.IslandLevelEvent;

public class IslandLevelCalc implements Listener {

    private ASkyBlockAPI api = ASkyBlockAPI.getInstance();
    private Set<UUID> playersWaiting = new HashSet<UUID>();

    public IslandLevelCalc() {
	this.playersWaiting.clear();
    }

    /**
     * Kicks off the island calculation for Player
     * @param player
     */
    public void getPlayersLevel(Player player) {
	// Add to the list of players that I'm asking levels for
	playersWaiting.add(player.getUniqueId());
	// Kick off the island calculation. When it is done, it will fire the IslandLevelEvent
	api.calculateIslandLevel(player.getUniqueId());
    }
    
    /**
     * Called when any island level calculation is complete. If it was by use
     * do something, otherwise ignore.
     * @param event
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNewLevel(final IslandLevelEvent event) {
	//Bukkit.getLogger().info("Island level event");
	// Check to see if this is a player that was requested
	if (playersWaiting.contains(event.getPlayer())) {
	    // It was, so remove them from the set and then do something
	    playersWaiting.remove(event.getPlayer());
	    // DO SOMETHING
	}
    }


}