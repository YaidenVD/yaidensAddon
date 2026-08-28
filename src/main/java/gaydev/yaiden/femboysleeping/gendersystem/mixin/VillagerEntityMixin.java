package gaydev.yaiden.femboysleeping.gendersystem.mixin;

import com.wildfire.main.Gender;

import gaydev.yaiden.femboysleeping.gayness.VillagerDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class VillagerEntityMixin implements VillagerDataAccessor {

    private Gender villagerGender;

    @Override
    public void setGender(Gender value) {
        this.villagerGender = value;
    }

    @Override
    public Gender getGender() {
        return this.villagerGender;
    }

    @Override
    public boolean hasGender() {
        return this.villagerGender != null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    public void saveGender(CompoundTag nbt, CallbackInfo ci) {
        if (this.villagerGender != null) {
            nbt.putString("gender", this.villagerGender.name());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    public void loadGender(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("gender")) {
            String genderName = nbt.getString("gender");

            try {
                this.villagerGender = Gender.valueOf(genderName);
            } catch (IllegalArgumentException ignored) {
                this.villagerGender = null;
            }
        }
    }
}