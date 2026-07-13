package com.darknetwork.hungergames.game;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.arena.Arena;
import com.darknetwork.hungergames.arena.ArenaState;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class GameManager {
   private final HungerGamesPlugin plugin;
   private final Map<String, Game> activeGames = new HashMap();
   private final Map<UUID, String> playerGameMap = new HashMap();

   public GameManager(HungerGamesPlugin plugin) {
      this.plugin = plugin;
   }

   public boolean joinGame(Player player, Arena arena) {
      if (this.isInGame(player)) {
         return false;
      } else if (!arena.isAvailable()) {
         return false;
      } else {
         Game game = (Game)this.activeGames.computeIfAbsent(arena.getName().toLowerCase(), (k) -> new Game(this.plugin, arena));
         if (!game.addPlayer(player)) {
            return false;
         } else {
            this.playerGameMap.put(player.getUniqueId(), arena.getName().toLowerCase());
            arena.setState(ArenaState.LOBBY);
            int minPlayers = this.plugin.getConfig().getInt("settings.min-players", 2);
            if (game.getPlayers().size() >= minPlayers && !game.isCountdownRunning()) {
               game.startLobbyCountdown();
            }

            return true;
         }
      }
   }

   public void leaveGame(Player player) {
      String arenaKey = (String)this.playerGameMap.remove(player.getUniqueId());
      if (arenaKey != null) {
         Game game = (Game)this.activeGames.get(arenaKey);
         if (game != null) {
            if (game.isAlive(player.getUniqueId())) {
               game.eliminatePlayer(player, (Player)null);
            }

            game.removePlayer(player);
            if (game.getPlayers().isEmpty()) {
               this.activeGames.remove(arenaKey);
               game.getArena().setState(ArenaState.WAITING);
            }

         }
      }
   }

   public void forceStartGame(Arena arena) {
      Game game = (Game)this.activeGames.get(arena.getName().toLowerCase());
      if (game == null) {
         game = new Game(this.plugin, arena);
         this.activeGames.put(arena.getName().toLowerCase(), game);
      }

      if (game.getState() == GameState.LOBBY) {
         game.startGame();
      }

   }

   public void stopGame(Arena arena) {
      Game game = (Game)this.activeGames.remove(arena.getName().toLowerCase());
      if (game != null) {
         for(UUID uuid : game.forceStop()) {
            this.playerGameMap.remove(uuid);
         }
      }

   }

   public void endGame(String arenaName, Set<UUID> allUUIDs) {
      this.activeGames.remove(arenaName.toLowerCase());

      for(UUID uuid : allUUIDs) {
         this.playerGameMap.remove(uuid);
      }

   }

   public void endGame(String arenaName) {
      this.activeGames.remove(arenaName.toLowerCase());
   }

   public boolean isInGame(Player player) {
      return this.playerGameMap.containsKey(player.getUniqueId());
   }

   public Game getGame(Player player) {
      String key = (String)this.playerGameMap.get(player.getUniqueId());
      return key != null ? (Game)this.activeGames.get(key) : null;
   }

   public Game getGame(Arena arena) {
      return (Game)this.activeGames.get(arena.getName().toLowerCase());
   }

   public boolean hasActiveGame(Arena arena) {
      return this.activeGames.containsKey(arena.getName().toLowerCase());
   }

   public Collection<Game> getAllGames() {
      return this.activeGames.values();
   }

   public void stopAllGames() {
      for(Game game : this.activeGames.values()) {
         game.forceStop();
      }

      this.activeGames.clear();
      this.playerGameMap.clear();
   }
}
