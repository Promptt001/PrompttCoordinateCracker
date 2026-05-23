package io.github.promptt001.coordinatecracker.cracker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.promptt001.coordinatecracker.Main;
import io.github.promptt001.coordinatecracker.data.EnumMCVersion;
import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.data.PatternRelative;
import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector3;
import io.github.promptt001.coordinatecracker.utils.VectorHelper;

public class BruteforceThread extends Thread {

	private static final boolean LOG_MATCHES = Boolean.getBoolean("coordinatecracker.logMatches");

	private final PatternRelative scope;
	
	private final int yStart;
	private final int yEnd;
	private Vector3 start;
	private Vector3 end;
	
	private final EnumMCVersion version;
	private final EnumRotation rotation;
	
	private final CoordinateBruteforcer coordinateBruteforcer;
	private final Main instance;
	private final GpuAccelerationBackend gpuBackend;
	private final ExactBitVectorSolverBackend exactSolverBackend;
	
	private final int id;
	
	/**
	* BruteforceThread constructor
	*/
	public BruteforceThread(PatternRelative scope, int yStart, int yEnd, EnumMCVersion version, EnumRotation rotation, Main instance, CoordinateBruteforcer coordinateBruteforcer, GpuAccelerationBackend gpuBackend, ExactBitVectorSolverBackend exactSolverBackend, int id) {
		this.scope = scope;
		this.yStart = yStart;
		this.yEnd = yEnd;
		this.version = version;
		this.rotation = rotation;
		this.instance = instance;
		this.coordinateBruteforcer = coordinateBruteforcer;
		this.gpuBackend = gpuBackend;
		this.exactSolverBackend = exactSolverBackend;
		this.id = id;
	}
	
	
	/**
	* Start cracking on region and pattern supplied to the constructor 
	*/
	public void run() {
		int totalMatches = 0;
		try {
			totalMatches = this.runCrackLoop();
		} catch(RuntimeException e) {
			this.handleWorkerFailure(e);
		} catch(Error e) {
			this.handleWorkerFailure(e);
		} finally {
			System.out.println("Thread[" + this.id + "] Done with " + totalMatches + " matches!");
			this.coordinateBruteforcer.setDone(this);
		}
	}

