package net.goutros.goutroscurrency.quest;

import com.google.gson.*;
import net.minecraft.server.MinecraftServer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class QuestPresetsConfig {
    public static List<QuestPreset> GATHER_PRESETS = new ArrayList<>();

    public static void load() {
        String[] possiblePaths = {
                "/goutroscurrency/quest_presets.json",
                "goutroscurrency/quest_presets.json",
                "/assets/goutroscurrency/quest_presets.json",
                "assets/goutroscurrency/quest_presets.json"
        };
        InputStream stream = null;
        String foundPath = null;
        for (String path : possiblePaths) {
            stream = QuestPresetsConfig.class.getResourceAsStream(path);
            if (stream != null) {
                foundPath = path;
                break;
            }
        }
        if (stream == null) {
            System.out.println("[QuestPresetsConfig] No quest preset resource found!");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(stream)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray presets = json.getAsJsonArray("gather_presets");
            if (presets == null) return;

            GATHER_PRESETS.clear();

            for (JsonElement elem : presets) {
                JsonObject o = elem.getAsJsonObject();
                String type = o.get("type").getAsString();
                int min = o.get("amount_min").getAsInt();
                int max = o.get("amount_max").getAsInt();
                int coinMin = o.has("coin_min") ? o.get("coin_min").getAsInt() : 1;
                int coinMax = o.has("coin_max") ? o.get("coin_max").getAsInt() : 1;

                List<String> items = new ArrayList<>();
                for (JsonElement i : o.getAsJsonArray("items")) items.add(i.getAsString());

                List<String> tags = new ArrayList<>();
                for (JsonElement t : o.getAsJsonArray("tags")) tags.add(t.getAsString());

                GATHER_PRESETS.add(new QuestPreset(type, min, max, coinMin, coinMax, items, tags));
            }
        } catch (Exception e) {
            System.out.println("[QuestPresetsConfig] EXCEPTION while reading quest presets:");
            e.printStackTrace();
        }
    }

    public static List<QuestPreset> getPresets(MinecraftServer server) {
        return GATHER_PRESETS;
    }

}
