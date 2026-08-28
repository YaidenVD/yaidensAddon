package gaydev.yaiden.femboysleeping.tryr;

import com.wildfire.main.Gender;

import gaydev.yaiden.femboysleeping.mixin.VillagerEntityMixin;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;


public class Commandthing {
    static VillagerEntityMixin gay;
    static Player player;
    Commandthing(VillagerEntityMixin gay, Player player) {
        Commandthing.gay = gay;
        Commandthing.player = player;
    }
    
    public static void register() {

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {

                dispatcher.register(
                    Commands.literal("yaidensapi")

                        /*
                         * /yaidensapi Getgender <entity>
                         *
                         * EntityArgument gives you the same vanilla
                         * entity/UUID suggestions you see with /kill.
                         */
                        .then(
                            Commands.literal("Getgender")
                                .then(
                                    Commands.argument(
                                        "entity",
                                        EntityArgument.entity()
                                    )
                                    .executes(context -> {

                                        Gender gender =
                                            gay.getGender();

                                        if (gender == null) {

                                            context.getSource().sendFailure(
                                                Component.literal(
                                                    "No gender found for this entity!"
                                                )
                                            );

                                            return 0;
                                        }

                                        context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                "Villager Gender: "
                                                    + gender.name()
                                            ),
                                            false
                                        );

                                        return 1;
                                    })
                                )
                        )

                        /*
                         * /yaidensapi GetUUID <entity>
                         *
                         * Returns the UUID of the selected entity.
                         */
                       .then(
    Commands.literal("giveweapon")
        .then(
            Commands.argument(
                "player",
                EntityArgument.player()
            )
            .executes(context -> {

                ServerPlayer player =
                    EntityArgument.getPlayer(
                        context,
                        "player"
                    );

                ItemStack sword =
                    new ItemStack(Items.NETHERITE_SWORD);

                Holder<Enchantment> sharpness =
                    context.getSource()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SHARPNESS);

                EnchantmentHelper.updateEnchantments(
                    sword,
                    enchantments ->
                        enchantments.set(sharpness, 255)
                );

                player.getInventory().add(sword);

                context.getSource().sendSuccess(
                    () -> Component.literal(
                        "Gave weapon to "
                            + player.getName().getString()
                    ),
                    true
                );

                return 1;
            })
        )
)
                );
            }
        );
    }
}