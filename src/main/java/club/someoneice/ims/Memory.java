package club.someoneice.ims;

import com.google.common.collect.Maps;
import net.minecraft.world.level.ChunkPos;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Memory {
    private static final Map<ChunkPos, Long> data = new ConcurrentHashMap<>();

    public static void remember(ChunkPos chunkPos) {
        data.put(chunkPos, Instant.now().getEpochSecond());
    }

    public static boolean hasForgotten(ChunkPos chunkPos) {
        long rememberedTime = data.getOrDefault(chunkPos, 0L);
        long currentTime = Instant.now().getEpochSecond();
        long difference = currentTime - rememberedTime;
        return difference >= 600;
    }

    public static void erase() {
        data.clear();
    }
}
