package com.darknetwork.hungergames.listeners;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.game.Game;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class GameListener implements Listener {
   private final HungerGamesPlugin plugin;

   public GameListener(HungerGamesPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGH
   )
   public void onEnvironmentDamage(EntityDamageEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player player) {
         if (this.plugin.getGameManager().isInGame(player)) {
            Game game = this.plugin.getGameManager().getGame(player);
            if (game != null) {
               if (game.isSpectator(player.getUniqueId())) {
                  event.setCancelled(true);
               }

            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onCommand(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (this.plugin.getGameManager().isInGame(player)) {
         if (!player.hasPermission("hungergames.admin")) {
            String cmd = event.getMessage().toLowerCase().split(" ")[0];
            if (cmd.equals("/hg") || cmd.equals("/hungergames") || cmd.equals("/hg ")) {
               ;
            }
         }
      }
   }
}
