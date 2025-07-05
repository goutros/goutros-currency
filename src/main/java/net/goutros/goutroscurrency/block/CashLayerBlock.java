package net.goutros.goutroscurrency.block;

import java.util.List;
import javax.annotation.Nullable;
import net.goutros.goutroscurrency.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CashLayerBlock extends Block {
   public static final IntegerProperty LAYERS;
   private static final VoxelShape[] SHAPES;

   public CashLayerBlock() {
      super(Properties.of().strength(0.1F).noOcclusion().replaceable().randomTicks().sound(SoundType.WOOL));
      this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LAYERS, 1));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{LAYERS});
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[(Integer)state.getValue(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
      return SHAPES[Math.max(0, (Integer)state.getValue(LAYERS) - 1)];
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return (Integer)state.getValue(LAYERS) == 8 ? 0.2F : 1.0F;
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      int layers = (Integer)state.getValue(LAYERS);
      return 2 + (layers - 1) * 2;
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      int layers = (Integer)state.getValue(LAYERS);
      return List.of(new ItemStack((ItemLike)ModItems.COUNTERFEIT_CASH.get(), layers));
   }

   public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      int current;
      if (stack.getItem() instanceof ShearsItem) {
         current = (Integer)state.getValue(LAYERS);
         if (current <= 1) {
            level.removeBlock(pos, false);
         } else {
            level.setBlock(pos, (BlockState)state.setValue(LAYERS, current - 1), 3);
         }

         if (!level.isClientSide) {
            popResource(level, pos, new ItemStack((ItemLike)ModItems.COUNTERFEIT_CASH.get()));
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
         }

         level.playSound((Player)null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
         return ItemInteractionResult.sidedSuccess(level.isClientSide);
      } else if (stack.is(this.asItem())) {
         current = (Integer)state.getValue(LAYERS);
         if (current >= 8) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
         } else {
            if (!level.isClientSide) {
               level.setBlock(pos, (BlockState)state.setValue(LAYERS, current + 1), 3);
               if (!player.getAbilities().instabuild) {
                  stack.shrink(1);
               }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
         }
      } else {
         return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
   }

   private static EquipmentSlot slotFromHand(InteractionHand hand) {
      return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
   }

   public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
      return new ItemStack((ItemLike)ModItems.COUNTERFEIT_CASH.get());
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
      return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
      if (existing.is(this)) {
         int current = (Integer)existing.getValue(LAYERS);
         return (BlockState)existing.setValue(LAYERS, Math.min(8, current + 1));
      } else {
         return this.defaultBlockState();
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      if (level.getBrightness(LightLayer.BLOCK, pos) > 11) {
         dropResources(state, level, pos);
         level.removeBlock(pos, false);
      }

   }

   static {
      LAYERS = BlockStateProperties.LAYERS;
      SHAPES = new VoxelShape[9];

      for(int i = 0; i <= 8; ++i) {
         SHAPES[i] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, (double)(i * 2), 16.0D);
      }

   }
}
