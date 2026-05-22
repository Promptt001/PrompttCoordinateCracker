package io.github.promptt001.coordinatecracker.cracker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import io.github.promptt001.coordinatecracker.data.EnumBlockType;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.math.Matrix2;
import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector2;
import io.github.promptt001.coordinatecracker.math.Vector3;
import io.github.promptt001.coordinatecracker.io.PatternCodec;
import io.github.promptt001.coordinatecracker.io.PatternData;
import io.github.promptt001.coordinatecracker.io.TextureManager;
import io.github.promptt001.coordinatecracker.utils.MatrixHelper;

public final class CoordinateCrackerRegressionTest {

    public static void main(String[] args) {
        testUnknownBlockTokensDoNotDefaultToDeepslate();
        testRemovedStaticTokensAreClassified();
        testEnumBlockTypeIdLookupIsStrict();
        testPatternCodecRoundTripAndValidation();
        testWallFacingOffsets();
        testHorizontalSurfaceOffsets();
        testSurfaceSelectionScanModes();
        testGpuFacingWireRoundTripIncludesHorizontalSurfaces();
        testFacingLabels();
        testRectangularMatrixCopyAndRotation();
        testOriginOutwardScanRegionsCoverSearchSquareExactlyOnce();
        testCompiledObservationAndPatternHelpers();
        testCandidateMaskRangeConstraints();
        testStateMaskPlaneExtractsVisibleBitsAcrossWordBoundaries();
        testMatchCollectorSortsByDistance();
        testTextureManagerUsesOneFrameForAnimatedTextureStrips();
        System.out.println("All regression tests passed.");
    }


    private static void testTextureManagerUsesOneFrameForAnimatedTextureStrips() {
        File root = new File(System.getProperty("java.io.tmpdir"), "pcc-texture-strip-" + System.nanoTime());
        try {
            File blockDir = new File(root, "assets/minecraft/textures/block");
            if(!blockDir.mkdirs()) throw new IOException("Could not create texture test directory");

            BufferedImage strip = new BufferedImage(16, 64, BufferedImage.TYPE_INT_ARGB);
            for(int y = 0; y < strip.getHeight(); y++) {
                for(int x = 0; x < strip.getWidth(); x++) {
                    int frame = y / 16;
                    int argb = 0xff000000 | (frame << 16) | (x << 8) | y;
                    strip.setRGB(x, y, argb);
                }
            }
            ImageIO.write(strip, "png", new File(blockDir, "sculk.png"));

            TextureManager textureManager = new TextureManager();
            textureManager.loadUserSource(root);
            BufferedImage sculk = textureManager.getTexture(EnumBlockType.SCULK);
            assertTrue(sculk != null, "sculk texture should load from a vanilla assets tree");
            assertEquals(Integer.valueOf(16), Integer.valueOf(sculk.getWidth()), "animated sculk strip should render as one frame width");
            assertEquals(Integer.valueOf(16), Integer.valueOf(sculk.getHeight()), "animated sculk strip should render as one frame height");
            assertEquals(Integer.valueOf(strip.getRGB(7, 15)), Integer.valueOf(sculk.getRGB(7, 15)), "representative frame should preserve first-frame pixels");
            assertEquals(Integer.valueOf(strip.getRGB(0, 0)), Integer.valueOf(sculk.getRGB(0, 0)), "representative frame should start at first frame origin");
        } catch(IOException e) {
            throw new AssertionError("Texture strip test setup failed", e);
        } finally {
            deleteRecursively(root);
        }
    }

    private static void deleteRecursively(File file) {
        if(file == null || !file.exists()) return;
        if(file.isDirectory()) {
            File[] children = file.listFiles();
            if(children != null) {
                for(File child : children) deleteRecursively(child);
            }
        }
        file.delete();
    }

