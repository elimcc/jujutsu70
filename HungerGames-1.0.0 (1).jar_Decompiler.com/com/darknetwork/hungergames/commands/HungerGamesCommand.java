package com.darknetwork.hungergames.commands;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.arena.Arena;
import com.darknetwork.hungergames.arena.ArenaState;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.gui.StatsGUI;
import com.darknetwork.hungergames.stats.PlayerStats;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class HungerGamesCommand implements CommandExecutor, TabCompleter {
   private final HungerGamesPlugin plugin;
   private final MessageUtil msg;

   public HungerGamesCommand(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      this.msg = plugin.getMessageUtil();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length != 0 && !args[0].equalsIgnoreCase("help")) {
         switch (args[0].toLowerCase()) {
            case "join":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.join")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String arenaName = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(arenaName);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", arenaName));
                  return true;
               }

               if (!arena.isSetup()) {
                  sender.sendMessage(this.msg.get("arena.not-setup", "{arena}", arenaName));
                  return true;
               }

               if (this.plugin.getGameManager().isInGame(player)) {
                  sender.sendMessage(this.msg.get("lobby.already-in-game"));
                  return true;
               }

               if (!arena.isAvailable()) {
                  sender.sendMessage(this.msg.get("lobby.full"));
                  return true;
               }

               boolean joined = this.plugin.getGameManager().joinGame(player, arena);
               if (!joined) {
                  sender.sendMessage(this.msg.get("lobby.full"));
                  return true;
               }

               Game game = this.plugin.getGameManager().getGame(arena);
               int current = game != null ? game.getPlayers().size() : 1;
               String joinMsg = this.msg.getRaw("lobby.joined").replace("{arena}", arenaName);
               String var10001 = this.msg.getPrefix();
               sender.sendMessage(var10001 + joinMsg);
               String broadcastMsg = this.msg.getRaw("lobby.joined-broadcast").replace("{player}", player.getName()).replace("{current}", String.valueOf(current)).replace("{max}", String.valueOf(arena.getMaxPlayers()));
               if (game != null) {
                  game.broadcastToAll(broadcastMsg);
               }
               break;
            case "leave":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.leave")) {
                  this.noPerms(sender);
                  return true;
               }

               if (!this.plugin.getGameManager().isInGame(player)) {
                  sender.sendMessage(this.msg.get("lobby.not-in-game"));
                  return true;
               }

               Game game = this.plugin.getGameManager().getGame(player);
               this.plugin.getGameManager().leaveGame(player);
               sender.sendMessage(this.msg.get("lobby.left"));
               if (game != null) {
                  String leftMsg = this.msg.getRaw("lobby.left-broadcast").replace("{player}", player.getName()).replace("{current}", String.valueOf(game.getPlayers().size())).replace("{max}", String.valueOf(game.getArena().getMaxPlayers()));
                  game.broadcastToAll(leftMsg);
               }
               break;
            case "list":
               if (!sender.hasPermission("hungergames.list")) {
                  this.noPerms(sender);
                  return true;
               }

               Collection<Arena> arenas = this.plugin.getArenaManager().getAllArenas();
               sender.sendMessage(this.msg.getRaw("list.header"));
               if (arenas.isEmpty()) {
                  sender.sendMessage(this.msg.getRaw("list.no-arenas"));
               } else {
                  for(Arena a : arenas) {
                     Game g = this.plugin.getGameManager().getGame(a);
                     int current = g != null ? g.getPlayers().size() : 0;
                     String var10000;
                     switch (a.getState()) {
                        case WAITING -> var10000 = "list.entry-waiting";
                        case LOBBY -> var10000 = "list.entry-lobby";
                        case IN_GAME -> var10000 = "list.entry-ingame";
                        default -> var10000 = "list.entry-setup";
                     }

                     String key = var10000;
                     String line = this.msg.getRaw(key).replace("{arena}", a.getName()).replace("{current}", String.valueOf(current)).replace("{max}", String.valueOf(a.getMaxPlayers()));
                     sender.sendMessage(line);
                  }
               }

               sender.sendMessage(this.msg.getRaw("list.footer"));
               break;
            case "stats":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.stats")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length > 1) {
                  (new StatsGUI(this.plugin)).open(player);
               } else {
                  PlayerStats stats = this.plugin.getStatsManager().getStats(player);
                  sender.sendMessage(this.msg.getRaw("stats.header"));
                  sender.sendMessage(this.msg.getRaw("stats.wins").replace("{wins}", String.valueOf(stats.getWins())));
                  sender.sendMessage(this.msg.getRaw("stats.kills").replace("{kills}", String.valueOf(stats.getKills())));
                  sender.sendMessage(this.msg.getRaw("stats.deaths").replace("{deaths}", String.valueOf(stats.getDeaths())));
                  sender.sendMessage(this.msg.getRaw("stats.games-played").replace("{games}", String.valueOf(stats.getGamesPlayed())));
                  sender.sendMessage(this.msg.getRaw("stats.kdr").replace("{kdr}", String.valueOf(stats.getKDR())));
                  sender.sendMessage(this.msg.getRaw("stats.footer"));
               }
               break;
            case "spectate":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.spectate")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("spectator.player-not-found", "{player}", "?"));
                  return true;
               }

               Player target = Bukkit.getPlayer(args[1]);
               if (target == null || !this.plugin.getGameManager().isInGame(target)) {
                  sender.sendMessage(this.msg.get("spectator.player-not-found", "{player}", args[1]));
                  return true;
               }

               Game targetGame = this.plugin.getGameManager().getGame(target);
               if (targetGame == null) {
                  sender.sendMessage(this.msg.get("spectator.player-not-found", "{player}", args[1]));
                  return true;
               }

               if (!this.plugin.getGameManager().isInGame(player)) {
                  targetGame.makeSpectatorFromCommand(player);
               }

               player.teleport(target.getLocation());
               sender.sendMessage(this.msg.get("spectator.now-spectating", "{player}", target.getName()));
               break;
            case "create":
               if (!sender.hasPermission("hungergames.create")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               if (this.plugin.getArenaManager().arenaExists(name)) {
                  sender.sendMessage(this.msg.get("arena.already-exists", "{arena}", name));
                  return true;
               }

               this.plugin.getArenaManager().createArena(name);
               sender.sendMessage(this.msg.get("arena.created", "{arena}", name));
               break;
            case "delete":
               if (!sender.hasPermission("hungergames.delete")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               if (this.plugin.getGameManager().hasActiveGame(arena)) {
                  this.plugin.getGameManager().stopGame(arena);
               }

               this.plugin.getArenaManager().deleteArena(arena);
               sender.sendMessage(this.msg.get("arena.deleted", "{arena}", name));
               break;
            case "setspawn":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.setspawn")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 3) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               try {
                  int idx = Integer.parseInt(args[2]);
                  arena.setSpawnPoint(idx - 1, player.getLocation());
                  this.plugin.getArenaManager().saveArena(arena);
                  if (arena.isSetup()) {
                     arena.setState(ArenaState.WAITING);
                  }

                  sender.sendMessage(this.msg.get("arena.spawn-set", "{number}", String.valueOf(idx), "{arena}", name));
               } catch (NumberFormatException var16) {
                  sender.sendMessage(this.msg.get("general.invalid-number"));
               }
               break;
            case "setlobby":
               if (!this.checkPlayer(sender)) {
                  return true;
               }

               Player player = (Player)sender;
               if (!player.hasPermission("hungergames.setlobby")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               arena.setLobbyLocation(player.getLocation());
               this.plugin.getArenaManager().saveArena(arena);
               if (arena.isSetup()) {
                  arena.setState(ArenaState.WAITING);
               }

               sender.sendMessage(this.msg.get("arena.lobby-set", "{arena}", name));
               break;
            case "forcestart":
               if (!sender.hasPermission("hungergames.forcestart")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               if (!arena.isSetup()) {
                  sender.sendMessage(this.msg.get("arena.not-setup", "{arena}", name));
                  return true;
               }

               sender.sendMessage(this.msg.get("game.force-started", "{arena}", name));
               this.plugin.getGameManager().forceStartGame(arena);
               break;
            case "stop":
               if (!sender.hasPermission("hungergames.stop")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               if (!this.plugin.getGameManager().hasActiveGame(arena)) {
                  sender.sendMessage(this.msg.get("game.no-game", "{arena}", name));
                  return true;
               }

               Game game = this.plugin.getGameManager().getGame(arena);
               if (game != null) {
                  game.broadcastToAll(this.msg.getRaw("game.stopped-broadcast").replace("{arena}", name));
               }

               this.plugin.getGameManager().stopGame(arena);
               sender.sendMessage(this.msg.get("game.stopped", "{arena}", name));
               break;
            case "reload":
               if (!sender.hasPermission("hungergames.reload")) {
                  this.noPerms(sender);
                  return true;
               }

               this.plugin.reload();
               sender.sendMessage(this.msg.get("general.reloaded"));
               break;
            case "info":
               if (!sender.hasPermission("hungergames.info")) {
                  this.noPerms(sender);
                  return true;
               }

               if (args.length < 2) {
                  sender.sendMessage(this.msg.get("general.console-no-arena"));
                  return true;
               }

               String name = args[1];
               Arena arena = this.plugin.getArenaManager().getArena(name);
               if (arena == null) {
                  sender.sendMessage(this.msg.get("general.invalid-arena", "{arena}", name));
                  return true;
               }

               Game game = this.plugin.getGameManager().getGame(arena);
               int current = game != null ? game.getPlayers().size() : 0;
               sender.sendMessage(this.msg.getRaw("arena.info-header").replace("{arena}", name));
               sender.sendMessage(this.msg.getRaw("arena.info-status").replace("{status}", arena.getDisplayState()));
               sender.sendMessage(this.msg.getRaw("arena.info-players").replace("{current}", String.valueOf(current)).replace("{max}", String.valueOf(arena.getMaxPlayers())));
               sender.sendMessage(this.msg.getRaw("arena.info-spawns").replace("{spawns}", String.valueOf(arena.getSpawnPoints().size())));
               sender.sendMessage(this.msg.getRaw("arena.info-lobby").replace("{lobby}", arena.getLobbyLocation() != null ? "Yes" : "No"));
               sender.sendMessage(this.msg.getRaw("arena.info-footer"));
               break;
            default:
               sender.sendMessage(this.msg.get("general.unknown-command"));
         }

         return true;
      } else {
         this.sendHelp(sender);
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (args.length == 1) {
         List<String> subs = new ArrayList();
         if (sender.hasPermission("hungergames.join")) {
            subs.add("join");
         }

         if (sender.hasPermission("hungergames.leave")) {
            subs.add("leave");
         }

         if (sender.hasPermission("hungergames.list")) {
            subs.add("list");
         }

         if (sender.hasPermission("hungergames.stats")) {
            subs.add("stats");
         }

         if (sender.hasPermission("hungergames.spectate")) {
            subs.add("spectate");
         }

         subs.add("help");
         if (sender.hasPermission("hungergames.create")) {
            subs.add("create");
         }

         if (sender.hasPermission("hungergames.delete")) {
            subs.add("delete");
         }

         if (sender.hasPermission("hungergames.setspawn")) {
            subs.add("setspawn");
         }

         if (sender.hasPermission("hungergames.setlobby")) {
            subs.add("setlobby");
         }

         if (sender.hasPermission("hungergames.forcestart")) {
            subs.add("forcestart");
         }

         if (sender.hasPermission("hungergames.stop")) {
            subs.add("stop");
         }

         if (sender.hasPermission("hungergames.reload")) {
            subs.add("reload");
         }

         if (sender.hasPermission("hungergames.info")) {
            subs.add("info");
         }

         return this.filter(subs, args[0]);
      } else if (args.length == 2) {
         List var10000;
         switch (args[0].toLowerCase()) {
            case "join":
               var10000 = this.filter(this.plugin.getArenaManager().getAvailableArenaNames(), args[1]);
               break;
            case "delete":
            case "setspawn":
            case "setlobby":
            case "forcestart":
            case "info":
               var10000 = this.filter(this.plugin.getArenaManager().getArenaNames(), args[1]);
               break;
            case "stop":
               var10000 = this.filter(this.plugin.getArenaManager().getActiveArenaNames(), args[1]);
               break;
            case "spectate":
               List<String> alive = new ArrayList();

               for(Game game : this.plugin.getGameManager().getAllGames()) {
                  for(UUID uuid : game.getAlivePlayers()) {
                     Player p = Bukkit.getPlayer(uuid);
                     if (p != null) {
                        alive.add(p.getName());
                     }
                  }
               }

               var10000 = this.filter(alive, args[1]);
               break;
            default:
               var10000 = Collections.emptyList();
         }

         return var10000;
      } else if (args.length == 3 && args[0].equalsIgnoreCase("setspawn")) {
         List<String> nums = new ArrayList();
         Arena arena = this.plugin.getArenaManager().getArena(args[1]);
         int max = arena != null ? arena.getSpawnPoints().size() + 1 : 10;

         for(int i = 1; i <= max; ++i) {
            nums.add(String.valueOf(i));
         }

         return this.filter(nums, args[2]);
      } else {
         return Collections.emptyList();
      }
   }

   private void sendHelp(CommandSender sender) {
      sender.sendMessage(this.msg.getRaw("help.header"));
      sender.sendMessage(this.msg.getRaw("help.player-commands"));
      if (sender.hasPermission("hungergames.join")) {
         sender.sendMessage(this.msg.getRaw("help.join"));
      }

      if (sender.hasPermission("hungergames.leave")) {
         sender.sendMessage(this.msg.getRaw("help.leave"));
      }

      if (sender.hasPermission("hungergames.list")) {
         sender.sendMessage(this.msg.getRaw("help.list"));
      }

      if (sender.hasPermission("hungergames.stats")) {
         sender.sendMessage(this.msg.getRaw("help.stats"));
      }

      if (sender.hasPermission("hungergames.spectate")) {
         sender.sendMessage(this.msg.getRaw("help.spectate"));
      }

      if (sender.hasPermission("hungergames.admin")) {
         sender.sendMessage(this.msg.getRaw("help.admin-commands"));
         sender.sendMessage(this.msg.getRaw("help.create"));
         sender.sendMessage(this.msg.getRaw("help.delete"));
         sender.sendMessage(this.msg.getRaw("help.setspawn"));
         sender.sendMessage(this.msg.getRaw("help.setlobby"));
         sender.sendMessage(this.msg.getRaw("help.forcestart"));
         sender.sendMessage(this.msg.getRaw("help.stop"));
         sender.sendMessage(this.msg.getRaw("help.reload"));
         sender.sendMessage(this.msg.getRaw("help.info"));
      }

      sender.sendMessage(this.msg.getRaw("help.footer"));
   }

   private boolean checkPlayer(CommandSender sender) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.msg.get("general.player-only"));
         return false;
      } else {
         return true;
      }
   }

   private void noPerms(CommandSender sender) {
      sender.sendMessage(this.msg.get("general.no-permission"));
   }

   private List<String> filter(List<String> list, String prefix) {
      return prefix.isEmpty() ? list : (List)list.stream().filter((s) -> s.toLowerCase().startsWith(prefix.toLowerCase())).collect(Collectors.toList());
   }
}