	private int runCrackLoop() {
		System.out.println("Thread[" + this.id + "] Starting new bruteforce thread with id [" + this.id + "]");

		CompiledPattern[] compiledPatterns = this.compilePatterns();
		int totalMatches = 0;
		CoordinateBruteforcer.ScanRegion region;

		while((region = this.coordinateBruteforcer.nextScanRegion()) != null) {
			if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
				break;
			}

			MatchCollector matchCollector = new MatchCollector();
			int regionMatches = 0;
			for(CoordinateBruteforcer.ScanRectangle rectangle : region.rectangles) {
				if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
					break;
				}

				this.start = new Vector3(rectangle.minX, this.yStart, rectangle.minZ);
				this.end = new Vector3(rectangle.maxXExclusive, this.yEnd, rectangle.maxZExclusive);

				List<Vector3> tmp = VectorHelper.adjustStartEndVectors(start, end);
				this.start = tmp.get(0);
				this.end = tmp.get(1);

				Integer exactSolverMatches = this.tryRunExactSolver(compiledPatterns, matchCollector);
				if(exactSolverMatches != null) {
					regionMatches += exactSolverMatches.intValue();
					continue;
				}

				Integer gpuMatches = this.tryRunGpu(compiledPatterns, matchCollector);
				if(gpuMatches != null) {
					regionMatches += gpuMatches.intValue();
				}
				else {
					regionMatches += this.canUsePlaneCacheSieve(compiledPatterns)
						? this.runPlaneCacheSieve(compiledPatterns, matchCollector)
						: this.runCoordinateLoop(compiledPatterns, matchCollector);
				}
			}
			totalMatches += regionMatches;
			this.coordinateBruteforcer.completeScanRegion(region, matchCollector.toSortedMatchString());
		}

		return totalMatches;
	}

	private void handleWorkerFailure(Throwable failure) {
		failure.printStackTrace();
		this.coordinateBruteforcer.abortScan("Worker " + this.id + " failed: " + failure.getMessage());
	}

	private Integer tryRunExactSolver(CompiledPattern[] compiledPatterns, MatchCollector matchCollector) {
		if(this.exactSolverBackend == null) {
			return null;
		}
		if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
			return Integer.valueOf(0);
		}

		EnumMCVersion selectedVersion = this.version == null ? EnumMCVersion.V1_21_11 : this.version;
		ExactBitVectorSolverBackend.SolverResult result = this.exactSolverBackend.scan(new ExactBitVectorSolverBackend.ScanRequest(
			selectedVersion,
			compiledPatterns,
			this.start.getX(),
			this.end.getX(),
			this.start.getZ(),
			this.end.getZ(),
			this.start.getY(),
			this.end.getY()
		));
		if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
			return Integer.valueOf(0);
		}

		switch(result.status) {
		case SUCCESS:
			int acceptedMatches = 0;
			for(ExactBitVectorSolverBackend.SolverMatch match : result.matches) {
				if(!this.coordinateBruteforcer.tryRecordMatch(match.x, match.y, match.z, match.viewDirection)) {
					break;
				}
				if(!this.addBufferedMatch(matchCollector, match.x, match.y, match.z, match.viewDirection)) {
					break;
				}
				acceptedMatches++;
			}
			this.coordinateBruteforcer.addCompletedIterations((long) (this.end.getX() - this.start.getX()) * (long) (this.end.getZ() - this.start.getZ()) * (long) (this.end.getY() - this.start.getY()));
			return Integer.valueOf(acceptedMatches);
		case CANCELLED:
			return Integer.valueOf(0);
		case UNSUPPORTED:
		case OVERFLOW:
		case FAILED:
		default:
			this.coordinateBruteforcer.noteExactSolverFallback(result.message);
			return null;
		}
	}

	private Integer tryRunGpu(CompiledPattern[] compiledPatterns, MatchCollector matchCollector) {
		if(this.gpuBackend == null || !this.gpuBackend.isAvailable()) {
			return null;
		}
		if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
			return Integer.valueOf(0);
		}

		int remainingBudget = this.coordinateBruteforcer.remainingMatchBudgetForBackend();
		if(remainingBudget <= 0) {
			return Integer.valueOf(0);
		}

		EnumMCVersion selectedVersion = this.version == null ? EnumMCVersion.V1_21_11 : this.version;
		GpuAccelerationBackend.GpuScanResult result = this.gpuBackend.scan(new GpuAccelerationBackend.ScanRequest(
			selectedVersion,
			compiledPatterns,
			this.start.getX(),
			this.end.getX(),
			this.start.getZ(),
			this.end.getZ(),
			this.start.getY(),
			this.end.getY(),
			remainingBudget
		));
		if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
			return Integer.valueOf(0);
		}

		switch(result.status) {
		case SUCCESS:
			int acceptedMatches = 0;
			for(GpuAccelerationBackend.GpuMatch match : result.matches) {
				if(!this.coordinateBruteforcer.tryRecordMatch(match.x, match.y, match.z, match.viewDirection)) {
					break;
				}
				if(!this.addBufferedMatch(matchCollector, match.x, match.y, match.z, match.viewDirection)) {
					break;
				}
				acceptedMatches++;
			}
			this.coordinateBruteforcer.addCompletedIterations((long) (this.end.getX() - this.start.getX()) * (long) (this.end.getZ() - this.start.getZ()) * (long) (this.end.getY() - this.start.getY()));
			return Integer.valueOf(acceptedMatches);
		case UNSUPPORTED:
		case OVERFLOW:
			if(this.coordinateBruteforcer.isGpuRequired()) {
				this.coordinateBruteforcer.abortScan(result.message);
				return Integer.valueOf(0);
			}
			this.coordinateBruteforcer.noteGpuFallback(result.message);
			return null;
		case FAILED:
		default:
			if(this.coordinateBruteforcer.isGpuRequired()) {
				this.coordinateBruteforcer.abortScan(result.message);
				return Integer.valueOf(0);
			}
			this.coordinateBruteforcer.noteGpuFallback(result.message);
			return null;
		}
	}

	private int runCoordinateLoop(CompiledPattern[] compiledPatterns, MatchCollector matchCollector) {
		int matches = 0;
		long rowProgress = 0;
		
		crackLoop:
		for(int x = start.getX(); x < end.getX(); x++) {
			for(int y = start.getY(); y < end.getY(); y++) {
				for(int z = start.getZ(); z < end.getZ(); z++) {
					if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
						break crackLoop;
					}

					for(CompiledPattern compiledPattern : compiledPatterns) {
						if(compiledPattern.matches(x, y, z)) {
							matches += this.recordMatch(matchCollector, compiledPattern, x, y, z);
						}
					}
					++rowProgress;
				}
				
				if(rowProgress > 0) {
					this.coordinateBruteforcer.addCompletedIterations(rowProgress);
					rowProgress = 0;
				}
			}
		}
		if(rowProgress > 0) {
			this.coordinateBruteforcer.addCompletedIterations(rowProgress);
		}
		return matches;
	}

	private int runPlaneCacheSieve(CompiledPattern[] compiledPatterns, MatchCollector matchCollector) {
		int width = this.end.getX() - this.start.getX();
		int depth = this.end.getZ() - this.start.getZ();
		if(width <= 0 || depth <= 0) {
			return 0;
		}

		ScanBounds bounds = ScanBounds.from(compiledPatterns);
		int chunkDepth = this.chooseChunkDepth(width, depth, bounds);
		int matches = 0;
		int distinctLookupYOffsets = countDistinctLookupYOffsets(compiledPatterns);

		for(int chunkStartZ = this.start.getZ(); chunkStartZ < this.end.getZ(); chunkStartZ += chunkDepth) {
			if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
				break;
			}

			int chunkEndZ = Math.min(this.end.getZ(), chunkStartZ + chunkDepth);
			PlaneCache planeCache = new PlaneCache(
				this.start.getX() + bounds.minDx,
				this.end.getX() + bounds.maxDx,
				chunkStartZ + bounds.minDz,
				chunkEndZ + bounds.maxDz,
				this.version == null ? EnumMCVersion.V1_21_11 : this.version,
				distinctLookupYOffsets
			);
			int chunkWidth = width;
			int chunkDepthActual = chunkEndZ - chunkStartZ;
			int chunkCells = chunkWidth * chunkDepthActual;
			CandidateMask candidates = new CandidateMask(chunkWidth, chunkDepthActual);

			for(int y = this.start.getY(); y < this.end.getY(); y++) {
				if(this.coordinateBruteforcer.isCancelled() || Thread.currentThread().isInterrupted()) {
					return matches;
				}

				for(CompiledPattern compiledPattern : compiledPatterns) {
					if(compiledPattern.impossible) {
						continue;
					}

					candidates.setAll();
					if(this.applyPatternConstraints(candidates, chunkWidth, chunkDepthActual, chunkStartZ, y, compiledPattern, planeCache, bounds)) {
						matches += this.emitCandidateMatches(candidates, chunkWidth, chunkStartZ, y, compiledPattern, matchCollector);
					}
				}

				this.coordinateBruteforcer.addCompletedIterations(chunkCells);
			}
		}

		return matches;
	}

	private boolean applyPatternConstraints(CandidateMask candidates, int chunkWidth, int chunkDepth, int chunkStartZ, int y, CompiledPattern compiledPattern, PlaneCache planeCache, ScanBounds bounds) {
		if(compiledPattern.observations.length == 0) {
			candidates.setAll();
			return !candidates.isEmpty();
		}

		boolean initialized = false;
		for(CompiledObservation observation : compiledPattern.observations) {
			if(initialized && candidates.isEmpty()) {
				return false;
			}

			StateMaskPlane plane = planeCache.get(y + observation.dy);
			int xShift = observation.dx - bounds.minDx;
			int zShift = observation.dz - bounds.minDz;

			for(int localZ = 0; localZ < chunkDepth; localZ++) {
				int sourceRow = localZ + zShift;
				for(int wordInRow = 0, localX = 0; localX < chunkWidth; wordInRow++, localX += 64) {
					int bitCount = Math.min(64, chunkWidth - localX);
					long allowedBits = plane.extractVisibleBits(observation, sourceRow, xShift + localX, bitCount);
					if(initialized) {
						candidates.andWord(localZ, wordInRow, allowedBits, bitCount);
					} else {
						candidates.setWord(localZ, wordInRow, allowedBits, bitCount);
					}
				}
			}
			initialized = true;
		}
		return !candidates.isEmpty();
	}

	private int emitCandidateMatches(CandidateMask candidates, int chunkWidth, int chunkStartZ, int y, CompiledPattern compiledPattern, MatchCollector matchCollector) {
		int matches = 0;
		int candidateBit = candidates.nextSetBit(0);
		while(candidateBit >= 0) {
			int localZ = candidateBit / chunkWidth;
			int localX = candidateBit - (localZ * chunkWidth);
			matches += this.recordMatch(matchCollector, compiledPattern, this.start.getX() + localX, y, chunkStartZ + localZ);
			candidateBit = candidates.nextSetBit(candidateBit + 1);
		}
		return matches;
	}

	private int recordMatch(MatchCollector matchCollector, CompiledPattern compiledPattern, int x, int y, int z) {
		if(!this.coordinateBruteforcer.tryRecordMatch(x, y, z, compiledPattern.viewDirection)) {
			return 0;
		}
		if(LOG_MATCHES) {
			System.out.println(x +  " " + y + " " + z + " facing " + compiledPattern.viewDirection);
		}
		if(!this.addBufferedMatch(matchCollector, x, y, z, compiledPattern.viewDirection)) {
			return 0;
		}
		return 1;
	}

	private boolean addBufferedMatch(MatchCollector matchCollector, int x, int y, int z, EnumRotation viewDirection) {
		matchCollector.add(x, y, z, viewDirection);
		if(this.coordinateBruteforcer.hasUnlimitedMatchBudget()
			&& matchCollector.size() > this.coordinateBruteforcer.maxBufferedMatchesPerRegion()) {
			this.coordinateBruteforcer.abortScan("Region result buffer exceeded " + this.coordinateBruteforcer.maxBufferedMatchesPerRegion() + " matches while Max matches is unlimited. Set a finite Max matches value, add more observations, or raise coordinatecracker.maxBufferedMatchesPerRegion.");
			return false;
		}
		return true;
	}

	private boolean canUsePlaneCacheSieve(CompiledPattern[] compiledPatterns) {
		for(CompiledPattern pattern : compiledPatterns) {
			if(!pattern.isTwoBitCacheCompatible()) {
				return false;
			}
		}
		return true;
	}

	private int chooseChunkDepth(int width, int depth, ScanBounds bounds) {
		long targetBytes = Long.getLong("coordinatecracker.sievePlaneBytes", 4L * 1024L * 1024L);
		int extendedWidth = width + bounds.maxDx - bounds.minDx;
		int dzRange = bounds.maxDz - bounds.minDz;
		if(extendedWidth <= 0) {
			return Math.max(1, depth);
		}

		long targetCells = Math.max(1L, (targetBytes * 8L) / (long) StateMaskPlane.MASK_COUNT);
		long chunk = (targetCells / extendedWidth) - dzRange;
		if(chunk < 1L) {
			chunk = 1L;
		}
		if(chunk > depth) {
			chunk = depth;
		}
		long candidateWordsPerRow = Math.max(1L, (long) ((width + 63) >>> 6));
		long maxChunkByBitSetIndex = Math.max(1L, Integer.MAX_VALUE / candidateWordsPerRow);
		if(chunk > maxChunkByBitSetIndex) {
			chunk = maxChunkByBitSetIndex;
		}
		if(chunk > Integer.MAX_VALUE) {
			chunk = Integer.MAX_VALUE;
		}
		return (int) chunk;
	}

	private static int countDistinctLookupYOffsets(CompiledPattern[] compiledPatterns) {
		Set<Integer> dyOffsets = new HashSet<Integer>();
		for(CompiledPattern pattern : compiledPatterns) {
			for(CompiledObservation observation : pattern.observations) {
				dyOffsets.add(observation.dy);
			}
		}
		return dyOffsets.size();
	}

	/**
	 * Compiles the selected pattern into primitive offsets and wanted states once per
	 * worker. This avoids Matrix3 lookups, EnumBlockType lookups, and Vector3
	 * allocation inside the candidate-coordinate loop.
	 */
	private CompiledPattern[] compilePatterns() {
		return ObservationCompiler.compilePatterns(this.scope, this.version, this.rotation);
	}

	static int getWallObservationOffsetX(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
		return ObservationCompiler.getWallObservationOffsetX(pattern, patternX, patternY, patternZ, viewDirection);
	}

	static int getWallObservationOffsetY(Matrix3 pattern, int patternX, int patternY, int patternZ) {
		return ObservationCompiler.getWallObservationOffsetY(pattern, patternX, patternY, patternZ);
	}

	static int getWallObservationOffsetZ(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
		return ObservationCompiler.getWallObservationOffsetZ(pattern, patternX, patternY, patternZ, viewDirection);
	}

	static int getObservationOffsetX(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
		return ObservationCompiler.getObservationOffsetX(pattern, patternX, patternY, patternZ, viewDirection);
	}

	static int getObservationOffsetY(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
		return ObservationCompiler.getObservationOffsetY(pattern, patternX, patternY, patternZ, viewDirection);
	}

	static int getObservationOffsetZ(Matrix3 pattern, int patternX, int patternY, int patternZ, EnumRotation viewDirection) {
		return ObservationCompiler.getObservationOffsetZ(pattern, patternX, patternY, patternZ, viewDirection);
	}


	static String facingLabel(EnumRotation viewDirection) {
		switch(viewDirection) {
		case R90: return "wall east";
		case R180: return "wall south";
		case R270: return "wall west";
		case FLOOR_R0: return "floor north";
		case FLOOR_R90: return "floor east";
		case FLOOR_R180: return "floor south";
		case FLOOR_R270: return "floor west";
		case CEILING_R0: return "ceiling north";
		case CEILING_R90: return "ceiling east";
		case CEILING_R180: return "ceiling south";
		case CEILING_R270: return "ceiling west";
		case R0:
		default: return "wall north";
		}
	}
	
}
