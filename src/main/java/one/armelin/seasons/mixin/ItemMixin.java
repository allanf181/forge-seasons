package one.armelin.seasons.mixin;

import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.resources.CropConfigs;
import one.armelin.seasons.utils.Season;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("HEAD"), method = "appendTooltip")
    public void appendTooltipInject(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type, CallbackInfo ci) {
        if(ForgeSeasons.CONFIG.isSeasonMessingCrops()) {
            Season season = ForgeSeasons.getCurrentSeason();
            Item item = stack.getItem();
            Block block = ForgeSeasons.SEEDS_MAP.getOrDefault(item, null);
            if (block != null) {


                MinecraftClient client = MinecraftClient.getInstance();
                long handle = client.getWindow().getHandle();
                KeyBinding sneakKey = client.options.sneakKey;
                InputUtil.Key boundKey = sneakKey.boundKey;
                Identifier cropIdentifier = Registries.BLOCK.getId(block);
                float multiplier = CropConfigs.getSeasonCropMultiplier(cropIdentifier, season);
                boolean sneak = false;
                if(boundKey.getCategory() == InputUtil.Type.MOUSE) {
                    sneak = GLFW.glfwGetMouseButton(handle, boundKey.getCode()) == 1;
                }else if(boundKey.getCategory() == InputUtil.Type.KEYSYM) {
                    sneak = GLFW.glfwGetKey(handle, boundKey.getCode()) == 1;
                }
                if (multiplier == 0f) {
                    tooltip.add(Text.translatable("tooltip.seasons.not_grow").formatted(Formatting.RED));
                } else if (multiplier == 1.0f) {
                    tooltip.add(Text.translatable("tooltip.seasons.normal_grow").formatted(Formatting.GREEN));
                }
                if (sneak) {
                    if (multiplier != 0f && multiplier < 1.0f) {
                        tooltip.add(Text.translatable("tooltip.seasons.slowed_grow").formatted(Formatting.GOLD));
                    } else if (multiplier > 1.0f) {
                        tooltip.add(Text.translatable("tooltip.seasons.faster_grow").formatted(Formatting.LIGHT_PURPLE));
                    }

                    for (Season s : Season.values()) {
                        if(season == s) {
                            tooltip.add(Text.translatable("tooltip.seasons.season_speed",
                                    Text.translatable(s.getTranslationKey()).formatted(s.getFormatting(), Formatting.UNDERLINE),
                                    Text.translatable("tooltip.seasons.season_delimitator").formatted(s.getFormatting()),
                                    Text.literal(String.format("%.1f", (CropConfigs.getSeasonCropMultiplier(cropIdentifier, s) * 100))).formatted(Formatting.WHITE)
                            ));
                        } else{
                            tooltip.add(Text.translatable("tooltip.seasons.season_speed",
                                    Text.translatable(s.getTranslationKey()).formatted(s.getFormatting()),
                                    Text.translatable("tooltip.seasons.season_delimitator").formatted(s.getFormatting()),
                                    Text.literal(String.format("%.1f", (CropConfigs.getSeasonCropMultiplier(cropIdentifier, s) * 100)))).formatted(Formatting.GRAY)
                            );
                        }
                    }
                } else {
                    if (multiplier != 0f && multiplier < 1.0f) {
                        tooltip.add(Text.translatable("tooltip.seasons.combined_grow",
                                Text.translatable("tooltip.seasons.slowed_grow"),
                                String.format("%.1f", (CropConfigs.getSeasonCropMultiplier(cropIdentifier, season) * 100))).formatted(Formatting.GOLD)
                        );
                    } else if (multiplier > 1.0f) {
                        tooltip.add(Text.translatable("tooltip.seasons.combined_grow",
                                Text.translatable("tooltip.seasons.faster_grow"),
                                String.format("%.1f", (CropConfigs.getSeasonCropMultiplier(cropIdentifier, season) * 100))).formatted(Formatting.LIGHT_PURPLE)
                        );
                    }
                    tooltip.add(Text.translatable("tooltip.seasons.show_more", sneakKey.getBoundKeyLocalizedText().copy().formatted(Formatting.BLUE)).formatted(Formatting.GRAY));
                }
            }
        }
    }

}

