package io.github.promptt001.coordinatecracker.data;

public enum EnumRotation {

	R0(0, 0, 1, false),
	R90(90, 90, 1, false),
	R180(180, 180, 1, false),
	R270(270, 270, 1, false),

	FLOOR_R0(1000, 0, 2, false),
	FLOOR_R90(1090, 90, 2, false),
	FLOOR_R180(1180, 180, 2, false),
	FLOOR_R270(1270, 270, 2, false),

	CEILING_R0(2000, 0, 4, false),
	CEILING_R90(2090, 90, 4, false),
	CEILING_R180(2180, 180, 4, false),
	CEILING_R270(2270, 270, 4, false),

	R_ALL(360, 0, 1, true),
	FLOOR_ALL(1360, 0, 2, true),
	CEILING_ALL(2360, 0, 4, true),

	WALL_FLOOR_R0(3000, 0, 3, false),
	WALL_FLOOR_R90(3090, 90, 3, false),
	WALL_FLOOR_R180(3180, 180, 3, false),
	WALL_FLOOR_R270(3270, 270, 3, false),
	WALL_FLOOR_ALL(3360, 0, 3, true),

	WALL_CEILING_R0(4000, 0, 5, false),
	WALL_CEILING_R90(4090, 90, 5, false),
	WALL_CEILING_R180(4180, 180, 5, false),
	WALL_CEILING_R270(4270, 270, 5, false),
	WALL_CEILING_ALL(4360, 0, 5, true),

	FLOOR_CEILING_R0(5000, 0, 6, false),
	FLOOR_CEILING_R90(5090, 90, 6, false),
	FLOOR_CEILING_R180(5180, 180, 6, false),
	FLOOR_CEILING_R270(5270, 270, 6, false),
	FLOOR_CEILING_ALL(5360, 0, 6, true),

	ALL_SURFACES_R0(6000, 0, 7, false),
	ALL_SURFACES_R90(6090, 90, 7, false),
	ALL_SURFACES_R180(6180, 180, 7, false),
	ALL_SURFACES_R270(6270, 270, 7, false),
	ALL_SURFACES(6360, 0, 7, true);

	private static final int ALL_SURFACE_MASK = 7;
	
	public final int value;
	private final int yawDegrees;
	private final int surfaceMask;
	private final boolean allYaw;
	
	private EnumRotation(int value, int yawDegrees, int surfaceMask, boolean allYaw) {
		this.value = value;
		this.yawDegrees = yawDegrees;
		this.surfaceMask = surfaceMask;
		this.allYaw = allYaw;
	}

	public int getYawDegrees() {
		return this.yawDegrees;
	}

	public boolean isWall() {
		return this.surfaceMask == 1;
	}

	public boolean isFloor() {
		return this.surfaceMask == 2;
	}

	public boolean isCeiling() {
		return this.surfaceMask == 4;
	}

	public boolean isHorizontalSurface() {
		return this.surfaceMask == 2 || this.surfaceMask == 4;
	}

	public boolean isCompositeScanMode() {
		return this.allYaw || Integer.bitCount(this.surfaceMask) > 1;
	}

	public boolean includesView(EnumRotation viewDirection) {
		return (this.surfaceMask & viewDirection.surfaceMask) != 0 && (this.allYaw || this.yawDegrees == viewDirection.yawDegrees);
	}

	public int getViewCount() {
		return Integer.bitCount(this.surfaceMask) * (this.allYaw ? 4 : 1);
	}

	public static EnumRotation fromSurfaceSelection(boolean walls, boolean floors, boolean ceilings, int yawDegrees, boolean allFacings) {
		int mask = 0;
		if(walls) mask |= 1;
		if(floors) mask |= 2;
		if(ceilings) mask |= 4;
		if(mask == 0) mask = 1;
		if((mask & ~ALL_SURFACE_MASK) != 0) return R_ALL;

		if(allFacings) {
			switch(mask) {
			case 1: return R_ALL;
			case 2: return FLOOR_ALL;
			case 4: return CEILING_ALL;
			case 3: return WALL_FLOOR_ALL;
			case 5: return WALL_CEILING_ALL;
			case 6: return FLOOR_CEILING_ALL;
			case 7: return ALL_SURFACES;
			default: return R_ALL;
			}
		}

		switch(mask) {
		case 1:
			return wallRotationFor(yawDegrees);
		case 2:
			return floorRotationFor(yawDegrees);
		case 4:
			return ceilingRotationFor(yawDegrees);
		case 3:
			return wallFloorRotationFor(yawDegrees);
		case 5:
			return wallCeilingRotationFor(yawDegrees);
		case 6:
			return floorCeilingRotationFor(yawDegrees);
		case 7:
			return allSurfaceRotationFor(yawDegrees);
		default:
			return R_ALL;
		}
	}

	private static EnumRotation wallRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return R90;
		case 180: return R180;
		case 270: return R270;
		case 0:
		default: return R0;
		}
	}

	private static EnumRotation floorRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return FLOOR_R90;
		case 180: return FLOOR_R180;
		case 270: return FLOOR_R270;
		case 0:
		default: return FLOOR_R0;
		}
	}

	private static EnumRotation ceilingRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return CEILING_R90;
		case 180: return CEILING_R180;
		case 270: return CEILING_R270;
		case 0:
		default: return CEILING_R0;
		}
	}

	private static EnumRotation wallFloorRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return WALL_FLOOR_R90;
		case 180: return WALL_FLOOR_R180;
		case 270: return WALL_FLOOR_R270;
		case 0:
		default: return WALL_FLOOR_R0;
		}
	}

	private static EnumRotation wallCeilingRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return WALL_CEILING_R90;
		case 180: return WALL_CEILING_R180;
		case 270: return WALL_CEILING_R270;
		case 0:
		default: return WALL_CEILING_R0;
		}
	}

	private static EnumRotation floorCeilingRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return FLOOR_CEILING_R90;
		case 180: return FLOOR_CEILING_R180;
		case 270: return FLOOR_CEILING_R270;
		case 0:
		default: return FLOOR_CEILING_R0;
		}
	}

	private static EnumRotation allSurfaceRotationFor(int yawDegrees) {
		switch(normalizeYaw(yawDegrees)) {
		case 90: return ALL_SURFACES_R90;
		case 180: return ALL_SURFACES_R180;
		case 270: return ALL_SURFACES_R270;
		case 0:
		default: return ALL_SURFACES_R0;
		}
	}

	private static int normalizeYaw(int yawDegrees) {
		return ((yawDegrees % 360) + 360) % 360;
	}
	
}
