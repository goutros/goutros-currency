package net.goutros.goutroscurrency.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
   public static void register(RegisterPayloadHandlersEvent event) {
      PayloadRegistrar registrar = event.registrar("1.0.0");
      registrar.playToServer(CoinPouchSelectPacket.TYPE, CoinPouchSelectPacket.STREAM_CODEC, (packet, context) -> {
         if (context instanceof ServerPayloadContext) {
            ServerPayloadContext serverContext = (ServerPayloadContext)context;
            CoinPouchSelectPacket.handle(packet, serverContext);
         }

      });
   }
}
