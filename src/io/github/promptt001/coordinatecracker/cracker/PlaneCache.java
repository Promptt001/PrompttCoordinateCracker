package io.github.promptt001.coordinatecracker.cracker;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;

/**
 * Small LRU cache for the two-bit state planes used by the CPU sieve.
 */
final class PlaneCache {
    private static final long TARGET_CACHE_BYTES = 32L * 1024L * 1024L;

    private final int minX;
    private final int maxXExclusive;
    private final int minZ;
    private final int maxZExclusive;
    private final int width;
    private final int depth;
    private final EnumMCVersion version;
    private final LinkedHashMap<Integer, StateMaskPlane> planes;

    PlaneCache(int minX, int maxXExclusive, int minZ, int maxZExclusive, EnumMCVersion version, int distinctLookupYOffsets) {
        this.minX = minX;
        this.maxXExclusive = maxXExclusive;
        this.minZ = minZ;
        this.maxZExclusive = maxZExclusive;
        this.width = maxXExclusive - minX;
        this.depth = maxZExclusive - minZ;
        this.version = version;

        long wordsPerRow = Math.max(1L, (long) ((this.width + 63) >>> 6));
        long words = Math.max(1L, wordsPerRow * (long) this.depth);
        long bytesPerPlane = words * Long.BYTES * (long) StateMaskPlane.MASK_COUNT;
        int byMemory = (int) Math.max(1L, Math.min(16L, TARGET_CACHE_BYTES / Math.max(1L, bytesPerPlane)));
        final int maxCachedPlanes = Math.max(1, Math.min(Math.max(1, distinctLookupYOffsets + 1), byMemory));

        this.planes = new LinkedHashMap<Integer, StateMaskPlane>(maxCachedPlanes + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, StateMaskPlane> eldest) {
                return size() > maxCachedPlanes;
            }
        };
    }

    StateMaskPlane get(int y) {
        StateMaskPlane plane = this.planes.get(y);
        if(plane == null) {
            plane = StateMaskPlane.build(this.version, this.minX, this.maxXExclusive, y, this.minZ, this.maxZExclusive);
            this.planes.put(y, plane);
        }
        return plane;
    }
}
