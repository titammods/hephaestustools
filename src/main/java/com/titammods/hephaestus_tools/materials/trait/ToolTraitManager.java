package com.titammods.hephaestus_tools.materials.trait;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.titammods.hephaestus_tools.HephaestusTools;
import com.titammods.hephaestus_tools.network.SyncToolTraitsPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = HephaestusTools.MOD_ID)
public final class ToolTraitManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTraitManager.class);
    private static final FileToIdConverter FILES = FileToIdConverter.json("hephaestus_tools/tool_traits");
    private static final String CONDITIONS_KEY = "neoforge:conditions";

    public static final Identifier LISTENER_ID =
            Identifier.fromNamespaceAndPath(HephaestusTools.MOD_ID, "tool_traits");

    private static final ToolTraitManager INSTANCE = new ToolTraitManager();

    private volatile Map<Identifier, List<TraitEntry>> traits = Map.of();

    private ToolTraitManager() {}

    public static ToolTraitManager getInstance() {
        return INSTANCE;
    }

    public List<TraitEntry> get(Item tool) {
        return traits.getOrDefault(BuiltInRegistries.ITEM.getKey(tool), List.of());
    }

    public Map<Identifier, List<TraitEntry>> all() {
        return traits;
    }

    private void replaceAll(Map<Identifier, List<TraitEntry>> source) {
        Map<Identifier, List<TraitEntry>> copy = new LinkedHashMap<>();
        source.forEach((tool, list) -> copy.put(tool, List.copyOf(list)));
        traits = Map.copyOf(copy);
        ToolTraits.invalidate();
    }

    public static void receiveSync(Map<Identifier, List<TraitEntry>> synced) {
        INSTANCE.replaceAll(synced);
        LOGGER.info("[HephaestusTools] Tool traits received from the server: {}", synced.size());
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(LISTENER_ID, new Loader(event.getConditionContext()));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        SyncToolTraitsPayload payload = new SyncToolTraitsPayload(INSTANCE.traits);
        if (event.getPlayer() != null) {
            PacketDistributor.sendToPlayer(event.getPlayer(), payload);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
        }
    }

    public static final class Loader implements ResourceManagerReloadListener {

        private final ICondition.IContext context;

        public Loader(ICondition.IContext context) {
            this.context = context;
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            Map<Identifier, List<TraitEntry>> loaded = new LinkedHashMap<>();
            int files = 0;
            int failed = 0;
            int skipped = 0;

            for (Map.Entry<Identifier, Resource> entry : FILES.listMatchingResources(manager).entrySet()) {
                Identifier file = entry.getKey();
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!conditionsMet(json)) {
                        skipped++;
                        continue;
                    }
                    ToolTraitDefinition definition = ToolTraitDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(msg -> new IllegalStateException(msg));
                    loaded.computeIfAbsent(definition.tool(), id -> new ArrayList<>()).addAll(definition.traits());
                    files++;
                } catch (Exception e) {
                    LOGGER.error("[HephaestusTools] Could not load tool traits {}: {}", file, e.getMessage());
                    failed++;
                }
            }

            INSTANCE.replaceAll(loaded);
            LOGGER.info("[HephaestusTools] Tool traits loaded: {} OK, {} failed, {} skipped by condition", files, failed, skipped);
        }

        private boolean conditionsMet(JsonElement json) {
            if (!json.isJsonObject() || !json.getAsJsonObject().has(CONDITIONS_KEY)) return true;
            List<ICondition> conditions = ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, json.getAsJsonObject().get(CONDITIONS_KEY))
                    .getOrThrow(msg -> new IllegalStateException(msg));
            for (ICondition condition : conditions) {
                if (!condition.test(context)) return false;
            }
            return true;
        }

        @Override
        public String getName() {
            return "HephaestusTools Tool Traits";
        }
    }
}