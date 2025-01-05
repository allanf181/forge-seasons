package one.armelin.seasons.mixin;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.mixed.JsonUnbakedModelMixed;
import one.armelin.seasons.utils.Season;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static one.armelin.seasons.ForgeSeasons.MOD_NAME;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {

    @Inject(at = @At("RETURN"), method = "loadModelFromJson", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void injectSeasonalModels(Identifier id, CallbackInfoReturnable<JsonUnbakedModel> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        Optional<Resource> optional = client.getResourceManager().getResource(Identifier.of(id.getNamespace(), "seasons/models/" + id.getPath() + ".json"));
        if(optional.isPresent()) {
            Resource resource = optional.get();
            try {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();
                if (json.has("textures")) {
                    JsonObject textures = json.getAsJsonObject("textures");
                    JsonUnbakedModelMixed model = (JsonUnbakedModelMixed) cir.getReturnValue();
                    model.setSeasonalTextureMap(new HashMap<>());
                    for (Season s : Season.values()) {
                        String code = s.name().toLowerCase(Locale.ROOT);
                        if (textures.has(code)) {
                            Map<String, Either<SpriteIdentifier, String>> map = Maps.newHashMap();
                            JsonObject jsonObject = textures.getAsJsonObject(code);
                            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                                map.put(entry.getKey(), JsonUnbakedModel.Deserializer.resolveReference(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, entry.getValue().getAsString()));
                            }
                            model.getSeasonalTextureMap().put(s, map);
                        }
                    }
                }
            }catch (Exception e) {
                ForgeSeasons.LOGGER.error("["+ MOD_NAME +"] Failed loading season texture variants for "+id, e);
            }
        }
    }


}
