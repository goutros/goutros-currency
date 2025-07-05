package net.goutros.goutroscurrency.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.goutros.goutroscurrency.Config;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.goutros.goutroscurrency.quest.Quest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class QuestBoardBlock extends HorizontalDirectionalBlock implements EntityBlock {
   public static final BooleanProperty HAS_QUEST = BooleanProperty.create("has_quest");
   public static final MapCodec<QuestBoardBlock> CODEC = simpleCodec(QuestBoardBlock::new);

   public QuestBoardBlock(Properties props) {
      super(props);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)).setValue(HAS_QUEST, false));
   }

   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      return List.of(new ItemStack(this));
   }

   protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
      return CODEC;
   }

   protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(new Property[]{BlockStateProperties.HORIZONTAL_FACING, HAS_QUEST});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)((BlockState)this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())).setValue(HAS_QUEST, false);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new QuestBoardBlockEntity(pos, state);
   }

   public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      if (level instanceof ServerLevel) {
         ServerLevel serverLevel = (ServerLevel)level;
         if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            QuestBoardBlockEntity board = (QuestBoardBlockEntity)serverLevel.getBlockEntity(pos);
            if (board == null) {
               return InteractionResult.PASS;
            }

            PlayerQuestComponent comp = PlayerQuestComponent.get(serverPlayer);
            long time = level.getGameTime();
            Quest quest = board.getQuest();
            boolean boardHasQuest = (Boolean)state.getValue(HAS_QUEST) && quest != null;
            boolean playerOnCooldown = comp != null && comp.isOnCooldown(time);
            long ticksLeft;
            if (boardHasQuest && playerOnCooldown) {
               ticksLeft = comp.getRemainingCooldownTicks(time);
               player.displayClientMessage(Component.literal("You’ve redeemed a quest recently. Please wait ").append(Component.literal(this.formatTime(ticksLeft)).withStyle(ChatFormatting.RED)), true);
               level.playSound((Player)null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
               player.swing(InteractionHand.MAIN_HAND, true);
               return InteractionResult.CONSUME;
            }

            if (!boardHasQuest) {
               if (playerOnCooldown) {
                  ticksLeft = comp.getRemainingCooldownTicks(time);
                  player.displayClientMessage(Component.literal("You’ve redeemed a quest recently. Please wait ").append(Component.literal(this.formatTime(ticksLeft)).withStyle(ChatFormatting.RED)), true);
                  level.playSound((Player)null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  player.swing(InteractionHand.MAIN_HAND, true);
                  return InteractionResult.CONSUME;
               }

               ticksLeft = board.getRemainingTicks(time);
               player.displayClientMessage(Component.literal("No Quests right now! Come back in ").append(Component.literal(this.formatTime(ticksLeft)).withStyle(ChatFormatting.YELLOW)), true);
               level.playSound((Player)null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 1.0F, 0.9F);
               player.swing(InteractionHand.MAIN_HAND, true);
               return InteractionResult.CONSUME;
            }

            ItemStack note = new ItemStack((ItemLike)ModItems.QUEST_NOTE.get());
            note.set(ModDataComponents.QUEST_DATA, quest);
            if (!player.getInventory().add(note)) {
               player.drop(note, false);
            }

            board.resetQuestTimer((long)Config.questBoardCooldownSeconds * 20L);
            level.setBlock(pos, (BlockState)state.setValue(HAS_QUEST, false), 3);
            if (comp != null) {
               comp.setLastQuestTime(time);
            }

            player.displayClientMessage(Component.literal("New Quest Received!").withStyle(ChatFormatting.GREEN), true);
            level.playSound((Player)null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 1.1F);
            player.swing(InteractionHand.MAIN_HAND, true);
            return InteractionResult.CONSUME;
         }
      }

      return InteractionResult.PASS;
   }

   private String formatTime(long ticks) {
      long seconds = ticks / 20L;
      long minutes = seconds / 60L;
      long secs = seconds % 60L;
      return String.format("%02d:%02d", minutes, secs);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return !level.isClientSide && type == ModBlockEntities.QUEST_BOARD.get() ? (lvl, pos, blockState, be) -> {
         ((QuestBoardBlockEntity)be).tick((ServerLevel)lvl, pos, blockState);
      } : null;
   }
}
