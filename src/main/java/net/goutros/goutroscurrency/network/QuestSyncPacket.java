package net.goutros.goutroscurrency.network;

import net.goutros.goutroscurrency.ModAttachments;
import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.goutros.goutroscurrency.quest.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.handling.ClientPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class QuestSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("goutroscurrency", "quest_sync");
    public static final CustomPacketPayload.Type<QuestSyncPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public final List<Quest> quests;
    public final long lastQuestTime;
    public final Quest previewQuest;

    public QuestSyncPacket(List<Quest> quests, long lastQuestTime, Quest previewQuest) {
        this.quests = quests;
        this.lastQuestTime = lastQuestTime;
        this.previewQuest = previewQuest;
    }

    @Override
    public Type<QuestSyncPacket> type() { return TYPE; }

    public static final StreamCodec<RegistryFriendlyByteBuf, QuestSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        ListTag list = new ListTag();
                        for (Quest q : packet.quests) list.add(q.toTag());
                        CompoundTag tag = new CompoundTag();
                        tag.put("quests", list);
                        tag.putLong("lastQuestTime", packet.lastQuestTime);
                        if (packet.previewQuest != null)
                            tag.put("previewQuest", packet.previewQuest.toTag());
                        buf.writeNbt(tag);
                    },
                    (buf) -> {
                        CompoundTag tag = buf.readNbt();
                        List<Quest> quests = new ArrayList<>();
                        long lastQuestTime = 0L;
                        Quest previewQuest = null;
                        if (tag != null) {
                            if (tag.contains("quests")) {
                                ListTag list = tag.getList("quests", CompoundTag.TAG_COMPOUND);
                                for (int i = 0; i < list.size(); i++)
                                    quests.add(Quest.fromTag((CompoundTag) list.get(i)));
                            }
                            if (tag.contains("lastQuestTime"))
                                lastQuestTime = tag.getLong("lastQuestTime");
                            if (tag.contains("previewQuest"))
                                previewQuest = Quest.fromTag(tag.getCompound("previewQuest"));
                        }
                        return new QuestSyncPacket(quests, lastQuestTime, previewQuest);
                    }
            );

    public static void handle(QuestSyncPacket packet, ClientPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            @SuppressWarnings("unchecked")
            AttachmentType<PlayerQuestComponent> questType = (AttachmentType<PlayerQuestComponent>)
                    NeoForgeRegistries.ATTACHMENT_TYPES.get(
                            ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID));
            if (questType == null) return;

            PlayerQuestComponent comp = mc.player.getData(questType);
            comp.clear();
            for (Quest quest : packet.quests) comp.addQuest(quest);
            comp.setLastQuestTime(packet.lastQuestTime);
            comp.setPreviewQuest(packet.previewQuest); // <--- SYNC PREVIEW QUEST!
        });
    }
}
