package io.github.promptt001.coordinatecracker.cracker;

import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Experimental exact solver backend for dense 1.21.11 patterns.
 *
 * This backend is intentionally opt-in because it depends on an external SMT solver and is not
 * universally faster. For strong patterns over very large rectangles it can avoid brute-force
 * scanning by asking a QF_BV solver for satisfying x/y/z assignments directly.
 */
final class ExactBitVectorSolverBackend {
    private static final String COMMAND_PROPERTY = "coordinatecracker.smtSolverCommand";
    private static final int DEFAULT_TIMEOUT_SECONDS = Integer.getInteger("coordinatecracker.smtTimeoutSeconds", 30);
    private static final int DEFAULT_MAX_MATCHES = Integer.getInteger("coordinatecracker.smtMaxMatches", 10000);
    private static final int DEFAULT_MIN_OBSERVATIONS = Integer.getInteger("coordinatecracker.smtMinObservations", 10);

    private static final long RANDOM_MULTIPLIER = 25214903917L;
    private static final long RANDOM_INCREMENT = 11L;
    private static final long RANDOM_SEED_MASK = (1L << 48) - 1L;

    private final List<String> command;
    private final int timeoutSeconds;
    private final int maxMatches;
    private final int minObservations;

    private ExactBitVectorSolverBackend(List<String> command, int timeoutSeconds, int maxMatches, int minObservations) {
        this.command = command;
        this.timeoutSeconds = timeoutSeconds;
        this.maxMatches = Math.max(1, maxMatches);
        this.minObservations = Math.max(1, minObservations);
    }

    static ExactBitVectorSolverBackend createFromSystemProperties() {
        String configured = System.getProperty(COMMAND_PROPERTY);
        if(configured == null || configured.trim().isEmpty()) {
            return null;
        }
        List<String> command = parseCommand(configured);
        if(command.isEmpty()) {
            System.err.println("Ignoring empty " + COMMAND_PROPERTY + " value.");
            return null;
        }
        return new ExactBitVectorSolverBackend(
            command,
            Math.max(1, DEFAULT_TIMEOUT_SECONDS),
            Math.max(1, DEFAULT_MAX_MATCHES),
            Math.max(1, DEFAULT_MIN_OBSERVATIONS)
        );
    }

    String getStatusMessage() {
        return "Exact bit-vector solver enabled using command: " + commandDisplay(this.command)
            + "; min observations: " + this.minObservations
            + "; per-rectangle model cap: " + this.maxMatches + ".";
    }

    SolverResult scan(ScanRequest request) {
        String unsupportedReason = this.supportFailureReason(request.compiledPatterns, request.version);
        if(unsupportedReason != null) {
            return SolverResult.unsupported(unsupportedReason);
        }

        List<SolverMatch> matches = new ArrayList<SolverMatch>();
        for(CompiledPattern pattern : request.compiledPatterns) {
            if(Thread.currentThread().isInterrupted()) {
                return SolverResult.cancelled("SMT solver scan was cancelled.");
            }
            if(pattern.impossible) {
                continue;
            }
            SolverResult result = this.enumeratePattern(request, pattern, matches);
            if(result.status != Status.SUCCESS) {
                return result;
            }
            if(matches.size() >= this.maxMatches) {
                return SolverResult.overflow("SMT solver hit the per-rectangle cap of " + this.maxMatches + " models; falling back to the complete scanner.");
            }
        }
        return SolverResult.success(matches);
    }

    private SolverResult enumeratePattern(ScanRequest request, CompiledPattern pattern, List<SolverMatch> outputMatches) {
        List<int[]> blockedModels = new ArrayList<int[]>();
        Set<String> seen = new HashSet<String>();

        while(outputMatches.size() < this.maxMatches) {
            if(Thread.currentThread().isInterrupted()) {
                return SolverResult.cancelled("SMT solver scan was cancelled.");
            }
            String smt = renderSmt(request, pattern, blockedModels);
            QueryResult query;
            try {
                query = this.runSolver(smt);
            } catch(InterruptedIOException e) {
                Thread.currentThread().interrupt();
                return SolverResult.cancelled("SMT solver scan was cancelled.");
            } catch(IOException e) {
                return SolverResult.failed("SMT solver failed: " + e.getMessage());
            }

            if(query.status == QueryStatus.UNSAT) {
                return SolverResult.success(outputMatches);
            }
            if(query.status == QueryStatus.UNKNOWN) {
                return SolverResult.unsupported("SMT solver returned unknown; falling back to scanner.");
            }
            if(query.status == QueryStatus.ERROR) {
                return SolverResult.failed(query.message);
            }

            if(query.x == null || query.y == null || query.z == null) {
                return SolverResult.failed("SMT solver returned sat without x/y/z values.");
            }

            int x = query.x.intValue();
            int y = query.y.intValue();
            int z = query.z.intValue();
            if(x < request.minX || x >= request.maxXExclusive || y < request.yStart || y >= request.yEnd || z < request.minZ || z >= request.maxZExclusive) {
                return SolverResult.failed("SMT solver returned a model outside the requested rectangle.");
            }
            if(!pattern.matches(x, y, z)) {
                return SolverResult.failed("SMT solver returned a model that did not pass the Java verifier.");
            }

            String key = x + ":" + y + ":" + z + ":" + pattern.viewDirection.value;
            if(!seen.add(key)) {
                return SolverResult.failed("SMT solver repeated a blocked model.");
            }

            outputMatches.add(new SolverMatch(x, y, z, pattern.viewDirection));
            blockedModels.add(new int[] {x, y, z});
        }
        return SolverResult.overflow("SMT solver hit the per-rectangle cap of " + this.maxMatches + " models; falling back to the complete scanner.");
    }

