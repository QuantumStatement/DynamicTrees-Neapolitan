package maxhyper.dtneapolitan.genfeatures;

import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.rooty.AerialRootsSoilProperties;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilHelper;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.teamabnormals.blueprint.core.util.BlockUtil;
import com.teamabnormals.neapolitan.core.NeapolitanConfig;
import com.teamabnormals.neapolitan.core.other.NeapolitanLootTables;
import com.teamabnormals.neapolitan.core.other.tags.NeapolitanBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

/**
 * This whole code is taken straight from Neapolitan's BananaPlantFeature,
 * then adapted into a gen feature
 */
public class SussyGravelGenFeature extends GenFeature {

    public SussyGravelGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {

    }

    @Override
    protected boolean postGenerate(@NotNull GenFeatureConfiguration configuration, PostGenerationContext context) {
        BlockPos pos = context.pos();
        LevelAccessor level = context.level();
        RandomSource random = context.random();
        boolean canSpawnChimps = level.getBiome(pos).is(NeapolitanBiomeTags.HAS_CHIMPANZEE);
        boolean suspicious = canSpawnChimps && NeapolitanConfig.COMMON.suspiciousBananaPlants.get() && (double)random.nextFloat() < NeapolitanConfig.COMMON.suspiciousBananaPlantChance.get();

        //Replace the soil with gravel
        BlockState soilState = level.getBlockState(pos);
        if (soilState.getBlock() instanceof RootyBlock rootyBlock) {
            SoilProperties gravelSoil = SoilHelper.getProperties(Blocks.GRAVEL);
            if (!rootyBlock.getSoilProperties().equals(gravelSoil)) {
                BlockEntity TE = level.getBlockEntity(pos);
                //if there's a TileEntity don't bother, not advisable to touch them during worldgen
                if (TE == null){
                    BlockState newSoil = gravelSoil.getSoilState(rootyBlock.getPrimitiveSoilState(soilState), 0, soilState.getValue(RootyBlock.IS_VARIANT));
                    level.setBlock(pos, newSoil, 3);
                }
//                if (TE != null) {
//                    Species species = rootyBlock.getSpecies(soilState, level, pos);
//                    lev.setBlockEntity(TE);
//                    if (TE instanceof SpeciesBlockEntity speciesTE) {
//                        speciesTE.setSpecies(species);
//                    }
//                }
            }
        }

        //Place sussy gravel around
        placeGravelAround(level, pos.above(), random, suspicious);

        return super.postGenerate(configuration, context);
    }

