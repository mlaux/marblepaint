package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import android.util.Log;

public class Marble {
	// true to store a line width for each part of the line (slower)
	private static boolean widthPerSegment = false;
	
	private float x;
	private float y;
	private float z;
	
	private float lastStoredX;
	private float lastStoredZ;

	private float xAccel;
	private float yAccel;

	private float linewidth;
	private float[] colorValue;
	
	private FloatBuffer linecoords;
	private FloatBuffer linecolors;
	private FloatBuffer linewidths;
	
	public Marble() {
		linewidth = 4.0f;
		colorValue = new float[] { 0.0f, 0.0f, 0.0f };
		
		linecoords = Calc.alloc(3 * 256);
		linecoords.put(new float[] { 0, 0.1f, 0 });
		
		linecolors = Calc.alloc(4 * 256);
		linecolors.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
		
		if(widthPerSegment) {
			linewidths = Calc.alloc(1 * 256);
			linewidths.put(linewidth);
		}
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

		int n = nv / 3;
		if(widthPerSegment) {
			// This really sacrifices a lot of our speed because instead of 
			// drawing the whole line strip in one call, we have to specify the
			// width of each segment and then draw it, which requires /n/ function
			// calls, where /n/ is the number of line segments in the trail.
			// So, we should probably optimize this.

			for(int k = 1; k < n; k++) {
				float w = linewidths.get(k);
				glLineWidth(w);
				glPointSize(w - 2.0f);
				glDrawArrays(GL_POINTS, k, 1);
				glDrawArrays(GL_LINE_STRIP, k - 1, 2);
			}
		} else {
			// Do it the fast way.
			glLineWidth(linewidth);
			glPointSize(linewidth - 2.0f);
			glDrawArrays(GL_POINTS, 0, n);
			glDrawArrays(GL_LINE_STRIP, 0, n);
		}
		
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		// Put the buffers back so we can continue putting stuff in them
		linecoords.position(nv);
		linecolors.position(nc);
		
		// Draw the actual marble
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
	 * This isn't so bad on memory as I initially thought
	 */
	private void addSegmentTo(float x, float y, float z) {
		if (!linecoords.hasRemaining())
			linecoords = resize(linecoords);

		linecoords.put(x).put(y).put(z);
		
		if (!linecolors.hasRemaining())
			linecolors = resize(linecolors);
		
		linecolors.put(colorValue[0]);
		linecolors.put(colorValue[1]);
		linecolors.put(colorValue[2]);
		linecolors.put(1.0f);
			
		if(widthPerSegment) {
			if (!linewidths.hasRemaining())
				linewidths = resize(linewidths);

			linewidths.put(linewidth);
		}

		lastStoredX = x;
		lastStoredZ = z;
	}
	
	private FloatBuffer resize(FloatBuffer fb) {
		Log.i(getClass().getName(), " ###### Resizing buffer " + fb + " ###### ");
		float[] f = new float[fb.position()];
		fb.position(0);
		fb.get(f);
		return Calc.alloc(f.length * 2).put(f);
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
