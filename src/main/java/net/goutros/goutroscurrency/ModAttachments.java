package net.goutros.goutroscurrency;

import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "goutroscurrency");

    public static final String PLAYER_QUEST_ID = "player_quest";

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(PLAYER_QUEST_ID,
                () -> AttachmentType.serializable(PlayerQuestComponent::new).build()
        );
        ATTACHMENT_TYPES.register(modBus);
    }

    public static AttachmentType<PlayerQuestComponent> getPlayerQuestType() {
        return (AttachmentType<PlayerQuestComponent>) NeoForgeRegistries.ATTACHMENT_TYPES.get(
                ResourceLocation.fromNamespaceAndPath("goutroscurrency", PLAYER_QUEST_ID)
        );
    }
}
