package net.goutros.goutroscurrency.core;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.goutros.goutroscurrency.enchantment.effect.CoinPickupEffect;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.Direct;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.Cost;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEnchantments {
   public static final DeferredRegister<Enchantment> ENCHANTMENTS;
   public static final DeferredHolder<Enchantment, Enchantment> COIN_MAGNET;
   public static final ResourceLocation COIN_MAGNET_ID;
   public static final ResourceKey<Enchantment> COIN_MAGNET_KEY;

   public static void register(IEventBus eventBus) {
      ENCHANTMENTS.register(eventBus);
   }

   static {
      ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, "goutroscurrency");
      COIN_MAGNET = ENCHANTMENTS.register("coin_magnet", () -> {
         TagKey<Item> tagKey = ItemTags.create(ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coin_pouch_enchantable"));
         Direct<Item> supported = HolderSet.direct(StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).spliterator(), false).toList());
         EnchantmentDefinition definition = new EnchantmentDefinition(supported, Optional.of(supported), 10, 1, new Cost(5, 10), new Cost(20, 10), 1, List.of(EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND));
         return Enchantment.enchantment(definition).withEffect((DataComponentType)ModEnchantmentComponents.COIN_PICKUP.get(), new CoinPickupEffect()).build(ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coin_magnet"));
      });
      COIN_MAGNET_ID = ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coin_magnet");
      COIN_MAGNET_KEY = ResourceKey.create(Registries.ENCHANTMENT, COIN_MAGNET_ID);
   }
}
