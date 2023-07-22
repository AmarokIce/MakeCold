package club.someoneice.ims;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Utils {
    private static final int SET_BLOCK_FLAGS = 2 | 16;

    public static void setBlock(ServerLevel world, BlockPos pos, Block block) {
        world.setBlock(pos, block.defaultBlockState(), 3 | 16);
    }

    public static void updateBlockFromAbove(ServerLevel world, BlockPos pos, Block above) {
        BlockState oldBlockState = world.getBlockState(pos);
        BlockState newBlockState = oldBlockState.updateShape(Direction.UP, above.defaultBlockState(), world, pos.above(), pos);
        Block.updateOrDestroy(oldBlockState, newBlockState, world, pos, SET_BLOCK_FLAGS & -34);
    }

    public static boolean shouldUpdateBlock(Block block) {
        return block.builtInRegistryHolder().is(BlockTags.DIRT);
    }

    public static boolean coldAndDark(Level world, Biome biome, BlockPos pos) {
        return biome.coldEnoughToSnow(pos) && world.getBrightness(LightLayer.BLOCK, pos) < 10;
    }

    public static boolean tryAddToQueue(Level world, ChunkPos chunkPos) {
        if (Memory.hasForgotten(chunkPos)) {
            Memory.remember(chunkPos);
            IMSEvent.chunkBuffer.add(world.getChunk(chunkPos.x, chunkPos.z));
            return true;
        } else return false;
    }
}
