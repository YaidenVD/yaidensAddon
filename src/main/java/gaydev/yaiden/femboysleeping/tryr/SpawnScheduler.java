package gaydev.yaiden.femboysleeping.tryr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

/**
 * Holds baby-villager spawns that were "rolled" successfully but shouldn't
 * appear immediately - they wait until dueDay (currentDay + gestationDays)
 * before actually spawning.
 *
 * NOTE: this list is in-memory only. If the server restarts before a spawn
 * is due, it's lost. Fine for a small personal server; if that ever
 * matters, this would need to persist to NBT/a saved data file instead.
 */
public final class SpawnScheduler {

    private SpawnScheduler() {}

    private record Pending(long dueDay, double x, double y, double z, ResourceKey<Level> dimension) {}

    private static final List<Pending> pending = new ArrayList<>();

    public static void schedule(Level level, double x, double y, double z, long dueDay) {
        pending.add(new Pending(dueDay, x, y, z, level.dimension()));

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

        Iterator<Pending> iterator = pending.iterator();

        while (iterator.hasNext()) {

            Pending spawn = iterator.next();

            if (spawn.dueDay() > currentDay) {
                continue;
            }

            Level level = server.getLevel(spawn.dimension());

            if (level != null) {
                spawnBabyAt(level, spawn.x(), spawn.y(), spawn.z());
            }

            iterator.remove();
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
