package com.darknetwork.hungergames.utils;

import com.darknetwork.hungergames.HungerGamesPlugin;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MessageUtil {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private final HungerGamesPlugin plugin;
   private FileConfiguration messages;
   private String prefix;

   public MessageUtil(HungerGamesPlugin plugin) {
      this.plugin = plugin;
      this.reload();
   }

   public void reload() {
      File file = new File(this.plugin.getDataFolder(), "messages.yml");
      if (!file.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messages = YamlConfiguration.loadConfiguration(file);
      this.prefix = colorize(this.messages.getString("prefix", "&#001CFF&lʜᴜɴɢᴇʀɢᴀᴍᴇѕ &7→ "));
   }

   public void send(Player player, String key) {
      String var10001 = this.prefix;
      player.sendMessage(var10001 + this.getRaw(key));
   }

   public void send(Player player, String key, String... replacements) {
      String var10001 = this.prefix;
      player.sendMessage(var10001 + this.format(this.getRaw(key), replacements));
   }

   public void sendRaw(Player player, String key) {
      player.sendMessage(this.getRaw(key));
   }

   public void sendRaw(Player player, String key, String... replacements) {
      player.sendMessage(this.format(this.getRaw(key), replacements));
   }

   public String get(String key) {
      String var10000 = this.prefix;
      return var10000 + this.getRaw(key);
   }

   public String get(String key, String... replacements) {
      String var10000 = this.prefix;
      return var10000 + this.format(this.getRaw(key), replacements);
   }

   public String getRaw(String key) {
      String raw = this.messages.getString(key, "&cMissing message: " + key);
      return colorize(raw);
   }

   private String format(String msg, String... replacements) {
      for(int i = 0; i + 1 < replacements.length; i += 2) {
         msg = msg.replace(replacements[i], replacements[i + 1]);
      }

      return msg;
   }

   public static String colorize(String text) {
      if (text == null) {
         return "";
      } else {
         Matcher matcher = HEX_PATTERN.matcher(text);
         StringBuilder sb = new StringBuilder();

         while(matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(sb, ChatColor.of("#" + hex).toString());
         }

         matcher.appendTail(sb);
         return ChatColor.translateAlternateColorCodes('&', sb.toString());
      }
   }

   public String getPrefix() {
      return this.prefix;
   }

   public FileConfiguration getMessages() {
      return this.messages;
   }
}
