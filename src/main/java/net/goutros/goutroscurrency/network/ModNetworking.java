package net.goutros.goutroscurrency.network;

import net.goutros.goutroscurrency.quest.Quest;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.List;

public class ModNetworking {

    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar("goutroscurrency")
                .playToServer(
                        CoinPouchSelectPacket.TYPE,
                        CoinPouchSelectPacket.STREAM_CODEC,
                        (pkt, context) -> {
                            if (context instanceof ServerPayloadContext serverCtx)
                                CoinPouchSelectPacket.handle(pkt, serverCtx);
                        }
                )
                .playToClient(
                        QuestSyncPacket.TYPE,
                        QuestSyncPacket.STREAM_CODEC,
                        (packet, context) -> {
                            if (context instanceof ClientPayloadContext clientCtx)
                                QuestSyncPacket.handle(packet, clientCtx);
                        }
                );
    }

    public static void sendQuestSyncPacket(ServerPlayer player, List<Quest> quests, long lastQuestTime, Quest previewQuest) {
        player.connection.send(new QuestSyncPacket(quests, lastQuestTime, previewQuest));
    }
}
