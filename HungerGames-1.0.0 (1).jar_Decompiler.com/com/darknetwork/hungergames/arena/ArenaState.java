package com.darknetwork.hungergames.arena;

public enum ArenaState {
   SETUP,
   WAITING,
   LOBBY,
   IN_GAME,
   ENDING;

   // $FF: synthetic method
   private static ArenaState[] $values() {
      return new ArenaState[]{SETUP, WAITING, LOBBY, IN_GAME, ENDING};
   }
}
