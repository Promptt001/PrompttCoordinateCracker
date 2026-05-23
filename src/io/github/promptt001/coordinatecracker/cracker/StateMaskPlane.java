package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.utils.MathHelper;

/**
 * Row-padded bit planes that encode raw 0..3 states and precomputed parity
 * states for a fixed Y layer.
 */
final class StateMaskPlane {
    private static final int DIRECT_MASK_COUNT = 4;
    private static final int PARITY_MASK_OFFSET = 4;
    static final int MASK_COUNT = 6;

    final int width;
    final int depth;
    final int wordsPerRow;
    private final long[][] stateMasks;

    private StateMaskPlane(int width, int depth, int wordsPerRow, long[][] stateMasks) {
        this.width = width;
        this.depth = depth;
        this.wordsPerRow = wordsPerRow;
        this.stateMasks = stateMasks;
    }

    static StateMaskPlane build(EnumMCVersion version, int minX, int maxXExclusive, int y, int minZ, int maxZExclusive) {
        int width = maxXExclusive - minX;
        int depth = maxZExclusive - minZ;
        int wordsPerRow = (width + 63) >>> 6;
        long wordCountLong = (long) wordsPerRow * (long) depth;
        if(wordCountLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sieve plane is too large; reduce radius or increase thread count.");
        }

        int wordCount = (int) wordCountLong;
        long[][] stateMasks = new long[MASK_COUNT][wordCount];

        if(version == EnumMCVersion.V1_21_11) {
            buildVariant4Plane_1_21_11(minX, y, minZ, width, depth, wordsPerRow, stateMasks);
        } else {
            buildLegacyPlane(version, minX, y, minZ, width, depth, wordsPerRow, stateMasks);
        }

        return new StateMaskPlane(width, depth, wordsPerRow, stateMasks);
    }

    long extractVisibleBits(CompiledObservation observation, int bitIndex, int bitCount) {
        if(observation.visibleMapping == CompiledObservation.MAPPING_CONSTANT_ZERO) {
            return observation.wanted == 0 ? lowBitsMask(bitCount) : 0L;
        }

        long result = 0L;
        for(int i = 0; i < bitCount; i++) {
            int compactIndex = bitIndex + i;
            int row = compactIndex / this.width;
            int x = compactIndex - (row * this.width);
            if(row >= this.depth) {
                break;
            }
            if((extractVisibleBits(observation, row, x, 1) & 1L) != 0L) {
                result |= 1L << i;
            }
        }
        return result;
    }

    long extractVisibleBits(CompiledObservation observation, int row, int bitIndex, int bitCount) {
        if(observation.visibleMapping == CompiledObservation.MAPPING_CONSTANT_ZERO) {
            return observation.wanted == 0 ? lowBitsMask(bitCount) : 0L;
        }

        if(observation.visibleMapping == CompiledObservation.MAPPING_MODULO_TWO) {
            if(observation.wanted == 0 || observation.wanted == 1) {
                return extractBits(this.stateMasks[PARITY_MASK_OFFSET + observation.wanted], row, bitIndex, bitCount);
            }
            return 0L;
        }

        if(observation.wanted < 0 || observation.wanted >= DIRECT_MASK_COUNT) {
            return 0L;
        }
        return extractBits(this.stateMasks[observation.wanted], row, bitIndex, bitCount);
    }

    private static void buildVariant4Plane_1_21_11(int minX, int y, int minZ, int width, int depth, int wordsPerRow, long[][] stateMasks) {
        int[] xTerms = new int[width];
        for(int xIndex = 0; xIndex < width; xIndex++) {
            xTerms[xIndex] = (minX + xIndex) * 3129871;
        }

        long[] zTerms = new long[depth];
        for(int zIndex = 0; zIndex < depth; zIndex++) {
            zTerms[zIndex] = (long) (minZ + zIndex) * 116129781L;
        }

        for(int zIndex = 0; zIndex < depth; zIndex++) {
            int rowBase = zIndex * wordsPerRow;
            long zTerm = zTerms[zIndex];
            for(int xIndex = 0; xIndex < width; xIndex++) {
                int state = CompiledObservation.getVariant4ForMixedInput((long) xTerms[xIndex] ^ zTerm ^ (long) y) & 3;
                setStateBit(stateMasks, rowBase, xIndex, state);
            }
        }
    }

    private static void buildLegacyPlane(EnumMCVersion version, int minX, int y, int minZ, int width, int depth, int wordsPerRow, long[][] stateMasks) {
        for(int zIndex = 0; zIndex < depth; zIndex++) {
            int z = minZ + zIndex;
            int rowBase = zIndex * wordsPerRow;
            for(int xIndex = 0; xIndex < width; xIndex++) {
                int x = minX + xIndex;
                int state = computeLegacyTwoBitState(version, x, y, z) & 3;
                setStateBit(stateMasks, rowBase, xIndex, state);
            }
        }
    }

    private static void setStateBit(long[][] stateMasks, int rowBase, int xIndex, int state) {
        long bit = 1L << (xIndex & 63);
        int wordIndex = rowBase + (xIndex >>> 6);
        stateMasks[state][wordIndex] |= bit;
        stateMasks[PARITY_MASK_OFFSET + (state & 1)][wordIndex] |= bit;
    }

    private static long lowBitsMask(int bitCount) {
        return bitCount >= Long.SIZE ? -1L : (1L << bitCount) - 1L;
    }

    private long extractBits(long[] words, int row, int bitIndex, int bitCount) {
        int wordIndex = row * this.wordsPerRow + (bitIndex >>> 6);
        int bitOffset = bitIndex & 63;
        long value = words[wordIndex] >>> bitOffset;
        if(bitOffset != 0 && (bitIndex >>> 6) + 1 < this.wordsPerRow) {
            value |= words[wordIndex + 1] << (64 - bitOffset);
        }
        return bitCount == 64 ? value : value & ((1L << bitCount) - 1L);
    }

    private static int computeLegacyTwoBitState(EnumMCVersion version, int x, int y, int z) {
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
