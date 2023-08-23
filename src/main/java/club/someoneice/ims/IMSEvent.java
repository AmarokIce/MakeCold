package club.someoneice.ims;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class IMSEvent {
    public static boolean shouldSnow = true;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Memory.erase();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getPlayer();

        shouldSnow = (player.getLevel().getGameTime() / 24000) < IMS.Config.days;

        /*
        if (player.getLevel().isClientSide()) return;
        int radius = 7;
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                Utils.tryAddToQueue(player.getLevel(), new ChunkPos(x, z));
            }
        }
        */
    }

    /*
    // @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor world = event.getWorld();
        if (world.isClientSide() || !((Level) world).dimensionTypeRegistration().is(DimensionType.OVERWORLD_LOCATION)) return;
        chunkBuffer.add((LevelChunk) event.getChunk());
    }

    static final List<LevelChunk> chunkBuffer = Lists.newArrayList();


    // @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;
        var world = (ServerLevel) event.world;
        if (!world.dimensionTypeRegistration().is(DimensionType.OVERWORLD_LOCATION) || chunkBuffer.isEmpty()) return;
        if (world.getDayTime() == 0) {
            shouldSnow = (world.getGameTime() / 24000) < 200;
        }

        var it = chunkBuffer.remove(0);
        if (it.getStatus().isOrAfter(ChunkStatus.FULL)) {
            ChunkPos chunkPos = it.getPos();

            Memory.remember(chunkPos);
            for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                    BlockPos topPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
                    recalculateBlock(world, topPos);
                }
            }
        } else chunkBuffer.add(it);

    }

    public static void recalculateBlock(ServerLevel world, BlockPos freePos) {
        BlockState freeState = world.getBlockState(freePos);
        Block freeBlock = freeState.getBlock();

        BlockPos topPos = freePos.below();
        BlockState topState = world.getBlockState(topPos);
        Block topBlock = topState.getBlock();

        BlockPos bottomPos = topPos.below();
        BlockState bottomState = world.getBlockState(bottomPos);
        Block bottomBlock = bottomState.getBlock();

        Biome biome = world.getBiome(topPos).value();
        boolean shouldSnow = IMSEvent.shouldSnow || biome.shouldSnow(world, freePos) || Utils.coldAndDark(world, biome, bottomPos);

        if (freeBlock == Blocks.WATER
                || topBlock == Blocks.WATER
                || bottomBlock == Blocks.WATER
                || topBlock instanceof IPlantable
                || bottomBlock instanceof IPlantable
                || freeBlock instanceof IPlantable
                || topBlock instanceof Container
                || bottomBlock instanceof Container
                || freeBlock instanceof Container
        ) return;

        if (shouldSnow) {
            if (freeBlock == Blocks.AIR && Blocks.SNOW.defaultBlockState().canSurvive(world, freePos)) {
                Utils.setBlock(world, freePos, Blocks.SNOW);
                if (Utils.shouldUpdateBlock(topBlock)) Utils.updateBlockFromAbove(world, topPos, Blocks.SNOW);
                return;
            }
            else if (topBlock == Blocks.AIR && Blocks.SNOW.defaultBlockState().canSurvive(world, topPos)) {
                Utils.setBlock(world, topPos, Blocks.SNOW);
                if (Utils.shouldUpdateBlock(bottomBlock)) Utils.updateBlockFromAbove(world, bottomPos, Blocks.SNOW);
                return;
            } else if (bottomBlock == Blocks.AIR && Blocks.SNOW.defaultBlockState().canSurvive(world, bottomPos)) {
                Utils.setBlock(world, bottomPos, Blocks.SNOW);
                Utils.updateBlockFromAbove(world, bottomPos.below(), Blocks.SNOW);
                return;
            }
        }

        if (!shouldSnow && topBlock == Blocks.SNOW) {
            Utils.setBlock(world, topPos, Blocks.AIR);
            if (Utils.shouldUpdateBlock(bottomBlock)) Utils.updateBlockFromAbove(world, bottomPos, Blocks.AIR);
        }
    }

    */
}
