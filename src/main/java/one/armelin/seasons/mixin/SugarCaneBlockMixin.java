package one.armelin.seasons.mixin;

import net.neoforged.neoforge.common.util.TriState;
import one.armelin.seasons.ForgeSeasons;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), method = "canPlaceAt", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void allowSugarCaneToGrowOnIce(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState blockstate, TriState soilDecision, BlockPos blockpos, Iterator var7, Direction direction, BlockState blockstate1) {
        if(!ForgeSeasons.CONFIG.shouldIceBreakSugarCane() && blockstate1.isOf(Blocks.ICE)) {
            cir.setReturnValue(true);
        }
    }

}
