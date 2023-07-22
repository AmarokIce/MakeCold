package club.someoneice.ims;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class TagManagers {
    public static TagKey<Block> snowLayerBlacklist = BlockTags.create(new ResourceLocation("snowcoated", "snow_layer_blacklist"));
    public static TagKey<Block> snowLayerWhitelist = BlockTags.create(new ResourceLocation("snowcoated", "snow_layer_whitelist"));
}
