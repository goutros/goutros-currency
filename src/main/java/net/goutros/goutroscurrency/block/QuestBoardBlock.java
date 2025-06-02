package net.goutros.goutroscurrency.block;

import com.mojang.serialization.MapCodec;
import net.goutros.goutroscurrency.ModAttachments;
import net.goutros.goutroscurrency.network.ModNetworking;
import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.goutros.goutroscurrency.quest.Quest;
import net.goutros.goutroscurrency.quest.QuestManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class QuestBoardBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<QuestBoardBlock> CODEC = simpleCodec(QuestBoardBlock::new);

    public static final long QUEST_COOLDOWN_MS = 30 * 1000L;

    public QuestBoardBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuestBoardBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        AttachmentType<PlayerQuestComponent> type = (AttachmentType<PlayerQuestComponent>)
                NeoForgeRegistries.ATTACHMENT_TYPES.get(ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID));
        PlayerQuestComponent comp = serverPlayer.getData(type);

        long now = System.currentTimeMillis();
        long timeLeft = Math.max(0, (comp.getLastQuestTime() + QUEST_COOLDOWN_MS - now) / 1000);

        player.sendSystemMessage(
                Component.literal("DEBUG: Quest cooldown: " + timeLeft + " seconds remaining.")
        );

        if (now - comp.getLastQuestTime() < QUEST_COOLDOWN_MS) {
            player.sendSystemMessage(Component.literal("You must wait before taking another quest!"));
            return InteractionResult.FAIL;
        }

        if (comp.getActiveQuests().size() >= 2) {
            player.sendSystemMessage(Component.literal("You already have the maximum number of active quests!"));
            return InteractionResult.CONSUME;
        }

        Quest newQuest = QuestManager.generateRandomGatherQuest(serverPlayer.getServer(), serverPlayer.getRandom());
        if (newQuest == null) {
            player.sendSystemMessage(Component.literal("Failed to generate a quest! Please check your quest presets/config."));
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            // Play sound code...
        }

        comp.addQuest(newQuest);
        comp.setLastQuestTime(now);
        ModNetworking.sendQuestSyncPacket(serverPlayer, comp.getActiveQuests(), comp.getLastQuestTime(), comp.getPreviewQuest());

        // **ADD THIS:**
        QuestManager.updatePlayerQuestPreview(serverPlayer);

        serverPlayer.sendSystemMessage(Component.literal("You received a new quest: " + newQuest.getDescription()));

        return InteractionResult.CONSUME;
    }
}
