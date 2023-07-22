package club.someoneice.ims;

import net.minecraftforge.fml.common.Mod;

@Mod(("ims"))
public class IMS {
    public static final String MODID = "ims";
    // No. Didn't try to play with chunk again.

    public IMS() {
        new TagManagers();
    }
}
