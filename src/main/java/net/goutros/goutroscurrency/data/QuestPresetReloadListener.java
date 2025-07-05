package net.goutros.goutroscurrency.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.goutros.goutroscurrency.quest.QuestPreset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class QuestPresetReloadListener extends SimpleJsonResourceReloadListener {
   public static final Map<ResourceLocation, QuestPreset> LOADED_PRESETS = new HashMap();

   public QuestPresetReloadListener() {
      super((new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create(), "quests");
      System.out.println("[QuestPresetReloadListener] Constructed!");
   }

   protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
      LOADED_PRESETS.clear();
      System.out.println("[QuestPresetReloadListener] Found " + jsonMap.size() + " JSON entries.");
      Iterator var4 = jsonMap.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<ResourceLocation, JsonElement> entry = (Entry)var4.next();
         System.out.println("  -> Parsing: " + String.valueOf(entry.getKey()));

         try {
            JsonObject o = GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), "quest");
            if (!o.has("type")) {
               System.err.println("[QuestPresetReloadListener] Skipping " + String.valueOf(entry.getKey()) + " due to missing 'type'");
            } else {
               String type = o.get("type").getAsString();
               int amountMin = GsonHelper.getAsInt(o, "amount_min", 1);
               int amountMax = GsonHelper.getAsInt(o, "amount_max", 1);
               int coinMin = GsonHelper.getAsInt(o, "coin_min", 1);
               int coinMax = GsonHelper.getAsInt(o, "coin_max", 1);
               int distanceMin = GsonHelper.getAsInt(o, "distance_min", 0);
               int distanceMax = GsonHelper.getAsInt(o, "distance_max", 0);
               JsonObject whitelist = o.has("whitelist") ? o.getAsJsonObject("whitelist") : new JsonObject();
               JsonObject blacklist = o.has("blacklist") ? o.getAsJsonObject("blacklist") : new JsonObject();
               List<String> whitelistItems = this.getList(whitelist, "items");
               List<String> whitelistTags = this.getList(whitelist, "tags");
               List<String> whitelistEntities = GsonHelper.getAsJsonArray(o, "whitelist_entities", (JsonArray)null) != null ? GsonHelper.getAsJsonArray(o, "whitelist_entities").asList().stream().map(JsonElement::getAsString).toList() : List.of("*");
               List<String> blacklistItems = this.getList(blacklist, "items");
               List<String> blacklistTags = this.getList(blacklist, "tags");
               List<String> blacklistEntities = this.getList(blacklist, "entities");
               QuestPreset preset = QuestPreset.fromJson(type, amountMin, amountMax, coinMin, coinMax, whitelistItems, whitelistTags, whitelistEntities, blacklistItems, blacklistTags, blacklistEntities, distanceMin, distanceMax);
               LOADED_PRESETS.put((ResourceLocation)entry.getKey(), preset);
            }
         } catch (Exception var23) {
            System.err.println("[QuestPresetReloadListener] Failed to load " + String.valueOf(entry.getKey()));
            var23.printStackTrace();
         }
      }

      System.out.println("[QuestPresetReloadListener] Reloaded " + LOADED_PRESETS.size() + " presets.");
   }

   private List<String> getList(JsonObject obj, String key) {
      List<String> result = new ArrayList();
      if (!obj.has(key)) {
         return result;
      } else {
         Iterator var4 = obj.getAsJsonArray(key).iterator();

         while(var4.hasNext()) {
            JsonElement el = (JsonElement)var4.next();
            result.add(el.getAsString());
         }

         return result;
      }
   }
}
