package club.someoneice.ims.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.api.season.Season;
import sereneseasons.season.SeasonTime;
import club.someoneice.ims.IMSEvent;

@Mixin(SeasonTime.class)
public class SeasonHookMixin {
    @Inject(method = "getTropicalSeason", at = @At(value = "HEAD", target = "getTropicalSeason()Lsereneseasons/api/season/Season/TropicalSeason;"), cancellable = true, remap = false)
    public void alwaysShouldRainInBiomeInSeason(CallbackInfoReturnable<Season.TropicalSeason> cir) {
        if (IMSEvent.shouldSnow)
            cir.setReturnValue(Season.TropicalSeason.MID_WET);
    }
}
