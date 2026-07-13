package com.darknetwork.hungergames.listeners;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.game.GameState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerListener implements Listener {
   private final HungerGamesPlugin plugin;

   public PlayerListener(HungerGamesPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.NORMAL
   )
   public void onQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (this.plugin.getGameManager().isInGame(player)) {
         this.plugin.getGameManager().leaveGame(player);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      if (this.plugin.getGameManager().isInGame(player)) {
         Game game = this.plugin.getGameManager().getGame(player);
         if (game != null) {
            event.setKeepInventory(true);
            event.setDroppedExp(0);
            event.getDrops().clear();
            event.setDeathMessage((String)null);
            Player killer = player.getKiller();
            game.eliminatePlayer(player, killer);
         }
      }
   }

   @EventHandler
   public void onRespawn(PlayerRespawnEvent event) {
      Player player = event.getPlayer();
      if (this.plugin.getGameManager().isInGame(player)) {
         Game game = this.plugin.getGameManager().getGame(player);
         if (game != null) {
            if (game.getArena().getLobbyLocation() != null) {
               event.setRespawnLocation(game.getArena().getLobbyLocation());
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageByEntityEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player victim) {
         Player var8 = null;
         Entity var7 = event.getDamager();
         if (var7 instanceof Player p) {
            var8 = p;
         } else {
            var7 = event.getDamager();
            if (var7 instanceof Projectile proj) {
               ProjectileSource var12 = proj.getShooter();
               if (var12 instanceof Player p) {
                  var8 = p;
               }
            }
         }

         Game victimGame = this.plugin.getGameManager().getGame(victim);
         Game attackerGame = var8 != null ? this.plugin.getGameManager().getGame(var8) : null;
         if (victimGame == null || attackerGame != null && attackerGame.getArena().getName().equalsIgnoreCase(victimGame.getArena().getName())) {
            if (attackerGame != null) {
               if (attackerGame.getState() != GameState.LOBBY && attackerGame.getState() != GameState.GRACE_PERIOD) {
                  if (var8 != null && attackerGame.isSpectator(var8.getUniqueId())) {
                     event.setCancelled(true);
                  }

               } else {
                  event.setCancelled(true);
               }
            }
         } else {
            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onInteract(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      Game game = this.plugin.getGameManager().getGame(player);
      if (game != null) {
         if (game.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onDrop(PlayerDropItemEvent event) {
      Player player = event.getPlayer();
      Game game = this.plugin.getGameManager().getGame(player);
      if (game != null) {
         if (game.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPickup(EntityPickupItemEvent event) {
      LivingEntity var3 = event.getEntity();
      if (var3 instanceof Player player) {
         Game game = this.plugin.getGameManager().getGame(player);
         if (game != null) {
            if (game.isSpectator(player.getUniqueId())) {
               event.setCancelled(true);
            }

         }
      }
   }

   @EventHandler
   public void onInventoryClick(InventoryClickEvent event) {
      HumanEntity var3 = event.getWhoClicked();
      if (var3 instanceof Player player) {
         ;
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onMove(PlayerMoveEvent event) {
      if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
         Player player = event.getPlayer();
         Game game = this.plugin.getGameManager().getGame(player);
         if (game != null) {
            if (game.getState() == GameState.LOBBY) {
               double radius = this.plugin.getConfig().getDouble("settings.lobby-radius", (double)5.0F);
               if (!(radius <= (double)0.0F)) {
                  Location lobby = game.getArena().getLobbyLocation();
                  if (lobby != null) {
                     if (event.getTo().getWorld() != null && event.getTo().getWorld().equals(lobby.getWorld()) && event.getTo().distanceSquared(lobby) > radius * radius) {
                        event.setCancelled(true);
                        player.teleport(lobby);
                     }

                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onFoodChange(FoodLevelChangeEvent event) {
      HumanEntity var3 = event.getEntity();
      if (var3 instanceof Player player) {
         Game game = this.plugin.getGameManager().getGame(player);
         if (game != null) {
            if (game.getState() == GameState.LOBBY || game.isSpectator(player.getUniqueId())) {
               event.setCancelled(true);
            }

         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockBreak(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Game game = this.plugin.getGameManager().getGame(player);
      if (game != null) {
         if (game.getState() == GameState.LOBBY || game.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onBlockPlace(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      Game game = this.plugin.getGameManager().getGame(player);
      if (game != null) {
         if (game.getState() == GameState.LOBBY || game.isSpectator(player.getUniqueId())) {
            event.setCancelled(true);
         }

      }
   }
}
