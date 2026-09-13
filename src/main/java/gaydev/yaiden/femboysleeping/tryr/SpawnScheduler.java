package gaydev.yaiden.femboysleeping.tryr;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.config.YaidensaddonConfigManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    public static void schedule(Level level, double x, double y, double z, UUID playerId, long startDay, long dueDay) {

        MinecraftServer server = level.getServer();

        if (server == null) {
            return;
        }

        SpawnSchedulerData.get(server).add(
            new SpawnSchedulerData.Pending(startDay, dueDay, x, y, z, level.dimension(), playerId, 0)
        );

        Yaidensaddon.LOGGER.info(
            "Scheduled baby spawn at {},{},{} for day {}",
            x, y, z, dueDay
        );
    }

    /**
     * Call once per day-change, before processDueSpawns. Advances each
     * pending spawn's stage (evenly spread across startDay..dueDay) and
     * sends the player a quiet chat message when it changes. No gameplay
     * effects are attached to stages - it's flavor only.
     */
    public static void processStageUpdates(MinecraftServer server, long currentDay) {

        SpawnSchedulerData data = SpawnSchedulerData.get(server);
        int stageCount = Math.max(1, YaidensaddonConfigManager.CONFIG.stageCount);

        // Copy first - we may replace entries in the backing list as we go.
        for (SpawnSchedulerData.Pending spawn : new ArrayList<>(data.getPending())) {

            long totalDays = Math.max(1, spawn.dueDay() - spawn.startDay());
            long elapsed = Math.min(totalDays, Math.max(0, currentDay - spawn.startDay()));

            int newStage = (int) Math.min(
                stageCount,
                (elapsed * stageCount) / totalDays
            );

            if (newStage <= spawn.currentStage()) {
                continue;
            }

            data.replace(spawn, spawn.withStage(newStage));

            ServerPlayer player = server.getPlayerList().getPlayer(spawn.playerId());

            if (player != null) {
                player.sendSystemMessage(
                    Component.literal("(stage " + newStage + "/" + stageCount + ")")
                );
            }
        }
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


