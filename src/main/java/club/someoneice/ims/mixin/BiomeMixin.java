package club.someoneice.ims.mixin;

import club.someoneice.ims.IMSEvent;
import club.someoneice.ims.TagManagers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class BiomeMixin {
    @Inject(method = "shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    public void setShouldSnow(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IMSEvent.shouldSnow) {
            if (pos.getY() >= reader.getMinBuildHeight() && pos.getY() < reader.getMaxBuildHeight()) {
                var block = reader.getBlockState(pos);
                var belowBlock = reader.getBlockState(pos.below());
                if (!(belowBlock.is(TagManagers.snowLayerBlacklist)) && block.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(reader, pos))
                    cir.setReturnValue(true);
            }
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getTemperature(Lnet/minecraft/core/BlockPos;)F", at = @At("HEAD"), cancellable = true)
    private void getTemperature(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (IMSEvent.shouldSnow) cir.setReturnValue(-8.5F);
    }

    /*
    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    public void shouldFreeze(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IMSEvent.shouldSnow) cir.setReturnValue(true);
    }

    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    public void shouldFreeze(LevelReader reader, BlockPos pos, boolean b, CallbackInfoReturnable<Boolean> cir) {
        if (IMSEvent.shouldSnow) cir.setReturnValue(true);
    }
    */

    @Inject(method = "coldEnoughToSnow(Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    public void coldEnoughSnow(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IMSEvent.shouldSnow) cir.setReturnValue(true);
    }
}
