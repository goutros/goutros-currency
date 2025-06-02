package net.goutros.goutroscurrency.network;

import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CoinPouchSelectPacket(int slot, int selected) implements CustomPacketPayload {
    public static final Type<CoinPouchSelectPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coinpouch_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoinPouchSelectPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CoinPouchSelectPacket::slot,
                    ByteBufCodecs.VAR_INT, CoinPouchSelectPacket::selected,
                    CoinPouchSelectPacket::new
            );

    @Override
    public Type<CoinPouchSelectPacket> type() { return TYPE; }

    // You do not need a static handle method for the registration if you use a lambda in ModNetworking.
    // If you want one, use this:
    public static void handle(CoinPouchSelectPacket pkt, ServerPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.player();
            if (player == null) return;
            if (pkt.slot < 0 || pkt.slot >= player.getInventory().getContainerSize()) return;
            ItemStack stack = player.getInventory().getItem(pkt.slot);
            if (stack.getItem() instanceof CoinPouchItem) {
                CoinPouchItem.setSelectedCoinIndex(stack, pkt.selected);
                player.getInventory().setChanged();
            }
        });
    }
}
