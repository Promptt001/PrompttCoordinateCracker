package io.github.promptt001.coordinatecracker.cracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;

/**
 * Optional GPU bridge.
 *
 * The main application deliberately avoids a hard OpenCL/CUDA Java dependency so the normal jar
 * still builds and runs with only a JDK. When GPU mode is selected, this backend invokes an
 * external helper executable using a small stdin/stdout protocol. The repository includes a
 * reference OpenCL helper under gpu/opencl_coordinatecracker.c.
 */
final class GpuAccelerationBackend implements AutoCloseable {
    private static final String PROTOCOL = "PCCGPU4";
    private static final int HELPER_TIMEOUT_SECONDS = Integer.getInteger("coordinatecracker.gpuTimeoutSeconds", 600);
    private static final int PROBE_TIMEOUT_SECONDS = Integer.getInteger("coordinatecracker.gpuProbeTimeoutSeconds", 10);
    private static final int OUTPUT_DRAIN_TIMEOUT_SECONDS = Integer.getInteger("coordinatecracker.gpuOutputDrainTimeoutSeconds", 5);
    private static final int DEFAULT_MAX_MATCHES = 1 << 20;
    private static final int MAX_HELPER_PATTERNS = Integer.getInteger("coordinatecracker.gpuMaxPatterns", 12);
    private static final int MAX_HELPER_OBSERVATIONS = Integer.getInteger("coordinatecracker.gpuMaxObservations", 4096);

    private final List<String> command;
    private final int maxMatches;
    private final boolean available;
    private final String statusMessage;
    private PersistentHelper sharedHelper;

    private GpuAccelerationBackend(List<String> command, int maxMatches, boolean available, String statusMessage) {
        this.command = command;
        this.maxMatches = maxMatches;
        this.available = available;
        this.statusMessage = statusMessage;
        this.sharedHelper = null;
    }

    static GpuAccelerationBackend create() {
        List<String> command = parseCommand(System.getProperty("coordinatecracker.gpuCommand", defaultHelperCommand()));
        int maxMatches = Integer.getInteger("coordinatecracker.gpuMaxMatches", DEFAULT_MAX_MATCHES);
        if(maxMatches < 1) {
            maxMatches = DEFAULT_MAX_MATCHES;
        }

        String probe = probe(command);
        return new GpuAccelerationBackend(command, maxMatches, probe.startsWith("OK"), probe);
    }

    boolean isAvailable() {
        return this.available;
    }

    String getStatusMessage() {
        return this.statusMessage;
    }

    String getCommandDisplay() {
        StringBuilder builder = new StringBuilder();
        for(String part : this.command) {
            if(builder.length() > 0) builder.append(' ');
            builder.append(part.indexOf(' ') >= 0 ? '"' + part + '"' : part);
        }
        return builder.toString();
    }

    boolean supports(CompiledPattern[] compiledPatterns, EnumMCVersion version) {
        return this.supportFailureReason(compiledPatterns, version) == null;
    }

    private String supportFailureReason(CompiledPattern[] compiledPatterns, EnumMCVersion version) {
        if(!this.available) {
            return "GPU helper is not available: " + this.statusMessage;
        }
        if(version != EnumMCVersion.V1_21_11) {
            return "GPU backend currently supports only Minecraft 1.21.11 observations.";
        }
        if(compiledPatterns == null || compiledPatterns.length == 0) {
            return "No compiled patterns were supplied to the GPU backend.";
        }

        int activePatterns = 0;
        int totalObservations = 0;
        for(CompiledPattern pattern : compiledPatterns) {
            if(pattern.impossible) {
                continue;
            }
            activePatterns++;
            if(pattern.observations.length == 0) {
                return "GPU backend requires at least one observation per active pattern.";
            }
            totalObservations += pattern.observations.length;
            if(activePatterns > MAX_HELPER_PATTERNS) {
                return "GPU helper supports at most " + MAX_HELPER_PATTERNS + " active patterns per request, but this scan has " + activePatterns + ".";
            }
            if(totalObservations > MAX_HELPER_OBSERVATIONS) {
                return "GPU helper supports at most " + MAX_HELPER_OBSERVATIONS + " observations per request, but this scan has " + totalObservations + ".";
            }
            for(CompiledObservation observation : pattern.observations) {
                if(observation.version != EnumMCVersion.V1_21_11) {
                    return "GPU backend currently supports only Minecraft 1.21.11 observations.";
                }
                if(observation.visibleMapping == CompiledObservation.MAPPING_CONSTANT_ZERO) {
                    if(observation.wanted != 0) {
                        return "GPU backend received a constant-state observation outside the visible 0 range.";
                    }
                    continue;
                }
                if(observation.variantCount != 4) {
                    return "GPU backend currently supports only four-state random block variants.";
                }
                if(observation.wanted < 0 || observation.wanted > 3) {
                    return "GPU backend received an observation with an unsupported wanted state: " + observation.wanted + ".";
                }
                if(observation.visibleMapping == CompiledObservation.MAPPING_MODULO_TWO && observation.wanted > 1) {
                    return "GPU backend received a two-state block observation outside the visible 0..1 range.";
                }
            }
        }
        return activePatterns > 0 ? null : "No active GPU-compatible patterns were supplied.";
    }

