package com.github.calfur.minecraftserverplugins.diamondkill;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.calfur.minecraftserverplugins.diamondkill.database.PlayerDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.database.TeamDbConnection;

public class PlayerModeManager {
	private TeamDbConnection teamDbConnection = Main.getInstance().getTeamDbConnection();
	private PlayerDbConnection playerDbConnection = Main.getInstance().getPlayerDbConnection();
	private TeamAttackManager teamAttackManager = Main.getInstance().getTeamAttackManager();
	
	private HashMap<String, PlayerMode> playerModes = new HashMap<String, PlayerMode>();
	public static final int buildModeCooldownInMinutes = 30;
	private static final int baseRange = 100;
	private static final int buildModeRangeCheckDelayInSeconds = 1;
	
	private PlayerMode getPlayerMode(String playerName) {
		return playerModes.get(playerName.toLowerCase());
	}
	
	private PlayerMode addPlayerMode(Player player) {
		PlayerMode playerMode = new PlayerMode(player.getName());
		playerModes.put(player.getName().toLowerCase(), playerMode);
		return playerMode;
	}
	
	public PlayerModeManager() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				for (PlayerMode playerMode : playerModes.values()) {					
					if(playerMode.isBuildModeActive()) {
						Player player = playerMode.getPlayer();
						if(player != null) {							
							if(!isPlayerWithinRangeOfHisBase(player)) {
								int secondsUntilBuildModeGetsDeactivated = playerMode.getSecondsUntilBuildModeGetsDeactivated();
								if(secondsUntilBuildModeGetsDeactivated <= 0) {
									deactivateBuildMode(playerMode);	
								}else {
									playerMode.reduceSecondsLeftUntilBuildModeGetsDeactivated(buildModeRangeCheckDelayInSeconds);
									player.sendMessage(ChatColor.RED + "Nicht mehr im Base Bereich! Baumodus wird in " + secondsUntilBuildModeGetsDeactivated + "s automatisch deaktiviert!");
								}
							}else {
								playerMode.resetSecondsLeftUntilBuildModeGetsDeactivatedBecauseNotInBaseRange();
							}					
						}						
					}
				}				
			}
		}, buildModeRangeCheckDelayInSeconds*20, buildModeRangeCheckDelayInSeconds*20); //repeating all 1s
	}
	
	public boolean toggleBuildMode(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode != null) {			
			if(playerMode.isBuildModeActive()) {
				deactivateBuildMode(playerMode);
				return true;
			}
		}
		return activateBuildModeIfAllowed(playerMode, player);
	}
	
	private void deactivateBuildMode(PlayerMode playerMode) {
		if(playerMode.isBuildModeActive()) {			
			playerMode.deactivateBuildMode();
			playerMode.getPlayer().sendMessage(ChatColor.GREEN + "Baumodus deaktiviert");
			Main.getInstance().getScoreboardLoader().reloadScoreboardFor(playerMode.getPlayer());
		}
	}
	
	private void activateBuildMode(PlayerMode playerMode) {
		playerMode.activateBuildMode();
		playerMode.getPlayer().sendMessage(ChatColor.GREEN + "Baumodus aktiviert." + ChatColor.RESET + " Deaktivieren mit /buildmode");
		Main.getInstance().getScoreboardLoader().reloadScoreboardFor(playerMode.getPlayer());		
	}

	public boolean isPlayerAllowedToFight(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode == null) {
			return true;
		}else {
			if(playerMode.isBuildModeActive()) {
				return false;
			}
			return true;
		}
	}
	
	private boolean activateBuildModeIfAllowed(PlayerMode playerMode, Player player) {
		
		if(Main.getInstance().getBeaconFightManager().isBeaconEventActive()) {
			player.sendMessage(ChatColor.DARK_RED + "W�hrend einem Beacon Event kann der Baumodus nicht aktiviert werden");
			return false;
		}
		
		int teamId = playerDbConnection.getPlayer(player.getName()).getTeamId();
		if(teamAttackManager.isTeamFighting(teamId)) {	
			player.sendMessage(ChatColor.DARK_RED + "Dein Team befindet sich momentan noch in einem Kampf. Der Baumodus kann erst aktiviert werden wenn der Kampf nicht mehr auf dem Scoreboard angezeigt wird.");
			return false;
		}
		
		if(!isPlayerWithinRangeOfHisBase(player)) {	
			player.sendMessage(ChatColor.DARK_RED + "Du befindest dich mehr als " + baseRange + " Bl�cke von deinem Beacon entfernt. Der Baumodus kann hier nicht aktiviert werden.");
			return false;
		}
		
		if(playerMode == null) {
			playerMode = addPlayerMode(player);
			activateBuildMode(playerMode);
			return true;
		}
		
		long minutesSinceDeactivated = ChronoUnit.SECONDS.between(playerMode.getBuildModeDeactivatedAt(), LocalDateTime.now())/60;
		if(minutesSinceDeactivated < buildModeCooldownInMinutes) {
			player.sendMessage(ChatColor.DARK_RED + "Der Baumodus kann erst in " + (buildModeCooldownInMinutes - minutesSinceDeactivated) + " Minuten erneut aktiviert werden");
			return false;
		}
		
		activateBuildMode(playerMode);
		return true;
	}
	
	private boolean isPlayerWithinRangeOfHisBase(Player player) {
		int teamId = playerDbConnection.getPlayer(player.getName()).getTeamId();
		Location baseLocation = teamDbConnection.getTeam(teamId).getBeaconPosition();
		Location playerLocation = player.getLocation();
		int distanceX = Math.abs(playerLocation.getBlockX() - baseLocation.getBlockX());
		int distanceZ = Math.abs(playerLocation.getBlockZ() - baseLocation.getBlockZ());
		double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceZ, 2));
		return (distance < baseRange) && (playerLocation.getWorld() == baseLocation.getWorld());
	}
	
	public void reloadPlayerMode(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode == null) {
			PlayerMode.removeModeEffects(player);
		}else {
			if(Main.getInstance().getBeaconFightManager().isBeaconEventActive()) {
				deactivateBuildMode(playerMode);
			}
			playerMode.reloadEffects();
		}
	}
	
	public boolean isPlayerInBuildMode(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode == null) {
			return false;
		}else {
			if(playerMode.isBuildModeActive()) {
				return true;
			}
			return false;
		}
	}

	public void activatePlayerHighlight(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode == null) {
			playerMode = addPlayerMode(player);		
		}
		playerMode.activateHighlighted();
	}
	
	public void deactivatePlayerHighlight(Player player) {
		PlayerMode playerMode = getPlayerMode(player.getName());
		if(playerMode != null) {
			playerMode.deactivateHighlighted();
		}
	}
}
