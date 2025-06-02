package net.goutros.goutroscurrency.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

public class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final Supplier<DataComponentType<Integer>> COIN_BALANCE =
            COMPONENTS.register("coin_pouch_balance", () ->
                    new DataComponentType.Builder<Integer>()
                            .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(StreamCodec.of(
                                    FriendlyByteBuf::writeVarInt,
                                    FriendlyByteBuf::readVarInt
                            ))
                            .build()
            );

    public static final Supplier<DataComponentType<Integer>> SELECTED_COIN =
            COMPONENTS.register("selected_coin", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );

    public static final Supplier<DataComponentType<Boolean>> FORCE_SYNC =
            COMPONENTS.register("force_sync", () ->
                    new DataComponentType.Builder<Boolean>()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build()
            );

    public static final Supplier<DataComponentType<Integer>> SCROLL_TIMER =
            COMPONENTS.register("scroll_timer", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build()
            );

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
