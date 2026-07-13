package com.darknetwork.hungergames.game;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.arena.Arena;
import com.darknetwork.hungergames.arena.ArenaState;
import com.darknetwork.hungergames.stats.PlayerStats;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Game {
   private final HungerGamesPlugin plugin;
   private final Arena arena;
   private GameState state;
   private final Set<UUID> players = new LinkedHashSet();
   private final Set<UUID> alivePlayers = new LinkedHashSet();
   private final Set<UUID> spectators = new LinkedHashSet();
   private final Map<UUID, Integer> killMap = new HashMap();
   private BukkitTask countdownTask;
   private BukkitTask graceTask;
   private BukkitTask gameTimerTask;
   private BukkitTask chestRefillTask;
   private int lobbyCountdown;
   private long gameStartTime;
   private long maxGameMillis;
   private final List<Location> shuffledSpawns = new ArrayList();

   public Game(HungerGamesPlugin plugin, Arena arena) {
      this.plugin = plugin;
      this.arena = arena;
      this.state = GameState.LOBBY;
      this.lobbyCountdown = plugin.getConfig().getInt("settings.lobby-countdown", 60);
      int maxMinutes = plugin.getConfig().getInt("settings.max-game-time", 20);
      this.maxGameMillis = maxMinutes > 0 ? (long)maxMinutes * 60000L : -1L;
   }

   public boolean addPlayer(Player player) {
      if (this.players.size() >= this.arena.getMaxPlayers()) {
         return false;
      } else {
         this.players.add(player.getUniqueId());
         this.killMap.put(player.getUniqueId(), 0);
         if (this.arena.getLobbyLocation() != null) {
            player.teleport(this.arena.getLobbyLocation());
         }

         player.setGameMode(GameMode.ADVENTURE);
         player.getInventory().clear();
         player.setHealth(player.getMaxHealth());
         player.setFoodLevel(20);
         player.getActivePotionEffects().forEach((e) -> player.removePotionEffect(e.getType()));
         return true;
      }
   }

   public void removePlayer(Player player) {
      UUID uuid = player.getUniqueId();
      this.players.remove(uuid);
      this.alivePlayers.remove(uuid);
      this.spectators.remove(uuid);
      this.killMap.remove(uuid);
      this.plugin.getScoreboardManager().removeBoard(player);
      player.setGameMode(GameMode.SURVIVAL);
      player.getInventory().clear();
      player.setHealth(player.getMaxHealth());
      player.setFoodLevel(20);
      player.setAllowFlight(false);
      player.setFlying(false);
      player.getActivePotionEffects().forEach((e) -> player.removePotionEffect(e.getType()));
   }

   public void startLobbyCountdown() {
      final int minPlayers = this.plugin.getConfig().getInt("settings.min-players", 2);
      final MessageUtil msg = this.plugin.getMessageUtil();
      this.countdownTask = (new BukkitRunnable() {
         int seconds;

         {
            this.seconds = Game.this.lobbyCountdown;
         }

         public void run() {
            if (Game.this.players.size() < minPlayers) {
               this.cancel();
               Game.this.broadcastToAll(msg.getRaw("lobby.countdown-cancel"));
               Game.this.lobbyCountdown = Game.this.plugin.getConfig().getInt("settings.lobby-countdown", 60);
            } else if (this.seconds <= 0) {
               this.cancel();
               Game.this.startGame();
            } else {
               if (this.seconds == 60 || this.seconds == 30 || this.seconds == 15 || this.seconds == 10 || this.seconds == 5 || this.seconds == 4 || this.seconds == 3 || this.seconds == 2 || this.seconds == 1) {
                  String countdownMsg = msg.getRaw("lobby.countdown-broadcast").replace("{seconds}", String.valueOf(this.seconds));
                  Game.this.broadcastToAll(countdownMsg);
                  Game.this.playCountdownSound(this.seconds);
               }

               if (this.seconds <= 5) {
                  for(UUID uuid : Game.this.players) {
                     Player p = Bukkit.getPlayer(uuid);
                     if (p != null) {
                        p.sendTitle(MessageUtil.colorize("&#001CFF&l" + this.seconds), MessageUtil.colorize("&7seconds remaining..."), 5, 15, 5);
                     }
                  }
               }

               --this.seconds;
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private void playCountdownSound(int seconds) {
      Sound sound = seconds <= 5 ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_HAT;

      for(UUID uuid : this.players) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            p.playSound(p.getLocation(), sound, 1.0F, seconds <= 5 ? 2.0F : 1.0F);
         }
      }

   }

   public void startGame() {
      this.state = GameState.GRACE_PERIOD;
      this.arena.setState(ArenaState.IN_GAME);
      this.alivePlayers.addAll(this.players);
      this.gameStartTime = System.currentTimeMillis();
      this.shuffledSpawns.clear();
      this.shuffledSpawns.addAll(this.arena.getSpawnPoints());
      Collections.shuffle(this.shuffledSpawns);
      int spawnIdx = 0;

      for(UUID uuid : this.alivePlayers) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            Location spawn = spawnIdx < this.shuffledSpawns.size() ? (Location)this.shuffledSpawns.get(spawnIdx++) : (Location)this.shuffledSpawns.get(0);
            p.teleport(spawn);
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, true, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 255, true, false));
            this.giveStartingItems(p);
            this.plugin.getScoreboardManager().giveBoard(p, this);
         }
      }

      MessageUtil msg = this.plugin.getMessageUtil();
      String startMsg1 = msg.getRaw("game.starting");
      String startMsg2 = msg.getRaw("game.starting-line2").replace("{arena}", this.arena.getName()).replace("{players}", String.valueOf(this.alivePlayers.size()));
      String startMsg3 = msg.getRaw("game.starting-line3");
      this.broadcastToAll(startMsg1);
      this.broadcastToAll(startMsg2);
      this.broadcastToAll(startMsg3);

      for(UUID uuid : this.alivePlayers) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            p.playSound(p.getLocation(), Sound.MUSIC_DISC_FAR, 0.5F, 1.0F);
         }
      }

      int graceSecs = this.plugin.getConfig().getInt("settings.grace-period", 30);
      String graceMsg = msg.getRaw("game.grace-period").replace("{seconds}", String.valueOf(graceSecs));
      this.broadcastToAll(graceMsg);
      this.graceTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
         this.state = GameState.ACTIVE;

         for(UUID uuid : this.alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
               p.removePotionEffect(PotionEffectType.SLOWNESS);
               p.removePotionEffect(PotionEffectType.BLINDNESS);
               p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5F, 1.5F);
            }
         }

         this.broadcastToAll(msg.getRaw("game.pvp-enabled"));
      }, (long)graceSecs * 20L);
      if (this.maxGameMillis > 0L) {
         long ticksMax = this.maxGameMillis / 50L;
         this.gameTimerTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.endGame((Player)null), ticksMax);
      }

      int refillMins = this.plugin.getConfig().getInt("settings.chest-refill-time", 10);
      if (refillMins > 0) {
         this.chestRefillTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.broadcastToAll(msg.getRaw("game.chest-refill"));

            for(UUID uuid : this.alivePlayers) {
               Player p = Bukkit.getPlayer(uuid);
               if (p != null) {
                  p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F);
               }
            }

         }, (long)(refillMins * 60) * 20L);
      }

      this.updateTablist();
   }

   private void giveStartingItems(Player player) {
      if (this.plugin.getConfig().getBoolean("starting-items.enabled", false)) {
         ConfigurationSection items = this.plugin.getConfig().getConfigurationSection("starting-items.items");
         if (items != null) {
            for(String key : items.getKeys(false)) {
               String matStr = this.plugin.getConfig().getString("starting-items.items." + key + ".material", "AIR");
               int amount = this.plugin.getConfig().getInt("starting-items.items." + key + ".amount", 1);
               Material mat = Material.getMaterial(matStr);
               if (mat != null) {
                  player.getInventory().addItem(new ItemStack[]{new ItemStack(mat, amount)});
               }
            }

         }
      }
   }

   public void eliminatePlayer(Player player, Player killer) {
      UUID uuid = player.getUniqueId();
      if (this.alivePlayers.contains(uuid)) {
         this.alivePlayers.remove(uuid);
         this.spectators.add(uuid);
         PlayerStats stats = this.plugin.getStatsManager().getStats(player);
         stats.addDeath();
         stats.addGame();
         this.plugin.getStatsManager().saveStats(stats);
         if (killer != null && !killer.equals(player)) {
            this.killMap.merge(killer.getUniqueId(), 1, Integer::sum);
            PlayerStats killerStats = this.plugin.getStatsManager().getStats(killer);
            killerStats.addKill();
            this.plugin.getStatsManager().saveStats(killerStats);
         }

         MessageUtil msg = this.plugin.getMessageUtil();
         String elimMsg = killer != null ? msg.getRaw("elimination.killed-by").replace("{player}", player.getName()).replace("{killer}", killer.getName()).replace("{alive}", String.valueOf(this.alivePlayers.size())) : msg.getRaw("elimination.killed").replace("{player}", player.getName()).replace("{alive}", String.valueOf(this.alivePlayers.size()));
         this.broadcastToAll(elimMsg);
         this.makeSpectator(player);
         player.sendMessage(msg.get("elimination.became-spectator"));
         this.checkWinCondition();
      }
   }

   private void makeSpectator(Player player) {
      player.setGameMode(GameMode.SPECTATOR);
      player.setAllowFlight(true);
      player.setFlying(true);
      this.plugin.getScoreboardManager().giveBoard(player, this);
      this.updateTablist();
   }

   public void makeSpectatorFromCommand(Player player) {
      this.spectators.add(player.getUniqueId());
      this.players.add(player.getUniqueId());
      player.teleport(this.arena.getLobbyLocation() != null ? this.arena.getLobbyLocation() : player.getLocation());
      this.makeSpectator(player);
      this.plugin.getMessageUtil().send(player, "spectator.enabled");
   }

   private void checkWinCondition() {
      if (this.alivePlayers.size() == 1) {
         UUID winnerUUID = (UUID)this.alivePlayers.iterator().next();
         Player winner = Bukkit.getPlayer(winnerUUID);
         this.endGame(winner);
      } else if (this.alivePlayers.isEmpty()) {
         this.endGame((Player)null);
      }

   }

   public void endGame(Player winner) {
      this.state = GameState.ENDING;
      this.cancelTimers();
      MessageUtil msg = this.plugin.getMessageUtil();
      if (winner != null) {
         int kills = (Integer)this.killMap.getOrDefault(winner.getUniqueId(), 0);
         this.broadcastToAll(msg.getRaw("end.winner"));
         this.broadcastToAll(msg.getRaw("end.winner-name").replace("{winner}", winner.getName()));
         this.broadcastToAll(msg.getRaw("end.winner-line3"));
         winner.sendMessage(msg.getRaw("end.winner-kills").replace("{kills}", String.valueOf(kills)));
         PlayerStats stats = this.plugin.getStatsManager().getStats(winner);
         stats.addWin();
         stats.addGame();
         this.plugin.getStatsManager().saveStats(stats);

         for(UUID uuid : this.players) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
               p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            }
         }

         this.giveRewards(winner);

         for(String cmd : this.plugin.getConfig().getStringList("rewards.commands")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", winner.getName()));
         }
      } else {
         this.broadcastToAll(msg.getRaw("end.no-winner"));
      }

      Bukkit.getScheduler().runTaskLater(this.plugin, this::reset, 100L);
   }

   private void giveRewards(Player winner) {
      if (this.plugin.getConfig().getBoolean("rewards.enabled", true)) {
         if (this.plugin.hasEconomy()) {
            double amount = this.plugin.getConfig().getDouble("rewards.vault-reward", (double)500.0F);
            Economy eco = this.plugin.getEconomy();
            eco.depositPlayer(winner, amount);
            winner.sendMessage(this.plugin.getMessageUtil().get("end.reward-given").replace("{amount}", String.format("%.0f", amount)));
         }
      }
   }

   private void reset() {
      Set<UUID> allUUIDs = new HashSet(this.players);

      for(UUID uuid : allUUIDs) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            this.removePlayer(p);
         }
      }

      this.players.clear();
      this.alivePlayers.clear();
      this.spectators.clear();
      this.killMap.clear();
      this.arena.setState(ArenaState.WAITING);
      this.state = GameState.LOBBY;
      this.lobbyCountdown = this.plugin.getConfig().getInt("settings.lobby-countdown", 60);
      this.gameStartTime = 0L;
      this.countdownTask = null;
      this.plugin.getGameManager().endGame(this.arena.getName(), allUUIDs);
   }

   public Set<UUID> forceStop() {
      this.cancelTimers();
      Set<UUID> allUUIDs = new HashSet(this.players);

      for(UUID uuid : allUUIDs) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            this.removePlayer(p);
         }
      }

      this.players.clear();
      this.alivePlayers.clear();
      this.spectators.clear();
      this.killMap.clear();
      this.killMap.clear();
      this.countdownTask = null;
      this.arena.setState(ArenaState.WAITING);
      return allUUIDs;
   }

   private void cancelTimers() {
      if (this.countdownTask != null) {
         this.countdownTask.cancel();
      }

      if (this.graceTask != null) {
         this.graceTask.cancel();
      }

      if (this.gameTimerTask != null) {
         this.gameTimerTask.cancel();
      }

      if (this.chestRefillTask != null) {
         this.chestRefillTask.cancel();
      }

   }

   private void updateTablist() {
      if (this.plugin.getConfig().getBoolean("tablist.enabled", true)) {
         ;
      }
   }

   public void broadcastToAll(String message) {
      for(UUID uuid : this.players) {
         Player p = Bukkit.getPlayer(uuid);
         if (p != null) {
            p.sendMessage(message);
         }
      }

   }

   public Arena getArena() {
      return this.arena;
   }

   public GameState getState() {
      return this.state;
   }

   public Set<UUID> getPlayers() {
      return this.players;
   }

   public Set<UUID> getAlivePlayers() {
      return this.alivePlayers;
   }

   public Set<UUID> getSpectators() {
      return this.spectators;
   }

   public boolean isAlive(UUID uuid) {
      return this.alivePlayers.contains(uuid);
   }

   public boolean isSpectator(UUID uuid) {
      return this.spectators.contains(uuid);
   }

   public boolean contains(UUID uuid) {
      return this.players.contains(uuid);
   }

   public int getKills(UUID uuid) {
      return (Integer)this.killMap.getOrDefault(uuid, 0);
   }

   public boolean isCountdownRunning() {
      return this.countdownTask != null && !this.countdownTask.isCancelled();
   }

   public long getSecondsRemaining() {
      if (this.maxGameMillis > 0L && this.gameStartTime != 0L) {
         long elapsed = System.currentTimeMillis() - this.gameStartTime;
         return Math.max(0L, (this.maxGameMillis - elapsed) / 1000L);
      } else {
         return -1L;
      }
   }

   public String getStateDisplay() {
      String var10000;
      switch (this.state) {
         case LOBBY -> var10000 = "Lobby";
         case GRACE_PERIOD -> var10000 = "Grace Period";
         case ACTIVE -> var10000 = "Active";
         case ENDING -> var10000 = "Ending";
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
