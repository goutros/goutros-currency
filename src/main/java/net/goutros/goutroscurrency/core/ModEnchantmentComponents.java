package net.goutros.goutroscurrency.core;

import com.mojang.serialization.Codec;
import java.util.List;
import net.goutros.goutroscurrency.enchantment.effect.CoinPickupEffect;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEnchantmentComponents {
   public static final DeferredRegister<DataComponentType<?>> ENCHANTMENT_COMPONENT_TYPES;
   public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ConditionalEffect<CoinPickupEffect>>>> COIN_PICKUP;

   public static void register(IEventBus bus) {
      ENCHANTMENT_COMPONENT_TYPES.register(bus);
   }

   static {
      ENCHANTMENT_COMPONENT_TYPES = DeferredRegister.create(Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, "goutroscurrency");
      COIN_PICKUP = ENCHANTMENT_COMPONENT_TYPES.register("coin_pickup", () -> {
         return DataComponentType.builder().persistent(Codec.list(ConditionalEffect.codec(CoinPickupEffect.CODEC, LootContextParamSets.ENCHANTED_ITEM))).build();
      });
   }
}
