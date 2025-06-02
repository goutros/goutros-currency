package net.goutros.goutroscurrency.client;

import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.item.CoinPouchItem;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

public class ClientSetup {
    public static void onClientSetup(final FMLClientSetupEvent event) {
        ModItemProperties.registerAll();
        NeoForge.EVENT_BUS.register(ClientHudEvents.class);
    }
}
