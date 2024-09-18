package maxhyper.dtneapolitan.genfeatures;

import com.ferreusveritas.dynamictrees.api.registry.Registry;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import maxhyper.dtneapolitan.DynamicTreesNeapolitan;
import net.minecraft.resources.ResourceLocation;

public class DTNeapolitanGenFeatures {

    public static GenFeature PALM_FRUIT_FEATURE = new BananaFruitGenFeature(
            new ResourceLocation(DynamicTreesNeapolitan.MOD_ID,"banana_fruit")
    );
    public static GenFeature SUSSY_GRAVEL_FEATURE = new SussyGravelGenFeature(
            new ResourceLocation(DynamicTreesNeapolitan.MOD_ID,"suspicious_gravel")
    );

    public static void register(final Registry<GenFeature> registry) {
        registry.registerAll(PALM_FRUIT_FEATURE, SUSSY_GRAVEL_FEATURE);
    }

}