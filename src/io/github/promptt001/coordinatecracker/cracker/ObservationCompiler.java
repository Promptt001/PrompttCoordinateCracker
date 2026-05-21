package io.github.promptt001.coordinatecracker.cracker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.data.PatternRelative;
import io.github.promptt001.coordinatecracker.math.Matrix3;

/**
 * Converts the editable 3D pattern into primitive observations for scanner backends.
 */
final class ObservationCompiler {
    private static final EnumRotation[] CANONICAL_VIEW_DIRECTIONS = new EnumRotation[] {
        EnumRotation.R0, EnumRotation.R90, EnumRotation.R180, EnumRotation.R270,
        EnumRotation.FLOOR_R0, EnumRotation.FLOOR_R90, EnumRotation.FLOOR_R180, EnumRotation.FLOOR_R270,
        EnumRotation.CEILING_R0, EnumRotation.CEILING_R90, EnumRotation.CEILING_R180, EnumRotation.CEILING_R270
    };

    private ObservationCompiler() {
        // utility class
    }

    static CompiledPattern[] compilePatterns(PatternRelative scope, EnumMCVersion version, EnumRotation rotation) {
        EnumRotation[] viewDirections = viewDirectionsFor(rotation);
        CompiledPattern[] compiledPatterns = new CompiledPattern[viewDirections.length];
        for(int i = 0; i < viewDirections.length; i++) {
            compiledPatterns[i] = compilePattern(scope, version, viewDirections[i]);
        }
        return compiledPatterns;
    }

    static EnumRotation[] viewDirectionsFor(EnumRotation scanMode) {
        if(!scanMode.isCompositeScanMode()) {
            return new EnumRotation[] {scanMode};
        }

        List<EnumRotation> viewDirections = new ArrayList<EnumRotation>();
        for(EnumRotation viewDirection : CANONICAL_VIEW_DIRECTIONS) {
            if(scanMode.includesView(viewDirection)) {
                viewDirections.add(viewDirection);
            }
        }
        return viewDirections.toArray(new EnumRotation[viewDirections.size()]);
    }

    private static CompiledPattern compilePattern(PatternRelative scope, EnumMCVersion version, EnumRotation viewDirection) {
        Matrix3 pattern = scope.getPatternMatrix(EnumRotation.R0);
        Matrix3 blockTypes = scope.getBlockTypeMatrix(EnumRotation.R0);
        EnumMCVersion selectedVersion = version == null ? EnumMCVersion.V1_21_11 : version;
        List<CompiledObservation> observations = new ArrayList<CompiledObservation>();
        boolean impossible = false;

        for(int px = 0; px < pattern.getSizeX(); px++) {
            for(int py = 0; py < pattern.getSizeY(); py++) {
                for(int pz = 0; pz < pattern.getSizeZ(); pz++) {
                    int wanted = pattern.getMatrixArray()[py][px][pz];
                    if(wanted == 4) continue;

                    EnumBlockType blockType = EnumBlockType.fromId(blockTypes.getMatrixArray()[py][px][pz]);
                    int visibleMapping = visibleMappingFor(blockType);

                    observations.add(new CompiledObservation(
                        getObservationOffsetX(pattern, px, py, pz, viewDirection),
                        getObservationOffsetY(pattern, px, py, pz, viewDirection),
                        getObservationOffsetZ(pattern, px, py, pz, viewDirection),
                        wanted,
                        blockType.getModelVariantCount(),
                        visibleMapping,
                        selectedVersion
                    ));
                }
            }
        }

        observations.sort(new Comparator<CompiledObservation>() {
            @Override
            public int compare(CompiledObservation a, CompiledObservation b) {
                return b.selectivityScore() - a.selectivityScore();
            }
        });

        return new CompiledPattern(viewDirection, observations.toArray(new CompiledObservation[observations.size()]), impossible);
    }

