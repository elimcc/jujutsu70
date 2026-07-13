package com.darknetwork.hungergames.game;

public enum GameState {
   LOBBY,
   GRACE_PERIOD,
   ACTIVE,
   ENDING;

   // $FF: synthetic method
   private static GameState[] $values() {
      return new GameState[]{LOBBY, GRACE_PERIOD, ACTIVE, ENDING};
   }
}
