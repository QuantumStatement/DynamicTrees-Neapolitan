package maxhyper.dtneapolitan.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.tree.family.Family;
import com.ferreusveritas.dynamictrees.tree.species.PalmSpecies;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.teamabnormals.neapolitan.common.block.BananaFrondBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

public class BananaSpecies extends PalmSpecies {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(BananaSpecies::new);

    public BananaSpecies(ResourceLocation resourceLocation, Family family, LeavesProperties leavesProperties) {
        super(resourceLocation, family, leavesProperties);
    }

    @Override
    public boolean canSaplingGrowNaturally(Level level, BlockPos pos) {
        return SoilHelper.isSoilAcceptable(level.getBlockState(pos.below()),
                SoilHelper.getSoilFlags(SoilHelper.SAND_LIKE, SoilHelper.GRAVEL_LIKE))
                && super.canSaplingGrowNaturally(level, pos);
    }

    @Override
    public boolean canSaplingGrow(LevelReader level, BlockPos pos) {
        if (level instanceof LevelAccessor levelAccessor)
            return BananaFrondBlock.canRainAt(levelAccessor, pos) && super.canSaplingGrow(level, pos);
        return false;
    }
}