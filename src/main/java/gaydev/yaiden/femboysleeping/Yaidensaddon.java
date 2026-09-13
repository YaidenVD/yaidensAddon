package gaydev.yaiden.femboysleeping;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import gaydev.yaiden.femboysleeping.config.YaidensaddonConfigManager;
import gaydev.yaiden.femboysleeping.gayness.VillagerDataAccessor;
import gaydev.yaiden.femboysleeping.tryr.Commandthing;
import gaydev.yaiden.femboysleeping.tryr.Vilgendersystem;
import gaydev.yaiden.femboysleeping.tryr.Villgaer;
import gaydev.yaiden.femboysleeping.tryr.SpawnScheduler;

public class Yaidensaddon implements ModInitializer {

    public static final String MOD_ID = "yaidensaddon";

    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    private static final Map<UUID, BlockPos> sleepingPositions =
            new HashMap<>();

    private static final Set<UUID> playersWhoSlept =
            new HashSet<>();

    // NEW: remembers which Minecraft day we already processed
    private static long lastDay = -1;

    public static Yaidensaddon instance;
	public static Yaidensaddon getInstance() {
        if (instance == null) {
            throw new IllegalStateException("this thing was accessed before initailsation ");
        }
        return instance;
    }

    @Override
    public void onInitialize() {

        YaidensaddonConfigManager.load();

        Commandthing.register();
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {

    if (entity instanceof Villager villager) {
        Vilgendersystem rand =
            new Vilgendersystem(villager);
        VillagerDataAccessor accessor =
            (VillagerDataAccessor) villager;
        if(!accessor.hasGender()) {
            accessor.setGender(rand.randomez(world));
        }
    }
    });

        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {

            if (entity instanceof Player player) {

                UUID uuid = player.getUUID();

                playersWhoSlept.add(uuid);
                sleepingPositions.put(uuid, sleepingPos);

                LOGGER.info(
                        "PLAYER SLEPT: {} at {}",
                        player.getName().getString(),
                        sleepingPos
                );
            }
        });


        ServerTickEvents.END_WORLD_TICK.register(world -> {

            // NEW: only let the Overworld handle the day change
            if (world != world.getServer().overworld()) {
                return;
            }


            long time = world.getDayTime();

            // NEW: calculate the current Minecraft day
            long day = time / 24000L;

            // NEW: only run once when the day changes
            if (day != lastDay) {

                lastDay = day;

                LOGGER.info(
                        "Players who slept: {}",
                        playersWhoSlept.size()
                );

                // Two players sharing a bed both end up in playersWhoSlept
                // with the same BlockPos. De-dupe by position first so a
                // shared bed only gets processed once instead of twice.
                Set<BlockPos> processedBeds = new HashSet<>();

                for (UUID uuid : playersWhoSlept) {

                    Player player =
                            world.getServer()
                                    .getPlayerList()
                                    .getPlayer(uuid);

                    if (player != null) {


                        BlockPos sleepingPos =
                                sleepingPositions.get(uuid);

                        if (sleepingPos != null && processedBeds.add(sleepingPos)) {

                            Villgaer villgaer =
                                    new Villgaer(
                                            player.level(),
                                            sleepingPos,
                                            null,
                                            player
                                    );

                            villgaer.gay(day);
                        }
                    }
                }

                playersWhoSlept.clear();
                sleepingPositions.clear();

                // Advance any pending spawns' stage counter, then spawn
                // anything whose gestationDays have elapsed.
                SpawnScheduler.processStageUpdates(world.getServer(), day);
                SpawnScheduler.processDueSpawns(world.getServer(), day);
            }
        });


        LOGGER.info(
                "Yaidensaddon initializing"
        );
    }


    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}