package maxhyper.dtneapolitan.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeature.FruitGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.teamabnormals.neapolitan.common.entity.animal.Chimpanzee;
import com.teamabnormals.neapolitan.core.NeapolitanConfig;
import com.teamabnormals.neapolitan.core.other.tags.NeapolitanBiomeTags;
import com.teamabnormals.neapolitan.core.registry.NeapolitanEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;

public class BananaFruitGenFeature extends FruitGenFeature {

    public BananaFruitGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(FRUIT, QUANTITY, FRUITING_RADIUS, PLACE_CHANCE);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return new GenFeatureConfiguration(this)
                .with(FRUIT, Fruit.NULL)
                .with(QUANTITY, 8)
                .with(FRUITING_RADIUS, 6)
                .with(PLACE_CHANCE, 0.25f);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.endPoints().isEmpty()) {
            return false;
        }
        int qty = configuration.get(QUANTITY);
        qty *= context.fruitProductionFactor();

        BlockPos fruitPos = null;
        for (int i = 0; i < qty; i++) {
            BlockPos newPos = this.placeDuringWorldGen(configuration, configuration.get(FRUIT), context.level(), context.pos(),
                    context.endPoints().get(0), context.seasonValue());
            if (newPos != null) fruitPos = newPos;
        }

        if (fruitPos != null && context.level() instanceof WorldGenLevel wgl){
            boolean canSpawnChimps = wgl.getBiome(fruitPos).is(NeapolitanBiomeTags.HAS_CHIMPANZEE);
            if (context.random().nextDouble() < NeapolitanConfig.COMMON.chimpanzeeGroupChance.get() && canSpawnChimps) {
                spawnChimps(wgl, fruitPos);
            }
        }
        return true;
    }

    protected BlockPos placeDuringWorldGen(GenFeatureConfiguration configuration, Fruit fruit, LevelAccessor level,
                                       BlockPos rootPos, BlockPos leavesPos, Float seasonValue) {
        Direction placeDirection = CoordUtils.HORIZONTALS[level.getRandom().nextInt(4)];
        if (shouldPlaceDuringWorldGen(configuration, level, rootPos, leavesPos, placeDirection)) {
            BlockPos placePos = leavesPos.offset(placeDirection.getNormal());
            fruit.placeDuringWorldGen(level, placePos, seasonValue);
            return placePos;
        }
        return null;
    }

    protected boolean shouldPlaceDuringWorldGen(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos,
                                                BlockPos leavesPos, Direction placeDirection) {
        return leavesPos.getY() != rootPos.getY() && level.isEmptyBlock(leavesPos.offset(placeDirection.getNormal()))
                && level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        LevelAccessor level = context.level();
        BlockPos rootPos = context.pos();
        if (TreeHelper.getRadius(level, rootPos.above()) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final Fruit fruit = configuration.get(FRUIT);
            final float fruitingFactor = fruit.seasonalFruitProductionFactor(context.levelContext(), rootPos);
            if (fruitingFactor > level.getRandom().nextFloat()) {
                place(configuration, fruit, level, rootPos, getLeavesHeight(rootPos, level).below(),
                        SeasonHelper.getSeasonValue(context.levelContext(), rootPos));
            }
            return true;
        }
        return false;
    }

    private BlockPos getLeavesHeight(BlockPos rootPos, LevelAccessor level) {
        for (int y = 1; y < 20; y++) {
            BlockPos testPos = rootPos.above(y);
            if ((level.getBlockState(testPos).getBlock() instanceof LeavesBlock)) {
                return testPos;
            }
        }
        return rootPos;
    }

    protected void place(GenFeatureConfiguration configuration, Fruit fruit, LevelAccessor level, BlockPos rootPos,
                         BlockPos leavesPos, Float seasonValue) {
        Direction placeDirection = CoordUtils.HORIZONTALS[level.getRandom().nextInt(4)];
        if (shouldPlace(configuration, level, rootPos, leavesPos, placeDirection)) {
            fruit.place(level, leavesPos.offset(placeDirection.getNormal()), seasonValue);
        }
    }

    protected boolean shouldPlace(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos,
                                  BlockPos leavesPos, Direction placeDirection) {
        return leavesPos.getY() != rootPos.getY() && level.isEmptyBlock(leavesPos.offset(placeDirection.getNormal()))
                && level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }


    /**
     * Code taken from BananaPlantFeature in the Neapolitan mod
     */
    private static void spawnChimps(WorldGenLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        int minSpawnAttempts = NeapolitanConfig.COMMON.chimpanzeeMinSpawnAttempts.get();
        int maxSpawnAttempts = NeapolitanConfig.COMMON.chimpanzeeMaxSpawnAttempts.get();
        if (maxSpawnAttempts >= minSpawnAttempts && maxSpawnAttempts > 0 && minSpawnAttempts >= 0) {
            int spawnCount = minSpawnAttempts + random.nextInt(maxSpawnAttempts - minSpawnAttempts);
            int spawnedChimps = 0;

            for(int i = 0; i < spawnCount; ++i) {
                int spawnRange = 4;
                double d0 = pos.getX() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;
                double d1 = (pos.getY() + random.nextInt(3) - 1);
                double d2 = pos.getZ() + (random.nextDouble() - random.nextDouble()) * spawnRange + 0.5;
                if (level.noCollision((NeapolitanEntityTypes.CHIMPANZEE.get()).getAABB(d0, d1, d2)) && spawnedChimps < NeapolitanConfig.COMMON.chimpanzeeMaxGroupSize.get()) {
                    Chimpanzee chimp = NeapolitanEntityTypes.CHIMPANZEE.get().create(level.getLevel());
                    if (chimp != null) {
                        chimp.moveTo(d0, d1, d2, level.getRandom().nextFloat() * 360.0F, 0.0F);
                        chimp.finalizeSpawn(level, level.getCurrentDifficultyAt(chimp.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                        chimp.setBaby(random.nextInt(4) == 0);
                        level.addFreshEntity(chimp);
                        chimp.spawnAnim();
                        ++spawnedChimps;
                    }
                }
            }

        }
    }

}