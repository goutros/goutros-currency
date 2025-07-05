package net.goutros.goutroscurrency.core;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS;
   public static final Supplier<SoundEvent> COIN_POUCH_INSERT;

   public static void register(IEventBus eventBus) {
      SOUND_EVENTS.register(eventBus);
   }

   private static Supplier<SoundEvent> registerSoundEvent(String name) {
      ResourceLocation id = ResourceLocation.fromNamespaceAndPath("goutroscurrency", name);
      return SOUND_EVENTS.register(name, () -> {
         return SoundEvent.createVariableRangeEvent(id);
      });
   }

   static {
      SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, "goutroscurrency");
      COIN_POUCH_INSERT = registerSoundEvent("coin_pouch_insert");
   }
}
