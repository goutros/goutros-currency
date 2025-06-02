package net.goutros.goutroscurrency.client;

import net.goutros.goutroscurrency.ModAttachments;
import net.goutros.goutroscurrency.quest.PlayerQuestComponent;
import net.goutros.goutroscurrency.quest.Quest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.*;

import static net.goutros.goutroscurrency.GoutrosCurrency.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientHudEvents {
    private static final Map<UUID, Long> popTimers = new HashMap<>();
    private static final Map<UUID, Integer> lastProgress = new HashMap<>();
    private static final Map<UUID, Long> completionTimers = new HashMap<>();
    private static final long POP_TIME_MS = 400L;
    private static final long COMPLETE_HOLD_MS = 1000L;
    private static final int QUEST_GAP = 6;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.screen == null && mc.options.keyPlayerList.isDown())) return;

        @SuppressWarnings("unchecked")
        AttachmentType<PlayerQuestComponent> questType =
                (AttachmentType<PlayerQuestComponent>) NeoForgeRegistries.ATTACHMENT_TYPES.get(
                        ResourceLocation.fromNamespaceAndPath("goutroscurrency", ModAttachments.PLAYER_QUEST_ID));
        if (questType == null) return;

        PlayerQuestComponent comp = mc.player.getData(questType);
        if (comp == null) return;

        List<Quest> quests = comp.getActiveQuests();
        long now = System.currentTimeMillis();

        // Track progress and update timers
        for (Quest quest : quests) {
            UUID id = quest.getId();
            int last = lastProgress.getOrDefault(id, -999);
            if (quest.progress != last || !lastProgress.containsKey(id)) {
                popTimers.put(id, now);
                lastProgress.put(id, quest.progress);
                if (quest.isComplete()) {
                    completionTimers.put(id, now);
                }
            }
        }

        // Add fading out dummy quests
        List<Quest> displayList = new ArrayList<>(quests);
        for (Map.Entry<UUID, Long> entry : completionTimers.entrySet()) {
            UUID id = entry.getKey();
            if (quests.stream().noneMatch(q -> q.getId().equals(id))) {
                long timeSince = now - entry.getValue();
                if (timeSince < COMPLETE_HOLD_MS) {
                    Quest dummy = new Quest("gather", "minecraft:stone", 1, "Completed Quest", 1, 1, id, true);
                    displayList.add(dummy);
                }
            }
        }

        // Sort by last update
        displayList.sort(Comparator.comparing(q -> -popTimers.getOrDefault(q.getId(), 0L)));

        GuiGraphics gui = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        float yBase = 10;

        for (int i = 0; i < displayList.size(); i++) {
            Quest quest = displayList.get(i);
            UUID id = quest.getId();
            float centerX = screenWidth / 2f;

            long popStart = popTimers.getOrDefault(id, 0L);
            float t = Math.min((now - popStart) / (float) POP_TIME_MS, 1f);

            // Bounce scale effect
            float s = 1.70158f;
            float bounce = (t < 1.0f) ? (1.0f + 0.01f * ((--t) * t * ((s + 1) * t + s) + 1)) : 1.0f;

            // Fade out effect
            boolean fading = completionTimers.containsKey(id) && quests.stream().noneMatch(q -> q.getId().equals(id));
            float fadeT = fading ? (now - completionTimers.get(id)) / (float) COMPLETE_HOLD_MS : 0f;
            float alpha = fading ? (float)(0.5 * (1 + Math.cos(Math.PI * Math.min(fadeT, 1)))) : 1f;

            int baseColor = fading || quest.isComplete() ? 0x55FF55 : 0xFFFFFF55;
            if ((quest.isComplete() || fading) && t < 0.5f) baseColor = 0xAAFFAA;
            int color = ((int)(alpha * 255) << 24) | (baseColor & 0x00FFFFFF);

            float y = yBase + i * (mc.font.lineHeight + QUEST_GAP);
            drawQuest(gui, centerX, y, quest, bounce, color);
        }

        // Remove fully faded-out quests from the component
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : completionTimers.entrySet()) {
            if (now - entry.getValue() > COMPLETE_HOLD_MS) {
                UUID id = entry.getKey();
                comp.removeQuest(id);
                toRemove.add(id);
            }
        }

        toRemove.forEach(completionTimers::remove);
        lastProgress.keySet().removeIf(id -> quests.stream().noneMatch(q -> q.getId().equals(id)));
    }

    private static void drawQuest(GuiGraphics gui, float centerX, float y, Quest quest, float scale, int color) {
        Minecraft mc = Minecraft.getInstance();
        String text = quest.isComplete()
                ? "COMPLETED: " + quest.getDescription() + " [" + quest.amount + "/" + quest.amount + "]"
                : "QUEST: " + quest.getDescription() + " [" + quest.progress + "/" + quest.amount + "]";

        ItemStack stack = getItemStackForMaterial(quest.material);
        int textWidth = mc.font.width(text);
        int iconSize = 16;
        int gap = 6;
        int totalWidth = iconSize + gap + textWidth;

        gui.pose().pushPose();
        gui.pose().translate(centerX, y, 0);
        gui.pose().scale(scale, scale, 1f);
        gui.pose().translate(-totalWidth / 2f, -iconSize / 2f, 0);

        if (!stack.isEmpty()) {
            gui.renderItem(stack, 0, 0);
        }

        gui.drawString(mc.font, text, iconSize + gap, (iconSize - mc.font.lineHeight) / 2, color, true);
        gui.pose().popPose();
    }

    private static ItemStack getItemStackForMaterial(String materialId) {
        Registry<Item> registry = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM)
                : BuiltInRegistries.ITEM;
        ResourceLocation loc = ResourceLocation.tryParse(materialId);
        if (loc != null) {
            Item item = registry.get(loc);
            if (item != null) return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
}
