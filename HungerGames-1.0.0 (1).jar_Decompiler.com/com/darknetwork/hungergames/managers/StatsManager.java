package com.darknetwork.hungergames.managers;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.stats.PlayerStats;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class StatsManager {
   private final HungerGamesPlugin plugin;
   private final Map<UUID, PlayerStats> statsCache = new HashMap();
   private File statsFile;
   private FileConfiguration statsConfig;

   public StatsManager(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      this.load();
   }

   private void load() {
      this.statsFile = new File(this.plugin.getDataFolder(), "stats.yml");
      if (!this.statsFile.exists()) {
         try {
            this.statsFile.createNewFile();
         } catch (IOException var2) {
         }
      }

      this.statsConfig = YamlConfiguration.loadConfiguration(this.statsFile);
   }

   public PlayerStats getStats(Player player) {
      return (PlayerStats)this.statsCache.computeIfAbsent(player.getUniqueId(), (uuid) -> this.loadStats(uuid, player.getName()));
   }

   public PlayerStats getStats(UUID uuid, String name) {
      return (PlayerStats)this.statsCache.computeIfAbsent(uuid, (u) -> this.loadStats(u, name));
   }

   private PlayerStats loadStats(UUID uuid, String name) {
      String path = "stats." + String.valueOf(uuid);
      if (!this.statsConfig.contains(path)) {
         return new PlayerStats(uuid, name);
      } else {
         PlayerStats s = new PlayerStats(uuid, this.statsConfig.getString(path + ".name", name));
         s.setWins(this.statsConfig.getInt(path + ".wins", 0));
         s.setKills(this.statsConfig.getInt(path + ".kills", 0));
         s.setDeaths(this.statsConfig.getInt(path + ".deaths", 0));
         s.setGamesPlayed(this.statsConfig.getInt(path + ".games-played", 0));
         return s;
      }
   }

   public void saveStats(PlayerStats stats) {
      String path = "stats." + String.valueOf(stats.getUuid());
      this.statsConfig.set(path + ".name", stats.getName());
      this.statsConfig.set(path + ".wins", stats.getWins());
      this.statsConfig.set(path + ".kills", stats.getKills());
      this.statsConfig.set(path + ".deaths", stats.getDeaths());
      this.statsConfig.set(path + ".games-played", stats.getGamesPlayed());
      String yamlSnapshot = this.statsConfig.saveToString();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            FileWriter fw = new FileWriter(this.statsFile);

            try {
               fw.write(yamlSnapshot);
            } catch (Throwable var6) {
               try {
                  fw.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }

               throw var6;
            }

            fw.close();
         } catch (IOException var7) {
         }

      });
   }

   public void saveAll() {
      for(PlayerStats stats : this.statsCache.values()) {
         String path = "stats." + String.valueOf(stats.getUuid());
         this.statsConfig.set(path + ".name", stats.getName());
         this.statsConfig.set(path + ".wins", stats.getWins());
         this.statsConfig.set(path + ".kills", stats.getKills());
         this.statsConfig.set(path + ".deaths", stats.getDeaths());
         this.statsConfig.set(path + ".games-played", stats.getGamesPlayed());
      }

      try {
         this.statsConfig.save(this.statsFile);
      } catch (IOException var4) {
      }

   }
}
