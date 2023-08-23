package club.someoneice.ims.mixin;

import club.someoneice.ims.IMSEvent;
import club.someoneice.ims.TagManagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract void blockEvent(BlockPos p_8746_, Block p_8747_, int p_8748_, int p_8749_);

    @Inject(method = "tickChunk(Lnet/minecraft/world/level/chunk/LevelChunk;I)V", at = @At("RETURN"))
    public void chunkSnow(LevelChunk chunk, int tick, CallbackInfo ci) {
        if (IMSEvent.shouldSnow) {
            ChunkPos chunkpos = chunk.getPos();
            int i = chunkpos.getMinBlockX();
            int j = chunkpos.getMinBlockZ();

            ServerLevel world = ((ServerLevel) (Object) this);
            BlockPos blockpos2 = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(i, 0, j, 15));
            BlockPos blockpos3 = blockpos2.below();

            if (blockpos2.getY() >= world.getMinBuildHeight() && blockpos2.getY() < world.getMaxBuildHeight()) {
                var block = world.getBlockState(blockpos2);
                var belowBlock = world.getBlockState(blockpos3);
                if (!(belowBlock.is(TagManagers.snowLayerBlacklist))) {
                    if ((block.isAir()) && Blocks.SNOW.defaultBlockState().canSurvive(world, blockpos2))
                        world.setBlockAndUpdate(blockpos2, Blocks.SNOW.defaultBlockState());
                    else if (belowBlock.getBlock() instanceof BushBlock) world.setBlockAndUpdate(blockpos3, Blocks.SNOW.defaultBlockState());
                }
            }
        }
    }
}
