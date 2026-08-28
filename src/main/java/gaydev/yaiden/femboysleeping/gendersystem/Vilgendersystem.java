package gaydev.yaiden.femboysleeping.gendersystem;

import com.wildfire.main.Gender;
import gaydev.yaiden.femboysleeping.functions.Fabricatedfuncs;
import gaydev.yaiden.femboysleeping.gendersystem.mixindef.VillagerDataAccessor;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public class Vilgendersystem {

    private final Villager villager;

    /*
     * This system belongs to this specific villager.
     */
    public Vilgendersystem(Villager villager) {
        this.villager = villager;
    }

    /*
     * Randomly generates a gender for THIS villager.
     */
    public Gender randomez(Level level) {

        Gender[] genders = {
            Gender.MALE,
            Gender.FEMALE,
            Gender.OTHER
        };

        Gender gender =
            genders[level.random.nextInt(genders.length)];

        if (gender == Gender.MALE) {
            Fabricatedfuncs.SystemMessage("MALE");
        }
        else if (gender == Gender.FEMALE) {
            Fabricatedfuncs.SystemMessage("FEMALE");
        }
        else if (gender == Gender.OTHER) {
            Fabricatedfuncs.SystemMessage("OTHER");
        }

        // Store the gender on the ACTUAL villager.
        ((VillagerDataAccessor) villager).setGender(gender);

        return gender;
    }

    /*
     * Get THIS villager's gender.
     */
    public Gender getGender() {
        return ((VillagerDataAccessor) villager).getGender();
    }

    /*
     * Check whether THIS villager already has a gender.
     */
    public boolean hasGender() {
        return ((VillagerDataAccessor) villager).hasGender();
    }

    /*
     * Manually set THIS villager's gender.
     */
    public void setGender(Gender gender) {
        ((VillagerDataAccessor) villager).setGender(gender);
    }

    /*
     * Get the villager this system belongs to.
     */
    public Villager getVillager() {
        return villager;
    }
}