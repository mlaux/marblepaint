package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import android.util.Log;

public class Marble {
	private float x;
	private float y;
	private float z;
	
	private float lastStoredX;
	private float lastStoredZ;

	private float xAccel;
	private float yAccel;
	

	private FloatBuffer linecoords = Calc.alloc(3 * 256);
	private FloatBuffer linecolors = Calc.alloc(4 * 256);
	private FloatBuffer linewidths = Calc.alloc(1 * 256);

	private float linewidth = 4.0f;
	private float[] colorValue = { 0.0f, 0.0f, 0.0f };
	
	public Marble() {
		linecoords.put(new float[] { 0, 0.1f, 0 });
		linecolors.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
		linewidths.put(linewidth);
	}
	
	public void render() {
		// Mark the current place in the buffer so we can reset it
		int nv = linecoords.position(), nc = linecolors.position();
		// Rewind the buffers to the beginning for OpenGL
		linecoords.position(0);
		linecolors.position(0);

		glDisable(GL_LIGHTING);
		glDisable(GL_DEPTH_TEST);

		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, linecoords);
		glColorPointer(4, GL_FLOAT, 0, linecolors);
		
		// This really sacrifices a lot of our speed because instead of 
		// drawing the whole line strip in one call, we have to specify the
		// width of each segment and then draw it, which requires /n/ function
		// calls, where /n/ is the number of line segments in the trail.
		// So, we should probably optimize this.
		
		int n = nv / 3;
		for(int k = 1; k < n; k++) {
			float w = linewidths.get(k);
			glLineWidth(w);
			glPointSize(w - 2.0f);
			glDrawArrays(GL_POINTS, k, 1);
			glDrawArrays(GL_LINE_STRIP, k - 1, 2);
		}
		
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		// Put the buffers back so we can continue putting stuff in them
		linecoords.position(nv);
		linecolors.position(nc);
		
		glPushMatrix();
			glColor4f(colorValue[0], colorValue[1], colorValue[2], 1.0f);
			glTranslatef(x, y, z);
			GLUT.glutSolidSphere(1.0f, 32, 32);
		glPopMatrix();
	}
	
	public void accelerate(float x, float y, float z) {
		xAccel += 0.02f * y;
		yAccel += 0.02f * x;
	}
	
	public void update() {
		float newx = x + xAccel;
		float newy = z + yAccel;

		if (Math.abs(newx) > 13.5f) {
			xAccel *= -0.2f;
		}
		if (Math.abs(newy) > 7.0f) {
			yAccel *= -0.2f;
		}

		x += xAccel;
		z += yAccel;
		
		if (Calc.distanceSquared(lastStoredX, lastStoredZ, x, z) > 1.0f)
			addSegmentTo(x, 0.1f, z);
	}
	
	/**
	 * This is such a memory hog, we're going to have to do some debugging here
	 */
	private void addSegmentTo(float x, float y, float z) {
		if (!linecoords.hasRemaining())
			linecoords = resize(linecoords);
		if (!linecolors.hasRemaining())
			linecolors = resize(linecolors);
		if (!linewidths.hasRemaining())
			linewidths = resize(linewidths);

		linecoords.put(x).put(y).put(z);
		
		linecolors.put(colorValue[0]);
		linecolors.put(colorValue[1]);
		linecolors.put(colorValue[2]);
		linecolors.put(1.0f);
		
		linewidths.put(linewidth);

		lastStoredX = x;
		lastStoredZ = z;
	}
	
	private FloatBuffer resize(FloatBuffer fb) {
		Log.i(getClass().getName(), " ###### Resizing buffer " + fb + " ###### ");
		float[] f = new float[fb.position()];
		fb.position(0);
		fb.get(f);
		fb = Calc.alloc(fb.capacity() * 2);
		fb.put(f);
		return fb;
	}
	
	public void setColor(float r, float g, float b) {
		colorValue[0] = r;
		colorValue[1] = g;
		colorValue[2] = b;
	}
	
	public void clear() {
		linecoords.position(0);
		linecolors.position(0);
		linewidths.position(0);
	}
	
	public void increaseSize() {
		if (linewidth < 10.0f)
			linewidth += 1.0f;
	}
	
	public void decreaseSize() {
		if (linewidth > 2.0f)
			linewidth -= 1.0f;
	}
}
