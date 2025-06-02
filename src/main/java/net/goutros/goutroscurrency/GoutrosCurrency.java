package net.goutros.goutroscurrency;

import net.goutros.goutroscurrency.block.ModBlockEntities;
import net.goutros.goutroscurrency.block.ModBlocks;
import net.goutros.goutroscurrency.block.QuestBoardTickHandler;
import net.goutros.goutroscurrency.client.ClientHudEvents;
import net.goutros.goutroscurrency.client.ClientSetup;
import net.goutros.goutroscurrency.core.ModDataComponents;
import net.goutros.goutroscurrency.core.ModSounds;
import net.goutros.goutroscurrency.item.ModItems;
import net.goutros.goutroscurrency.network.ModNetworking;
import net.goutros.goutroscurrency.quest.QuestEvents;
import net.goutros.goutroscurrency.quest.QuestEventsPlayerLifecycle;
import net.goutros.goutroscurrency.quest.QuestPresetsConfig;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.goutros.goutroscurrency.client.ClientEvents;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(GoutrosCurrency.MOD_ID)
public class GoutrosCurrency {

    public static final String MOD_ID = "goutroscurrency";

    private static final Logger LOGGER = LogUtils.getLogger();

    public GoutrosCurrency(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);


        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ClientSetup::onClientSetup);
        NeoForge.EVENT_BUS.register(QuestEvents.class);
        QuestPresetsConfig.load();
        NeoForge.EVENT_BUS.register(QuestBoardTickHandler.class);
        NeoForge.EVENT_BUS.register(QuestEventsPlayerLifecycle.class);



        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(ClientEvents::onHotbarScroll);
            NeoForge.EVENT_BUS.addListener(ClientEvents::onInventoryScroll);
        }

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModSounds.register(modEventBus);
        ModAttachments.register(modEventBus);


        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.COPPER_COIN);
            event.accept(ModItems.IRON_COIN);
            event.accept(ModItems.GOLD_COIN);
            event.accept(ModItems.DIAMOND_COIN);
            event.accept(ModItems.NETHERITE_COIN);
            event.accept(ModItems.COUNTERFEIT_CASH);
            event.accept(ModItems.COIN_POUCH);
            event.accept(ModBlocks.QUEST_BOARD_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }


}
