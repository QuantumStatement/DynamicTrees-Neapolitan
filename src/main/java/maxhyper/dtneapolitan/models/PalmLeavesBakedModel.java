package maxhyper.dtneapolitan.models;

import com.ferreusveritas.dynamictrees.block.leaves.PalmLeavesProperties;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.google.common.primitives.Ints;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PalmLeavesBakedModel implements IDynamicBakedModel {

    public static List<PalmLeavesBakedModel> INSTANCES = new ArrayList<>();

    protected final BlockModel blockModel;

    ResourceLocation frondsResLoc;
    TextureAtlasSprite frondsTexture;

    private final BakedModel[] bakedFronds = new BakedModel[8]; // 8 = Number of surrounding blocks

    public PalmLeavesBakedModel(ResourceLocation modelResLoc, ResourceLocation frondsResLoc){
        this.blockModel = new BlockModel(null, new ArrayList<>(), new HashMap<>(), false, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
        this.frondsResLoc = frondsResLoc;
        INSTANCES.add(this);
    }

    public void setupModels (){
        frondsTexture = ModelUtils.getTexture(frondsResLoc);

        for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {

            SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(blockModel, ItemOverrides.EMPTY, false).particle(frondsTexture);

            BlockVertexData[] quadData = {
                    new BlockVertexData(0, 0, 3, 15, 4),
                    new BlockVertexData(0, 1, 3, 15, 0),
                    new BlockVertexData(0, 1, 0, 0, 0),
                    new BlockVertexData(0, 0, 0, 0, 4),
                    new BlockVertexData(0, 0, 3, 15, 4),
                    new BlockVertexData(0, 0, 0, 0, 4),
                    new BlockVertexData(0, 1, 0, 0, 0),
                    new BlockVertexData(0, 1, 3, 15, 0)
            };

            for (int pass = 0; pass < 3; pass++) {
                for (int half = 0; half < 2; half++) {

                    //we skip some of the leaves to create a more sparse look
                    if (pass == 0 && surr.ordinal()%2 != 0) continue;
                    if (pass == 2 && surr.ordinal()%2 == 0) continue;

                    BlockVertexData[] outData = new BlockVertexData[8];

                    for (int v = 0; v < 8; v++) {

                        // Nab the vertex;
                        float x = quadData[v].x;
                        float z = quadData[v].z;
                        float y = quadData[v].y;

                        x *= (40f / 32f);
                        z *= (40f / 32f);

                        double len;
                        double angle;


                        // Rotate the vertex around x0,y=0.75
                        // Rotate on z axis
                        len = 0.75 - y;
                        angle = Math.atan2(x, y);
                        angle += Math.PI * (half == 1 ? 1.2 : -1.2);
                        x = (float) (Math.sin(angle) * len);
                        y = (float) (Math.cos(angle) * len);

                        //Random rand = new Random();
                        //rand.setSeed(pass);
                        // Rotate the vertex around x0,z0
                        // Rotate on x axis
                        len = Math.sqrt(y * y + z * z);
                        angle = Math.atan2(y, z);
                        angle += Math.PI * ((pass == 2 ? 0.28 : pass == 1 ? 0.06 : -0.17) );// + 0.1*rand.nextFloat());
                        y = (float) (Math.sin(angle) * len);
                        z = (float) (Math.cos(angle) * len);


                        // Rotate the vertex around x0,z0
                        // Rotate on y axis
                        len = Math.sqrt(x * x + z * z);
                        angle = Math.atan2(x, z);
                        angle += Math.PI * 0.25 * surr.ordinal() + (Math.PI * (pass == 1 ? (0.185 - 0.25) : pass == 2 ? 0.08 : 0.005));
                        x = (float) (Math.sin(angle) * len);
                        z = (float) (Math.cos(angle) * len);


                        // Move to center of block
                        x += 0.5f;
                        z += 0.5f;
                        y += pass == 2 ? -0.125 : pass == 0 ? 0.125 : 0;
                        //y -= 0.25f;


                        // Move to center of palm crown
                        x += surr.getOffset().getX();
                        z += surr.getOffset().getZ();


                        outData[v] = new BlockVertexData(x, y, z, quadData[v].u, quadData[v].v);
                    }

                    int[] vertices = Ints.concat(
                            outData[0].toInts(frondsTexture),
                            outData[1].toInts(frondsTexture),
                            outData[2].toInts(frondsTexture),
                            outData[3].toInts(frondsTexture)
                    );
                    builder.addUnculledFace(new BakedQuad(vertices,
                            0, FaceBakery.calculateFacing(vertices), frondsTexture, true)
                    );

                    vertices = Ints.concat(
                            outData[4].toInts(frondsTexture),
                            outData[5].toInts(frondsTexture),
                            outData[6].toInts(frondsTexture),
                            outData[7].toInts(frondsTexture)
                    );
                    builder.addUnculledFace(new BakedQuad(vertices,
                            0, FaceBakery.calculateFacing(vertices), frondsTexture, true)
                    );


                    bakedFronds[surr.ordinal()] = builder.build();

                }
            }
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
        if (state == null || side != null)
            return Collections.emptyList();

        LinkedList<BakedQuad> quads = new LinkedList<>();

        int direction = state.getValue(PalmLeavesProperties.DynamicPalmLeavesBlock.DIRECTION);

        if (direction != 0)
            quads.addAll(bakedFronds[direction-1].getQuads(state, null, rand, extraData, renderType));


        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Nonnull
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return frondsTexture;
    }

    @Nonnull
    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    public static class BlockVertexData {

        public float x;
        public float y;
        public float z;
        public int color;
        public float u;
        public float v;

        public BlockVertexData(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            color = 0xFFFFFFFF;
        }

        public BlockVertexData(BakedQuad quad, int vIndex) {
            this(quad.getVertices(), vIndex);
        }

        public BlockVertexData(int[] data, int vIndex) {
            vIndex *= 8;
            x = Float.intBitsToFloat(data[vIndex++]);
            y = Float.intBitsToFloat(data[vIndex++]);
            z = Float.intBitsToFloat(data[vIndex++]);
            color = data[vIndex++];
            u = Float.intBitsToFloat(data[vIndex++]);
            v = Float.intBitsToFloat(data[vIndex]);
        }

        public int[] toInts() {
            return new int[] { Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
                    color, Float.floatToRawIntBits(u), Float.floatToRawIntBits(v), 0, 0 };
        }

        protected int[] toInts(TextureAtlasSprite texture) {
            return new int[] {
                    Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
                    color,
                    Float.floatToRawIntBits(texture.getU(u)), Float.floatToRawIntBits(texture.getV(v)),
                    0, 0
            };
        }

    }

}