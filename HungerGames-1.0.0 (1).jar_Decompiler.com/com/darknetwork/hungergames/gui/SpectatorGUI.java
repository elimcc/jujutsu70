package com.darknetwork.hungergames.gui;

import com.darknetwork.hungergames.HungerGamesPlugin;
import com.darknetwork.hungergames.game.Game;
import com.darknetwork.hungergames.utils.MessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import org.bukkit.inventory.meta.SkullMeta;

public class SpectatorGUI implements Listener {
   private final HungerGamesPlugin plugin;
   private static final String GUI_TITLE = MessageUtil.colorize("&#001CFF&lSpectate a Player");

   public SpectatorGUI(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   public void open(Player spectator, Game game) {
      int size = Math.max(9, (int)Math.ceil((double)game.getAlivePlayers().size() / (double)9.0F) * 9);
      size = Math.min(size, 54);
      Inventory gui = Bukkit.createInventory((InventoryHolder)null, size, GUI_TITLE);
      int slot = 0;

      for(UUID uuid : game.getAlivePlayers()) {
         Player target = Bukkit.getPlayer(uuid);
         if (target != null) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta)head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName(MessageUtil.colorize("&#001CFF&l" + target.getName()));
            List<String> lore = new ArrayList();
            int var10001 = game.getKills(uuid);
            lore.add(MessageUtil.colorize("&7Kills this game: &f" + var10001));
            lore.add("");
            lore.add(MessageUtil.colorize("&aClick to spectate!"));
            meta.setLore(lore);
            head.setItemMeta(meta);
            gui.setItem(slot++, head);
            if (slot >= size) {
               break;
            }
         }
      }

      spectator.openInventory(gui);
   }

   @EventHandler
   public void onClick(InventoryClickEvent event) {
      if (event.getView().getTitle().equals(GUI_TITLE)) {
         event.setCancelled(true);
         HumanEntity var3 = event.getWhoClicked();
         if (var3 instanceof Player) {
            Player spectator = (Player)var3;
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
               SkullMeta meta = (SkullMeta)event.getCurrentItem().getItemMeta();
               if (meta != null && meta.getOwningPlayer() != null) {
                  Player target = Bukkit.getPlayer(meta.getOwningPlayer().getUniqueId());
                  if (target != null) {
                     spectator.closeInventory();
                     spectator.teleport(target.getLocation());
                     spectator.sendMessage(this.plugin.getMessageUtil().get("spectator.now-spectating", "{player}", target.getName()));
                  }
               }
            }
         }
      }
   }
}
