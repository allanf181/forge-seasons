package one.armelin.seasons.utils;

import one.armelin.seasons.ForgeSeasons;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;

import java.util.Locale;

public enum Season implements StringIdentifiable {
    SPRING(2, Formatting.GREEN, Formatting.DARK_GREEN),
    SUMMER(3, Formatting.GOLD, Formatting.GOLD),
    FALL(1, Formatting.RED, Formatting.RED),
    WINTER(0, Formatting.AQUA, Formatting.DARK_AQUA);

    private final int temperature;
    private final Formatting formatting;
    private final Formatting darkFormatting;

    Season(int temperature, Formatting formatting, Formatting darkFormatting) {
        this.temperature = temperature;
        this.formatting = formatting;
        this.darkFormatting = darkFormatting;
    }

    public int getTemperature() {
        return temperature;
    }

    public Formatting getFormatting() {
        return formatting;
    }

    public Formatting getDarkFormatting() {
        return darkFormatting;
    }

    public String getTranslationKey() {
        return "tooltip.seasons."+name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String asString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public Season getNext() {
        return switch (this) {
            case SUMMER -> Season.FALL;
            case FALL -> Season.WINTER;
            case WINTER -> Season.SPRING;
            default -> Season.SUMMER;
        };
    }

    public int getSeasonLength() {
        return switch (this) {
            case SUMMER -> ForgeSeasons.CONFIG.getSummerLength();
            case FALL -> ForgeSeasons.CONFIG.getFallLength();
            case WINTER -> ForgeSeasons.CONFIG.getWinterLength();
            default -> ForgeSeasons.CONFIG.getSpringLength();
        };
    }
}
