package one.armelin.seasons.commands;

import com.mojang.brigadier.CommandDispatcher;
import one.armelin.seasons.ForgeSeasons;
import one.armelin.seasons.utils.Season;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TimeCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class SeasonCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("season")
            .then(CommandManager.literal("set").requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spring")
                    .executes(
                        context -> TimeCommand.executeSet(context.getSource(),
                        switch(ForgeSeasons.CONFIG.getStartingSeason()) {
                            case SPRING -> 0;
                            case WINTER -> ForgeSeasons.CONFIG.getWinterLength();
                            case FALL -> ForgeSeasons.CONFIG.getFallLength() + ForgeSeasons.CONFIG.getWinterLength();
                            case SUMMER -> ForgeSeasons.CONFIG.getSummerLength() + ForgeSeasons.CONFIG.getFallLength() + ForgeSeasons.CONFIG.getWinterLength();
                        }
                    ))
                )
                .then(CommandManager.literal("summer")
                    .executes(context -> TimeCommand.executeSet(
                        context.getSource(),
                        switch(ForgeSeasons.CONFIG.getStartingSeason()) {
                            case SUMMER -> 0;
                            case SPRING -> ForgeSeasons.CONFIG.getSpringLength();
                            case WINTER -> ForgeSeasons.CONFIG.getWinterLength() + ForgeSeasons.CONFIG.getSpringLength();
                            case FALL -> ForgeSeasons.CONFIG.getFallLength() + ForgeSeasons.CONFIG.getWinterLength() + ForgeSeasons.CONFIG.getSpringLength();
                        }
                    ))
                )
                .then(CommandManager.literal("fall")
                    .executes(context -> TimeCommand.executeSet(
                        context.getSource(),
                        switch(ForgeSeasons.CONFIG.getStartingSeason()) {
                            case FALL -> 0;
                            case SUMMER -> ForgeSeasons.CONFIG.getSummerLength();
                            case SPRING -> ForgeSeasons.CONFIG.getSpringLength() + ForgeSeasons.CONFIG.getSummerLength();
                            case WINTER -> ForgeSeasons.CONFIG.getWinterLength() + ForgeSeasons.CONFIG.getSpringLength() + ForgeSeasons.CONFIG.getSummerLength();
                        }
                    ))
                )
                .then(CommandManager.literal("winter")
                    .executes(context -> TimeCommand.executeSet(
                        context.getSource(),
                        switch(ForgeSeasons.CONFIG.getStartingSeason()) {
                            case WINTER -> 0;
                            case FALL -> ForgeSeasons.CONFIG.getFallLength();
                            case SUMMER -> ForgeSeasons.CONFIG.getSummerLength() + ForgeSeasons.CONFIG.getFallLength();
                            case SPRING -> ForgeSeasons.CONFIG.getSpringLength() + ForgeSeasons.CONFIG.getSummerLength() + ForgeSeasons.CONFIG.getFallLength();
                        }
                    ))
                )
            )
            .then(CommandManager.literal("query")
                .executes(context -> {
                    World world = context.getSource().getWorld();
                    Season currentSeason = ForgeSeasons.getCurrentSeason(world);
                    Season nextSeason = ForgeSeasons.getNextSeason(world, currentSeason);
                    long ticksLeft = ForgeSeasons.getTimeToNextSeason(world);
                    context.getSource().sendFeedback(() -> Text.translatable("commands.seasons.query_1",
                            Text.translatable(currentSeason.getTranslationKey()).formatted(currentSeason.getFormatting())
                    ), false);
                    context.getSource().sendFeedback(() -> Text.translatable("commands.seasons.query_2",
                            Long.toString(ticksLeft/24000L),
                            Long.toString(ticksLeft),
                            Text.translatable(nextSeason.getTranslationKey()).formatted(nextSeason.getFormatting())
                    ), false);
                    return currentSeason.ordinal();
                })
            )
            .then(CommandManager.literal("skip").requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(context.getSource().getWorld())))
                .then(CommandManager.literal("spring")
                    .executes(context -> {
                        World world = context.getSource().getWorld();
                        Season season = ForgeSeasons.getCurrentSeason(world);
                        return switch (season) {
                            case SPRING -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SUMMER.getSeasonLength() + Season.FALL.getSeasonLength() + Season.WINTER.getSeasonLength());
                            case SUMMER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.FALL.getSeasonLength() + Season.WINTER.getSeasonLength());
                            case FALL -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.WINTER.getSeasonLength());
                            case WINTER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world));
                        };
                    })
                )
                .then(CommandManager.literal("summer")
                    .executes(context -> {
                        World world = context.getSource().getWorld();
                        Season season = ForgeSeasons.getCurrentSeason(world);
                        return switch (season) {
                            case SPRING -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world));
                            case SUMMER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.FALL.getSeasonLength() + Season.WINTER.getSeasonLength() + Season.SPRING.getSeasonLength());
                            case FALL -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.WINTER.getSeasonLength() + Season.SPRING.getSeasonLength());
                            case WINTER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SPRING.getSeasonLength());
                        };
                    })
                )
                .then(CommandManager.literal("fall")
                    .executes(context -> {
                        World world = context.getSource().getWorld();
                        Season season = ForgeSeasons.getCurrentSeason(world);
                        return switch (season) {
                            case SPRING -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SUMMER.getSeasonLength());
                            case SUMMER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world));
                            case FALL -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.WINTER.getSeasonLength() + Season.SPRING.getSeasonLength() + Season.SUMMER.getSeasonLength());
                            case WINTER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SPRING.getSeasonLength() + Season.SUMMER.getSeasonLength());
                        };
                    })
                )
                .then(CommandManager.literal("winter")
                    .executes(context -> {
                        World world = context.getSource().getWorld();
                        Season season = ForgeSeasons.getCurrentSeason(world);
                        return switch (season) {
                            case SPRING -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SUMMER.getSeasonLength() + Season.FALL.getSeasonLength());
                            case SUMMER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.FALL.getSeasonLength());
                            case FALL -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world));
                            case WINTER -> executeLongAdd(context.getSource(), ForgeSeasons.getTimeToNextSeason(world) + Season.SPRING.getSeasonLength() + Season.SUMMER.getSeasonLength() + Season.FALL.getSeasonLength());
                        };
                    })
                )
            )
        );
    }

    public static int executeLongAdd(ServerCommandSource source, long time) {

        for (ServerWorld serverWorld : source.getServer().getWorlds()) {
            serverWorld.setTimeOfDay(serverWorld.getTimeOfDay() + time);
        }

        int i = (int) (source.getWorld().getTimeOfDay() % 24000L);
        source.sendFeedback(() -> Text.translatable("commands.time.set", i), true);
        return i;
    }

}
