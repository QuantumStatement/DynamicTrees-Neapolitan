package maxhyper.dtneapolitan.models;

import maxhyper.dtneapolitan.DynamicTreesNeapolitan;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicTreesNeapolitan.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelBakeEventHandler {

    @SubscribeEvent
    public static void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders event) {
        event.register("palm_fronds", new PalmLeavesModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        // Setup fronds models
        PalmLeavesBakedModel.INSTANCES.forEach(PalmLeavesBakedModel::setupModels);
    }

}