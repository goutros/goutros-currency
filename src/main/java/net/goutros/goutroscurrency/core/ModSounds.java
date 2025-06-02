package net.goutros.goutroscurrency.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);

    // Register your sounds as suppliers
    public static final Supplier<SoundEvent> COIN_POUCH_INSERT =
            registerSoundEvent("coin_pouch_insert");

    // Register with event bus in mod constructor
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
