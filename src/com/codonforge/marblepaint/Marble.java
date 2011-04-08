package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import android.util.Log;

public class Marble {
	private static final float radius = 25.0f;
	// true to store a line width for each part of the line (slower)
	private static boolean widthPerSegment = false;
	
	private int counter;
	
	private float x;
	private float y;
	
	private float xAccel;
	private float yAccel;
	
	private float lastStoredX;
	private float lastStoredZ;

	private float linewidth;
	private float[] colorValue;
	
	private boolean rainbowMode;
	
	private FloatBuffer linecoords;
	private FloatBuffer linecolors;
	private FloatBuffer linewidths;

	private transient boolean needClear;
	
	public Marble(int x, int y) {
		this.x = x;
		this.y = y;
		
		linewidth = 4.0f;
		colorValue = new float[] { 0.0f, 0.0f, 0.0f };
		
		linecoords = Calc.alloc(2 * 256);
		linecoords.put(new float[] { x, y });
		
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

		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glVertexPointer(2, GL_FLOAT, 0, linecoords);
		glColorPointer(4, GL_FLOAT, 0, linecolors);

		int n = nv / 2;
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

		// Put the buffers back so we can continue putting stuff in them
		linecoords.position(nv);
		linecolors.position(nc);
		
		// Draw the actual marble
		glPushMatrix();
			if(rainbowMode)
				updateRainbow();
			glColor4f(colorValue[0], colorValue[1], colorValue[2], 1.0f);
			glTranslatef(x, y, 0);

			glEnable(GL_LIGHTING);
			GLUT.glutSolidSphere(radius, 32, 32);

			glDisable(GL_LIGHTING);
		glPopMatrix();
		
		if(needClear) {
			linecoords.position(0);
			linecolors.position(0);
			if(widthPerSegment)
				linewidths.position(0);
			needClear = false;
		}
	}
	
	public void accelerate(float x, float y, float z) {
		xAccel += ((0.5f * y) - (4.472135955e-1 * ((1.26f * y) * (.1f / .5f))));
		yAccel += ((0.5f * x) - (4.472135955e-1 * ((1.26f * x) * (.1f / .5f))));
		// Acceleration - Coefficient of Rotational Friction * Normal Force * Force of Rolling Resistance
	}
	
	public void update(int w, int h) {
		float newx = x + xAccel;
		float newy = y + yAccel;

		if (newx <= radius || newx >= w - radius) {
			xAccel *= -0.2f;
		}
		if (newy <= radius || newy >= h - radius) {
			yAccel *= -0.2f;
		}

		x += xAccel;
		y += yAccel;
		
		if (Calc.distanceSquared(lastStoredX, lastStoredZ, x, y) > 750.0f)
			addSegmentTo(x, y); 
	}
	
	/**
	 * This isn't so bad on memory as I initially thought
	 */
	private void addSegmentTo(float x, float y) {
		if (!linecoords.hasRemaining())
			linecoords = resize(linecoords);

		linecoords.put(x).put(y);
		
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
		lastStoredZ = y;
	}
	
	private FloatBuffer resize(FloatBuffer fb) {
		Log.i(getClass().getName(), " ###### Resizing buffer " + fb + " ###### ");
		float[] f = new float[fb.position()];
		fb.position(0);
		fb.get(f);
		return Calc.alloc(f.length * 2).put(f);
	}
	
	public void setColor(float r, float g, float b) {
		rainbowMode = false;
		colorValue[0] = r;
		colorValue[1] = g;
		colorValue[2] = b;
	}
	
	public void clear() {
		needClear = true;
	}
	
	public void increaseSize() {
		if (linewidth < 10.0f)
			linewidth += 1.0f;
	}
	
	public void decreaseSize() {
		if (linewidth > 2.0f)
			linewidth -= 1.0f;
	}
	
	private void updateRainbow() {
		//red
		if (counter == 0)
			colorValue = new float[] { 1, 0, 0 };
		//green
		else if (counter == 20)
			colorValue = new float[] { 0, 1, 0 };
		//blue
		else if (counter == 40)
			colorValue = new float[] { 0, 0, 1 };
		//yellow
		else if (counter == 60)
			colorValue = new float[] { 1, 1, 0 };
		//orange
		else if (counter == 80)
			colorValue = new float[] { 1, 0.5f, 0 };
		//purple
		else if (counter == 100)
			colorValue = new float[] { 0.5f, 0, 1 };

		counter = (counter + 1) % 120;
	}

	public void setRainbow(boolean b) {
		rainbowMode = b;
	}
}
