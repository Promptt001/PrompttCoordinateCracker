package io.github.promptt001.coordinatecracker.utils;

public class SystemHelper {
	/**
	 * Clamps the requested worker count to the CPUs available to the JVM.
	 */
	public static int adjustThreadCount(int threadCount) {
		int processors = Math.max(1, Runtime.getRuntime().availableProcessors());
		if(threadCount < 1) return 1;
		return Math.min(threadCount, processors);
	}
	
	public static boolean validThreadCount(int threadCount) {
		return threadCount >= 1 && Runtime.getRuntime().availableProcessors() >= threadCount;
	}
	
}
