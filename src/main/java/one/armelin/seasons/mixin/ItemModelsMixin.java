package one.armelin.seasons.mixin;

import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.ForgeSeasonsClient;
import one.armelin.seasons.utils.Season;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModels.class)
public class ItemModelsMixin {

    @Inject(at = @At("RETURN"), method = "getModel(Lnet/minecraft/item/Item;)Lnet/minecraft/client/render/model/BakedModel;", cancellable = true)
    public void injectSeasonalModel(Item item, CallbackInfoReturnable<BakedModel> cir) {
        BakedModel originalModel = cir.getReturnValue();
        Season season = ForgeSeasons.getCurrentSeason();
        if(ForgeSeasonsClient.originalToSeasonModelMap.containsKey(originalModel) && ForgeSeasonsClient.originalToSeasonModelMap.get(originalModel).containsKey(season)) {
            cir.setReturnValue(ForgeSeasonsClient.originalToSeasonModelMap.get(originalModel).get(season));
        }
    }

}
