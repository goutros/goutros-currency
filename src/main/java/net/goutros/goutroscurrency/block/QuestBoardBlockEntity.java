package net.goutros.goutroscurrency.block;

import javax.annotation.Nullable;
import net.goutros.goutroscurrency.Config;
import net.goutros.goutroscurrency.quest.Quest;
import net.goutros.goutroscurrency.quest.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class QuestBoardBlockEntity extends BlockEntity {
   private long nextQuestTime = 0L;
   private Quest currentQuest;

   public QuestBoardBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)ModBlockEntities.QUEST_BOARD.get(), pos, state);
   }

   public void loadAdditional(CompoundTag tag, Provider provider) {
      super.loadAdditional(tag, provider);
      this.nextQuestTime = tag.getLong("NextQuestTime");
   }

   public void saveAdditional(CompoundTag tag, Provider provider) {
      super.saveAdditional(tag, provider);
      tag.putLong("NextQuestTime", this.nextQuestTime);
   }

   public long getRemainingTicks(long currentTime) {
      return Math.max(0L, this.nextQuestTime - currentTime);
   }

   @Nullable
   public Quest getQuest() {
      return this.currentQuest;
   }

   public void generateQuest(ServerLevel level) {
      Player nearest = level.getNearestPlayer((double)this.worldPosition.getX(), (double)this.worldPosition.getY(), (double)this.worldPosition.getZ(), 32.0D, false);
      Quest var10001;
      if (nearest instanceof ServerPlayer) {
         ServerPlayer sp = (ServerPlayer)nearest;
         var10001 = QuestManager.generateRandomQuest(level.getServer(), sp, level.random);
      } else {
         var10001 = QuestManager.generateRandomQuest(level.getServer(), level.random);
      }

      this.currentQuest = var10001;
      level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(QuestBoardBlock.HAS_QUEST, true), 3);
   }

   public void resetQuestTimer(long delayTicks) {
      this.currentQuest = null;
      this.nextQuestTime = this.level.getGameTime() + delayTicks;
      this.level.setBlock(this.worldPosition, (BlockState)this.getBlockState().setValue(QuestBoardBlock.HAS_QUEST, false), 3);
   }

   public void tick(ServerLevel level, BlockPos pos, BlockState state) {
      if (this.currentQuest == null && (Boolean)state.getValue(QuestBoardBlock.HAS_QUEST) || this.currentQuest != null && !(Boolean)state.getValue(QuestBoardBlock.HAS_QUEST)) {
         level.setBlock(this.worldPosition, (BlockState)state.setValue(QuestBoardBlock.HAS_QUEST, this.currentQuest != null), 3);
      }

      long time = level.getGameTime();
      if (this.currentQuest == null && this.nextQuestTime != 0L && time >= this.nextQuestTime) {
         this.generateQuest(level);
      } else if (this.nextQuestTime == 0L) {
         this.resetQuestTimer((long)Config.questBoardCooldownSeconds * 20L);
      }

   }
}
