package one.armelin.seasons.mixed;

import net.minecraft.world.biome.Biome;

public interface BiomeMixed {

    Biome.Weather getOriginalWeather();

    void setOriginalWeather(Biome.Weather originalWeather);


}
