package net.goutros.goutroscurrency.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.goutros.goutroscurrency.quest.Quest;
import net.goutros.goutroscurrency.util.WalkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
   public static final Codec<WalkPos> WALK_POS_CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.FLOAT.fieldOf("x").forGetter(WalkPos::x), Codec.FLOAT.fieldOf("z").forGetter(WalkPos::z)).apply(instance, WalkPos::new);
   });
   public static final StreamCodec<RegistryFriendlyByteBuf, WalkPos> WALK_POS_STREAM_CODEC;
   public static final DeferredRegister<DataComponentType<?>> COMPONENTS;
   public static final Supplier<DataComponentType<Integer>> COIN_BALANCE;
   public static final Supplier<DataComponentType<Integer>> SELECTED_COIN;
   public static final Supplier<DataComponentType<Boolean>> FORCE_SYNC;
   public static final Supplier<DataComponentType<Integer>> SCROLL_TIMER;
   public static final Supplier<DataComponentType<Quest>> QUEST_DATA;
   public static final Supplier<DataComponentType<WalkPos>> LAST_WALK_POS;
   public static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> BLOCK_POS_STREAM_CODEC;
   public static final Supplier<DataComponentType<BlockPos>> LAST_WALK_BLOCKPOS;
   public static final Supplier<DataComponentType<String>> LAST_KILL_ID;

   public static void register(IEventBus bus) {
      COMPONENTS.register(bus);
   }

   static {
      WALK_POS_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, WalkPos::x, ByteBufCodecs.FLOAT, WalkPos::z, WalkPos::new);
      COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "goutroscurrency");
      COIN_BALANCE = COMPONENTS.register("coin_pouch_balance", () -> {
         return DataComponentType.builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
      });
      SELECTED_COIN = COMPONENTS.register("selected_coin", () -> {
         return DataComponentType.builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
      });
      FORCE_SYNC = COMPONENTS.register("force_sync", () -> {
         return DataComponentType.builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();
      });
      SCROLL_TIMER = COMPONENTS.register("scroll_timer", () -> {
         return DataComponentType.builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
      });
      QUEST_DATA = COMPONENTS.register("quest_data", () -> {
         return DataComponentType.builder().persistent(Quest.CODEC).build();
      });
      LAST_WALK_POS = COMPONENTS.register("last_walk_pos", () -> {
         return DataComponentType.builder().persistent(WALK_POS_CODEC).networkSynchronized(WALK_POS_STREAM_CODEC).build();
      });
      BLOCK_POS_STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, Vec3i::getX, ByteBufCodecs.INT, Vec3i::getY, ByteBufCodecs.INT, Vec3i::getZ, BlockPos::new);
      LAST_WALK_BLOCKPOS = COMPONENTS.register("last_walk_blockpos", () -> {
         return DataComponentType.builder().persistent(BlockPos.CODEC).networkSynchronized(BLOCK_POS_STREAM_CODEC).build();
      });
      LAST_KILL_ID = COMPONENTS.register("last_kill_id", () -> {
         return DataComponentType.builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build();
      });
   }
}