    synchronized GpuScanResult scan(ScanRequest request) {
        String unsupportedReason = this.supportFailureReason(request.compiledPatterns, request.version);
        if(unsupportedReason != null) {
            return GpuScanResult.unsupported(unsupportedReason);
        }

        try {
            PersistentHelper helper = this.getOrStartHelper();
            GpuOutput output = helper.scan(request);
            if(output.errorMessage != null) {
                helper.close();
                if(this.sharedHelper == helper) this.sharedHelper = null;
                return GpuScanResult.failed(output.errorMessage);
            }
            if(!helper.isAlive()) {
                int exitCode = helper.exitValue();
                helper.close();
                if(this.sharedHelper == helper) this.sharedHelper = null;
                if(exitCode != 0) {
                    return GpuScanResult.failed("GPU helper exited with code " + exitCode + (output.firstLine == null ? "." : ": " + output.firstLine));
                }
            }
            if(output.overflow > 0) {
                return GpuScanResult.overflow("GPU helper found more than " + request.effectiveMaxMatches(this.maxMatches) + " matches in one rectangle; falling back to CPU to preserve complete output.");
            }
            if(output.declaredCount >= 0 && output.declaredCount != output.matches.size()) {
                return GpuScanResult.failed("GPU helper reported " + output.declaredCount + " matches but returned " + output.matches.size() + ".");
            }
            return GpuScanResult.success(output.matches);
        } catch(Exception e) {
            this.closeCurrentHelper();
            return GpuScanResult.failed("GPU helper failed: " + e.getMessage());
        }
    }

    private PersistentHelper getOrStartHelper() throws IOException {
        if(this.sharedHelper == null || !this.sharedHelper.isAlive()) {
            this.sharedHelper = new PersistentHelper(this.command);
        }
        return this.sharedHelper;
    }

    private void closeCurrentHelper() {
        if(this.sharedHelper != null) {
            this.sharedHelper.close();
            this.sharedHelper = null;
        }
    }

    @Override
    public synchronized void close() {
        if(this.sharedHelper != null) {
            this.sharedHelper.close();
            this.sharedHelper = null;
        }
    }

    private final class PersistentHelper implements AutoCloseable {
        private final Process process;
        private final BufferedReader reader;
        private final BufferedWriter writer;
        private final ExecutorService outputExecutor;

        PersistentHelper(List<String> command) throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            this.process = processBuilder.start();
            this.reader = new BufferedReader(new InputStreamReader(this.process.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream(), "UTF-8"));
            this.outputExecutor = Executors.newSingleThreadExecutor();
        }

        boolean isAlive() {
            return this.process.isAlive();
        }

        int exitValue() {
            return this.process.exitValue();
        }