    private static void testUnknownBlockTokensDoNotDefaultToDeepslate() {
        assertEquals(EnumBlockType.DEEPSLATE, EnumBlockType.fromToken("deepslate"), "known deepslate token");
        assertEquals(EnumBlockType.RED_SAND, EnumBlockType.fromToken("red-sand"), "hyphenated aliases normalize");
        assertEquals(EnumBlockType.SAND_TOP_BOTTOM, EnumBlockType.fromToken("sand_top"), "sand horizontal profile token");
        assertEquals(EnumBlockType.BEDROCK, EnumBlockType.fromToken("bedrock"), "bedrock token");
        assertEquals(null, EnumBlockType.tryFromToken("definitely_not_a_block"), "unknown token should not resolve");
        expectThrows(new Runnable() {
            @Override
            public void run() {
                EnumBlockType.fromToken("definitely_not_a_block");
            }
        }, "fromToken should reject unknown tokens");
    }

    private static void testRemovedStaticTokensAreClassified() {
        assertTrue(EnumBlockType.isRemovedStaticToken("tuff"), "tuff should be a removed static token");
        assertTrue(EnumBlockType.isRemovedStaticToken("black-stone") == false, "misspelled removed token should not match accidentally");
        assertTrue(EnumBlockType.isRemovedStaticToken("blackstone"), "blackstone should be a removed static token");
        assertEquals(null, EnumBlockType.tryFromToken("tuff"), "removed static token should not be a supported profile");
        assertTrue(EnumBlockType.unsupportedReasonForToken("netherrack") != null, "netherrack should have a specific unsupported reason");
    }


    private static void testEnumBlockTypeIdLookupIsStrict() {
        assertEquals(EnumBlockType.DEEPSLATE, EnumBlockType.fromId(EnumBlockType.DEEPSLATE.getId()), "known block id should resolve");
        expectThrows(new Runnable() {
            @Override
            public void run() {
                EnumBlockType.fromId(9999);
            }
        }, "fromId should reject unknown block ids");
    }

    private static void testPatternCodecRoundTripAndValidation() {
        final int size = 7;
        final int center = 3;
        try {
            PatternData empty = PatternCodec.emptyPattern(size, center);
            Matrix3 pattern = empty.getPatternMatrix();
            Matrix3 blockTypes = empty.getBlockTypeMatrix();
            Vector3 first = new Vector3(0, 0, 0);
            Vector3 second = new Vector3(1, 3, 2);
            pattern.setValue(first, 1);
            blockTypes.setValue(first, EnumBlockType.DEEPSLATE.getId());
            pattern.setValue(second, 3);
            blockTypes.setValue(second, EnumBlockType.SAND_TOP_BOTTOM.getId());

            File saved = File.createTempFile("pcc-pattern-roundtrip", ".txt");
            try {
                PatternCodec.save(saved, pattern, blockTypes, size);
                PatternData reloaded = PatternCodec.load(saved, size, center);
                assertEquals(Integer.valueOf(1), Integer.valueOf(reloaded.getPatternMatrix().getValue(first)), "round-tripped first pattern value");
                assertEquals(Integer.valueOf(EnumBlockType.DEEPSLATE.getId()), Integer.valueOf(reloaded.getBlockTypeMatrix().getValue(first)), "round-tripped first block type");
                assertEquals(Integer.valueOf(3), Integer.valueOf(reloaded.getPatternMatrix().getValue(second)), "round-tripped second pattern value");
                assertEquals(Integer.valueOf(EnumBlockType.SAND_TOP_BOTTOM.getId()), Integer.valueOf(reloaded.getBlockTypeMatrix().getValue(second)), "round-tripped second block type");
            } finally {
                saved.delete();
            }

            File malformed = File.createTempFile("pcc-pattern-malformed", ".txt");
            try {
                FileWriter writer = new FileWriter(malformed);
                try {
                    writer.write("? ? ? ? ? ? ?\n");
                } finally {
                    writer.close();
                }
                expectThrows(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PatternCodec.load(malformed, size, center);
                        } catch(IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, "strict pattern loader should reject incomplete files");
            } finally {
                malformed.delete();
            }

            File invalidSideState = File.createTempFile("pcc-pattern-invalid-side-state", ".txt");
            try {
                FileWriter writer = new FileWriter(invalidSideState);
                try {
                    for(int y = 0; y < size; y++) {
                        for(int z = 0; z < size; z++) {
                            for(int x = 0; x < size; x++) {
                                writer.write(y == 0 && z == 0 && x == 0 ? "sand:1" : "?");
                                if(x < size - 1) writer.write(" ");
                            }
                            writer.write("\n");
                        }
                        if(y < size - 1) writer.write("---------------------------\n");
                    }
                } finally {
                    writer.close();
                }
                expectThrows(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PatternCodec.load(invalidSideState, size, center);
                        } catch(IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, "one-state side profiles should reject states above 0");
            } finally {
                invalidSideState.delete();
            }
        } catch(IOException e) {
            throw new AssertionError("Pattern codec test setup failed", e);
        }
    }

