package net.goutros.goutroscurrency.quest;

import net.goutros.goutroscurrency.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class QuestEventsClone {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        AttachmentType<PlayerQuestComponent> questType = (AttachmentType<PlayerQuestComponent>)
                NeoForgeRegistries.ATTACHMENT_TYPES.get(ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID));
        if (event.isWasDeath() && event.getOriginal().hasData(questType)) {
            PlayerQuestComponent orig = event.getOriginal().getData(questType);
            event.getEntity().setData(questType, orig);
        }
    }
}
