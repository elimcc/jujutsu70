package com.darknetwork.hungergames.gui;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.stats.PlayerStats;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class StatsGUI implements Listener {
   private final HungerGamesPlugin plugin;
   private static final String GUI_TITLE = MessageUtil.colorize("&#001CFF&lYour Statistics");

   public StatsGUI(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public void open(Player player) {
      Inventory gui = Bukkit.createInventory((InventoryHolder)null, 27, GUI_TITLE);
      PlayerStats stats = this.plugin.getStatsManager().getStats(player);
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta headMeta = (SkullMeta)head.getItemMeta();
      headMeta.setOwningPlayer(player);
      headMeta.setDisplayName(MessageUtil.colorize("&#001CFF&l" + player.getName()));
      headMeta.setLore(List.of(MessageUtil.colorize("&7Your HungerGames stats")));
      head.setItemMeta(headMeta);
      gui.setItem(4, head);
      gui.setItem(10, this.buildStat(Material.GOLDEN_SWORD, "&6Wins", stats.getWins() + " wins"));
      gui.setItem(12, this.buildStat(Material.IRON_SWORD, "&cKills", stats.getKills() + " kills"));
      gui.setItem(14, this.buildStat(Material.BONE, "&7Deaths", stats.getDeaths() + " deaths"));
      gui.setItem(16, this.buildStat(Material.COMPASS, "&bGames Played", stats.getGamesPlayed() + " games"));
      gui.setItem(22, this.buildStat(Material.BOOK, "&eK/D Ratio", stats.getKDR() + " K/D"));
      ItemStack filler = this.buildFiller();

      for(int i = 0; i < 27; ++i) {
         if (gui.getItem(i) == null) {
            gui.setItem(i, filler);
         }
      }

      player.openInventory(gui);
   }

   private ItemStack buildStat(Material mat, String name, String value) {
      ItemStack item = new ItemStack(mat);
      ItemMeta meta = item.getItemMeta();
      meta.setDisplayName(MessageUtil.colorize(name));
      meta.setLore(Arrays.asList(MessageUtil.colorize("&f" + value), "", MessageUtil.colorize("&8HungerGames Statistics")));
      item.setItemMeta(meta);
      return item;
   }

   private ItemStack buildFiller() {
      ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta meta = glass.getItemMeta();
      meta.setDisplayName(" ");
      glass.setItemMeta(meta);
      return glass;
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView().getTitle().equals(GUI_TITLE)) {
         event.setCancelled(true);
      }
   }
}