    private void placeGravelAround (LevelAccessor level, BlockPos pos, RandomSource random, boolean suspicious){
        boolean chimpHead = suspicious && random.nextFloat() < 0.25F;
        int horizontalRange = (suspicious ? 3 : 2) + random.nextInt(2);
        int verticalMin = suspicious ? -8 : -2;
        int rareSusGravel = 0;
        int commonSusGravel = 0;
        int rareSusGravelMax = NeapolitanConfig.COMMON.rareSuspiciousGravelMin.get() + random.nextInt(2);
        int susGravelAmount = 8 + random.nextInt(3) + random.nextInt(2);
        int x;
        if (chimpHead) {
            x = 2 + random.nextInt(3);
            rareSusGravelMax += x;
            susGravelAmount += x - 1;
            Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            generateChimpHead(level, pos.relative(facing, random.nextInt(2)).relative(facing.getClockWise(), 1 + random.nextInt(2)).below(3 + random.nextInt(3)), facing, random);
        }

        for(x = -horizontalRange; x <= horizontalRange; ++x) {
            for(int y = verticalMin; y < 2; ++y) {
                for(int z = -horizontalRange; z <= horizontalRange; ++z) {
                    if ((Mth.abs(x) != Math.abs(z) || Mth.abs(x) != horizontalRange) && (y >= -3 || Math.abs(x) < horizontalRange && Math.abs(z) < horizontalRange) && (y >= -6 || Math.abs(x) < horizontalRange - 1 && Math.abs(z) < horizontalRange - 1)) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        int dist = (int)Mth.sqrt((float)offsetPos.distSqr(pos));
                        int clamped = Math.max(1, Math.min(y < -1 && (Math.abs(x) >= 3 || Math.abs(z) >= 3) ? dist : 3, dist)) + (suspicious ? 0 : random.nextInt(2));
                        BlockState offState = level.getBlockState(offsetPos);
                        if (random.nextInt(clamped) == 0 && (suspicious && offState.is(Tags.Blocks.STONE) || offState.is(BlockTags.DIRT))) {
                            if (!suspicious) {
                                level.setBlock(offsetPos, Blocks.GRAVEL.defaultBlockState(), 19);
                            } else if (commonSusGravel + rareSusGravel < susGravelAmount && random.nextFloat() < 0.05F * (float)(Math.abs(y) + 1)) {
                                level.setBlock(offsetPos, Blocks.SUSPICIOUS_GRAVEL.defaultBlockState(), 19);
                                boolean rare = rareSusGravel < rareSusGravelMax && random.nextInt(3) == 0 || commonSusGravel == susGravelAmount - rareSusGravelMax;
                                if (rare) {
                                    ++rareSusGravel;
                                } else {
                                    ++commonSusGravel;
                                }

                                level.getBlockEntity(offsetPos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent((block) -> {
                                    block.setLootTable(rare ? NeapolitanLootTables.BANANA_PLANT_ARCHAEOLOGY_RARE : NeapolitanLootTables.BANANA_PLANT_ARCHAEOLOGY_COMMON, offsetPos.asLong());
                                });
                            } else {
                                level.setBlock(offsetPos, Blocks.GRAVEL.defaultBlockState(), 19);
                            }

                            if (!level.isStateAtPosition(offsetPos.above(), (state) -> state.canSurvive(level, offsetPos.above()))) {
                                level.setBlock(offsetPos.above(), Blocks.AIR.defaultBlockState(), 19);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void generateChimpHead(LevelAccessor level, BlockPos origin, Direction facing, RandomSource random) {
        BlockPos.betweenClosedStream(origin, origin.below(3).relative(facing.getOpposite(), 2).relative(facing.getCounterClockWise(), 3)).map(BlockPos::immutable).forEach((pos) -> {
            placeMossyBlock(level, random, null, pos, 0, 0, 0, Blocks.COBBLESTONE.defaultBlockState());
        });
        placeMossyBlock(level, random, facing, origin, -1, -1, -1, Blocks.COBBLESTONE.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 4, -1, -1, Blocks.COBBLESTONE.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, -1, 0, -1, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 4, 0, -1, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 1, -1, -1, Blocks.EMERALD_BLOCK.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 2, -1, -1, Blocks.EMERALD_BLOCK.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 1, -1, 0, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing.getCounterClockWise()));
        placeMossyBlock(level, random, facing, origin, 2, -1, 0, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing.getClockWise()));
        placeMossyBlock(level, random, facing, origin, 1, -1, 1, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 2, -1, 1, Blocks.COBBLESTONE_SLAB.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 1, -2, 1, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing.getClockWise()).setValue(StairBlock.HALF, Half.TOP));
        placeMossyBlock(level, random, facing, origin, 2, -2, 1, Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing.getCounterClockWise()).setValue(StairBlock.HALF, Half.TOP));
        placeMossyBlock(level, random, facing, origin, 1, -3, 1, Blocks.COBBLESTONE.defaultBlockState());
        placeMossyBlock(level, random, facing, origin, 2, -3, 1, Blocks.COBBLESTONE.defaultBlockState());
    }

    private static void placeMossyBlock(LevelAccessor level, RandomSource random, Direction facing, BlockPos pos, int x, int y, int z, BlockState state) {
        if (state.is(Blocks.EMERALD_BLOCK)) {
            if (random.nextBoolean()) {
                return;
            }
        } else if (random.nextFloat() < 0.4F) {
            Block block = state.is(Blocks.COBBLESTONE) ? Blocks.MOSSY_COBBLESTONE : (state.is(Blocks.COBBLESTONE_SLAB) ? Blocks.MOSSY_COBBLESTONE_SLAB : Blocks.MOSSY_COBBLESTONE_STAIRS);
            state = BlockUtil.transferAllBlockStates(state, block.defaultBlockState());
        }

        if (facing != null) {
            if (facing.getAxis() == Direction.Axis.X) {
                int temp = x;
                x = z;
                z = temp - 3;
                if (state.hasProperty(StairBlock.FACING)) {
                    state = state.setValue(StairBlock.FACING, (state.getValue(StairBlock.FACING)).getOpposite());
                }
            }

            x *= facing.getAxisDirection().getStep();
            z *= facing.getAxisDirection().getStep();
        }

        level.setBlock(pos.offset(x, y, z), state, 19);
    }

}
