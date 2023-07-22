package club.someoneice.ims.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import club.someoneice.ims.IMSEvent;

@Mixin(Biome.class)
public class BiomeMixin {
    @Inject(method = "shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    public void setShouldSnow(LevelReader reader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (IMSEvent.shouldSnow) {
            if (pos.getY() >= reader.getMinBuildHeight() && pos.getY() < reader.getMaxBuildHeight()) {
                BlockState blockstate = reader.getBlockState(pos);
                BlockState bottom = reader.getBlockState(pos.below());
                if (bottom.getBlock() == Blocks.WATER) cir.setReturnValue(false);
                if (blockstate.isAir() && Blocks.SNOW.defaultBlockState().canSurvive(reader, pos)) {
                    cir.setReturnValue(true);
                }
            }

            cir.setReturnValue(false);
        }
    }
}
