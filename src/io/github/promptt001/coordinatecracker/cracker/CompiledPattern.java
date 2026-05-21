package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.data.EnumRotation;

/**
 * Immutable, primitive representation of one view direction's observations.
 */
final class CompiledPattern {
    final EnumRotation viewDirection;
    final CompiledObservation[] observations;
    final boolean impossible;

    CompiledPattern(EnumRotation viewDirection, CompiledObservation[] observations, boolean impossible) {
        this.viewDirection = viewDirection;
        this.observations = observations;
        this.impossible = impossible;
    }

    boolean matches(int x, int y, int z) {
        if(this.impossible) return false;
        for(CompiledObservation observation : this.observations) {
            if(!observation.matches(x, y, z)) return false;
        }
        return true;
    }

    boolean isTwoBitCacheCompatible() {
        if(this.impossible) return true;
        for(CompiledObservation observation : this.observations) {
            if(!observation.isTwoBitCacheCompatible()) return false;
        }
        return true;
    }
}