        GpuOutput scan(final ScanRequest request) throws Exception {
            writeRequest(this.writer, request);
            this.writer.flush();

            Future<GpuOutput> outputFuture = this.outputExecutor.submit(new Callable<GpuOutput>() {
                @Override
                public GpuOutput call() throws Exception {
                    return readScanOutput(reader);
                }
            });

            try {
                return outputFuture.get(HELPER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch(TimeoutException e) {
                outputFuture.cancel(true);
                this.close();
                throw new IOException("GPU helper timed out after " + HELPER_TIMEOUT_SECONDS + " seconds.");
            }
        }

        @Override
        public void close() {
            try {
                this.writer.close();
            } catch(IOException ignored) {
                // best-effort shutdown
            }
            try {
                this.reader.close();
            } catch(IOException ignored) {
                // best-effort shutdown
            }
            this.process.destroy();
            this.outputExecutor.shutdownNow();
        }
    }

    private static GpuOutput readScanOutput(BufferedReader reader) throws IOException {
        GpuOutput output = new GpuOutput();
        String line;
        while((line = reader.readLine()) != null) {
            if(line.length() == 0) continue;
            if(output.firstLine == null) output.firstLine = line;

            if(line.startsWith("MATCH ")) {
                String[] parts = line.split(" ");
                if(parts.length >= 5) {
                    output.matches.add(new GpuMatch(
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]),
                        rotationFromWireValue(Integer.parseInt(parts[4]))
                    ));
                }
            } else if(line.startsWith("DONE ")) {
                String[] parts = line.split(" ");
                if(parts.length >= 3) {
                    output.declaredCount = Integer.parseInt(parts[1]);
                    output.overflow = Integer.parseInt(parts[2]);
                }
                return output;
            } else if(line.startsWith("ERROR ")) {
                output.errorMessage = line.substring("ERROR ".length());
                return output;
            }
        }
        output.errorMessage = output.firstLine == null ? "GPU helper closed without a response." : "GPU helper closed before reporting DONE.";
        return output;
    }

    private static String readTextOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while((line = reader.readLine()) != null) {
                if(output.length() > 0) output.append(' ');
                output.append(line);
            }
        }
        return output.toString();
    }

    private static <T> T getProcessOutput(Future<T> outputFuture) throws Exception {
        try {
            return outputFuture.get(OUTPUT_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            throw new IOException("GPU helper output reader did not finish after process exit.");
        }
    }

    private void writeRequest(BufferedWriter writer, ScanRequest request) throws IOException {
        writer.write(PROTOCOL);
        writer.newLine();
        writer.write("VERSION 12111");
        writer.newLine();
        writer.write("RECT " + request.minX + " " + request.maxXExclusive + " " + request.minZ + " " + request.maxZExclusive + " " + request.yStart + " " + request.yEnd);
        writer.newLine();
        writer.write("MAX_MATCHES " + request.effectiveMaxMatches(this.maxMatches));
        writer.newLine();

        int patternCount = 0;
        for(CompiledPattern pattern : request.compiledPatterns) {
            if(!pattern.impossible) patternCount++;
        }
        writer.write("PATTERNS " + patternCount);
        writer.newLine();
        for(CompiledPattern pattern : request.compiledPatterns) {
            if(pattern.impossible) continue;
            writer.write("PATTERN " + rotationWireValue(pattern.viewDirection) + " " + pattern.observations.length);
            writer.newLine();
            for(CompiledObservation observation : pattern.observations) {
                writer.write(observation.dx + " " + observation.dy + " " + observation.dz + " " + observation.wanted + " " + observation.visibleMapping);
                writer.newLine();
            }
        }
        writer.write("END");
        writer.newLine();
    }

    private static String probe(List<String> command) {
        Process process = null;
        ExecutorService outputExecutor = null;
        Future<String> outputFuture = null;
        try {
            List<String> probeCommand = new ArrayList<String>(command);
            probeCommand.add("--probe");
            process = new ProcessBuilder(probeCommand).redirectErrorStream(true).start();

            outputExecutor = Executors.newSingleThreadExecutor();
            final InputStream stdout = process.getInputStream();
            outputFuture = outputExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return readTextOutput(stdout);
                }
            });

            boolean exited = process.waitFor(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if(!exited) {
                process.destroyForcibly();
                if(outputFuture != null) outputFuture.cancel(true);
                return "GPU probe timed out after " + PROBE_TIMEOUT_SECONDS + " seconds.";
            }

            String output = getProcessOutput(outputFuture);
            if(process.exitValue() != 0) {
                return output.length() == 0 ? "GPU helper probe failed with exit code " + process.exitValue() + "." : output;
            }
            return output.length() == 0 ? "OK GPU helper is available." : "OK " + output;
        } catch(IOException e) {
            return "GPU helper not found. Set -Dcoordinatecracker.gpuCommand=/path/to/coordinatecracker-opencl-helper after building gpu/opencl_coordinatecracker.c.";
        } catch(Exception e) {
            return "GPU probe failed: " + e.getMessage();
        } finally {
            if(process != null) {
                process.destroy();
            }
            if(outputFuture != null && !outputFuture.isDone()) {
                outputFuture.cancel(true);
            }
            if(outputExecutor != null) {
                outputExecutor.shutdownNow();
            }
        }
    }

    private static String defaultHelperCommand() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win") ? "coordinatecracker-opencl-helper.exe" : "coordinatecracker-opencl-helper";
    }

    private static List<String> parseCommand(String command) {
        if(command == null || command.trim().isEmpty()) {
            return Collections.singletonList(defaultHelperCommand());
        }
        List<String> parts = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = 0;
        for(int i = 0; i < command.length(); i++) {
            char ch = command.charAt(i);
            if(inQuote) {
                if(ch == quoteChar) {
                    inQuote = false;
                } else {
                    current.append(ch);
                }
            } else if(ch == '\'' || ch == '"') {
                inQuote = true;
                quoteChar = ch;
            } else if(Character.isWhitespace(ch)) {
                if(current.length() > 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }
        if(current.length() > 0) {
            parts.add(current.toString());
        }
        return parts.isEmpty() ? Collections.singletonList(defaultHelperCommand()) : parts;
    }

    static final class ScanRequest {
        final EnumMCVersion version;
        final CompiledPattern[] compiledPatterns;
        final int minX;
        final int maxXExclusive;
        final int minZ;
        final int maxZExclusive;
        final int yStart;
        final int yEnd;
        final int maxMatches;

        ScanRequest(EnumMCVersion version, CompiledPattern[] compiledPatterns,
                    int minX, int maxXExclusive, int minZ, int maxZExclusive, int yStart, int yEnd, int maxMatches) {
            this.version = version;
            this.compiledPatterns = compiledPatterns;
            this.minX = minX;
            this.maxXExclusive = maxXExclusive;
            this.minZ = minZ;
            this.maxZExclusive = maxZExclusive;
            this.yStart = yStart;
            this.yEnd = yEnd;
            this.maxMatches = maxMatches;
        }

        int effectiveMaxMatches(int backendMaxMatches) {
            int requestMax = this.maxMatches < 1 ? backendMaxMatches : this.maxMatches;
            return Math.max(1, Math.min(backendMaxMatches, requestMax));
        }
    }

    private static final class GpuOutput {
        final List<GpuMatch> matches = new ArrayList<GpuMatch>();
        String firstLine;
        int declaredCount = -1;
        int overflow = 0;
        String errorMessage;
    }

    static final class GpuScanResult {
        enum Status {
            SUCCESS,
            UNSUPPORTED,
            OVERFLOW,
            FAILED
        }

        final Status status;
        final List<GpuMatch> matches;
        final String message;

        private GpuScanResult(Status status, List<GpuMatch> matches, String message) {
            this.status = status;
            this.matches = matches;
            this.message = message;
        }

        static GpuScanResult success(List<GpuMatch> matches) {
            return new GpuScanResult(Status.SUCCESS, matches, "GPU scan completed.");
        }

        static GpuScanResult unsupported(String message) {
            return new GpuScanResult(Status.UNSUPPORTED, Collections.<GpuMatch>emptyList(), message);
        }

        static GpuScanResult overflow(String message) {
            return new GpuScanResult(Status.OVERFLOW, Collections.<GpuMatch>emptyList(), message);
        }

        static GpuScanResult failed(String message) {
            return new GpuScanResult(Status.FAILED, Collections.<GpuMatch>emptyList(), message);
        }
    }

    static int rotationWireValue(EnumRotation rotation) {
        switch(rotation) {
        case R90: return 1;
        case R180: return 2;
        case R270: return 3;
        case FLOOR_R0: return 4;
        case FLOOR_R90: return 5;
        case FLOOR_R180: return 6;
        case FLOOR_R270: return 7;
        case CEILING_R0: return 8;
        case CEILING_R90: return 9;
        case CEILING_R180: return 10;
        case CEILING_R270: return 11;
        case R0:
        default: return 0;
        }
    }

    static EnumRotation rotationFromWireValue(int value) {
        switch(value) {
        case 1: return EnumRotation.R90;
        case 2: return EnumRotation.R180;
        case 3: return EnumRotation.R270;
        case 4: return EnumRotation.FLOOR_R0;
        case 5: return EnumRotation.FLOOR_R90;
        case 6: return EnumRotation.FLOOR_R180;
        case 7: return EnumRotation.FLOOR_R270;
        case 8: return EnumRotation.CEILING_R0;
        case 9: return EnumRotation.CEILING_R90;
        case 10: return EnumRotation.CEILING_R180;
        case 11: return EnumRotation.CEILING_R270;
        case 0:
        default: return EnumRotation.R0;
        }
    }

    static final class GpuMatch {
        final int x;
        final int y;
        final int z;
        final EnumRotation viewDirection;

        GpuMatch(int x, int y, int z, EnumRotation viewDirection) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.viewDirection = viewDirection;
        }
    }
}
