package net.goutros.goutroscurrency.block;

import net.goutros.goutroscurrency.GoutrosCurrency;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GoutrosCurrency.MOD_ID);

    public static final Supplier<BlockEntityType<QuestBoardBlockEntity>> QUEST_BOARD =
            BLOCK_ENTITY_TYPES.register(
                    "quest_board",
                    () -> BlockEntityType.Builder.of(
                            QuestBoardBlockEntity::new,
                            ModBlocks.QUEST_BOARD.get()
                    ).build(null)
            );

    public static void register(IEventBus bus) {
        BLOCK_ENTITY_TYPES.register(bus);
    }
}
