package io.github.promptt001.coordinatecracker.io;

import io.github.promptt001.coordinatecracker.math.Matrix3;

/**
 * Immutable holder for the two aligned matrices that make up an editor pattern.
 */
public final class PatternData {
    private final Matrix3 patternMatrix;
    private final Matrix3 blockTypeMatrix;

    public PatternData(Matrix3 patternMatrix, Matrix3 blockTypeMatrix) {
        if(patternMatrix == null) throw new IllegalArgumentException("patternMatrix must not be null");
        if(blockTypeMatrix == null) throw new IllegalArgumentException("blockTypeMatrix must not be null");
        this.patternMatrix = new Matrix3(patternMatrix);
        this.blockTypeMatrix = new Matrix3(blockTypeMatrix);
    }

    public Matrix3 getPatternMatrix() {
        return new Matrix3(patternMatrix);
    }

    public Matrix3 getBlockTypeMatrix() {
        return new Matrix3(blockTypeMatrix);
    }
}
