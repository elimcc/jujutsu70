package com.darknetwork.hungergames.arena;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

public class Arena {
   private final String name;
   private ArenaState state;
   private Location lobbyLocation;
   private final List<Location> spawnPoints;
   private int minPlayers;
   private int maxPlayers;

   public Arena(String name) {
      this.name = name;
      this.state = ArenaState.SETUP;
      this.spawnPoints = new ArrayList();
      this.minPlayers = 2;
      this.maxPlayers = 24;
   }

   public boolean isSetup() {
      return this.lobbyLocation != null && !this.spawnPoints.isEmpty();
   }

   public boolean isAvailable() {
      return this.state == ArenaState.WAITING;
   }

   public String getDisplayState() {
      String var10000;
      switch (this.state) {
         case SETUP -> var10000 = "Setting Up";
         case WAITING -> var10000 = "Waiting";
         case LOBBY -> var10000 = "In Lobby";
         case IN_GAME -> var10000 = "In Game";
         case ENDING -> var10000 = "Ending";
         default -> throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public String getName() {
      return this.name;
   }

   public ArenaState getState() {
      return this.state;
   }

   public void setState(ArenaState state) {
      this.state = state;
   }

   public Location getLobbyLocation() {
      return this.lobbyLocation;
   }

   public void setLobbyLocation(Location loc) {
      this.lobbyLocation = loc;
   }

   public List<Location> getSpawnPoints() {
      return this.spawnPoints;
   }

   public void addSpawnPoint(Location loc) {
      this.spawnPoints.add(loc);
   }

   public void setSpawnPoint(int index, Location loc) {
      if (index < this.spawnPoints.size()) {
         this.spawnPoints.set(index, loc);
      } else {
         this.spawnPoints.add(loc);
      }

   }

   public int getMinPlayers() {
      return this.minPlayers;
   }

   public void setMinPlayers(int min) {
      this.minPlayers = min;
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public void setMaxPlayers(int max) {
      this.maxPlayers = max;
   }
}
