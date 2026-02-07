package com.jagrosh.discordipc.entities;

public enum DiscordBuild {
   CANARY("canary"),
   PTB("ptb"),
   STABLE("stable"),
   UNKNOWN("unknown");

   private final String key;

   private DiscordBuild(String nullxx) {
      this.key = nullxx;
   }

   public String getKey() {
      return this.key;
   }

   public static DiscordBuild from(String var0) {
      for (DiscordBuild var4 : values()) {
         if (var4.key.equals(var0)) {
            return var4;
         }
      }

      return UNKNOWN;
   }
}
