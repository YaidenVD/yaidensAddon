package gaydev.yaiden.femboysleeping.tryr;

import java.util.ArrayList;
import java.util.List;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

/**
 * Thin wrapper around SpawnSchedulerData (the actual persisted storage).
 * Kept as a separate class so call sites (Villgaer, Yaidensaddon) don't
 * need to know about SavedData plumbing.
 */
public final class SpawnScheduler {

    private SpawnScheduler() {}

    public static void schedule(Level level, double x, double y, double z, long dueDay) {

        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        SpawnSchedulerData.get(server).add(
            new SpawnSchedulerData.Pending(dueDay, x, y, z, level.dimension())
        );

        Yaidensaddon.LOGGER.info(
            "Scheduled baby spawn at {},{},{} for day {}",
            x, y, z, dueDay
        );
    }

    /**
     * Call once per day-change. Spawns anything whose dueDay has arrived
     * and removes it from the pending list.
     */
    public static void processDueSpawns(MinecraftServer server, long currentDay) {

        SpawnSchedulerData data = SpawnSchedulerData.get(server);

        List<SpawnSchedulerData.Pending> due = new ArrayList<>();

        for (SpawnSchedulerData.Pending spawn : data.getPending()) {
            if (spawn.dueDay() <= currentDay) {
                due.add(spawn);
            }
        }

        for (SpawnSchedulerData.Pending spawn : due) {

            Level level = server.getLevel(spawn.dimension());

            if (level != null) {
                spawnBabyAt(level, spawn.x(), spawn.y(), spawn.z());
            }
        }

        if (!due.isEmpty()) {
            data.removeAll(due);
        }
    }

    private static void spawnBabyAt(Level level, double x, double y, double z) {

        Villager villager = EntityType.VILLAGER.create(level);

        if (villager == null) {
            return;
        }

        villager.setAge(-24000);
        villager.setBaby(true);
        villager.moveTo(x, y, z, 0.0F, 0.0F);

        level.addFreshEntity(villager);

        Yaidensaddon.LOGGER.info(
            "Baby villager spawned at {},{},{}",
            x, y, z
        );
    }
}

