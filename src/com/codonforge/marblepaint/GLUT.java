package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;


/**
 * Since Android doesn't contain the GLUT library, and GLUT has a great
 * function for drawing spheres, I ripped this out of a freely available GLUT
 * implementation and ported it for Android.
 * 
 * @author Matt
 *
 */
public class GLUT {
	private static FloatBuffer sphereVerts;
	private static FloatBuffer sphereNorms;

	private static IntBuffer cubeVerts;
	private static ByteBuffer cubeIndices;
	
	private static float lastRadius;
	private static int lastSlices;
	private static int lastStacks;
	
	public static void glutSolidSphere(float radius, int slices, int stacks) {
		if (lastRadius != radius || lastSlices != slices || lastStacks != stacks) {
			lastRadius = radius;
			lastSlices = slices;
			lastStacks = stacks;
			plotSpherePoints(radius, stacks, slices);
		}
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_NORMAL_ARRAY);

		glVertexPointer(3, GL_FLOAT, 0, sphereVerts);
		glNormalPointer(GL_FLOAT, 0, sphereNorms);

		int triangles = (slices + 1) * 2;

		for (int i = 0; i < stacks; i++)
			glDrawArrays(GL_TRIANGLE_STRIP, i * triangles, triangles);

		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
	}

	private static void plotSpherePoints(float radius, int stacks, int slices) {
		sphereVerts = Calc.alloc(4 * 6 * stacks * (slices + 1));
		sphereNorms = Calc.alloc(4 * 6 * stacks * (slices + 1));
		
		float stackstep = ((float) Math.PI) / stacks;
		float slicestep = 2.0f * ((float) Math.PI) / slices;

		for (int i = 0; i < stacks; ++i) {
			float a = i * stackstep;
			float b = a + stackstep;

			float s0 = (float) Math.sin(a);
			float s1 = (float) Math.sin(b);

			float c0 = (float) Math.cos(a);
			float c1 = (float) Math.cos(b);

			float nv;
			for (int j = 0; j <= slices; ++j) {
				float c = j * slicestep;
				float x = (float) Math.cos(c);
				float y = (float) Math.sin(c);

				nv = x * s0;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);

				nv = y * s0;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);

				nv = c0;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);

				nv = x * s1;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);

				nv = y * s1;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);

				nv = c1;
				sphereNorms.put(nv);
				sphereVerts.put(nv * radius);
			}
		}
		sphereNorms.position(0);
		sphereVerts.position(0);
	}
}
