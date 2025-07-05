package net.goutros.goutroscurrency.quest;

import net.goutros.goutroscurrency.Config;
import net.goutros.goutroscurrency.ModAttachments;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerQuestComponent implements INBTSerializable<CompoundTag> {
   private long lastQuestTime = 0L;

   public long getLastQuestTime() {
      return this.lastQuestTime;
   }

   public void setLastQuestTime(long time) {
      this.lastQuestTime = time;
   }

   public static long getCooldownTicks() {
      return 20L * (long)Config.questPlayerCooldownSeconds;
   }

   public boolean isOnCooldown(long currentTime) {
      return currentTime - this.lastQuestTime < getCooldownTicks();
   }

   public long getRemainingCooldownTicks(long currentTime) {
      return Math.max(0L, getCooldownTicks() - (currentTime - this.lastQuestTime));
   }

   public CompoundTag serializeNBT(Provider provider) {
      CompoundTag tag = new CompoundTag();
      tag.putLong("lastQuestTime", this.lastQuestTime);
      return tag;
   }

   public void deserializeNBT(Provider provider, CompoundTag tag) {
      this.lastQuestTime = tag.getLong("lastQuestTime");
   }

   public static PlayerQuestComponent get(Player player) {
      AttachmentType<PlayerQuestComponent> type = ModAttachments.PLAYER_QUEST;
      return (PlayerQuestComponent)player.getData(type);
   }
}
