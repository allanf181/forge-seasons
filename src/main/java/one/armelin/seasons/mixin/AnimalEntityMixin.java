package one.armelin.seasons.mixin;

import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.utils.Season;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin {

    @Inject(at = @At("HEAD"), method = "breed", cancellable = true)
    public void breedInject(ServerWorld serverWorld, AnimalEntity animalEntity, CallbackInfo info) {
        if(ForgeSeasons.getCurrentSeason(serverWorld) == Season.WINTER && !ForgeSeasons.CONFIG.doAnimalsBreedInWinter()) {
            info.cancel();
        }
    }

}
