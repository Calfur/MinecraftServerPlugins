package com.github.calfur.minecraftserverplugins.diamondkill.commands;

import org.bukkit.entity.Player;

import com.github.calfur.minecraftserverplugins.diamondkill.Main;
import com.github.calfur.minecraftserverplugins.diamondkill.database.PlayerJson;
import com.github.calfur.minecraftserverplugins.diamondkill.database.TeamDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.database.KillDbConnection;
import com.github.calfur.minecraftserverplugins.diamondkill.database.PlayerDbConnection;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandPlayer implements CommandExecutor {

	private PlayerDbConnection playerDbConnection = Main.getInstance().getPlayerDbConnection();
	private TeamDbConnection teamDbConnection = Main.getInstance().getTeamDbConnection();
	private KillDbConnection killDbConnection = Main.getInstance().getKillDbConnection();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player executor = (Player)sender;						
			if(args.length >= 1) {
				String subCommand = args[0].toLowerCase();
				switch(subCommand) {
					case "info":
						return sendPlayerInfo(executor, args);
					case "list":
						return sendPlayerList(executor, args);
					case "delete":
						if(executor.hasPermission("admin")) {	
							return deletePlayer(executor, args);
						}else {
							executor.sendMessage(ChatColor.RED + "Fehlende Berechtigung f�r diesen Command");
							return true;
						}
					case "add":
						if(executor.hasPermission("admin")) {	
							return addPlayer(executor, args);
						}else {
							executor.sendMessage(ChatColor.RED + "Fehlende Berechtigung f�r diesen Command");
							return true;
						}
					case "edit":
						if(executor.hasPermission("admin")) {	
							return editPlayer(executor, args);
						}else {
							executor.sendMessage(ChatColor.RED + "Fehlende Berechtigung f�r diesen Command");
							return true;
						}
					default:
						executor.sendMessage(ChatColor.RED + subCommand + " ist kein vorhandener Command");
						return false;
				}
			}			
		}
		return false;
	}

	private boolean sendPlayerList(Player executor, String[] args) {
		if(args.length != 1) {
			executor.sendMessage(ChatColor.RED + "Der Command enth�lt nicht die richtige anzahl Parameter");
			return false;
		}
		Map<String, PlayerJson> players = playerDbConnection.getPlayers();
		executor.sendMessage(ChatColor.AQUA + "" + players.size() + " Spieler gefunden:");
		for (Entry<String, PlayerJson> player : players.entrySet()) {
			executor.sendMessage(player.getKey() + " " + "404 k/d");
		}
		return true;
	}

	private boolean sendPlayerInfo(Player executor, String[] args) {
		if(args.length != 2) {
			executor.sendMessage(ChatColor.RED + "Der Command enth�lt nicht die richtige anzahl Parameter");
			return false;
		}
		String name = args[1];
		if(!playerDbConnection.existsPlayer(name)) {
			executor.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht registriert");
			return false;
		}
		PlayerJson playerJson = playerDbConnection.getPlayer(name);
		executor.sendMessage(ChatColor.AQUA + "Name: " + name); 
		executor.sendMessage((ChatColor.AQUA + "Team: ") + (teamDbConnection.getTeam(playerJson.getTeamId()).getColor() + "" + playerJson.getTeamId()));
		executor.sendMessage(ChatColor.AQUA + "Discord Name: " + playerJson.getDiscordName());
		executor.sendMessage(ChatColor.AQUA + "Nicht eingesammelte Diamanten: " + playerJson.getCollectableDiamonds());
		executor.sendMessage(ChatColor.AQUA + "K/D: " + killDbConnection.getAmountOfKills(name) + "/" + killDbConnection.getAmountOfDeaths(name));
		executor.sendMessage(ChatColor.AQUA + "Kopfgeld: " + killDbConnection.getBounty(name));
		return true;
	}
	
	private boolean deletePlayer(Player executor, String[] args) {
		if(args.length != 2) {
			executor.sendMessage(ChatColor.RED + "Der Command enth�lt nicht die richtige anzahl Parameter");
			return false;
		}
		String name = args[1];
		if(!playerDbConnection.existsPlayer(name)) {
			executor.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht registriert");
			return false;
		}
		playerDbConnection.removePlayer(name);
		executor.sendMessage(ChatColor.GREEN + name + " gel�scht.");
		return true;
	}
	
	private boolean editPlayer(Player executor, String[] args) {
		if(args.length != 4) {
			executor.sendMessage(ChatColor.RED + "Der Command enth�lt nicht die richtige anzahl Parameter");
			return false;
		}
		String name;
		int team;
		String discordName;
		try {
			name = args[1];
			team = Integer.parseInt(args[2]);
			discordName = args[3].toLowerCase();
		}catch(NumberFormatException e) {
			executor.sendMessage(ChatColor.RED + "Der Team Parameter muss dem Typ Int entsprechen");
			return false;
		}
		if(!playerDbConnection.existsPlayer(name)) {
			executor.sendMessage(ChatColor.RED + "Dieser Spieler ist nicht vorhanden");
			return false;
		}
		playerDbConnection.addPlayer(name, new PlayerJson(team, discordName, 0));
		executor.sendMessage(ChatColor.GREEN + name + " editiert.");
		return true;
	}
	
	private boolean addPlayer(Player executor, String[] args) {
		if(args.length != 4) {
			executor.sendMessage(ChatColor.RED + "Der Command enth�lt nicht die richtige anzahl Parameter");
			return false;
		}
		String name;
		int team;
		String discordName;
		try {
			name = args[1];
			team = Integer.parseInt(args[2]);
			discordName = args[3].toLowerCase();
		}catch(NumberFormatException e) {
			executor.sendMessage(ChatColor.RED + "Der Team Parameter muss dem Typ Int entsprechen");
			return false;
		}
		if(playerDbConnection.existsPlayer(name)) {
			executor.sendMessage(ChatColor.RED + "Dieser Spieler wurde bereits registriert");	
			return false;
		}
		if(!teamDbConnection.existsTeam(team)) {
			executor.sendMessage(ChatColor.RED + "Ein Team mit der Id " + team + " existiert nicht.");	
			return false;
		}
		playerDbConnection.addPlayer(name, new PlayerJson(team, discordName, 0));
		executor.sendMessage(ChatColor.GREEN + name + " registriert.");
		return true;
	}
}
