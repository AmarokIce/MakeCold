package club.someoneice.ims;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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

        shouldSnow = (player.getLevel().getGameTime() / 24000) < 200;

        if (player.getLevel().isClientSide()) return;
        int radius = 7;
        for (int x = -radius; x < radius; x++) {
            for (int z = -radius; z < radius; z++) {
                Utils.tryAddToQueue(player.getLevel(), new ChunkPos(x, z));
            }
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor world = event.getWorld();
        if (world.isClientSide() || !((Level) world).dimensionTypeRegistration().is(DimensionType.OVERWORLD_LOCATION)) return;
        chunkBuffer.add((LevelChunk) event.getChunk());
    }

    // static final List<LevelChunk> chunkUnload = Lists.newArrayList();
    static final List<LevelChunk> chunkBuffer = Lists.newCopyOnWriteArrayList();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) return;
        var world = (ServerLevel) event.world;
        if (!world.dimensionTypeRegistration().is(DimensionType.OVERWORLD_LOCATION) || chunkBuffer.isEmpty()) return;
        if (world.getDayTime() == 0) {
            shouldSnow = (world.getGameTime() / 24000) < 200;
        }

        /*
        for (int i = 0; i < chunkUnload.size(); i++) {
            LevelChunk chunk = chunkUnload.remove(0);
            if (!chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                if (!Utils.tryAddToQueue(world, chunk.getPos())) {
                    chunkBuffer.add(chunk);
                }

                return;
            }

            ChunkPos chunkPos = chunk.getPos();

            for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                    BlockPos topPos = new BlockPos(x, 0, z);
                    topPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, topPos);

                    BlockPos groundPos = new BlockPos(x, 0, z);
                    groundPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, groundPos);

                    recalculateBlock(world, topPos);

                    boolean groundPositionDifferent = !topPos.equals(groundPos);
                    Block groundBlock = world.getBlockState(groundPos).getBlock();

                    if (groundPositionDifferent && groundBlock == Blocks.AIR) {
                        recalculateBlock(world, groundPos);
                    } else if (groundPositionDifferent && groundBlock == Blocks.SNOW) {
                        recalculateBlock(world, groundPos.above());
                    }
                }
            }
        }
         */

        LevelChunk chunk = chunkBuffer.get(0);
        if (!chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            if (Utils.tryAddToQueue(world, chunk.getPos())) chunkBuffer.remove(0);
            return;
        } else chunkBuffer.remove(0);


        ChunkPos chunkPos = chunk.getPos();
        Memory.remember(chunkPos);
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                BlockPos topPos = world.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z));
                recalculateBlock(world, topPos);

                /*
                BlockPos groundPos = new BlockPos(x, 0, z);
                groundPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, groundPos);

                boolean groundPositionDifferent = !topPos.equals(groundPos);
                Block groundBlock = world.getBlockState(groundPos).getBlock();

                if (groundPositionDifferent && groundBlock == Blocks.AIR) {
                    recalculateBlock(world, groundPos);
                } else if (groundPositionDifferent && groundBlock == Blocks.SNOW) {
                    recalculateBlock(world, groundPos.above());
                }
                */
            }
        }
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
        boolean shouldFreeze = IMSEvent.shouldSnow || biome.shouldFreeze(world, freePos); // TODO: Water freezing, biomes.shouldFreeze causes a lot of lag, MC issue?

        if ((shouldSnow || shouldFreeze)) {
            if (freeBlock == Blocks.WATER) {
                Utils.setBlock(world, freePos, Blocks.ICE);
                return;
            } else if (topBlock == Blocks.WATER) {
                Utils.setBlock(world, topPos, Blocks.ICE);
                return;
            } else if (bottomBlock == Blocks.WATER) {
                Utils.setBlock(world, bottomPos, Blocks.ICE);
                return;
            }
        }

        if (topBlock instanceof DoublePlantBlock
                || bottomBlock instanceof DoublePlantBlock
                || freeBlock instanceof DoublePlantBlock
                || topBlock instanceof SugarCaneBlock
                || bottomBlock instanceof SugarCaneBlock
                || freeBlock instanceof SugarCaneBlock
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

        /*
        if (topState.is(TagManagers.snowLayerBlacklist)
                || topBlock    instanceof LiquidBlock
                || topBlock    instanceof HalfTransparentBlock
                || topBlock    instanceof IPlantable
                || topBlock    instanceof BonemealableBlock
                || topBlock    == Blocks.SEA_LANTERN
        ) {
            Utils.setBlock(world, freePos, Blocks.AIR);
            return;
        }

        if (bottomState.is(TagManagers.snowLayerBlacklist)
                || bottomBlock instanceof LiquidBlock
                || bottomBlock instanceof HalfTransparentBlock
                || bottomBlock instanceof IPlantable
                || bottomBlock instanceof BonemealableBlock
                || bottomBlock == Blocks.SEA_LANTERN
        ) {
            Utils.setBlock(world, topPos, Blocks.AIR);
            return;
        }

         */
    }
}
