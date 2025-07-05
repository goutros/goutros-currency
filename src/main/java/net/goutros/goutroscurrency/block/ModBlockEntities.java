package net.goutros.goutroscurrency.block;

import com.mojang.datafixers.types.Type;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES;
   public static final Supplier<BlockEntityType<QuestBoardBlockEntity>> QUEST_BOARD;

   public static void register(IEventBus bus) {
      BLOCK_ENTITY_TYPES.register(bus);
   }

   static {
      BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "goutroscurrency");
      QUEST_BOARD = BLOCK_ENTITY_TYPES.register("quest_board", () -> {
         return Builder.of(QuestBoardBlockEntity::new, new Block[]{(Block)ModBlocks.QUEST_BOARD.get()}).build((Type)null);
      });
   }
}