    private static void testWallFacingOffsets() {
        Matrix3 pattern = new Matrix3(new Vector3(7, 7, 7), new Vector3(3, 3, 3));
        int patternX = 4; // horizontal +1 from center
        int patternY = 5; // depth +2 from visible wall plane
        int patternZ = 2; // vertical +1 from center because GUI rows invert into world Y

        assertOffset(pattern, patternX, patternY, patternZ, EnumRotation.R0, 1, 1, -2);
        assertOffset(pattern, patternX, patternY, patternZ, EnumRotation.R90, 2, 1, 1);
        assertOffset(pattern, patternX, patternY, patternZ, EnumRotation.R180, -1, 1, 2);
        assertOffset(pattern, patternX, patternY, patternZ, EnumRotation.R270, -2, 1, -1);
    }

    private static void testHorizontalSurfaceOffsets() {
        Matrix3 pattern = new Matrix3(new Vector3(7, 7, 7), new Vector3(3, 3, 3));
        int patternX = 4; // screen/right +1 from center
        int patternY = 5; // depth +2 from visible plane
        int patternZ = 2; // screen/forward +1 from center because GUI rows invert

        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.FLOOR_R0, 1, -2, -1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.FLOOR_R90, 1, -2, 1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.FLOOR_R180, -1, -2, 1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.FLOOR_R270, -1, -2, -1);

        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.CEILING_R0, 1, 2, -1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.CEILING_R90, 1, 2, 1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.CEILING_R180, -1, 2, 1);
        assertSurfaceOffset(pattern, patternX, patternY, patternZ, EnumRotation.CEILING_R270, -1, 2, -1);
    }

    private static void testSurfaceSelectionScanModes() {
        assertEquals(EnumRotation.R_ALL, EnumRotation.fromSurfaceSelection(true, false, false, 0, true), "walls all facings");
        assertEquals(EnumRotation.FLOOR_ALL, EnumRotation.fromSurfaceSelection(false, true, false, 0, true), "floors all facings");
        assertEquals(EnumRotation.CEILING_ALL, EnumRotation.fromSurfaceSelection(false, false, true, 0, true), "ceilings all facings");
        assertEquals(EnumRotation.ALL_SURFACES, EnumRotation.fromSurfaceSelection(true, true, true, 0, true), "all surfaces all facings");
        assertEquals(EnumRotation.WALL_FLOOR_R90, EnumRotation.fromSurfaceSelection(true, true, false, 90, false), "wall and floor east");
        assertEquals(EnumRotation.ALL_SURFACES_R270, EnumRotation.fromSurfaceSelection(true, true, true, 270, false), "all surfaces west");
        assertEquals(Integer.valueOf(12), Integer.valueOf(EnumRotation.ALL_SURFACES.getViewCount()), "all-surface view count");
        assertEquals(Integer.valueOf(2), Integer.valueOf(EnumRotation.FLOOR_CEILING_R180.getViewCount()), "floor/ceiling single-facing view count");
    }

    private static void testGpuFacingWireRoundTripIncludesHorizontalSurfaces() {
        EnumRotation[] canonical = new EnumRotation[] {
            EnumRotation.R0, EnumRotation.R90, EnumRotation.R180, EnumRotation.R270,
            EnumRotation.FLOOR_R0, EnumRotation.FLOOR_R90, EnumRotation.FLOOR_R180, EnumRotation.FLOOR_R270,
            EnumRotation.CEILING_R0, EnumRotation.CEILING_R90, EnumRotation.CEILING_R180, EnumRotation.CEILING_R270
        };

        for(EnumRotation rotation : canonical) {
            int wire = GpuAccelerationBackend.rotationWireValue(rotation);
            assertEquals(rotation, GpuAccelerationBackend.rotationFromWireValue(wire), "GPU facing wire round-trip for " + rotation);
        }

        assertEquals(Integer.valueOf(4), Integer.valueOf(GpuAccelerationBackend.rotationWireValue(EnumRotation.FLOOR_R0)), "GPU floor north wire code");
        assertEquals(Integer.valueOf(11), Integer.valueOf(GpuAccelerationBackend.rotationWireValue(EnumRotation.CEILING_R270)), "GPU ceiling west wire code");

        GpuAccelerationBackend.ScanRequest capped = new GpuAccelerationBackend.ScanRequest(
            null, new CompiledPattern[0], 0, 1, 0, 1, 0, 1, 37
        );
        assertEquals(Integer.valueOf(37), Integer.valueOf(capped.effectiveMaxMatches(1000)), "GPU request should cap helper max to remaining match budget");

        GpuAccelerationBackend.ScanRequest unlimited = new GpuAccelerationBackend.ScanRequest(
            null, new CompiledPattern[0], 0, 1, 0, 1, 0, 1, Integer.MAX_VALUE
        );
        assertEquals(Integer.valueOf(1000), Integer.valueOf(unlimited.effectiveMaxMatches(1000)), "GPU unlimited request should preserve backend helper cap");
    }

    private static void testFacingLabels() {
        assertEquals("wall north", BruteforceThread.facingLabel(EnumRotation.R0), "R0 label");
        assertEquals("wall east", BruteforceThread.facingLabel(EnumRotation.R90), "R90 label");
        assertEquals("wall south", BruteforceThread.facingLabel(EnumRotation.R180), "R180 label");
        assertEquals("wall west", BruteforceThread.facingLabel(EnumRotation.R270), "R270 label");
        assertEquals("floor north", BruteforceThread.facingLabel(EnumRotation.FLOOR_R0), "floor R0 label");
        assertEquals("ceiling west", BruteforceThread.facingLabel(EnumRotation.CEILING_R270), "ceiling R270 label");
    }


    private static void testRectangularMatrixCopyAndRotation() {
        int[][] values = new int[][] {
            {1, 2, 3},
            {4, 5, 6}
        };
        Matrix2 matrix = new Matrix2(values, new Vector2(0, 1));
        Matrix2 copy = new Matrix2(matrix);
        assertEquals(Integer.valueOf(6), Integer.valueOf(copy.getValue(new Vector2(1, 2))), "rectangular Matrix2 copy should preserve x/z dimensions");

        Matrix2 rotated = MatrixHelper.rotateMatrix(matrix);
        assertEquals(Integer.valueOf(3), Integer.valueOf(rotated.getSizeX()), "rotated Matrix2 x size");
        assertEquals(Integer.valueOf(2), Integer.valueOf(rotated.getSizeZ()), "rotated Matrix2 z size");
        assertEquals(Integer.valueOf(1), Integer.valueOf(rotated.getValue(new Vector2(0, 1))), "rotated Matrix2 value 1");
        assertEquals(Integer.valueOf(4), Integer.valueOf(rotated.getValue(new Vector2(0, 0))), "rotated Matrix2 value 4");
        assertEquals(Integer.valueOf(3), Integer.valueOf(rotated.getValue(new Vector2(2, 1))), "rotated Matrix2 value 3");
    }

    private static void testOriginOutwardScanRegionsCoverSearchSquareExactlyOnce() {
        int[] radii = new int[] {0, 1, 2, 5, 64, 65};
        int[] bandSizes = new int[] {1, 2, 64};
        for(int radius : radii) {
            for(int bandSize : bandSizes) {
                verifyRegionCoverage(radius, bandSize);
            }
        }
    }


    private static void testCompiledObservationAndPatternHelpers() {
        CompiledObservation impossibleObservation = new CompiledObservation(
            0, 0, 0, 1, 1, CompiledObservation.MAPPING_CONSTANT_ZERO, EnumMCVersion.V1_21_11
        );
        assertTrue(!impossibleObservation.matches(123, 64, -456), "constant-zero mapping should reject wanted state 1");
        assertTrue(!impossibleObservation.isTwoBitCacheCompatible(), "constant-zero wanted state 1 should be sieve-incompatible");

        CompiledObservation noOpObservation = new CompiledObservation(
            0, 0, 0, 0, 1, CompiledObservation.MAPPING_CONSTANT_ZERO, EnumMCVersion.V1_21_11
        );
        assertTrue(noOpObservation.matches(123, 64, -456), "constant-zero wanted state 0 should match every coordinate");
        assertTrue(noOpObservation.isTwoBitCacheCompatible(), "constant-zero wanted state 0 should be sieve-compatible");
        assertEquals(Integer.valueOf(1), Integer.valueOf(noOpObservation.selectivityScore()), "constant-zero observation selectivity");

        CompiledPattern impossiblePattern = new CompiledPattern(EnumRotation.R0, new CompiledObservation[] {noOpObservation}, true);
        assertTrue(!impossiblePattern.matches(0, 0, 0), "impossible compiled pattern should never match");
        assertTrue(impossiblePattern.isTwoBitCacheCompatible(), "impossible pattern should not block sieve compatibility");
    }

    private static void testCandidateMaskRangeConstraints() {
        CandidateMask small = new CandidateMask(6);
        small.setAll();
        small.andRange(0, 0b101001L, 6);
        assertEquals(Integer.valueOf(0), Integer.valueOf(small.nextSetBit(0)), "first constrained bit");
        assertEquals(Integer.valueOf(3), Integer.valueOf(small.nextSetBit(1)), "second constrained bit");
        assertEquals(Integer.valueOf(5), Integer.valueOf(small.nextSetBit(4)), "third constrained bit");
        assertEquals(Integer.valueOf(-1), Integer.valueOf(small.nextSetBit(6)), "no constrained bits after tail");

        CandidateMask boundary = new CandidateMask(70);
        boundary.setAll();
        boundary.andRange(0, 0L, 60);
        boundary.andRange(60, 0b1010101010L, 10);
        assertEquals(Integer.valueOf(61), Integer.valueOf(boundary.nextSetBit(0)), "cross-word first constrained bit");
        assertEquals(Integer.valueOf(63), Integer.valueOf(boundary.nextSetBit(62)), "cross-word second constrained bit");
        assertEquals(Integer.valueOf(69), Integer.valueOf(boundary.nextSetBit(68)), "cross-word tail constrained bit");
        assertEquals(Integer.valueOf(-1), Integer.valueOf(boundary.nextSetBit(70)), "cross-word range should honor total bit count");
    }

    private static void testStateMaskPlaneExtractsVisibleBitsAcrossWordBoundaries() {
        int minX = -3;
        int minZ = 7;
        int width = 13;
        StateMaskPlane plane = StateMaskPlane.build(EnumMCVersion.V1_21_11, minX, minX + width, 11, minZ, minZ + 6);
        CompiledObservation directZero = new CompiledObservation(
            0, 0, 0, 0, 4, CompiledObservation.MAPPING_DIRECT, EnumMCVersion.V1_21_11
        );

        int bitIndex = 5;
        int bitCount = 64;
        long extracted = plane.extractVisibleBits(directZero, bitIndex, bitCount);
        for(int i = 0; i < bitCount; i++) {
            int cell = bitIndex + i;
            int x = minX + (cell % width);
            int z = minZ + (cell / width);
            boolean expected = directZero.matches(x, 11, z);
            boolean actual = ((extracted >>> i) & 1L) != 0L;
            assertTrue(expected == actual, "state mask direct extraction mismatch at local bit " + i);
        }

        CompiledObservation constantZero = new CompiledObservation(
            0, 0, 0, 0, 1, CompiledObservation.MAPPING_CONSTANT_ZERO, EnumMCVersion.V1_21_11
        );
        assertEquals(Long.valueOf(-1L), Long.valueOf(plane.extractVisibleBits(constantZero, bitIndex, bitCount)), "constant-zero extraction should allow every bit");
    }

    private static void testMatchCollectorSortsByDistance() {
        MatchCollector collector = new MatchCollector();
        collector.add(3, 10, 0, EnumRotation.R90);
        collector.add(0, 8, 1, EnumRotation.R0);
        collector.add(2, 7, 0, EnumRotation.CEILING_R270);

        String[] lines = collector.toSortedMatchString().trim().split("\\R");
        assertEquals(Integer.valueOf(3), Integer.valueOf(lines.length), "collector serialized line count");
        assertEquals("0 8 1 facing wall north", lines[0], "nearest match should sort first");
        assertEquals("2 7 0 facing ceiling west", lines[1], "second nearest match should sort second");
        assertEquals("3 10 0 facing wall east", lines[2], "farthest match should sort last");
    }

    private static void verifyRegionCoverage(int radius, int bandSize) {
        List<CoordinateBruteforcer.ScanRegion> regions = CoordinateBruteforcer.buildOriginOutwardScanRegions(radius, bandSize);
        boolean[][] seen = new boolean[(radius * 2) + 1][(radius * 2) + 1];
        long visited = 0L;

        for(CoordinateBruteforcer.ScanRegion region : regions) {
            for(CoordinateBruteforcer.ScanRectangle rectangle : region.rectangles) {
                assertTrue(rectangle.minX >= -radius, "rectangle minX below radius");
                assertTrue(rectangle.maxXExclusive <= radius + 1, "rectangle maxX above radius");
                assertTrue(rectangle.minZ >= -radius, "rectangle minZ below radius");
                assertTrue(rectangle.maxZExclusive <= radius + 1, "rectangle maxZ above radius");
                assertTrue(rectangle.minX < rectangle.maxXExclusive, "rectangle width must be positive");
                assertTrue(rectangle.minZ < rectangle.maxZExclusive, "rectangle depth must be positive");

                for(int x = rectangle.minX; x < rectangle.maxXExclusive; x++) {
                    for(int z = rectangle.minZ; z < rectangle.maxZExclusive; z++) {
                        int gx = x + radius;
                        int gz = z + radius;
                        assertTrue(!seen[gx][gz], "scan regions overlap at " + x + "," + z + " for radius " + radius + " band " + bandSize);
                        seen[gx][gz] = true;
                        visited++;
                    }
                }
            }
        }

        long expected = (long) ((radius * 2) + 1) * (long) ((radius * 2) + 1);
        assertEquals(Long.valueOf(expected), Long.valueOf(visited), "covered cell count for radius " + radius + " band " + bandSize);
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                assertTrue(seen[x + radius][z + radius], "missing cell " + x + "," + z + " for radius " + radius + " band " + bandSize);
            }
        }
    }

    private static void assertOffset(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation facing, int expectedX, int expectedY, int expectedZ) {
        int actualX = BruteforceThread.getWallObservationOffsetX(pattern, patternX, patternY, patternZ, facing);
        int actualY = BruteforceThread.getWallObservationOffsetY(pattern, patternX, patternY, patternZ);
        int actualZ = BruteforceThread.getWallObservationOffsetZ(pattern, patternX, patternY, patternZ, facing);
        assertEquals(Integer.valueOf(expectedX), Integer.valueOf(actualX), facing + " dx");
        assertEquals(Integer.valueOf(expectedY), Integer.valueOf(actualY), facing + " dy");
        assertEquals(Integer.valueOf(expectedZ), Integer.valueOf(actualZ), facing + " dz");
    }

    private static void assertSurfaceOffset(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation facing, int expectedX, int expectedY, int expectedZ) {
        int actualX = BruteforceThread.getObservationOffsetX(pattern, patternX, patternY, patternZ, facing);
        int actualY = BruteforceThread.getObservationOffsetY(pattern, patternX, patternY, patternZ, facing);
        int actualZ = BruteforceThread.getObservationOffsetZ(pattern, patternX, patternY, patternZ, facing);
        assertEquals(Integer.valueOf(expectedX), Integer.valueOf(actualX), facing + " dx");
        assertEquals(Integer.valueOf(expectedY), Integer.valueOf(actualY), facing + " dy");
        assertEquals(Integer.valueOf(expectedZ), Integer.valueOf(actualZ), facing + " dz");
    }

    private static void expectThrows(Runnable runnable, String message) {
        try {
            runnable.run();
        } catch(IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError(message);
    }

    private static void assertTrue(boolean condition, String message) {
        if(!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if(expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
