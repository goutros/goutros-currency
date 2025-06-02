package net.goutros.goutroscurrency;

import net.goutros.goutroscurrency.quest.QuestManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = GoutrosCurrency.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandlers {
    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        net.goutros.goutroscurrency.network.ModNetworking.register(event);
    }

}

