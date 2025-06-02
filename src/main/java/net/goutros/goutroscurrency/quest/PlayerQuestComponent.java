package net.goutros.goutroscurrency.quest;

import net.goutros.goutroscurrency.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;

import static net.goutros.goutroscurrency.block.QuestBoardBlock.QUEST_COOLDOWN_MS;

public class PlayerQuestComponent implements INBTSerializable<CompoundTag> {
    private final Map<UUID, Quest> quests = new LinkedHashMap<>();
    private long lastQuestTime = 0;
    private Quest previewQuest;

    public long getLastQuestTime() { return lastQuestTime; }
    public void setLastQuestTime(long time) { this.lastQuestTime = time; setChanged(); }

    public Quest getPreviewQuest() { return previewQuest; }
    public void setPreviewQuest(Quest q) { this.previewQuest = q; setChanged(); }
    public void clearPreviewQuest() { this.previewQuest = null; setChanged(); }

    public boolean isQuestAvailable() {
        return System.currentTimeMillis() - lastQuestTime >= QUEST_COOLDOWN_MS;
    }

    public List<Quest> getActiveQuests() {
        return new ArrayList<>(quests.values());
    }

    public boolean hasQuest(UUID id) { return quests.containsKey(id); }
    public Quest get(UUID id) { return quests.get(id); }

    public void addQuest(Quest quest) { quests.put(quest.getId(), quest); setChanged(); }
    public void removeQuest(UUID id) { quests.remove(id); setChanged(); }
    public void removeQuest(Quest quest) { quests.remove(quest.getId()); setChanged(); }
    public void clear() { quests.clear(); }
    public void updateProgress(UUID id, int newProgress) {
        Quest quest = quests.get(id);
        if (quest != null) quests.put(id, quest.copyWithProgress(newProgress));
    }

    public Map<UUID, Quest> getQuests() { return Collections.unmodifiableMap(quests); }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag questList = new ListTag();
        for (Quest quest : quests.values()) questList.add(quest.toTag());
        tag.put("quests", questList);
        tag.putLong("lastQuestTime", lastQuestTime);
        if (previewQuest != null) tag.put("previewQuest", previewQuest.toTag());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        quests.clear();
        ListTag questList = nbt.getList("quests", Tag.TAG_COMPOUND);
        for (Tag t : questList) {
            Quest quest = Quest.fromTag((CompoundTag)t);
            quests.put(quest.getId(), quest);
        }
        lastQuestTime = nbt.getLong("lastQuestTime");
        previewQuest = nbt.contains("previewQuest") ? Quest.fromTag(nbt.getCompound("previewQuest")) : null;
    }

    public void setChanged() {}

    public static PlayerQuestComponent get(Player player) {
        @SuppressWarnings("unchecked")
        AttachmentType<PlayerQuestComponent> type =
                (AttachmentType<PlayerQuestComponent>) NeoForgeRegistries.ATTACHMENT_TYPES.get(
                        ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID)
                );
        return type != null ? player.getData(type) : null;
    }
}
