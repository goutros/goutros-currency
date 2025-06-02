package net.goutros.goutroscurrency;

import net.goutros.goutroscurrency.GoutrosCurrency;
import net.goutros.goutroscurrency.quest.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber(modid = GoutrosCurrency.MOD_ID) // NOTE: No 'bus = ...'
public class CommonPlayerEvents {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            QuestManager.updatePlayerQuestPreview(serverPlayer);
        }
    }
}
