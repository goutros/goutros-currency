package net.goutros.goutroscurrency.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;

public class ModBlocks {
   public static final Blocks BLOCKS = DeferredRegister.createBlocks("goutroscurrency");
   public static final Items ITEMS = DeferredRegister.createItems("goutroscurrency");
   public static final DeferredBlock<QuestBoardBlock> QUEST_BOARD;
   public static final DeferredBlock<CashLayerBlock> CASH_LAYER;
   public static final DeferredItem<Item> QUEST_BOARD_ITEM;
   public static final DeferredItem<Item> CASH_LAYER_ITEM;

   public static void register(IEventBus eventBus) {
      BLOCKS.register(eventBus);
      ITEMS.register(eventBus);
   }

   static {
      QUEST_BOARD = BLOCKS.register("quest_board", () -> {
         return new QuestBoardBlock(Properties.of().mapColor(MapColor.COLOR_BROWN).strength(2.0F).sound(SoundType.WOOD).requiresCorrectToolForDrops());
      });
      CASH_LAYER = BLOCKS.register("cash_layer", () -> {
         return new CashLayerBlock();
      });
      QUEST_BOARD_ITEM = ITEMS.register("quest_board", () -> {
         return new BlockItem((Block)QUEST_BOARD.get(), new net.minecraft.world.item.Item.Properties());
      });
      CASH_LAYER_ITEM = ITEMS.register("cash_layer", () -> {
         return new BlockItem((Block)CASH_LAYER.get(), new net.minecraft.world.item.Item.Properties());
      });
   }
}
