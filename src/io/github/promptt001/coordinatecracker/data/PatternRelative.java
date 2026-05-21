package io.github.promptt001.coordinatecracker.data;

import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector3;

public class PatternRelative {

	public Matrix3 patternMatrix;
	public Matrix3 blockTypeMatrix;
	
	public Vector3 center;
	public Vector3 size;
	public Vector3 iterator;
	
	/**
	* PatternRelative constructor
	* size: pattern size
	* center: center of pattern
	*/
	public PatternRelative(Vector3 size, Vector3 center) {
		this.patternMatrix = new Matrix3(size, center);
		this.blockTypeMatrix = new Matrix3(size, center);
		setDefaultBlockTypes(this.blockTypeMatrix, EnumBlockType.DEEPSLATE.getId());
		
		this.center = center;
		this.size = size;
		
		this.iterator = new Vector3(0, 0, 0);
	}
	
	
	/**
	* Returns true if a value could be added to the pattern
	* Used for "initializing" values of the pattern
	*/
	public boolean canAddToPattern(int value) {
		this.patternMatrix.setValue(iterator, value);
		
		if(iterator.getX() < size.getX() - 1) {
			iterator.incrementX(1);
		}
		else if(iterator.getY() < size.getY() - 1) {
			iterator.setX(0);
			iterator.incrementY(1);
		}
		else if(iterator.getZ() < size.getZ() - 1) {
			iterator.setX(0);
			iterator.setY(0);
			iterator.incrementZ(1);
		}
		else if(iterator.getZ() == size.getZ() - 1) {
			return false;
		}
		return true;
	}
	
	
	/**
	* Sets the pattern to values of a Matrix3 instance.
	* reload is retained for source compatibility and has no effect because wall
	* facing is applied at scan-compile time instead of through rotated clones.
	*/
	public void setPatternMatrix(Matrix3 patternMatrix, boolean reload) {
		this.patternMatrix = patternMatrix;
	}

	/**
	* Sets the per-observation block types.
	* reload is retained for source compatibility and has no effect because wall
	* facing is applied at scan-compile time instead of through rotated clones.
	*/
	public void setBlockTypeMatrix(Matrix3 blockTypeMatrix, boolean reload) {
		this.blockTypeMatrix = blockTypeMatrix;
	}
	
	
	/**
	* Kept for source compatibility with older callers. The current wall-mode
	* matcher compiles the base GUI pattern with explicit facing offsets, so there
	* are no rotated pattern clones to refresh.
	*/
	public void load() {
		// no-op by design
	}
	
	
	/**
	* Returns the pattern as entered in the GUI. The rotation argument is ignored;
	* wall-facing transforms are handled by BruteforceThread when compiling scan
	* offsets, preventing stale rotated matrices from disagreeing with matcher
	* behavior.
	*/
	public Matrix3 getPatternMatrix(EnumRotation rotation) {
		return this.patternMatrix;
	}

	/**
	* Returns the block-type matrix aligned with the base GUI pattern.
	*/
	public Matrix3 getBlockTypeMatrix(EnumRotation rotation) {
		return this.blockTypeMatrix;
	}
	
	private void setDefaultBlockTypes(Matrix3 matrix, int blockTypeId) {
		for(int x = 0; x < matrix.getSizeX(); x++) {
			for(int y = 0; y < matrix.getSizeY(); y++) {
				for(int z = 0; z < matrix.getSizeZ(); z++) {
					matrix.setValue(new Vector3(x, y, z), blockTypeId);
				}
			}
		}
	}
	
}
