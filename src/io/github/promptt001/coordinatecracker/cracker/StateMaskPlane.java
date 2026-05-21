package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.utils.MathHelper;

/**
 * Four bit planes that encode raw 0..3 states for a fixed Y layer.
 */
final class StateMaskPlane {
    final int width;
    final int depth;
    private final long[][] stateMasks;

    private StateMaskPlane(int width, int depth, long[][] stateMasks) {
        this.width = width;
        this.depth = depth;
        this.stateMasks = stateMasks;
    }

    static StateMaskPlane build(EnumMCVersion version, int minX, int maxXExclusive, int y, int minZ, int maxZExclusive) {
        int width = maxXExclusive - minX;
        int depth = maxZExclusive - minZ;
        long cellCountLong = (long) width * (long) depth;
        if(cellCountLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sieve plane is too large; reduce radius or increase thread count.");
        }

        int cellCount = (int) cellCountLong;
        long[][] stateMasks = new long[4][(cellCount + 63) >>> 6];
        int index = 0;
        for(int z = minZ; z < maxZExclusive; z++) {
            for(int x = minX; x < maxXExclusive; x++) {
                int state = computeTwoBitState(version, x, y, z);
                stateMasks[state & 3][index >>> 6] |= 1L << (index & 63);
                ++index;
            }
        }

        return new StateMaskPlane(width, depth, stateMasks);
    }

    long extractVisibleBits(CompiledObservation observation, int bitIndex, int bitCount) {
        if(observation.visibleMapping == CompiledObservation.MAPPING_CONSTANT_ZERO) {
            return observation.wanted == 0 ? lowBitsMask(bitCount) : 0L;
        }

        if(observation.visibleMapping == CompiledObservation.MAPPING_MODULO_TWO) {
            if(observation.wanted == 0) {
                return extractBits(this.stateMasks[0], bitIndex, bitCount) | extractBits(this.stateMasks[2], bitIndex, bitCount);
            }
            if(observation.wanted == 1) {
                return extractBits(this.stateMasks[1], bitIndex, bitCount) | extractBits(this.stateMasks[3], bitIndex, bitCount);
            }
            return 0L;
        }

        if(observation.wanted < 0 || observation.wanted > 3) {
            return 0L;
        }
        return extractBits(this.stateMasks[observation.wanted], bitIndex, bitCount);
    }

    private static long lowBitsMask(int bitCount) {
        return bitCount >= Long.SIZE ? -1L : (1L << bitCount) - 1L;
    }

    private static long extractBits(long[] words, int bitIndex, int bitCount) {
        int wordIndex = bitIndex >>> 6;
        int bitOffset = bitIndex & 63;
        long value = words[wordIndex] >>> bitOffset;
        if(bitOffset != 0 && wordIndex + 1 < words.length) {
            value |= words[wordIndex + 1] << (64 - bitOffset);
        }
        return bitCount == 64 ? value : value & ((1L << bitCount) - 1L);
    }

    private static int computeTwoBitState(EnumMCVersion version, int x, int y, int z) {
        switch(version) {
        case V1_12_2:
            return MathHelper.getRotationForCoordinates(x, y, z) & 3;
        case V1_16_5:
            return MathHelper.getRotationForCoordinates_1_16(x, y, z) & 3;
        case V1_21_11:
        default:
            return CompiledObservation.getVariant4ForCoordinates_1_21_11(x, y, z);
        }
    }
}
