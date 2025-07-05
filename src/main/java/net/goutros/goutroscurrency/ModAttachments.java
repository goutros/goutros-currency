package net.goutros.goutroscurrency;

import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
   public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES;
   public static final AttachmentType<PlayerQuestComponent> PLAYER_QUEST;

   public static void register(IEventBus modBus) {
      ATTACHMENT_TYPES.register("player_quest", () -> {
         return PLAYER_QUEST;
      });
      ATTACHMENT_TYPES.register(modBus);
   }

   static {
      ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "goutroscurrency");
      PLAYER_QUEST = AttachmentType.serializable(PlayerQuestComponent::new).copyOnDeath().build();
   }
}
