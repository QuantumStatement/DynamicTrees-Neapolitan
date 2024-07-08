package maxhyper.dtneapolitan.blocks;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.block.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.block.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.block.leaves.PalmLeavesProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BananaLeavesProperties extends PalmLeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(BananaLeavesProperties::new);

    public BananaLeavesProperties(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public BlockBehaviour.Properties getDefaultBlockProperties(MapColor mapColor) {
        return super.getDefaultBlockProperties(mapColor).strength(0.2F).sound(SoundType.WEEPING_VINES);

    }

    @Override
    protected DynamicLeavesBlock createDynamicLeaves(BlockBehaviour.Properties properties) {
        return new DynamicPalmLeavesBlock(this, properties){
            //When destroying, we update the fruit below if one is there.
            private void updateFruit (LevelAccessor world, BlockPos pos, int offset){
                BlockState downState = world.getBlockState(pos.below(offset));
                if (downState.getBlock() instanceof BananaFruitBlock){
                    downState.onNeighborChange(world, pos.below(offset), pos.below(offset).above());
                }
            }
            @Override
            public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
                updateFruit(world, pos, 2);
                super.destroy(world, pos, state);
            }
            @Override
            public void onPlace(BlockState thisState, Level world, BlockPos pos, BlockState oldState, boolean bool) {
                updateFruit(world, pos, 2);
                updateFruit(world, pos, 3);
                super.onPlace(thisState, world, pos, oldState, bool);
            }

        };
    }

    @Override
    public ItemStack getPrimitiveLeavesItemStack() {
        return getPrimitiveLeaves().getCloneItemStack(null, null, null, null);
    }
}