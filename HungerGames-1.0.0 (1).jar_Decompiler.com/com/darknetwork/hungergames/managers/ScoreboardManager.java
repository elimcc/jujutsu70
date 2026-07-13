package com.darknetwork.hungergames.managers;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {
   private final HungerGamesPlugin plugin;
   private final Map<UUID, Scoreboard> playerBoards = new HashMap();
   private int taskId = -1;

   public ScoreboardManager(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      this.startUpdater();
   }

   private void startUpdater() {
      int interval = this.plugin.getConfig().getInt("scoreboard.update-interval", 20);
      this.taskId = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
         for(UUID uuid : this.playerBoards.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
               Game game = this.plugin.getGameManager().getGame(p);
               if (game != null) {
                  this.updateBoard(p, game);
               }
            }
         }

      }, 0L, (long)interval).getTaskId();
   }

   public void giveBoard(Player player, Game game) {
      if (this.plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
         if (Bukkit.getScoreboardManager() != null) {
            ScoreboardManager var10000 = (ScoreboardManager)null;
         } else {
            ScoreboardManager var8 = null;
         }

         org.bukkit.scoreboard.ScoreboardManager sbm = Bukkit.getScoreboardManager();
         Scoreboard board = sbm.getNewScoreboard();
         String title = MessageUtil.colorize(this.plugin.getConfig().getString("scoreboard.title", "&#001CFF&lHUNGERGAMES"));
         Objective obj = board.registerNewObjective("hg_" + player.getUniqueId().toString().substring(0, 8), Criteria.DUMMY, title);
         obj.setDisplaySlot(DisplaySlot.SIDEBAR);
         this.playerBoards.put(player.getUniqueId(), board);
         player.setScoreboard(board);
         this.updateBoard(player, game);
      }
   }

   public void updateBoard(Player player, Game game) {
      Scoreboard board = (Scoreboard)this.playerBoards.get(player.getUniqueId());
      if (board != null) {
         String var10001 = player.getUniqueId().toString();
         Objective obj = board.getObjective("hg_" + var10001.substring(0, 8));
         if (obj != null) {
            for(String entry : board.getEntries()) {
               board.resetScores(entry);
            }

            int line = 10;
            this.setScore(board, obj, " ", line--);
            this.setScore(board, obj, MessageUtil.colorize("&7Arena: &f" + game.getArena().getName()), line--);
            this.setScore(board, obj, MessageUtil.colorize("&7Status: &f" + game.getStateDisplay()), line--);
            this.setScore(board, obj, "  ", line--);
            this.setScore(board, obj, MessageUtil.colorize("&7Players Alive: &#001CFF&l" + game.getAlivePlayers().size()), line--);
            this.setScore(board, obj, "   ", line--);
            long secsLeft = game.getSecondsRemaining();
            String timeStr = secsLeft < 0L ? "∞" : this.formatTime(secsLeft);
            this.setScore(board, obj, MessageUtil.colorize("&7Time: &f" + timeStr), line--);
            this.setScore(board, obj, "    ", line--);
            int kills = game.getKills(player.getUniqueId());
            this.setScore(board, obj, MessageUtil.colorize("&7Kills: &#001CFF&l" + kills), line--);
            this.setScore(board, obj, "     ", line--);
            this.setScore(board, obj, MessageUtil.colorize("&7darknetwork.com"), line--);
         }
      }
   }

   private void setScore(Scoreboard board, Objective obj, String text, int score) {
      while(board.getEntries().contains(text)) {
         text = text + "§r";
      }

      Score s = obj.getScore(text);
      s.setScore(score);
   }

   public void removeBoard(Player player) {
      this.playerBoards.remove(player.getUniqueId());
      player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
   }

   private String formatTime(long totalSeconds) {
      long min = totalSeconds / 60L;
      long sec = totalSeconds % 60L;
      return String.format("%02d:%02d", min, sec);
   }

   public void stopUpdater() {
      if (this.taskId != -1) {
         Bukkit.getScheduler().cancelTask(this.taskId);
      }

   }
}
