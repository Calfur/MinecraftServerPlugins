package com.github.calfur.minecraftserverplugins.diamondkill;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.calfur.minecraftserverplugins.diamondkill.beaconFight.BeaconFightManager;
import com.github.calfur.minecraftserverplugins.diamondkill.commands.CommandRegistrator;
import com.github.calfur.minecraftserverplugins.diamondkill.database.KillDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.database.PlayerDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.database.TeamDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.disabling.FeatureDisabler;
import com.github.calfur.minecraftserverplugins.diamondkill.hungerGamesLootDrop.ItemSpawnAnnouncer;
import com.github.calfur.minecraftserverplugins.diamondkill.hungerGamesLootDrop.ItemSpawner;

public class Main extends JavaPlugin {
	private static Main instance;
	private final PlayerDbConnection playerDbConnection = new PlayerDbConnection();
	private final TeamDbConnection teamDbConnection = new TeamDbConnection();
	private final KillDbConnection killDbConnection = new KillDbConnection();
	private TeamAttackManager teamAttackManager;
	private PlayerModeManager playerModeManager;
	private BeaconFightManager beaconFightManager;
	private static ScoreboardLoader scoreboardLoader;
	
	@Override
	public void onEnable() {
		instance = this;
		
		playerDbConnection.loadConfig();
		teamDbConnection.loadConfig();
		killDbConnection.loadConfig();

		teamAttackManager = new TeamAttackManager();
		playerModeManager = new PlayerModeManager();
		beaconFightManager = new BeaconFightManager();
		scoreboardLoader = new ScoreboardLoader();
		
		new FeatureDisabler(); 
		new CommandRegistrator();
		new EventRegistrator();

		DeathBanPluginInteraction.tryChangeBanDuration(10);
		//BeaconBreakStrengthIncreaser.increaseBreakStrength();
		
		startItemSpawner();
	}

	private void startItemSpawner() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime fullHour = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
		long ticksUntilFullHour = ChronoUnit.SECONDS.between(now, fullHour) * 20;
		if(ticksUntilFullHour-6000 > 0) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ItemSpawner(new Location(Bukkit.getWorlds().get(0), 0.5, 80, 0.5)), ticksUntilFullHour, 72000);
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ItemSpawnAnnouncer(), ticksUntilFullHour-6000, 72000);
		}else {
			ticksUntilFullHour += 72000;
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ItemSpawner(new Location(Bukkit.getWorlds().get(0), 0.5, 80, 0.5)), ticksUntilFullHour, 72000);
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new ItemSpawnAnnouncer(), ticksUntilFullHour-6000, 72000);
		}
	}
	
	@Override
	public void onDisable() {
		
	}
	
	public static Main getInstance() {
		return instance;
	}

	public PlayerDbConnection getPlayerDbConnection() {
		return playerDbConnection;
	}

	public TeamDbConnection getTeamDbConnection() {
		return teamDbConnection;
	}
	
	public KillDbConnection getKillDbConnection() {
		return killDbConnection;
	}
	
	public TeamAttackManager getTeamAttackManager() {
		return teamAttackManager;
	}

	public ScoreboardLoader getScoreboardLoader() {
		return scoreboardLoader;
	}
	
	public PlayerModeManager getPlayerModeManager() {
		return playerModeManager;
	}

	public BeaconFightManager getBeaconFightManager() {
		return beaconFightManager;
	}
}
