package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.Main;
import io.github.promptt001.coordinatecracker.data.EnumAccelerationMode;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.data.PatternRelative;
import io.github.promptt001.coordinatecracker.math.Matrix3;

/**
 * Immutable snapshot of the UI state needed to start one coordinate scan.
 */
public final class ScanLaunchRequest {
    private final Matrix3 pattern;
    private final Matrix3 blockTypes;
    private final int radius;
    private final int yMin;
    private final int yMax;
    private final EnumMCVersion version;
    private final EnumRotation rotation;
    private final int threadCount;
    private final EnumAccelerationMode accelerationMode;
    private final int maxMatches;

    public ScanLaunchRequest(Matrix3 pattern, Matrix3 blockTypes, int radius, int yMin, int yMax,
                             EnumMCVersion version, EnumRotation rotation, int threadCount,
                             EnumAccelerationMode accelerationMode, int maxMatches) {
        this.pattern = new Matrix3(pattern);
        this.blockTypes = new Matrix3(blockTypes);
        this.radius = radius;
        this.yMin = yMin;
        this.yMax = yMax;
        this.version = version;
        this.rotation = rotation;
        this.threadCount = threadCount;
        this.accelerationMode = accelerationMode;
        this.maxMatches = maxMatches;
    }

    public CoordinateBruteforcer createBruteforcer(Main mainInstance) {
        PatternRelative relativePattern = new PatternRelative(this.pattern.getSize(), this.pattern.getCenter());
        relativePattern.setPatternMatrix(new Matrix3(this.pattern), false);
        relativePattern.setBlockTypeMatrix(new Matrix3(this.blockTypes), false);
        relativePattern.load();

        return new CoordinateBruteforcer(
            relativePattern,
            this.radius,
            this.yMin,
            this.yMax,
            this.version,
            this.rotation,
            mainInstance,
            this.threadCount,
            this.accelerationMode,
            this.maxMatches
        );
    }
}
