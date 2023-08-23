package club.someoneice.ims;

import net.minecraft.core.HolderSet;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(("ims"))
public class IMS {
    public static final String MODID = "ims";
    // No. Didn't try to play with chunk again.

    public IMS() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, new Config().common);
        new TagManagers();
    }

    public static final class Config {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ForgeConfigSpec common = init();

        public static int days = 200;

        public ForgeConfigSpec init() {
            builder.comment("General settings").push("general");

            days = builder.define("day", days).get();

            builder.pop();
            return builder.build();
        }
    }
}