    private String supportFailureReason(CompiledPattern[] compiledPatterns, EnumMCVersion version) {
        if(version != EnumMCVersion.V1_21_11) {
            return "SMT backend currently supports only Minecraft 1.21.11 observations.";
        }
        if(compiledPatterns == null || compiledPatterns.length == 0) {
            return "No compiled patterns were supplied to the SMT backend.";
        }

        int activePatterns = 0;
        for(CompiledPattern pattern : compiledPatterns) {
            if(pattern.impossible) {
                continue;
            }
            activePatterns++;
            if(pattern.observations.length == 0) {
                return "SMT backend requires at least one observation per active pattern.";
            }
            if(pattern.observations.length < this.minObservations) {
                return "SMT backend skipped a weak active pattern with " + pattern.observations.length + " observations; set -Dcoordinatecracker.smtMinObservations to lower the gate.";
            }
            for(CompiledObservation observation : pattern.observations) {
                if(observation.version != EnumMCVersion.V1_21_11) {
                    return "SMT backend currently supports only Minecraft 1.21.11 observations.";
                }
                if(observation.variantCount != 4) {
                    return "SMT backend currently supports only four-state random block variants.";
                }
                if(observation.wanted < 0 || observation.wanted > 3) {
                    return "SMT backend received an unsupported wanted state: " + observation.wanted + ".";
                }
                if(observation.visibleMapping == CompiledObservation.MAPPING_CONSTANT_ZERO) {
                    return "SMT backend does not run one-state side-face profiles because they do not add a coordinate constraint.";
                }
                if(observation.visibleMapping == CompiledObservation.MAPPING_MODULO_TWO && observation.wanted > 1) {
                    return "SMT backend received a two-state block observation outside the visible 0..1 range.";
                }
            }
        }
        if(activePatterns == 0) {
            return "No active SMT-compatible patterns were supplied.";
        }
        return null;
    }

