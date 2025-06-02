package net.goutros.goutroscurrency.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class QuestBoardBlockEntity extends BlockEntity {
    public QuestBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUEST_BOARD.get(), pos, state);
    }

    // If you want to store data, override loadAdditional/saveAdditional.
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        // load custom fields here
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        // save custom fields here
    }
}
