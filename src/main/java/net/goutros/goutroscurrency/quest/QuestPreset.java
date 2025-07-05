package net.goutros.goutroscurrency.quest;

import java.util.List;

public class QuestPreset {
   public final String type;
   public final int amountMin;
   public final int amountMax;
   public final int coinMin;
   public final int coinMax;
   public final List<String> whitelistItems;
   public final List<String> whitelistTags;
   public final List<String> whitelistEntities;
   public final List<String> blacklistItems;
   public final List<String> blacklistTags;
   public final List<String> blacklistEntities;
   public final int distanceMin;
   public final int distanceMax;

   public QuestPreset(String type, int amountMin, int amountMax, int coinMin, int coinMax, List<String> whitelistItems, List<String> whitelistTags, List<String> whitelistEntities, List<String> blacklistItems, List<String> blacklistTags, List<String> blacklistEntities, int distanceMin, int distanceMax) {
      this.type = type;
      this.amountMin = amountMin;
      this.amountMax = amountMax;
      this.coinMin = coinMin;
      this.coinMax = coinMax;
      this.whitelistItems = whitelistItems;
      this.whitelistTags = whitelistTags;
      this.whitelistEntities = whitelistEntities;
      this.blacklistItems = blacklistItems;
      this.blacklistTags = blacklistTags;
      this.blacklistEntities = blacklistEntities;
      this.distanceMin = distanceMin;
      this.distanceMax = distanceMax;
   }

   public static QuestPreset fromJson(String type, int amountMin, int amountMax, int coinMin, int coinMax, List<String> whitelistItems, List<String> whitelistTags, List<String> whitelistEntities, List<String> blacklistItems, List<String> blacklistTags, List<String> blacklistEntities, int distanceMin, int distanceMax) {
      if ("walk".equalsIgnoreCase(type) && distanceMin == 0 && distanceMax == 0) {
         distanceMin = amountMin;
         distanceMax = amountMax;
      }

      return new QuestPreset(type, amountMin, amountMax, coinMin, coinMax, whitelistItems, whitelistTags, whitelistEntities, blacklistItems, blacklistTags, blacklistEntities, distanceMin, distanceMax);
   }

   public boolean isGather() {
      return this.type.equalsIgnoreCase("gather");
   }

   public boolean isKill() {
      return this.type.equalsIgnoreCase("kill");
   }

   public boolean isWalk() {
      return this.type.equalsIgnoreCase("walk");
   }

   public boolean isHunt() {
      return this.type.equalsIgnoreCase("hunt");
   }
}
