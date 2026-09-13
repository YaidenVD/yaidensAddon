package gaydev.yaiden.femboysleeping.client.gay;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import gaydev.yaiden.femboysleeping.client.screen.Yaidensaddonscreen;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Yaidensaddonscreen::create;
    }
}