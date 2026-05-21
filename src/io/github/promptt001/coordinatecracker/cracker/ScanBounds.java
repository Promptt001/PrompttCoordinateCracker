package io.github.promptt001.coordinatecracker.cracker;

/**
 * Inclusive offset bounds needed to extend cached state planes around a scan chunk.
 */
final class ScanBounds {
    final int minDx;
    final int maxDx;
    final int minDz;
    final int maxDz;

    ScanBounds(int minDx, int maxDx, int minDz, int maxDz) {
        this.minDx = minDx;
        this.maxDx = maxDx;
        this.minDz = minDz;
        this.maxDz = maxDz;
    }

    static ScanBounds from(CompiledPattern[] compiledPatterns) {
        int minDx = 0;
        int maxDx = 0;
        int minDz = 0;
        int maxDz = 0;
        boolean found = false;

        for(CompiledPattern pattern : compiledPatterns) {
            for(CompiledObservation observation : pattern.observations) {
                if(!found) {
                    minDx = observation.dx;
                    maxDx = observation.dx;
                    minDz = observation.dz;
                    maxDz = observation.dz;
                    found = true;
                } else {
                    if(observation.dx < minDx) minDx = observation.dx;
                    if(observation.dx > maxDx) maxDx = observation.dx;
                    if(observation.dz < minDz) minDz = observation.dz;
                    if(observation.dz > maxDz) maxDz = observation.dz;
                }
            }
        }

        return new ScanBounds(minDx, maxDx, minDz, maxDz);
    }
}
