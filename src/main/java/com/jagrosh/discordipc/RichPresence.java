package com.jagrosh.discordipc;

public class RichPresence {
   private String state;
   private String details;
   private long startTimestamp;
   private long endTimestamp;
   private String largeImageKey;
   private String largeImageText;
   private String smallImageKey;
   private String smallImageText;
   private String partyId;
   private int partySize;
   private int partyMax;
   private String matchSecret;
   private String joinSecret;
   private String spectateSecret;
   private int instance;

   public String getState() {
      return this.state;
   }

   public RichPresence setState(String var1) {
      this.state = var1;
      return this;
   }

   public String getDetails() {
      return this.details;
   }

   public RichPresence setDetails(String var1) {
      this.details = var1;
      return this;
   }

   public long getStartTimestamp() {
      return this.startTimestamp;
   }

   public RichPresence setStartTimestamp(long var1) {
      this.startTimestamp = var1;
      return this;
   }

   public long getEndTimestamp() {
      return this.endTimestamp;
   }

   public RichPresence setEndTimestamp(long var1) {
      this.endTimestamp = var1;
      return this;
   }

   public String getLargeImageKey() {
      return this.largeImageKey;
   }

   public RichPresence setLargeImageKey(String var1) {
      this.largeImageKey = var1;
      return this;
   }

   public String getLargeImageText() {
      return this.largeImageText;
   }

   public RichPresence setLargeImageText(String var1) {
      this.largeImageText = var1;
      return this;
   }

   public String getSmallImageKey() {
      return this.smallImageKey;
   }

   public RichPresence setSmallImageKey(String var1) {
      this.smallImageKey = var1;
      return this;
   }

   public String getSmallImageText() {
      return this.smallImageText;
   }

   public RichPresence setSmallImageText(String var1) {
      this.smallImageText = var1;
      return this;
   }

   public String getPartyId() {
      return this.partyId;
   }

   public RichPresence setPartyId(String var1) {
      this.partyId = var1;
      return this;
   }

   public int getPartySize() {
      return this.partySize;
   }

   public RichPresence setPartySize(int var1) {
      this.partySize = var1;
      return this;
   }

   public int getPartyMax() {
      return this.partyMax;
   }

   public RichPresence setPartyMax(int var1) {
      this.partyMax = var1;
      return this;
   }

   public String getMatchSecret() {
      return this.matchSecret;
   }

   public RichPresence setMatchSecret(String var1) {
      this.matchSecret = var1;
      return this;
   }

   public String getJoinSecret() {
      return this.joinSecret;
   }

   public RichPresence setJoinSecret(String var1) {
      this.joinSecret = var1;
      return this;
   }

   public String getSpectateSecret() {
      return this.spectateSecret;
   }

   public RichPresence setSpectateSecret(String var1) {
      this.spectateSecret = var1;
      return this;
   }

   public int getInstance() {
      return this.instance;
   }

   public RichPresence setInstance(int var1) {
      this.instance = var1;
      return this;
   }
}