    private QueryResult runSolver(String smt) throws IOException {
        Process process = null;
        ExecutorService outputExecutor = null;
        Future<String> outputFuture = null;
        try {
            process = new ProcessBuilder(this.command).redirectErrorStream(true).start();
            final InputStream stdout = process.getInputStream();
            outputExecutor = Executors.newSingleThreadExecutor();
            outputFuture = outputExecutor.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return readTextOutput(stdout);
                }
            });

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), "UTF-8"));
            writer.write(smt);
            writer.flush();
            writer.close();

            boolean exited;
            try {
                exited = process.waitFor(this.timeoutSeconds, TimeUnit.SECONDS);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                if(outputFuture != null) outputFuture.cancel(true);
                throw new InterruptedIOException("SMT solver interrupted.");
            }
            if(!exited) {
                process.destroyForcibly();
                if(outputFuture != null) outputFuture.cancel(true);
                return QueryResult.error("SMT solver timed out after " + this.timeoutSeconds + " seconds.");
            }

            String output = getProcessOutput(outputFuture);
            int exitCode = process.exitValue();
            if(exitCode != 0) {
                return QueryResult.error("SMT solver exited with code " + exitCode + ": " + abbreviate(output));
            }
            return parseSolverOutput(output);
        } finally {
            if(process != null) {
                process.destroy();
            }
            if(outputExecutor != null) {
                outputExecutor.shutdownNow();
            }
        }
    }

    private static QueryResult parseSolverOutput(String output) {
        String[] lines = output.split("\\r?\\n");
        QueryStatus status = null;
        for(String line : lines) {
            String trimmed = line.trim();
            if("sat".equals(trimmed)) {
                status = QueryStatus.SAT;
                break;
            }
            if("unsat".equals(trimmed)) {
                return QueryResult.unsat();
            }
            if("unknown".equals(trimmed)) {
                return QueryResult.unknown();
            }
        }
        if(status != QueryStatus.SAT) {
            return QueryResult.error("SMT solver did not return sat/unsat/unknown: " + abbreviate(output));
        }

        Integer x = parseBitVectorValue(output, "x");
        Integer y = parseBitVectorValue(output, "y");
        Integer z = parseBitVectorValue(output, "z");
        return QueryResult.sat(x, y, z);
    }

    private static Integer parseBitVectorValue(String output, String name) {
        Pattern hexPattern = Pattern.compile("\\(\\s*" + name + "\\s+#x([0-9a-fA-F]+)\\s*\\)");
        Matcher hexMatcher = hexPattern.matcher(output);
        if(hexMatcher.find()) {
            long unsigned = Long.parseUnsignedLong(hexMatcher.group(1), 16);
            return Integer.valueOf((int) unsigned);
        }

        Pattern decimalPattern = Pattern.compile("\\(\\s*" + name + "\\s+\\(_\\s+bv([0-9]+)\\s+32\\)\\s*\\)");
        Matcher decimalMatcher = decimalPattern.matcher(output);
        if(decimalMatcher.find()) {
            long unsigned = Long.parseUnsignedLong(decimalMatcher.group(1));
            return Integer.valueOf((int) unsigned);
        }
        return null;
    }

    private static String renderSmt(ScanRequest request, CompiledPattern pattern, List<int[]> blockedModels) {
        StringBuilder smt = new StringBuilder(8192 + pattern.observations.length * 512);
        smt.append("(set-logic QF_BV)\n");
        smt.append("(set-option :produce-models true)\n");
        smt.append("(declare-fun x () (_ BitVec 32))\n");
        smt.append("(declare-fun y () (_ BitVec 32))\n");
        smt.append("(declare-fun z () (_ BitVec 32))\n");
        smt.append("(assert (bvsge x ").append(bv32(request.minX)).append("))\n");
        smt.append("(assert (bvslt x ").append(bv32(request.maxXExclusive)).append("))\n");
        smt.append("(assert (bvsge y ").append(bv32(request.yStart)).append("))\n");
        smt.append("(assert (bvslt y ").append(bv32(request.yEnd)).append("))\n");
        smt.append("(assert (bvsge z ").append(bv32(request.minZ)).append("))\n");
        smt.append("(assert (bvslt z ").append(bv32(request.maxZExclusive)).append("))\n");

        for(int[] blocked : blockedModels) {
            smt.append("(assert (not (and (= x ").append(bv32(blocked[0]))
               .append(") (= y ").append(bv32(blocked[1]))
               .append(") (= z ").append(bv32(blocked[2]))
               .append("))))\n");
        }

        for(CompiledObservation observation : pattern.observations) {
            String variant = variantExpr(add32("x", observation.dx), add32("y", observation.dy), add32("z", observation.dz));
            if(observation.visibleMapping == CompiledObservation.MAPPING_MODULO_TWO) {
                smt.append("(assert (= ((_ extract 0 0) ").append(variant).append(") ")
                   .append(observation.wanted == 0 ? "#b0" : "#b1").append("))\n");
            } else {
                smt.append("(assert (= ").append(variant).append(' ').append(bv2(observation.wanted)).append("))\n");
            }
        }

        smt.append("(check-sat)\n");
        smt.append("(get-value (x y z))\n");
        return smt.toString();
    }

    private static String variantExpr(String x32, String y32, String z32) {
        String xprod32 = "(bvmul " + x32 + " " + bv32(3129871) + ")";
        String xprod64 = "((_ sign_extend 32) " + xprod32 + ")";
        String z64 = "((_ sign_extend 32) " + z32 + ")";
        String y64 = "((_ sign_extend 32) " + y32 + ")";
        return "(let ((xprod " + xprod64 + ") (z64 " + z64 + ") (y64 " + y64 + ")) "
            + "(let ((i (bvxor (bvxor xprod (bvmul z64 " + bv64(116129781L) + ")) y64))) "
            + "(let ((mixed (bvadd (bvmul (bvmul i i) " + bv64(42317861L) + ") (bvmul i " + bv64(11L) + ")))) "
            + "(let ((seed (bvashr mixed " + bv64(16L) + "))) "
            + "(let ((rs (bvand (bvxor seed " + bv64(RANDOM_MULTIPLIER) + ") " + bv64(RANDOM_SEED_MASK) + "))) "
            + "(let ((next (bvand (bvadd (bvmul rs " + bv64(RANDOM_MULTIPLIER) + ") " + bv64(RANDOM_INCREMENT) + ") " + bv64(RANDOM_SEED_MASK) + "))) "
            + "((_ extract 47 46) next))))))";
    }

    private static String add32(String variable, int offset) {
        return offset == 0 ? variable : "(bvadd " + variable + " " + bv32(offset) + ")";
    }

    private static String bv2(int value) {
        switch(value & 3) {
        case 0: return "#b00";
        case 1: return "#b01";
        case 2: return "#b10";
        default: return "#b11";
        }
    }

    private static String bv32(int value) {
        return String.format("#x%08x", value);
    }

    private static String bv64(long value) {
        return String.format("#x%016x", value);
    }

    private static List<String> parseCommand(String commandLine) {
        if(commandLine == null || commandLine.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> parts = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar = '\0';
        boolean escaping = false;

        for(int i = 0; i < commandLine.length(); i++) {
            char c = commandLine.charAt(i);
            if(escaping) {
                current.append(c);
                escaping = false;
            } else if(c == '\\') {
                escaping = true;
            } else if(inQuote) {
                if(c == quoteChar) {
                    inQuote = false;
                } else {
                    current.append(c);
                }
            } else if(c == '\'' || c == '"') {
                inQuote = true;
                quoteChar = c;
            } else if(Character.isWhitespace(c)) {
                if(current.length() > 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if(escaping) {
            current.append('\\');
        }
        if(current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static String commandDisplay(List<String> command) {
        StringBuilder builder = new StringBuilder();
        for(String part : command) {
            if(builder.length() > 0) builder.append(' ');
            builder.append(part.indexOf(' ') >= 0 ? '"' + part + '"' : part);
        }
        return builder.toString();
    }

    private static String readTextOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            String line;
            while((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return output.toString();
    }

    private static String getProcessOutput(Future<String> outputFuture) throws IOException {
        if(outputFuture == null) {
            return "";
        }
        try {
            return outputFuture.get(5, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            throw new IOException("SMT solver output reader did not finish after process exit.");
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("SMT solver output read was interrupted.");
        } catch(Exception e) {
            throw new IOException("Unable to read SMT solver output.", e);
        }
    }

    private static String abbreviate(String value) {
        if(value == null) {
            return "";
        }
        String trimmed = value.trim().replace('\n', ' ');
        return trimmed.length() <= 400 ? trimmed : trimmed.substring(0, 400) + "...";
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

        ScanRequest(EnumMCVersion version, CompiledPattern[] compiledPatterns,
                    int minX, int maxXExclusive, int minZ, int maxZExclusive, int yStart, int yEnd) {
            this.version = version;
            this.compiledPatterns = compiledPatterns;
            this.minX = minX;
            this.maxXExclusive = maxXExclusive;
            this.minZ = minZ;
            this.maxZExclusive = maxZExclusive;
            this.yStart = yStart;
            this.yEnd = yEnd;
        }
    }

    enum Status { SUCCESS, UNSUPPORTED, FAILED, OVERFLOW, CANCELLED }

    static final class SolverResult {
        final Status status;
        final List<SolverMatch> matches;
        final String message;

        private SolverResult(Status status, List<SolverMatch> matches, String message) {
            this.status = status;
            this.matches = matches;
            this.message = message;
        }

        static SolverResult success(List<SolverMatch> matches) {
            return new SolverResult(Status.SUCCESS, matches, null);
        }

        static SolverResult unsupported(String message) {
            return new SolverResult(Status.UNSUPPORTED, Collections.<SolverMatch>emptyList(), message);
        }

        static SolverResult failed(String message) {
            return new SolverResult(Status.FAILED, Collections.<SolverMatch>emptyList(), message);
        }

        static SolverResult overflow(String message) {
            return new SolverResult(Status.OVERFLOW, Collections.<SolverMatch>emptyList(), message);
        }

        static SolverResult cancelled(String message) {
            return new SolverResult(Status.CANCELLED, Collections.<SolverMatch>emptyList(), message);
        }
    }

    static final class SolverMatch {
        final int x;
        final int y;
        final int z;
        final EnumRotation viewDirection;

        SolverMatch(int x, int y, int z, EnumRotation viewDirection) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.viewDirection = viewDirection;
        }
    }

    private enum QueryStatus { SAT, UNSAT, UNKNOWN, ERROR }

    private static final class QueryResult {
        final QueryStatus status;
        final Integer x;
        final Integer y;
        final Integer z;
        final String message;

        private QueryResult(QueryStatus status, Integer x, Integer y, Integer z, String message) {
            this.status = status;
            this.x = x;
            this.y = y;
            this.z = z;
            this.message = message;
        }

        static QueryResult sat(Integer x, Integer y, Integer z) {
            return new QueryResult(QueryStatus.SAT, x, y, z, null);
        }

        static QueryResult unsat() {
            return new QueryResult(QueryStatus.UNSAT, null, null, null, null);
        }

        static QueryResult unknown() {
            return new QueryResult(QueryStatus.UNKNOWN, null, null, null, null);
        }

        static QueryResult error(String message) {
            return new QueryResult(QueryStatus.ERROR, null, null, null, message);
        }
    }
}
