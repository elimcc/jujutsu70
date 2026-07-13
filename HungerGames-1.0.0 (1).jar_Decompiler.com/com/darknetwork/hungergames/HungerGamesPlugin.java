package com.darknetwork.hungergames;

import com.darknetwork.hungergames.arena.ArenaManager;
import com.darknetwork.hungergames.commands.HungerGamesCommand;
import com.darknetwork.hungergames.game.GameManager;
import com.darknetwork.hungergames.listeners.GameListener;
import com.darknetwork.hungergames.listeners.PlayerListener;
import com.darknetwork.hungergames.managers.ScoreboardManager;
import com.darknetwork.hungergames.managers.StatsManager;
import com.darknetwork.hungergames.placeholders.HungerGamesExpansion;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class HungerGamesPlugin extends JavaPlugin {
   private static HungerGamesPlugin instance;
   private ArenaManager arenaManager;
   private GameManager gameManager;
   private StatsManager statsManager;
   private ScoreboardManager scoreboardManager;
   private MessageUtil messageUtil;
   private Economy economy;
   private final Logger log = Bukkit.getLogger();

   public void onEnable() {
      instance = this;
      this.saveDefaultConfig();
      this.saveResource("messages.yml", false);
      this.messageUtil = new MessageUtil(this);
      this.arenaManager = new ArenaManager(this);
      this.gameManager = new GameManager(this);
      this.statsManager = new StatsManager(this);
      this.scoreboardManager = new ScoreboardManager(this);
      this.arenaManager.loadArenas();
      HungerGamesCommand cmd = new HungerGamesCommand(this);
      this.getCommand("hungergames").setExecutor(cmd);
      this.getCommand("hungergames").setTabCompleter(cmd);
      this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
      this.getServer().getPluginManager().registerEvents(new GameListener(this), this);
      if (this.setupEconomy()) {
         this.log.info("[HungerGames] Vault economy hooked successfully.");
      } else {
         this.log.info("[HungerGames] Vault not found — economy rewards disabled.");
      }

      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         (new HungerGamesExpansion(this)).register();
         this.log.info("[HungerGames] PlaceholderAPI hooked successfully.");
      }

      this.log.info("[HungerGames] HungerGames v" + this.getDescription().getVersion() + " has been enabled.");
   }

   public void onDisable() {
      if (this.gameManager != null) {
         this.gameManager.stopAllGames();
      }

      if (this.statsManager != null) {
         this.statsManager.saveAll();
      }

      this.log.info("[HungerGames] HungerGames has been disabled.");
   }

   private boolean setupEconomy() {
      if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
         return false;
      } else {
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
            return false;
         } else {
            this.economy = (Economy)rsp.getProvider();
            return this.economy != null;
         }
      }
   }

   public void reload() {
      this.reloadConfig();
      this.messageUtil.reload();
      this.arenaManager.loadArenas();
   }

   public static HungerGamesPlugin getInstance() {
      return instance;
   }

   public ArenaManager getArenaManager() {
      return this.arenaManager;
   }

   public GameManager getGameManager() {
      return this.gameManager;
   }

   public StatsManager getStatsManager() {
      return this.statsManager;
   }

   public ScoreboardManager getScoreboardManager() {
      return this.scoreboardManager;
   }

   public MessageUtil getMessageUtil() {
      return this.messageUtil;
   }

   public Economy getEconomy() {
      return this.economy;
   }

   public boolean hasEconomy() {
      return this.economy != null;
   }
}
