package com.darknetwork.hungergames.placeholders;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.stats.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HungerGamesExpansion extends PlaceholderExpansion {
   private final HungerGamesPlugin plugin;

   public HungerGamesExpansion(HungerGamesPlugin plugin) {
      this.plugin = plugin;
   }

   public @NotNull String getIdentifier() {
      return "hg";
   }

   public @NotNull String getAuthor() {
      return "DarkNetwork";
   }

   public @NotNull String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public boolean canRegister() {
      return true;
   }

   public String onPlaceholderRequest(Player player, @NotNull String identifier) {
      if (player == null) {
         return "";
      } else {
         PlayerStats stats = this.plugin.getStatsManager().getStats(player);
         switch (identifier) {
            case "wins":
               return String.valueOf(stats.getWins());
            case "kills":
               return String.valueOf(stats.getKills());
            case "deaths":
               return String.valueOf(stats.getDeaths());
            case "games_played":
               return String.valueOf(stats.getGamesPlayed());
            case "kdr":
               return String.valueOf(stats.getKDR());
            default:
               Game game = this.plugin.getGameManager().getGame(player);
               if (game == null) {
                  String var10;
                  switch (identifier) {
                     case "arena" -> var10 = "N/A";
                     case "status" -> var10 = "Not in game";
                     case "players_alive" -> var10 = "0";
                     case "time_remaining" -> var10 = "-";
                     case "in_game" -> var10 = "false";
                     case "game_kills" -> var10 = "0";
                     default -> var10 = null;
                  }

                  return var10;
               } else {
                  String var10000;
                  switch (identifier) {
                     case "arena":
                        var10000 = game.getArena().getName();
                        break;
                     case "status":
                        var10000 = game.getStateDisplay();
                        break;
                     case "players_alive":
                        var10000 = String.valueOf(game.getAlivePlayers().size());
                        break;
                     case "time_remaining":
                        long s = game.getSecondsRemaining();
                        var10000 = s < 0L ? "∞" : String.format("%02d:%02d", s / 60L, s % 60L);
                        break;
                     case "in_game":
                        var10000 = "true";
                        break;
                     case "game_kills":
                        var10000 = String.valueOf(game.getKills(player.getUniqueId()));
                        break;
                     default:
                        var10000 = null;
                  }

                  return var10000;
               }
         }
      }
   }
}
