package net.goutros.goutroscurrency.block;

import net.goutros.goutroscurrency.GoutrosCurrency;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    // Deferred register for blocks and items
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GoutrosCurrency.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GoutrosCurrency.MOD_ID);

    public static final DeferredBlock<QuestBoardBlock> QUEST_BOARD = BLOCKS.register(
            "quest_board", () -> new QuestBoardBlock(Block.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN) // set map color here
                    .strength(3.0F, 6.0F) // tougher than plank
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
            )
    );

    // Register Quest Board item for inventory/creative
    public static final DeferredItem<Item> QUEST_BOARD_ITEM = ITEMS.register(
            "quest_board", () -> new BlockItem(QUEST_BOARD.get(), new Item.Properties())
    );

    // Call this from your mod's init!
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
    }
}
