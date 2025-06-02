package net.goutros.goutroscurrency.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.goutros.goutroscurrency.quest.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

public class QuestBoardRenderer implements BlockEntityRenderer<QuestBoardBlockEntity> {
    private static final ResourceLocation PAGE_FRONT =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/block/quest_board_front_available.png");

    private static final boolean DEBUG_FORCE_PREVIEW = false;

    // Only print missing player/component warning once per run
    private static boolean warnedNoPlayer = false, warnedNoComponent = false;

    public QuestBoardRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(QuestBoardBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            if (!warnedNoPlayer) {
                System.out.println("[QBR] No player found, skipping render");
                warnedNoPlayer = true;
            }
            return;
        }

        AttachmentType<PlayerQuestComponent> type = (AttachmentType<PlayerQuestComponent>)
                NeoForgeRegistries.ATTACHMENT_TYPES.get(ResourceLocation.fromNamespaceAndPath("goutroscurrency", "player_quest"));
        PlayerQuestComponent comp = player.getData(type);
        if (comp == null) {
            if (!warnedNoComponent) {
                System.out.println("[QBR] No PlayerQuestComponent found, skipping render");
                warnedNoComponent = true;
            }
            return;
        }

        Quest previewQuest = comp.getPreviewQuest();
        if (DEBUG_FORCE_PREVIEW && previewQuest == null) {
            previewQuest = new Quest("gather", "minecraft:diamond", 1, "Collect a diamond", 10);
        }

        // Only print debug info if debug mode enabled
        if (DEBUG_FORCE_PREVIEW) {
            System.out.println("[QBR] Render: previewQuest=" + previewQuest +
                    ", cooldown=" + (System.currentTimeMillis() - comp.getLastQuestTime()) +
                    ", quests=" + comp.getActiveQuests().size());
        }

        if (previewQuest == null) return;
        if ((System.currentTimeMillis() - comp.getLastQuestTime()) < QuestBoardBlock.QUEST_COOLDOWN_MS) return;
        if (comp.getActiveQuests().size() >= 2) return;

        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof QuestBoardBlock qb)) return;
        Direction front = state.getValue(QuestBoardBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float yRot = switch (front) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST  -> 90f;
            case EAST  -> -90f;
            default -> 0f;
        };
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        poseStack.translate(0, 0, -0.501);

        // Draw the quest paper overlay
        var vb = buffer.getBuffer(RenderType.entityCutout(PAGE_FRONT));
        int fullBright = 0xF000F0;
        float min = -0.5f, max = 0.5f;
        vb.addVertex(poseStack.last().pose(), min, max, 0)
                .setColor(255, 255, 255, 255).setUv(0, 0)
                .setUv1(overlay & 0xFFFF, overlay >> 16 & 0xFFFF)
                .setUv2(fullBright & 0xFFFF, fullBright >> 16 & 0xFFFF)
                .setNormal(0, 0, 1);
        vb.addVertex(poseStack.last().pose(), max, max, 0)
                .setColor(255, 255, 255, 255).setUv(1, 0)
                .setUv1(overlay & 0xFFFF, overlay >> 16 & 0xFFFF)
                .setUv2(fullBright & 0xFFFF, fullBright >> 16 & 0xFFFF)
                .setNormal(0, 0, 1);
        vb.addVertex(poseStack.last().pose(), max, min, 0)
                .setColor(255, 255, 255, 255).setUv(1, 1)
                .setUv1(overlay & 0xFFFF, overlay >> 16 & 0xFFFF)
                .setUv2(fullBright & 0xFFFF, fullBright >> 16 & 0xFFFF)
                .setNormal(0, 0, 1);
        vb.addVertex(poseStack.last().pose(), min, min, 0)
                .setColor(255, 255, 255, 255).setUv(0, 1)
                .setUv1(overlay & 0xFFFF, overlay >> 16 & 0xFFFF)
                .setUv2(fullBright & 0xFFFF, fullBright >> 16 & 0xFFFF)
                .setNormal(0, 0, 1);

        // Draw the quest's target item icon
        ItemStack stack = previewQuest.getTargetItemStack(blockEntity.getLevel());
        if (!stack.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, 0, 0.06);
            float scale = 0.4f;
            poseStack.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    net.minecraft.world.item.ItemDisplayContext.FIXED,
                    light,
                    overlay,
                    poseStack,
                    buffer,
                    blockEntity.getLevel(),
                    0
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}