    /**
     * Maps the visible GUI grid to a wall plane. The selected coordinate is the
     * center/reference block: columns are horizontal screen offsets, rows are
     * vertical offsets, and the layer spinner is depth into the wall.
     */
    static int getWallObservationOffsetX(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
        int horizontal = patternX - pattern.getCenterX();
        int depth = patternY - pattern.getCenterY();

        switch(viewDirection) {
        case R90: // facing east: right is south (+Z), depth is +X
            return depth;
        case R180: // facing south: right is west (-X), depth is +Z
            return -horizontal;
        case R270: // facing west: right is north (-Z), depth is -X
            return -depth;
        case R0: // facing north: right is east (+X), depth is -Z
        default:
            return horizontal;
        }
    }

    static int getWallObservationOffsetY(Matrix3 pattern, int patternX, int patternY, int patternZ) {
        return pattern.getCenterZ() - patternZ;
    }

    static int getWallObservationOffsetZ(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
        int horizontal = patternX - pattern.getCenterX();
        int depth = patternY - pattern.getCenterY();

        switch(viewDirection) {
        case R90: // facing east: right is south (+Z), depth is +X
            return horizontal;
        case R180: // facing south: right is west (-X), depth is +Z
            return depth;
        case R270: // facing west: right is north (-Z), depth is -X
            return -horizontal;
        case R0: // facing north: right is east (+X), depth is -Z
        default:
            return -depth;
        }
    }

    static int getObservationOffsetX(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
        if(!viewDirection.isHorizontalSurface()) {
            return getWallObservationOffsetX(pattern, patternX, patternY, patternZ, viewDirection);
        }

        int horizontal = patternX - pattern.getCenterX();
        int forward = pattern.getCenterZ() - patternZ;
        return getHorizontalSurfaceOffsetX(horizontal, forward, viewDirection);
    }

    static int getObservationOffsetY(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
        if(!viewDirection.isHorizontalSurface()) {
            return getWallObservationOffsetY(pattern, patternX, patternY, patternZ);
        }

        int depth = patternY - pattern.getCenterY();
        return viewDirection.isFloor() ? -depth : depth;
    }

    static int getObservationOffsetZ(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
        if(!viewDirection.isHorizontalSurface()) {
            return getWallObservationOffsetZ(pattern, patternX, patternY, patternZ, viewDirection);
        }

        int horizontal = patternX - pattern.getCenterX();
        int forward = pattern.getCenterZ() - patternZ;
        return getHorizontalSurfaceOffsetZ(horizontal, forward, viewDirection);
    }

    private static int getHorizontalSurfaceOffsetX(int horizontal, int forward, EnumRotation viewDirection) {
        switch(viewDirection) {
        case FLOOR_R90:
        case CEILING_R90:
            return forward;
        case FLOOR_R180:
        case CEILING_R180:
            return -horizontal;
        case FLOOR_R270:
        case CEILING_R270:
            return -forward;
        case FLOOR_R0:
        case CEILING_R0:
        default:
            return horizontal;
        }
    }

    private static int getHorizontalSurfaceOffsetZ(int horizontal, int forward, EnumRotation viewDirection) {
        switch(viewDirection) {
        case FLOOR_R90:
        case CEILING_R90:
            return horizontal;
        case FLOOR_R180:
        case CEILING_R180:
            return forward;
        case FLOOR_R270:
        case CEILING_R270:
            return -horizontal;
        case FLOOR_R0:
        case CEILING_R0:
        default:
            return -forward;
        }
    }

    private static int visibleMappingFor(EnumBlockType blockType) {
        if(blockType == null) return CompiledObservation.MAPPING_MODULO_TWO;
        if(blockType.getGuiStateCount() == 1) return CompiledObservation.MAPPING_CONSTANT_ZERO;
        if(blockType.getGuiStateCount() == 2) return CompiledObservation.MAPPING_MODULO_TWO;
        return CompiledObservation.MAPPING_DIRECT;
    }
}
