package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.utils.MathHelper;

/**
 * One precomputed block-state observation used by all scanner backends.
 */
final class CompiledObservation {
    static final int MAPPING_DIRECT = 0;
    static final int MAPPING_MODULO_TWO = 1;
    static final int MAPPING_CONSTANT_ZERO = 2;

    final int dx;
    final int dy;
    final int dz;
    final int wanted;
    final int variantCount;
    final int visibleMapping;
    final EnumMCVersion version;

    private static final long RANDOM_MULTIPLIER = 25214903917L;
    private static final long RANDOM_INCREMENT = 11L;
    private static final long RANDOM_SEED_MASK = (1L << 48) - 1;

    CompiledObservation(int dx, int dy, int dz, int wanted, int variantCount, int visibleMapping, EnumMCVersion version) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.wanted = wanted;
        this.variantCount = variantCount;
        this.visibleMapping = visibleMapping;
        this.version = version;
    }

    boolean matches(int x, int y, int z) {
        int lookupX = x + this.dx;
        int lookupY = y + this.dy;
        int lookupZ = z + this.dz;
        int rawState;

        switch(this.version) {
        case V1_12_2:
            rawState = MathHelper.getRotationForCoordinates(lookupX, lookupY, lookupZ);
            break;
        case V1_16_5:
            rawState = MathHelper.getRotationForCoordinates_1_16(lookupX, lookupY, lookupZ);
            break;
        case V1_21_11:
        default:
            rawState = this.variantCount == 4
                ? getVariant4ForCoordinates_1_21_11(lookupX, lookupY, lookupZ)
                : MathHelper.getRotationForCoordinates_1_21_11(lookupX, lookupY, lookupZ, this.variantCount);
            break;
        }

        return visibleStateFor(rawState) == this.wanted;
    }

    boolean matchesCachedState(int rawState) {
        return visibleStateFor(rawState) == this.wanted;
    }

    private int visibleStateFor(int rawState) {
        if(this.visibleMapping == MAPPING_CONSTANT_ZERO) return 0;
        if(this.visibleMapping == MAPPING_MODULO_TWO) return rawState & 1;
        return rawState;
    }

    boolean isTwoBitCacheCompatible() {
        if(this.visibleMapping == MAPPING_CONSTANT_ZERO) {
            return this.wanted == 0;
        }
        if(this.visibleMapping == MAPPING_MODULO_TWO) {
            return this.wanted >= 0 && this.wanted < 2;
        }
        return this.variantCount == 4
            && this.wanted >= 0
            && this.wanted < 4;
    }

    static int getVariant4ForCoordinates_1_21_11(int x, int y, int z) {
        long i = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        i = i * i * 42317861L + i * 11L;
        long seed = i >> 16;
        long randomSeed = (seed ^ RANDOM_MULTIPLIER) & RANDOM_SEED_MASK;
        randomSeed = (randomSeed * RANDOM_MULTIPLIER + RANDOM_INCREMENT) & RANDOM_SEED_MASK;
        return (int) (randomSeed >>> 46);
    }

    int selectivityScore() {
        if(this.visibleMapping == MAPPING_DIRECT) return this.variantCount;
        if(this.visibleMapping == MAPPING_MODULO_TWO) return 2;
        return 1;
    }
}
