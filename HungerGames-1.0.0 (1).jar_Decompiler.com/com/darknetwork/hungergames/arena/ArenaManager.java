package com.darknetwork.hungergames.arena;

import com.darknetwork.hungergames.HungerGamesPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ArenaManager {
   private final HungerGamesPlugin plugin;
   private final Map<String, Arena> arenas = new HashMap();
   private File arenasFile;
   private FileConfiguration arenasConfig;
   private final Logger log;

   public ArenaManager(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      this.log = plugin.getLogger();
   }

   public void loadArenas() {
      this.arenas.clear();
      this.arenasFile = new File(this.plugin.getDataFolder(), "arenas.yml");
      if (!this.arenasFile.exists()) {
         try {
            this.arenasFile.createNewFile();
         } catch (IOException var10) {
            this.log.warning("[HG] Could not create arenas.yml");
         }
      }

      this.arenasConfig = YamlConfiguration.loadConfiguration(this.arenasFile);
      ConfigurationSection section = this.arenasConfig.getConfigurationSection("arenas");
      if (section != null) {
         for(String name : section.getKeys(false)) {
            Arena arena = new Arena(name);
            String path = "arenas." + name + ".";
            if (this.arenasConfig.contains(path + "lobby")) {
               arena.setLobbyLocation(this.deserializeLocation(this.arenasConfig.getConfigurationSection(path + "lobby")));
            }

            arena.setMinPlayers(this.arenasConfig.getInt(path + "min-players", this.plugin.getConfig().getInt("settings.min-players", 2)));
            arena.setMaxPlayers(this.arenasConfig.getInt(path + "max-players", this.plugin.getConfig().getInt("settings.max-players", 24)));
            ConfigurationSection spawns = this.arenasConfig.getConfigurationSection(path + "spawns");
            if (spawns != null) {
               for(String key : spawns.getKeys(false)) {
                  Location loc = this.deserializeLocation(spawns.getConfigurationSection(key));
                  if (loc != null) {
                     arena.addSpawnPoint(loc);
                  }
               }
            }

            arena.setState(arena.isSetup() ? ArenaState.WAITING : ArenaState.SETUP);
            this.arenas.put(name.toLowerCase(), arena);
            this.log.info("[HG] Loaded arena: " + name);
         }

         this.log.info("[HG] Loaded " + this.arenas.size() + " arena(s).");
      }
   }

   public void saveArena(Arena arena) {
      String path = "arenas." + arena.getName() + ".";
      this.arenasConfig.set(path + "min-players", arena.getMinPlayers());
      this.arenasConfig.set(path + "max-players", arena.getMaxPlayers());
      if (arena.getLobbyLocation() != null) {
         this.serializeLocation(this.arenasConfig, path + "lobby", arena.getLobbyLocation());
      }

      this.arenasConfig.set(path + "spawns", (Object)null);
      List<Location> spawns = arena.getSpawnPoints();

      for(int i = 0; i < spawns.size(); ++i) {
         this.serializeLocation(this.arenasConfig, path + "spawns." + i, (Location)spawns.get(i));
      }

      String snapshot = this.arenasConfig.saveToString();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            FileWriter fw = new FileWriter(this.arenasFile);

            try {
               fw.write(snapshot);
            } catch (Throwable var7) {
               try {
                  fw.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            fw.close();
         } catch (IOException var8) {
            this.log.warning("[HG] Failed to save arena: " + arena.getName());
         }

      });
   }

   public void deleteArena(Arena arena) {
      this.arenas.remove(arena.getName().toLowerCase());
      this.arenasConfig.set("arenas." + arena.getName(), (Object)null);
      String snapshot = this.arenasConfig.saveToString();
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            FileWriter fw = new FileWriter(this.arenasFile);

            try {
               fw.write(snapshot);
            } catch (Throwable var7) {
               try {
                  fw.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            fw.close();
         } catch (IOException var8) {
            this.log.warning("[HG] Failed to save after deleting arena: " + arena.getName());
         }

      });
   }

   public Arena createArena(String name) {
      Arena arena = new Arena(name);
      this.arenas.put(name.toLowerCase(), arena);
      this.arenasConfig.set("arenas." + name + ".min-players", arena.getMinPlayers());
      this.arenasConfig.set("arenas." + name + ".max-players", arena.getMaxPlayers());
      Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
         try {
            this.arenasConfig.save(this.arenasFile);
         } catch (IOException var2) {
         }

      });
      return arena;
   }

   public Arena getArena(String name) {
      return (Arena)this.arenas.get(name.toLowerCase());
   }

   public boolean arenaExists(String name) {
      return this.arenas.containsKey(name.toLowerCase());
   }

   public Collection<Arena> getAllArenas() {
      return this.arenas.values();
   }

   public List<String> getArenaNames() {
      return new ArrayList(this.arenas.keySet());
   }

   public List<String> getAvailableArenaNames() {
      List<String> list = new ArrayList();

      for(Arena a : this.arenas.values()) {
         if (a.isAvailable()) {
            list.add(a.getName());
         }
      }

      return list;
   }

   public List<String> getActiveArenaNames() {
      List<String> list = new ArrayList();

      for(Arena a : this.arenas.values()) {
         if (a.getState() == ArenaState.IN_GAME || a.getState() == ArenaState.LOBBY) {
            list.add(a.getName());
         }
      }

      return list;
   }

   private void serializeLocation(FileConfiguration cfg, String path, Location loc) {
      cfg.set(path + ".world", loc.getWorld() != null ? loc.getWorld().getName() : "");
      cfg.set(path + ".x", loc.getX());
      cfg.set(path + ".y", loc.getY());
      cfg.set(path + ".z", loc.getZ());
      cfg.set(path + ".yaw", loc.getYaw());
      cfg.set(path + ".pitch", loc.getPitch());
   }

   private Location deserializeLocation(ConfigurationSection sec) {
      if (sec == null) {
         return null;
      } else {
         World world = Bukkit.getWorld(sec.getString("world", ""));
         if (world == null) {
            return null;
         } else {
            double x = sec.getDouble("x");
            double y = sec.getDouble("y");
            double z = sec.getDouble("z");
            float yaw = (float)sec.getDouble("yaw");
            float pitch = (float)sec.getDouble("pitch");
            return new Location(world, x, y, z, yaw, pitch);
         }
      }
   }
}
