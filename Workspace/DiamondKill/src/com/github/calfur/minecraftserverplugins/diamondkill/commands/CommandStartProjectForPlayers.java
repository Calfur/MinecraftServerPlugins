package com.github.calfur.minecraftserverplugins.diamondkill.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;

import com.github.calfur.minecraftserverplugins.diamondkill.BeaconManager;

public class CommandStartProjectForPlayers  implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) { // Spieler f�hrt den Command aus
			Player executor = (Player)sender;						
			if(executor.hasPermission("admin")) {			
				if(args.length == 1) {
					Player player = Bukkit.getPlayerExact(args[0]);
					if(player != null) {
						startProjectForPlayer(player);
					}else {
						executor.sendMessage(ChatColor.DARK_RED + "Spieler " + args[0] + " konnte nicht gefunden werden (muss online sein)");
					}
				}else {
					executor.sendMessage(ChatColor.DARK_RED + "Falsche Anzahl Parameter");
					return false;
				}
			}else {
				executor.sendMessage(ChatColor.DARK_RED + "Fehlende Berechtigung f�r diesen Command");
				return false;
			}
		}else if(sender instanceof BlockCommandSender) { // Commandblock f�hrt den Command aus
			BlockCommandSender commandBlock = (BlockCommandSender)sender;
			Location location = commandBlock.getBlock().getLocation();
			return startProjectForAllNearbyPlayers(location);
		}
		return false;
	}

	public static void startProjectForAllOnlinePlayers() {
		startProjectForPlayers(Bukkit.getOnlinePlayers());
	}

	private static boolean startProjectForAllNearbyPlayers(Location location) {
		List<Player> nearbyPlayers = getNearbyPlayers(location);
		if(nearbyPlayers.size() == 0) {			
			return false;
		}
		startProjectForPlayers(nearbyPlayers);
		return true;
	}

	private static List<Player> getNearbyPlayers(Location location) {
		List<Player> nearbyPlayers = new ArrayList<Player>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			Location playerLocation = player.getLocation();
			double distance = location.distance(playerLocation);
			if(distance < 5) {
				nearbyPlayers.add(player);
			}
		}
		return nearbyPlayers;
	}

	private static void startProjectForPlayers(Collection<? extends Player> collection) {
		for (Player player : collection) {
			startProjectForPlayer(player);
		}
	}
	
	private static void startProjectForPlayer(Player player) {
		BeaconManager.teleportPlayerToBeacon(player);
		BeaconManager.setBeaconAsRespawnLocation(player);
		player.setGameMode(GameMode.SURVIVAL);
	}
}
