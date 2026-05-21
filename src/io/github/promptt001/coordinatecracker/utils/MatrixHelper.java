package io.github.promptt001.coordinatecracker.utils;

import io.github.promptt001.coordinatecracker.data.EnumRotation;
import io.github.promptt001.coordinatecracker.math.Matrix2;
import io.github.promptt001.coordinatecracker.math.Matrix3;
import io.github.promptt001.coordinatecracker.math.Vector2;
import io.github.promptt001.coordinatecracker.math.Vector3;

public class MatrixHelper {
	
	/**
	* Rotates a Matrix3 instance by a given angle (0, 90, 180, 270) clockwise
	* around each X/Z layer. Rectangular X/Z dimensions are supported; each 90
	* degree turn swaps the X and Z dimensions and transforms the stored center.
	* matrixIn: Matrix3 instance to be rotated
	* degrees: EnumRotation value the matrix should be rotated
	*/
	public static Matrix3 rotateMatrix(Matrix3 matrixIn, EnumRotation degrees) {
		int rotations = 0;
		
		if(degrees.value % 90 == 0) {
			rotations = ((degrees.getYawDegrees() / 90) % 4 + 4) % 4;
		}
		else return new Matrix3(matrixIn);
		
		Matrix3 current = new Matrix3(matrixIn);
		for(int i = 0; i < rotations; i++) {
			Vector3 newSize = new Vector3(current.getSizeZ(), current.getSizeY(), current.getSizeX());
			Vector3 newCenter = new Vector3(current.getCenterZ(), current.getCenterY(), current.getSizeX() - 1 - current.getCenterX());
			Matrix3 next = new Matrix3(newSize, newCenter);
			for(int y = 0; y < current.getSizeY(); y++) {
				next.setLayer(y, rotateMatrix(current.getLayer(y)));
			}
			current = next;
		}
		
		return current;
	}
	
	
	/**
	* Rotates a Matrix2 instance by 90 degrees clockwise.
	* matrixIn: Matrix2 instance to be rotated
	*/
	public static Matrix2 rotateMatrix(Matrix2 matrixIn) {
		final int m = matrixIn.getSizeX();
		final int n = matrixIn.getSizeZ();
		int[][] ret = new int[n][m];
		for (int x = 0; x < m; x++) {
			for (int z = 0; z < n; z++) {
				ret[z][m - 1 - x] = matrixIn.getValue(new Vector2(x, z));
			}
		}
		
		Vector2 center = new Vector2(matrixIn.getCenterZ(), matrixIn.getSizeX() - 1 - matrixIn.getCenterX());
		return new Matrix2(ret, center);
	}
	
}
