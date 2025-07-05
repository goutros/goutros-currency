package net.goutros.goutroscurrency.client;

import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
   public static void onClientSetup(FMLClientSetupEvent event) {
      ModItemProperties.registerAll();
   }
}
