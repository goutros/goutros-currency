package net.goutros.goutroscurrency.client;

import net.goutros.goutroscurrency.GoutrosCurrency;
import net.goutros.goutroscurrency.block.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.goutros.goutroscurrency.block.QuestBoardRenderer;

@EventBusSubscriber(
        modid = GoutrosCurrency.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class QuestBoardRendererRegistration {
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.QUEST_BOARD.get(),
                QuestBoardRenderer::new
        );
    }
}
