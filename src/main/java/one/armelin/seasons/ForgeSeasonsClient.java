package one.armelin.seasons;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import one.armelin.seasons.commands.SeasonDebugCommand;
import one.armelin.seasons.payload.ConfigSyncPacket;
import one.armelin.seasons.payload.UpdateCropsPaycket;
import one.armelin.seasons.resources.CropConfigs;
import one.armelin.seasons.resources.FoliageSeasonColors;
import one.armelin.seasons.resources.GrassSeasonColors;
import one.armelin.seasons.utils.CompatWarnState;
import one.armelin.seasons.utils.CropConfig;
import one.armelin.seasons.utils.ModConfig;
import one.armelin.seasons.utils.Season;
import one.armelin.seasons.utils.SeasonalFertilizable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

import static one.armelin.seasons.ForgeSeasons.CONFIG;
import static one.armelin.seasons.ForgeSeasons.MOD_ID;
import static one.armelin.seasons.ForgeSeasons.MOD_NAME;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
public class ForgeSeasonsClient implements ClientModInitializer {

    private static boolean isServerConfig = false;
    private static ModConfig clientConfig = null;
    private static final Map<RegistryKey<World>, Season> lastRenderedSeasonMap = new HashMap<>();

    public static final Map<BakedModel, Map<Season, BakedModel>> originalToSeasonModelMap = new HashMap<>();

    @Override
    public void onInitializeClient() {
        clientConfig = ForgeSeasons.CONFIG;
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new GrassSeasonColors());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FoliageSeasonColors());
        
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ForgeSeasons.SEEDS_MAP.clear();
            Registries.ITEM.forEach(item -> {
                if (item instanceof BlockItem) {
                    Block block = ((BlockItem) item).getBlock();
                    if (block instanceof SeasonalFertilizable) {
                        ForgeSeasons.SEEDS_MAP.put(item, ((BlockItem) item).getBlock());
                    }
                }
            });
        });
        
        ClientTickEvents.END_WORLD_TICK.register((clientWorld) -> {
            if (ForgeSeasons.getCurrentSeason(clientWorld) != lastRenderedSeasonMap.get(clientWorld.getRegistryKey())) {
                lastRenderedSeasonMap.put(clientWorld.getRegistryKey(), ForgeSeasons.getCurrentSeason(clientWorld));
                MinecraftClient.getInstance().worldRenderer.reload();
            }
        });
        
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPacket.ID, (payload, handler) -> {
            String configJson = payload.config();
            handler.client().execute(() -> {
                ForgeSeasons.CONFIG = ForgeSeasons.GSON.fromJson(configJson, ModConfig.class);
                isServerConfig = true;
                ForgeSeasons.LOGGER.info("[" + MOD_NAME + "] Received dedicated server config.");
            });
        });
        
        ClientPlayNetworking.registerGlobalReceiver(UpdateCropsPaycket.ID, (payload, context) -> {
            CropConfig receivedConfig = payload.cropConfig();
            HashMap<Identifier, CropConfig> receivedMap = payload.cropConfigMap();
            
            context.client().execute(() -> {
                CropConfigs.receiveConfig(receivedConfig, receivedMap);
                ForgeSeasons.LOGGER.info("[" + MOD_NAME + "] Received dedicated server crops.");
            });
        });
        
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (CONFIG.shouldNotifyCompat()) {
                CompatWarnState.join(client);
            }
            if (!client.isIntegratedServerRunning()) {
                ForgeSeasons.LOGGER.info("[" + MOD_NAME + "] Joined dedicated server, asking for config.");
                ClientPlayNetworking.send(new ConfigSyncPacket("request"));
            }
        });
        
        ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
            CropConfigs.clear();
            if (isServerConfig && clientConfig != null) {
                ForgeSeasons.LOGGER.info("[" + MOD_NAME + "] Left dedicated server, restoring config.");
                ForgeSeasons.CONFIG = clientConfig;
                isServerConfig = false;
            }
        }));
        
        if (FabricLoader.getInstance().isDevelopmentEnvironment() || CONFIG.isDebugCommandEnabled()) {
            ClientCommandRegistrationCallback.EVENT.register((SeasonDebugCommand::register));
        }
        
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent((container) -> {
            ResourceManagerHelper.registerBuiltinResourcePack(ForgeSeasons.identifier("seasonal_lush_caves"), container, Text.literal("Seasonal Lush Caves"), ResourcePackActivationType.DEFAULT_ENABLED);
        });
    }

    public ForgeSeasonsClient(IEventBus modBus) {
        onInitializeClient();
    }
}
