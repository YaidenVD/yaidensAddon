package gaydev.yaiden.femboysleeping.client;

import java.util.concurrent.CompletableFuture;


import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;

public class YaidensEnglishProvider extends FabricLanguageProvider {
	protected YaidensEnglishProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		// Specifying en_us is optional, as it's the default language code
		super(dataOutput, "en_us", registryLookup);
	}

	@Override
	public void generateTranslations(Provider registryLookup, TranslationBuilder translationBuilder) {
	}
}