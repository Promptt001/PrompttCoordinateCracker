package io.github.promptt001.coordinatecracker.data;

public enum EnumAccelerationMode {
    CPU("CPU only"),
    GPU_AUTO("GPU auto"),
    GPU_REQUIRED("GPU required");

    private final String displayName;

    EnumAccelerationMode(String displayName) {
        this.displayName = displayName;
    }

    public boolean wantsGpu() {
        return this == GPU_AUTO || this == GPU_REQUIRED;
    }

    public boolean requiresGpu() {
        return this == GPU_REQUIRED;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
