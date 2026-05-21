package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.Main;
import io.github.promptt001.coordinatecracker.data.EnumAccelerationMode;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.data.PatternRelative;
import io.github.promptt001.coordinatecracker.utils.SystemHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CoordinateBruteforcer {
    private static final int DEFAULT_SCAN_BAND_SIZE = 64;
    private static final int DEFAULT_MAX_MATCHES = 1_000_000;
    private static final int DEFAULT_UNLIMITED_SCAN_BAND_SIZE = 16;
    private static final int DEFAULT_MAX_BUFFERED_MATCHES_PER_REGION = 250_000;
    private static final int DEFAULT_MAX_PENDING_REGION_MATCH_CHARS = 64 * 1024 * 1024;
    private static final long UI_UPDATE_INTERVAL_NANOS = 250_000_000L;


    private List<BruteforceThread> bruteforceThreads;
    private List<ScanRegion> scanRegions;
    private final PatternRelative scope;
    private final int radius;
    private final int y_start;
    private final int y_end;
    private final EnumMCVersion version;
    private final EnumRotation rotation;
    private final Main mainInstance;
    private final int threads;
    private final EnumAccelerationMode accelerationMode;
    private final AtomicInteger matches;
    private final AtomicLong completedIterations;
    private final AtomicInteger lastProgressTenths;
    private final AtomicLong lastMetricsUpdateNanos;
    private final AtomicLong lastMatchUiUpdateNanos;
    private final Map<Integer, String> pendingRegionMatches;
    private final HashSet<Integer> completedRegionIndexes;
    private long totalIterations;
    private long scanStartNanos;
    private volatile boolean cancelled;
    private BufferedWriter resultWriter;
    private int nextScanRegionIndex;
    private int nextWriteRegionIndex;
    private GpuAccelerationBackend gpuBackend;
    private ExactBitVectorSolverBackend exactSolverBackend;
    private final AtomicBoolean gpuFallbackLogged;
    private final AtomicBoolean exactSolverFallbackLogged;
    private final AtomicBoolean scanErrorShown;
    private final AtomicInteger completedThreadCount;
    private final AtomicBoolean completionFinalized;
    private final AtomicBoolean matchLimitReported;
    private final int maxMatches;
    private final int maxBufferedMatchesPerRegion;
    private final int maxPendingRegionMatchChars;
    private int pendingRegionMatchChars;

    public CoordinateBruteforcer(PatternRelative scope, int radius, int y_start, int y_end,
                                 EnumMCVersion version, EnumRotation rotation, Main mainInstance, int threads, EnumAccelerationMode accelerationMode) {
        this(scope, radius, y_start, y_end, version, rotation, mainInstance, threads, accelerationMode, resolveMaxMatches());
    }

    public CoordinateBruteforcer(PatternRelative scope, int radius, int y_start, int y_end,
                                 EnumMCVersion version, EnumRotation rotation, Main mainInstance, int threads,
                                 EnumAccelerationMode accelerationMode, int maxMatches) {
        this.scope = scope;
        this.radius = radius;
        this.y_start = y_start;
        this.y_end = y_end;
        this.version = version;
        this.rotation = rotation;
        this.mainInstance = mainInstance;
        this.threads = SystemHelper.adjustThreadCount(threads);
        this.accelerationMode = accelerationMode == null ? EnumAccelerationMode.CPU : accelerationMode;
        this.matches = new AtomicInteger(0);
        this.completedIterations = new AtomicLong(0);
        this.lastProgressTenths = new AtomicInteger(-1);
        this.lastMetricsUpdateNanos = new AtomicLong(0L);
        this.lastMatchUiUpdateNanos = new AtomicLong(0L);
        this.pendingRegionMatches = new TreeMap<Integer, String>();
        this.completedRegionIndexes = new HashSet<Integer>();
        this.totalIterations = 0L;
        this.scanStartNanos = 0L;
        this.cancelled = false;
        this.nextScanRegionIndex = 0;
        this.nextWriteRegionIndex = 0;
        this.gpuBackend = null;
        this.exactSolverBackend = null;
        this.gpuFallbackLogged = new AtomicBoolean(false);
        this.exactSolverFallbackLogged = new AtomicBoolean(false);
        this.scanErrorShown = new AtomicBoolean(false);
        this.completedThreadCount = new AtomicInteger(0);
        this.completionFinalized = new AtomicBoolean(false);
        this.matchLimitReported = new AtomicBoolean(false);
        this.maxMatches = maxMatches;
        this.maxBufferedMatchesPerRegion = resolvePositiveIntProperty("coordinatecracker.maxBufferedMatchesPerRegion", DEFAULT_MAX_BUFFERED_MATCHES_PER_REGION);
        this.maxPendingRegionMatchChars = resolvePositiveIntProperty("coordinatecracker.maxPendingRegionMatchChars", DEFAULT_MAX_PENDING_REGION_MATCH_CHARS);
        this.pendingRegionMatchChars = 0;
        if(this.maxMatches == 0) {
            System.out.println("Max matches is unlimited; using region-buffer guardrail " + this.maxBufferedMatchesPerRegion + " matches per region and " + this.maxPendingRegionMatchChars + " pending characters.");
        }
    }

    public CoordinateBruteforcer(PatternRelative scope, int radius, int y_start, int y_end,
                                 EnumMCVersion version, EnumRotation rotation, Main mainInstance, int threads) {
        this(scope, radius, y_start, y_end, version, rotation, mainInstance, threads, EnumAccelerationMode.CPU);
    }

    public CoordinateBruteforcer(PatternRelative scope, int radius,
                                 EnumMCVersion version, EnumRotation rotation, Main mainInstance, int threads) {
        this(scope, radius, 0, 256, version, rotation, mainInstance, threads, EnumAccelerationMode.CPU);
    }

    public void load() {
        this.bruteforceThreads = new ArrayList<BruteforceThread>();

        if(this.cancelled) {
            this.mainInstance.crackingEnd();
            return;
        }

        if(this.accelerationMode.wantsGpu()) {
            this.gpuBackend = GpuAccelerationBackend.create();
            if(this.cancelled) {
                this.closeGpuBackend();
                this.mainInstance.crackingEnd();
                return;
            }
            if(this.gpuBackend.isAvailable()) {
                System.out.println("GPU acceleration enabled using helper: " + this.gpuBackend.getCommandDisplay());
                System.out.println(this.gpuBackend.getStatusMessage());
            }
            else if(this.accelerationMode.requiresGpu()) {
                this.mainInstance.showErrorAsync("GPU acceleration unavailable", this.gpuBackend.getStatusMessage());
                this.closeGpuBackend();
                this.mainInstance.crackingEnd();
                return;
            }
            else {
                System.out.println("GPU acceleration unavailable; falling back to CPU. " + this.gpuBackend.getStatusMessage());
                this.gpuBackend = null;
            }
        }

        if(this.cancelled) {
            this.closeGpuBackend();
            this.mainInstance.crackingEnd();
            return;
        }

        this.exactSolverBackend = ExactBitVectorSolverBackend.createFromSystemProperties();
        if(this.exactSolverBackend != null) {
            System.out.println(this.exactSolverBackend.getStatusMessage());
        }

        try {
            this.openResultWriter();
        } catch (IOException e) {
            e.printStackTrace();
            this.mainInstance.showErrorAsync("Unable to open results file", e.getMessage());
            this.closeGpuBackend();
            this.mainInstance.crackingEnd();
            return;
        }

        if(this.cancelled) {
            this.closeResultWriter();
            this.closeGpuBackend();
            this.mainInstance.crackingEnd();
            return;
        }

        this.loadThreads(this.threads);
        this.scanStartNanos = System.nanoTime();
        this.lastMetricsUpdateNanos.set(0L);
        this.lastMatchUiUpdateNanos.set(0L);
        this.updateScanMetrics(0L, true);
        this.crack();
    }

    public void requestCancel() {
        this.cancelled = true;
        this.closeGpuBackend();
        if(this.bruteforceThreads != null) {
            for (BruteforceThread thread : this.bruteforceThreads) {
                if(thread != null) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * @deprecated use {@link #requestCancel()} instead.
     */
    @Deprecated
    public void veryVeryBadTermination() {
        this.requestCancel();
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    int remainingMatchBudgetForBackend() {
        if(this.maxMatches <= 0) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, this.maxMatches - this.matches.get());
    }

    boolean hasUnlimitedMatchBudget() {
        return this.maxMatches <= 0;
    }

    int maxBufferedMatchesPerRegion() {
        return this.maxBufferedMatchesPerRegion;
    }

    public void setDone(BruteforceThread completedThread) {
        int threadCount = this.bruteforceThreads == null ? 0 : this.bruteforceThreads.size();
        int completed = this.completedThreadCount.incrementAndGet();
        if(completed < threadCount) {
            return;
        }
        if(!this.completionFinalized.compareAndSet(false, true)) {
            return;
        }

        this.closeGpuBackend();
        this.closeResultWriter();
        this.mainInstance.updateMatchesCount(this.matches.get());
        if(!this.cancelled) {
            this.updateScanMetrics(this.totalIterations, true);
        }
        this.mainInstance.crackingEnd();
    }

    /**
     * Attempts to reserve one global match slot before buffering a match.
     *
     * The UI max-match setting protects the GUI, result table, and ordered region
     * buffers from unbounded memory growth on underconstrained scans. A value of
     * zero disables the limit.
     */
    public boolean tryRecordMatch(int x, int y, int z, EnumRotation viewDirection) {
        while(true) {
            int current = this.matches.get();
            if(this.maxMatches > 0 && current >= this.maxMatches) {
                this.reportMatchLimitReached();
                return false;
            }

            int next = current + 1;
            if(this.matches.compareAndSet(current, next)) {
                this.maybeUpdateMatchCount(next);
                this.mainInstance.queueResultMatch(x, y, z, BruteforceThread.facingLabel(viewDirection));
                if(this.maxMatches > 0 && next >= this.maxMatches) {
                    this.reportMatchLimitReached();
                }
                return true;
            }
        }
    }

    public boolean tryRecordMatch() {
        return this.tryRecordMatch(0, 0, 0, EnumRotation.R0);
    }

    private void maybeUpdateMatchCount(int matchesSoFar) {
        if(matchesSoFar < 10 || matchesSoFar % 1000 == 0) {
            this.mainInstance.updateMatchesCount(matchesSoFar);
            return;
        }

        long now = System.nanoTime();
        long previous = this.lastMatchUiUpdateNanos.get();
        if(now - previous >= UI_UPDATE_INTERVAL_NANOS && this.lastMatchUiUpdateNanos.compareAndSet(previous, now)) {
            this.mainInstance.updateMatchesCount(matchesSoFar);
        }
    }

    /**
     * @deprecated use {@link #tryRecordMatch()} so the match limit is enforced.
     */
    @Deprecated
    public void updateMatchesCount() {
        this.tryRecordMatch();
    }


    public boolean isGpuRequired() {
        return this.accelerationMode.requiresGpu();
    }

    public void noteGpuFallback(String reason) {
        if(this.accelerationMode.wantsGpu() && this.gpuFallbackLogged.compareAndSet(false, true)) {
            System.out.println("GPU acceleration fell back to CPU: " + reason);
        }
    }

    public void noteExactSolverFallback(String reason) {
        if(this.exactSolverBackend != null && this.exactSolverFallbackLogged.compareAndSet(false, true)) {
            System.out.println("Exact SMT solver fell back to scanner: " + reason);
        }
    }

    public void abortScan(String reason) {
        this.requestCancel();
        if(this.scanErrorShown.compareAndSet(false, true)) {
            System.err.println("Scan stopped: " + reason);
            this.mainInstance.showErrorAsync("Scan stopped", reason);
        }
    }

    private void reportMatchLimitReached() {
        if(this.matchLimitReported.compareAndSet(false, true)) {
            this.abortScan("Match limit reached (" + this.maxMatches + "). Increase the Max matches setting, add more observations, or reduce the search bounds.");
        }
    }

    private static int resolveMaxMatches() {
        String configured = System.getProperty("coordinatecracker.maxMatches");
        if(configured == null || configured.trim().isEmpty()) {
            return DEFAULT_MAX_MATCHES;
        }

        try {
            return Integer.parseInt(configured.trim());
        } catch(NumberFormatException e) {
            System.err.println("Invalid coordinatecracker.maxMatches value '" + configured + "'; using " + DEFAULT_MAX_MATCHES + ".");
            return DEFAULT_MAX_MATCHES;
        }
    }

    private static int resolvePositiveIntProperty(String property, int defaultValue) {
        String configured = System.getProperty(property);
        if(configured == null || configured.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(configured.trim());
            return parsed > 0 ? parsed : defaultValue;
        } catch(NumberFormatException e) {
            System.err.println("Invalid " + property + " value '" + configured + "'; using " + defaultValue + ".");
            return defaultValue;
        }
    }


    public void addCompletedIterations(long iterations) {
        if(iterations <= 0 || this.totalIterations <= 0) {
            return;
        }

        long completed = this.completedIterations.addAndGet(iterations);
        this.updateScanMetrics(completed, false);
    }

    private void updateScanMetrics(long completed, boolean force) {
        if(this.totalIterations <= 0L) {
            this.mainInstance.updateScanMetrics(0, 0L, 0L, 0.0d, -1L);
            return;
        }

        long clampedCompleted = Math.max(0L, Math.min(completed, this.totalIterations));
        int progressTenths = (int) Math.min(1000L, (clampedCompleted * 1000L) / this.totalIterations);
        long now = System.nanoTime();
        int previousProgress = this.lastProgressTenths.get();
        long previousMetricsUpdate = this.lastMetricsUpdateNanos.get();
        boolean progressChanged = progressTenths != previousProgress
            && this.lastProgressTenths.compareAndSet(previousProgress, progressTenths);
        boolean timeElapsed = now - previousMetricsUpdate >= UI_UPDATE_INTERVAL_NANOS
            && this.lastMetricsUpdateNanos.compareAndSet(previousMetricsUpdate, now);

        if(force || progressChanged || timeElapsed) {
            double elapsedSeconds = this.scanStartNanos <= 0L ? 0.0d : (now - this.scanStartNanos) / 1_000_000_000.0d;
            double candidatesPerSecond = elapsedSeconds <= 0.0d ? 0.0d : clampedCompleted / elapsedSeconds;
            long etaSeconds = candidatesPerSecond <= 0.0d
                ? -1L
                : (long) Math.ceil((this.totalIterations - clampedCompleted) / candidatesPerSecond);
            this.mainInstance.updateScanMetrics(progressTenths, clampedCompleted, this.totalIterations, candidatesPerSecond, etaSeconds);
        }
    }

    private void updateProgress(int progressTenths) {
        this.mainInstance.updateProgress(progressTenths);
    }

    public synchronized ScanRegion nextScanRegion() {
        if(this.cancelled || this.scanRegions == null || this.nextScanRegionIndex >= this.scanRegions.size()) {
            return null;
        }
        return this.scanRegions.get(this.nextScanRegionIndex++);
    }

    public synchronized void completeScanRegion(ScanRegion region, String bufferedMatches) {
        this.completedRegionIndexes.add(region.index);
        if(bufferedMatches != null && !bufferedMatches.isEmpty()) {
            this.pendingRegionMatches.put(region.index, bufferedMatches);
            this.pendingRegionMatchChars += bufferedMatches.length();
            if(this.pendingRegionMatchChars > this.maxPendingRegionMatchChars) {
                this.abortScan("Buffered region results exceeded " + this.maxPendingRegionMatchChars + " characters. Increase coordinatecracker.maxPendingRegionMatchChars, lower Max matches, or add more observations.");
            }
        }

        while(this.completedRegionIndexes.remove(this.nextWriteRegionIndex)) {
            String matchesForRegion = this.pendingRegionMatches.remove(this.nextWriteRegionIndex);
            if(matchesForRegion != null && !matchesForRegion.isEmpty()) {
                this.pendingRegionMatchChars -= matchesForRegion.length();
                if(this.pendingRegionMatchChars < 0) this.pendingRegionMatchChars = 0;
                try {
                    this.writeMatchesUnlocked(matchesForRegion);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.abortScan("Unable to write results file: " + e.getMessage());
                    break;
                }
            }
            this.nextWriteRegionIndex++;
        }
    }

    public synchronized void writeMatches(String bufferedMatches) {
        if (bufferedMatches == null || bufferedMatches.isEmpty()) {
            return;
        }

        try {
            this.writeMatchesUnlocked(bufferedMatches);
        } catch (IOException e) {
            e.printStackTrace();
            this.abortScan("Unable to write results file: " + e.getMessage());
        }
    }

    private void writeMatchesUnlocked(String bufferedMatches) throws IOException {
        if (this.resultWriter != null) {
            this.resultWriter.write(bufferedMatches);
        }
    }

    private void openResultWriter() throws IOException {
        String path = this.mainInstance.getSaveFile();
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create results directory: " + parent.getAbsolutePath());
        }
        if(parent != null && !parent.isDirectory()) {
            throw new IOException("Results parent path is not a directory: " + parent.getAbsolutePath());
        }
        this.resultWriter = new BufferedWriter(new FileWriter(file, true), 1024 * 1024);
    }


    private void closeGpuBackend() {
        if(this.gpuBackend != null) {
            this.gpuBackend.close();
        }
    }

    private synchronized void closeResultWriter() {
        if (this.resultWriter == null) {
            return;
        }

        try {
            this.resultWriter.flush();
            this.resultWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.resultWriter = null;
        }
    }

    /**
     * Build scan work as origin-outward square bands over the complete X/Z search area.
     *
     * The old worker partition split the X axis and each worker began at its negative edge, so
     * result files naturally started near (-radius, -radius). These bands keep the complete
     * inclusive search bounds [-radius, +radius] but dispatch work in increasing distance from
     * (0, 0). Region completion is also written in this same order, so faster outer workers do not
     * race ahead of slower inner workers in the result file.
     *
     * The band width can be tuned with -Dcoordinatecracker.scanBandSize=<blocks>. Use 1 for exact
     * Chebyshev-ring ordering, or a larger value for less scheduling overhead.
     */
    private void loadThreads(int threadCount) {
        this.scanRegions = this.buildOriginOutwardScanRegions();
        this.nextScanRegionIndex = 0;
        this.nextWriteRegionIndex = 0;
        this.pendingRegionMatches.clear();
        this.pendingRegionMatchChars = 0;
        this.completedRegionIndexes.clear();
        this.completedThreadCount.set(0);
        this.completionFinalized.set(false);

        long total = 0L;
        long yRange = Math.max(0L, (long) this.y_end - (long) this.y_start);
        for(ScanRegion region : this.scanRegions) {
            total += region.cellCount() * yRange;
        }
        this.totalIterations = Math.max(0L, total);

        int effectiveThreadCount = Math.max(1, Math.min(Math.max(1, threadCount), Math.max(1, this.scanRegions.size())));
        for (int i = 0; i < effectiveThreadCount; i++) {
            this.bruteforceThreads.add(new BruteforceThread(
                    this.scope,
                    this.y_start,
                    this.y_end,
                    this.version,
                    this.rotation,
                    this.mainInstance,
                    this,
                    this.gpuBackend,
                    this.exactSolverBackend,
                    i + 1
            ));
        }
    }

    private List<ScanRegion> buildOriginOutwardScanRegions() {
        int defaultBandSize = this.gpuBackend != null
            ? Integer.getInteger("coordinatecracker.gpuScanBandSize", 2048)
            : (this.maxMatches <= 0 ? DEFAULT_UNLIMITED_SCAN_BAND_SIZE : DEFAULT_SCAN_BAND_SIZE);
        int bandSize = Integer.getInteger("coordinatecracker.scanBandSize", defaultBandSize);
        if(bandSize < 1) {
            bandSize = defaultBandSize;
        }
        return buildOriginOutwardScanRegions(this.radius, bandSize);
    }

    static List<ScanRegion> buildOriginOutwardScanRegions(int radius, int bandSize) {
        List<ScanRegion> regions = new ArrayList<ScanRegion>();
        if(radius < 0) {
            throw new IllegalArgumentException("Radius must be zero or greater.");
        }
        if(bandSize < 1) {
            throw new IllegalArgumentException("Band size must be at least 1.");
        }

        int minBound = -radius;
        int maxBoundExclusive = radius + 1;
        int index = 0;

        for(int inner = 0; inner <= radius; inner += bandSize) {
            int outerExclusive = Math.min(radius + 1, inner + bandSize);
            int outer = outerExclusive - 1;
            List<ScanRectangle> rectangles = new ArrayList<ScanRectangle>(4);

            if(inner == 0) {
                addClippedRectangle(rectangles, -outer, outer + 1, -outer, outer + 1, minBound, maxBoundExclusive);
            } else {
                addClippedRectangle(rectangles, -outer, outer + 1, -outer, -inner + 1, minBound, maxBoundExclusive);
                addClippedRectangle(rectangles, -outer, outer + 1, inner, outer + 1, minBound, maxBoundExclusive);
                addClippedRectangle(rectangles, -outer, -inner + 1, -inner + 1, inner, minBound, maxBoundExclusive);
                addClippedRectangle(rectangles, inner, outer + 1, -inner + 1, inner, minBound, maxBoundExclusive);
            }

            if(!rectangles.isEmpty()) {
                regions.add(new ScanRegion(index++, inner, outerExclusive, rectangles));
            }
        }
        return regions;
    }

    private static void addClippedRectangle(List<ScanRectangle> rectangles,
                                            int minX, int maxXExclusive,
                                            int minZ, int maxZExclusive,
                                            int minBound, int maxBoundExclusive) {
        int clippedMinX = Math.max(minX, minBound);
        int clippedMaxXExclusive = Math.min(maxXExclusive, maxBoundExclusive);
        int clippedMinZ = Math.max(minZ, minBound);
        int clippedMaxZExclusive = Math.min(maxZExclusive, maxBoundExclusive);

        if(clippedMinX < clippedMaxXExclusive && clippedMinZ < clippedMaxZExclusive) {
            rectangles.add(new ScanRectangle(clippedMinX, clippedMaxXExclusive, clippedMinZ, clippedMaxZExclusive));
        }
    }

    private void crack() {
        for (BruteforceThread thread : this.bruteforceThreads) {
            thread.start();
        }
    }

    static final class ScanRegion {
        final int index;
        final int innerRadius;
        final int outerRadiusExclusive;
        final ScanRectangle[] rectangles;

        ScanRegion(int index, int innerRadius, int outerRadiusExclusive, List<ScanRectangle> rectangles) {
            this.index = index;
            this.innerRadius = innerRadius;
            this.outerRadiusExclusive = outerRadiusExclusive;
            this.rectangles = rectangles.toArray(new ScanRectangle[rectangles.size()]);
        }

        long cellCount() {
            long cells = 0L;
            for(ScanRectangle rectangle : this.rectangles) {
                cells += rectangle.cellCount();
            }
            return cells;
        }
    }

    static final class ScanRectangle {
        final int minX;
        final int maxXExclusive;
        final int minZ;
        final int maxZExclusive;

        ScanRectangle(int minX, int maxXExclusive, int minZ, int maxZExclusive) {
            this.minX = minX;
            this.maxXExclusive = maxXExclusive;
            this.minZ = minZ;
            this.maxZExclusive = maxZExclusive;
        }

        int width() {
            return this.maxXExclusive - this.minX;
        }

        int depth() {
            return this.maxZExclusive - this.minZ;
        }

        long cellCount() {
            return (long) this.width() * (long) this.depth();
        }
    }
}
