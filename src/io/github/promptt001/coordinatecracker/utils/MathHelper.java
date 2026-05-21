package io.github.promptt001.coordinatecracker.utils;

import java.util.ArrayList;
import java.util.List;

import io.github.promptt001.coordinatecracker.math.Vector3;

public class MathHelper {

	/**
	* Returns rotation for a specific coordinate for minecraft 1.12
	* pos: Vector3 instance representing the position
	*/
	public static byte getRotationForVector(Vector3 pos) {
		return getRotationForCoordinates(pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	* Primitive-coordinate overload used by the hot brute-force loop.
	*/
	public static byte getRotationForCoordinates(int x, int y, int z) {
		return (byte) (Math.abs((int) getRandomForCoordinates(x, y, z) >> 16) % 4);
	}

	
	/**
	* Returns a randomness seed depending on the position for minecraft 1.12
	* This function is useless
	* pos: Vector3 instance representing the position
	*/
	private static long getRandomForVector(Vector3 pos) {
		return getRandomForCoordinates(pos.getX(), pos.getY(), pos.getZ());
	}

	
	/**
	* Returns a randomness seed depending on the position for minecraft 1.12
	* pos: Vector3 instance representing the position
	*/
	private static long getRandomForCoordinates(int x, int y, int z) {
		long i = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
		i = i * i * 42317861L + i * 11L;
		return i;
	}

	
	/**
	* Returns rotation for a specific coordinate for minecraft 1.16.
	* This preserves the original project's legacy 1.16 implementation.
	* pos: Vector3 instance representing the position
	*/
	public static int getRotationForVector_1_16(Vector3 pos) {
		return getRotationForCoordinates_1_16(pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	* Primitive-coordinate overload matching java.util.Random#setSeed + nextLong.
	*/
	public static int getRotationForCoordinates_1_16(int x, int y, int z) {
		long seed = getRandomForCoordinates_1_16(x, y, z);
		long randomSeed = (seed ^ MinecraftLocalRandom.MULTIPLIER) & MinecraftLocalRandom.SEED_MASK;
		randomSeed = (randomSeed * MinecraftLocalRandom.MULTIPLIER + MinecraftLocalRandom.INCREMENT) & MinecraftLocalRandom.SEED_MASK;
		randomSeed = (randomSeed * MinecraftLocalRandom.MULTIPLIER + MinecraftLocalRandom.INCREMENT) & MinecraftLocalRandom.SEED_MASK;
		return Math.abs((int) (randomSeed >>> 16)) % 4;
	}

	
	/**
	* Returns a randomness seed depending on the position for minecraft 1.16
	* This function is useless
	* pos: Vector3 instance representing the position
	*/
	private static long getRandomForVector_1_16(Vector3 pos) {
		return getRandomForCoordinates_1_16(pos.getX(), pos.getY(), pos.getZ());
	}

	
	/**
	* Returns a randomness seed depending on the position for minecraft 1.16
	* pos: Vector3 instance representing the position
	*/
	private static long getRandomForCoordinates_1_16(int x, int y, int z) {
		long i = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;		
		i = i * i * 42317861L + i * 11L;
		return i >> 16;
	}

	/**
	* Returns the selected 1.21.11 blockstate-model variant index for the
	* coordinate. For vanilla deepslate, variantCount is 4 for each axis.
	*
	* Minecraft 1.21.11 still derives a rendering seed from the block position,
	* then feeds that seed into the client-side random used by blockstate model
	* pools. LocalRandom/CheckedRandom use the java.util.Random LCG constants, so
	* this lightweight implementation is enough for deterministic model-pool
	* selection without depending on Minecraft jars.
	*/
	public static int getVariantForVector_1_21_11(Vector3 pos, int variantCount) {
		return getRotationForCoordinates_1_21_11(pos.getX(), pos.getY(), pos.getZ(), variantCount);
	}

	/**
	* Compatibility wrapper for the rest of this program, whose UI represents
	* observed states as values 0..3. In 1.21.11 this is a weighted model variant
	* index, not necessarily a literal 0/90/180/270 texture rotation.
	*/
	public static int getRotationForVector_1_21_11(Vector3 pos) {
		return getVariantForVector_1_21_11(pos, 4);
	}

	/**
	* Compatibility wrapper for block profiles that have a known four-state
	* model/rotation pool. The GUI currently exposes rotations 0..3, so block
	* profiles with variantCount other than 4 should only be added after their
	* GUI mapping is documented.
	*/
	public static int getRotationForVector_1_21_11(Vector3 pos, int variantCount) {
		return getRotationForCoordinates_1_21_11(pos.getX(), pos.getY(), pos.getZ(), variantCount);
	}

	/**
	* Primitive-coordinate overload used by the hot brute-force loop.
	*/
	public static int getRotationForCoordinates_1_21_11(int x, int y, int z, int variantCount) {
		if(variantCount == 1) return 0;
		return nextIntFromSeed(getRenderingSeed_1_21_11(x, y, z), variantCount);
	}

	/**
	* Position-derived rendering seed used by modern Java Edition block rendering.
	*/
	public static long getRenderingSeed_1_21_11(Vector3 pos) {
		return getRenderingSeed_1_21_11(pos.getX(), pos.getY(), pos.getZ());
	}

	public static long getRenderingSeed_1_21_11(int x, int y, int z) {
		long i = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
		i = i * i * 42317861L + i * 11L;
		return i >> 16;
	}

	/**
	* Minimal java.util.Random-compatible generator matching Minecraft's
	* LocalRandom/CheckedRandom LCG behaviour in modern Yarn mappings.
	*/
	private static int nextIntFromSeed(long seed, int bound) {
		if(bound <= 0) {
			throw new IllegalArgumentException("bound must be positive");
		}

		long randomSeed = (seed ^ MinecraftLocalRandom.MULTIPLIER) & MinecraftLocalRandom.SEED_MASK;
		randomSeed = (randomSeed * MinecraftLocalRandom.MULTIPLIER + MinecraftLocalRandom.INCREMENT) & MinecraftLocalRandom.SEED_MASK;
		int bits = (int) (randomSeed >>> 17);

		if((bound & -bound) == bound) {
			return (int) ((bound * (long) bits) >> 31);
		}

		int value = bits % bound;
		while(bits - value + (bound - 1) < 0) {
			randomSeed = (randomSeed * MinecraftLocalRandom.MULTIPLIER + MinecraftLocalRandom.INCREMENT) & MinecraftLocalRandom.SEED_MASK;
			bits = (int) (randomSeed >>> 17);
			value = bits % bound;
		}

		return value;
	}

	private static final class MinecraftLocalRandom {
		private static final long MULTIPLIER = 25214903917L;
		private static final long INCREMENT = 11L;
		private static final long SEED_MASK = (1L << 48) - 1;

		private long seed;

		MinecraftLocalRandom(long seed) {
			this.setSeed(seed);
		}

		void setSeed(long seed) {
			this.seed = (seed ^ MULTIPLIER) & SEED_MASK;
		}

		int next(int bits) {
			this.seed = (this.seed * MULTIPLIER + INCREMENT) & SEED_MASK;
			return (int) (this.seed >>> (48 - bits));
		}

		int nextInt(int bound) {
			if(bound <= 0) {
				throw new IllegalArgumentException("bound must be positive");
			}

			if((bound & -bound) == bound) {
				return (int) ((bound * (long) this.next(31)) >> 31);
			}

			int bits;
			int value;
			do {
				bits = this.next(31);
				value = bits % bound;
			} while(bits - value + (bound - 1) < 0);

			return value;
		}
	}
	
	/**
	* Flips order of two Integers and returns them as a list
	* Who would make somthing useless as that?
	* Oh it was me who made this
	* a: Integer 1
	* b: Integer 2
	*/
	public static List<Integer> flip(int a, int b) {
		List<Integer> result = new ArrayList<>();
		result.add(b);
		result.add(a);

		return result;
	}
	
	
	/**
	* Returns true if a String is an Integer
	* strIn: String to be tested
	*/
	public static boolean isInteger(String strIn) {
		try {
			Integer.valueOf(strIn);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	
	/**
	* Gets the absolute value of a long
	* l: long to be returned absolute
	*/
	public static long abs(long l) {
		return l<0 ? l*-1 : l;
	}

	
	/**
	* Gets the absolute value of a float
	* f: float to be returned absolute
	*/
	public static float abs(float f) {
		return f<0 ? f*-1 : f; 
	}
	
	
	/**
	* Returns the bigger long
	* a: long 1
	* b: long 2
	*/
	public static long max(long a, long b) {
		return a < b ? b : a;
	}
	
	
	/**
	* Returns the bigger float
	* a: float 1
	* b: float 2
	*/
	public static float max(float a, float b) {
		return a < b ? b : a;
	}
}
