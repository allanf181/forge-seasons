package one.armelin.seasons.mixin;

import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.ForgeSeasonsClient;
import one.armelin.seasons.utils.Season;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public class BlockModelsMixin {

    @Inject(at = @At("RETURN"), method = "getModel", cancellable = true)
    public void injectSeasonalModel(BlockState state, CallbackInfoReturnable<BakedModel> cir) {
        BakedModel originalModel = cir.getReturnValue();
        Season season = ForgeSeasons.getCurrentSeason();
        if(ForgeSeasonsClient.originalToSeasonModelMap.containsKey(originalModel) && ForgeSeasonsClient.originalToSeasonModelMap.get(originalModel).containsKey(season)) {
            cir.setReturnValue(ForgeSeasonsClient.originalToSeasonModelMap.get(originalModel).get(season));
        }
    }

}
