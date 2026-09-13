package gaydev.yaiden.femboysleeping.tryr;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * World-saved copy of the pending baby-villager spawns. Unlike a plain
 * in-memory list, this survives server restarts - it's written to
 * `<world>/data/yaidensaddon_spawns.dat` alongside vanilla saved data
 * like maps/raids.
 */
public class SpawnSchedulerData extends SavedData {

    /**
     * currentStage is 0 at the start and counts up as days pass, purely
     * for a quiet "you're on stage N" chat message - no gameplay effects
     * attached to it.
     */
    public record Pending(
        long startDay,
        long dueDay,
        double x,
        double y,
        double z,
        ResourceKey<Level> dimension,
        UUID playerId,
        int currentStage
    ) {
        public Pending withStage(int newStage) {
            return new Pending(startDay, dueDay, x, y, z, dimension, playerId, newStage);
        }
    }

    private final List<Pending> pending = new ArrayList<>();

    private static final String KEY = "yaidensaddon_spawns";

    public static final Factory<SpawnSchedulerData> FACTORY = new Factory<>(
        SpawnSchedulerData::new,
        SpawnSchedulerData::load,
        DataFixTypes.LEVEL
    );

    public static SpawnSchedulerData get(MinecraftServer server) {
        return server.overworld()
            .getDataStorage()
            .computeIfAbsent(FACTORY, KEY);
    }

    public List<Pending> getPending() {
        return pending;
    }

    public void add(Pending spawn) {
        pending.add(spawn);
        setDirty();
    }

    public void removeAll(List<Pending> toRemove) {
        pending.removeAll(toRemove);
        setDirty();
    }

    /**
     * Replaces an entry (used when its currentStage advances).
     */
    public void replace(Pending oldEntry, Pending newEntry) {
        int index = pending.indexOf(oldEntry);
        if (index >= 0) {
            pending.set(index, newEntry);
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {

        ListTag list = new ListTag();

        for (Pending spawn : pending) {

            CompoundTag entry = new CompoundTag();

            entry.putLong("startDay", spawn.startDay());
            entry.putLong("dueDay", spawn.dueDay());
            entry.putDouble("x", spawn.x());
            entry.putDouble("y", spawn.y());
            entry.putDouble("z", spawn.z());
            entry.putString("dimension", spawn.dimension().location().toString());
            entry.putUUID("playerId", spawn.playerId());
            entry.putInt("currentStage", spawn.currentStage());

            list.add(entry);
        }

        tag.put("pending", list);

        return tag;
    }

    public static SpawnSchedulerData load(CompoundTag tag, HolderLookup.Provider registries) {

        SpawnSchedulerData data = new SpawnSchedulerData();

        ListTag list = tag.getList("pending", 10); // 10 = CompoundTag id

        for (int i = 0; i < list.size(); i++) {

            CompoundTag entry = list.getCompound(i);

            ResourceLocation dimId = ResourceLocation.parse(entry.getString("dimension"));
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimId);

            data.pending.add(new Pending(
                entry.getLong("startDay"),
                entry.getLong("dueDay"),
                entry.getDouble("x"),
                entry.getDouble("y"),
                entry.getDouble("z"),
                dimension,
                entry.getUUID("playerId"),
                entry.getInt("currentStage")
            ));
        }

        return data;
    }
}

