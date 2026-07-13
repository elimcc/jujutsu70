package com.darknetwork.hungergames.stats;

import java.util.UUID;

public class PlayerStats {
   private final UUID uuid;
   private String name;
   private int wins;
   private int kills;
   private int deaths;
   private int gamesPlayed;

   public PlayerStats(UUID uuid, String name) {
      this.uuid = uuid;
      this.name = name;
      this.wins = 0;
      this.kills = 0;
      this.deaths = 0;
      this.gamesPlayed = 0;
   }

   public void addWin() {
      ++this.wins;
   }

   public void addKill() {
      ++this.kills;
   }

   public void addDeath() {
      ++this.deaths;
   }

   public void addGame() {
      ++this.gamesPlayed;
   }

   public double getKDR() {
      return this.deaths == 0 ? (double)this.kills : (double)Math.round((double)this.kills / (double)this.deaths * (double)100.0F) / (double)100.0F;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getWins() {
      return this.wins;
   }

   public void setWins(int wins) {
      this.wins = wins;
   }

   public int getKills() {
      return this.kills;
   }

   public void setKills(int kills) {
      this.kills = kills;
   }

   public int getDeaths() {
      return this.deaths;
   }

   public void setDeaths(int deaths) {
      this.deaths = deaths;
   }

   public int getGamesPlayed() {
      return this.gamesPlayed;
   }

   public void setGamesPlayed(int gamesPlayed) {
      this.gamesPlayed = gamesPlayed;
   }
}
