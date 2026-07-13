package com.darknetwork.hungergames.gui;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.arena.Arena;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaGUI implements Listener {
   private final HungerGamesPlugin plugin;
   private static final String GUI_TITLE = MessageUtil.colorize("&#001CFF&lSelect Arena");

   public ArenaGUI(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public void open(Player player) {
      Collection<Arena> arenas = this.plugin.getArenaManager().getAllArenas();
      int size = Math.max(9, (int)Math.ceil((double)arenas.size() / (double)9.0F) * 9);
      size = Math.min(size, 54);
      Inventory gui = Bukkit.createInventory((InventoryHolder)null, size, GUI_TITLE);
      int slot = 0;

      for(Arena arena : arenas) {
         if (slot >= size) {
            break;
         }

         gui.setItem(slot++, this.buildArenaItem(arena));
      }

      player.openInventory(gui);
   }

   private ItemStack buildArenaItem(Arena arena) {
      Game game = this.plugin.getGameManager().getGame(arena);
      int current = game != null ? game.getPlayers().size() : 0;
      Material mat;
      String statusColor;
      switch (arena.getState()) {
         case WAITING:
            mat = Material.LIME_WOOL;
            statusColor = "&a";
            break;
         case LOBBY:
            mat = Material.YELLOW_WOOL;
            statusColor = "&e";
            break;
         case IN_GAME:
            mat = Material.RED_WOOL;
            statusColor = "&c";
            break;
         default:
            mat = Material.GRAY_WOOL;
            statusColor = "&7";
      }

      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(MessageUtil.colorize("&#001CFF&l" + arena.getName()));
      List<String> lore = new ArrayList();
      lore.add(MessageUtil.colorize("&8▸ " + statusColor + arena.getDisplayState()));
      lore.add(MessageUtil.colorize("&8▸ &7Players: &f" + current + "&7/&f" + arena.getMaxPlayers()));
      lore.add(MessageUtil.colorize("&8▸ &7Spawns: &f" + arena.getSpawnPoints().size()));
      lore.add("");
      if (arena.isAvailable()) {
         lore.add(MessageUtil.colorize("&aClick to join!"));
      } else {
         lore.add(MessageUtil.colorize("&cCurrently unavailable."));
      }

      meta.setLore(lore);
      item.setItemMeta(meta);
      return item;
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView().getTitle().equals(GUI_TITLE)) {
         event.setCancelled(true);
         HumanEntity var3 = event.getWhoClicked();
         if (var3 instanceof Player) {
            Player player = (Player)var3;
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
               ItemMeta meta = event.getCurrentItem().getItemMeta();
               if (meta != null) {
                  String arenaName = MessageUtil.colorize(meta.getDisplayName()).replace(MessageUtil.colorize("&#001CFF&l"), "").strip();
                  Arena arena = this.plugin.getArenaManager().getArena(arenaName);
                  if (arena != null && arena.isAvailable()) {
                     player.closeInventory();
                     if (this.plugin.getGameManager().isInGame(player)) {
                        player.sendMessage(this.plugin.getMessageUtil().get("lobby.already-in-game"));
                     } else {
                        boolean joined = this.plugin.getGameManager().joinGame(player, arena);
                        if (joined) {
                           player.sendMessage(this.plugin.getMessageUtil().get("lobby.joined", "{arena}", arena.getName()));
                        } else {
                           player.sendMessage(this.plugin.getMessageUtil().get("lobby.full"));
                        }

                     }
                  } else {
                     player.closeInventory();
                     player.sendMessage(this.plugin.getMessageUtil().get("lobby.full"));
                  }
               }
            }
         }
      }
   }
}
