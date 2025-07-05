package net.goutros.goutroscurrency.network;

import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record CoinPouchSelectPacket(int slot, int selected) implements CustomPacketPayload {
   public static final Type<CoinPouchSelectPacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath("goutroscurrency", "coinpouch_select"));
   public static final StreamCodec<RegistryFriendlyByteBuf, CoinPouchSelectPacket> STREAM_CODEC;

   public CoinPouchSelectPacket(int slot, int selected) {
      this.slot = slot;
      this.selected = selected;
   }

   public Type<CoinPouchSelectPacket> type() {
      return TYPE;
   }

   public static void handle(CoinPouchSelectPacket pkt, ServerPayloadContext context) {
      context.enqueueWork(() -> {
         ServerPlayer player = context.player();
         if (player != null) {
            if (pkt.slot >= 0 && pkt.slot < player.getInventory().getContainerSize()) {
               ItemStack stack = player.getInventory().getItem(pkt.slot);
               if (stack.getItem() instanceof CoinPouchItem) {
                  CoinPouchItem.setSelectedCoinIndex(stack, pkt.selected);
                  player.getInventory().setChanged();
               }

            }
         }
      });
   }

   public int slot() {
      return this.slot;
   }

   public int selected() {
      return this.selected;
   }

   static {
      STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, CoinPouchSelectPacket::slot, ByteBufCodecs.VAR_INT, CoinPouchSelectPacket::selected, CoinPouchSelectPacket::new);
   }
}
