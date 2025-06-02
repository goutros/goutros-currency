package net.goutros.goutroscurrency.quest;

import net.goutros.goutroscurrency.ModAttachments;
import net.goutros.goutroscurrency.network.ModNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.UUID;

public class QuestManager {
    public static Quest getQuestById(Player player, UUID id) {
        PlayerQuestComponent comp = PlayerQuestComponent.get(player);
        return comp != null ? comp.get(id) : null;
    }

    public static void addQuest(Player player, Quest quest) {
        PlayerQuestComponent comp = PlayerQuestComponent.get(player);
        if (comp != null) comp.addQuest(quest);
    }

    public static void removeQuest(Player player, UUID id) {
        PlayerQuestComponent comp = PlayerQuestComponent.get(player);
        if (comp != null) comp.removeQuest(id);
    }

    public static void updatePlayerQuestPreview(ServerPlayer player) {
        PlayerQuestComponent comp = PlayerQuestComponent.get(player);
        if (comp == null) return;

        if (comp.getActiveQuests().size() >= 2) {
            comp.clearPreviewQuest();
        } else if (comp.isQuestAvailable()) {
            // Only generate a new preview if not present
            if (comp.getPreviewQuest() == null) {
                Quest newPreview = generateRandomGatherQuest(player.server, player.getRandom());
                comp.setPreviewQuest(newPreview);
            }
        } else {
            comp.clearPreviewQuest();
        }
        // Always sync to client
        ModNetworking.sendQuestSyncPacket(player, comp.getActiveQuests(), comp.getLastQuestTime(), comp.getPreviewQuest());
    }

    public static Quest generateRandomGatherQuest(net.minecraft.server.MinecraftServer server, RandomSource random) {
        List<QuestPreset> presets = QuestPresetsConfig.getPresets(server);
        if (presets.isEmpty()) return null;

        QuestPreset preset = presets.get(random.nextInt(presets.size()));

        String material = null;
        if (!preset.items.isEmpty()) {
            material = preset.items.get(random.nextInt(preset.items.size()));
        } else if (!preset.tags.isEmpty()) {
            material = "#" + preset.tags.get(random.nextInt(preset.tags.size()));
        } else {
            return null;
        }

        int amount = preset.amountMin + random.nextInt(preset.amountMax - preset.amountMin + 1);
        int coins = preset.coinMin + random.nextInt(preset.coinMax - preset.coinMin + 1);

        return new Quest(
                preset.type,
                material,
                amount,
                "Collect " + amount + "x " + material,
                coins,
                0,
                UUID.randomUUID(),
                false
        );
    }
}
