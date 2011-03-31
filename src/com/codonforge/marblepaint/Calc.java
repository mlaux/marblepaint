package com.codonforge.marblepaint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Some common stuff related to 3D math and memory allocation
 * 
 * @author Matt
 * 
 */
public class Calc {
	/**
	 * Multiplies the 4x4 matrix in the FloatBuffer with the 4 coordinates (x,
	 * y, z, w). Returns the transformed point.
	 */
	public static float[] multiply(FloatBuffer matrix, float x, float y,
			float z, float w) {
		float[] res = new float[4];
		res[0] = matrix.get(0) * x + matrix.get(4) * y + matrix.get(8) * z
				+ matrix.get(12) * w;
		res[1] = matrix.get(1) * x + matrix.get(5) * y + matrix.get(9) * z
				+ matrix.get(13) * w;
		res[2] = matrix.get(2) * x + matrix.get(6) * y + matrix.get(10) * z
				+ matrix.get(14) * w;
		res[3] = matrix.get(3) * x + matrix.get(7) * y + matrix.get(11) * z
				+ matrix.get(15) * w;
		return res;
	}

	/**
	 * Multiplies the 3x3 matrix in the FloatBuffer with the 3 coordinates (x,
	 * y, z). Returns the transformed point.
	 */
	public static float[] multiply(FloatBuffer matrix, float x, float y, float z) {
		float[] res = new float[3];
		res[0] = matrix.get(0) * x + matrix.get(4) * y + matrix.get(8) * z;
		res[1] = matrix.get(1) * x + matrix.get(5) * y + matrix.get(9) * z;
		res[2] = matrix.get(2) * x + matrix.get(6) * y + matrix.get(10) * z;
		return res;
	}

	/**
	 * Returns the 3D Cartesian distance between the 2 specified points, (x1,
	 * y1, z1) and (x2, y2, z2).
	 */
	public static float distance(float x1, float y1, float z1, float x2,
			float y2, float z2) {
		float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		return (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
	}

	/**
	 * Wraps the specified float[] in a native FloatBuffer.
	 */
	public static FloatBuffer wrapDirect(float... arr) {
		FloatBuffer fb = alloc(arr.length);
		fb.put(arr);
		fb.flip();
		return fb;
	}
	
	public static FloatBuffer wrapDirect(List<Float> list) {
		FloatBuffer fb = alloc(list.size());
		for(int k = 0; k < list.size(); k++)
			fb.put(list.get(k).floatValue());
		fb.flip();
		return fb;
	}

	/**
	 * Allocates a FloatBuffer suitable for holding <code>size</code> floats.
	 */
	public static FloatBuffer alloc(int size) {
		return ByteBuffer.allocateDirect(size * 4).order(
				ByteOrder.nativeOrder()).asFloatBuffer();
	}
}
