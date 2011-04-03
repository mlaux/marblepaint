package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f,
			0.0f, 1.0f);

	private int width;
	private int height;

	private Object3D enclosure;

	private float marblex;
	private float marbley = 1.0f;
	private float marblez;

	private float lastStoredX;
	private float lastStoredZ;

	private float xAccel;
	private float yAccel;

	private float linewidth = 4.0f;
	private float pointsize = 2.0f;

	private FloatBuffer linecoords = Calc.alloc(3 * 256);
	private FloatBuffer linecolors = Calc.alloc(4 * 256);

	private float[] colorValue = { 0.0f, 0.0f, 0.0f };

	public void onDrawFrame(GL10 gl) {
		float newx = marblex + xAccel;
		float newy = marblez + yAccel;

		if (Math.abs(newx) > 13.5f) {
			xAccel *= -0.2f;
		}
		if (Math.abs(newy) > 7.0f) {
			yAccel *= -0.2f;
		}

		marblex += xAccel;
		marblez += yAccel;

		if (Calc.distanceSquared(lastStoredX, lastStoredZ, marblex, marblez) > 1.0f)
			push(marblex, 0.1f, marblez);

		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glLoadIdentity();
		GLU.gluLookAt(gl, 0, 25, 5, 0, 0, 0, 0, 1, 0);

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		enclosure.render();

		glColor4f(colorValue[0], colorValue[1], colorValue[2], 1.0f);
		drawTrail();

		glPushMatrix();
		glTranslatef(marblex, marbley, marblez);
		GLUT.glutSolidSphere(1.0f, 32, 32);
		glPopMatrix();
		
		glPointSize(pointsize);
		glLineWidth(linewidth);
	}

	private void drawTrail() {
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
		glDrawArrays(GL_POINTS, 0, nv / 3);
		glDrawArrays(GL_LINE_STRIP, 0, nv / 3);
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_VERTEX_ARRAY);

		glEnable(GL_DEPTH_TEST);
		glEnable(GL_LIGHTING);

		// Put the buffers back so we can continue putting stuff in them
		linecoords.position(nv);
		linecolors.position(nc);
	}

	/**
	 * This is such a memory hog, we're going to have to do some debugging here
	 */
	private void push(float x, float y, float z) {
		if (!linecoords.hasRemaining())
			linecoords = resize(linecoords);
		if (!linecolors.hasRemaining())
			linecolors = resize(linecolors);

		linecoords.put(x);
		linecoords.put(y);
		linecoords.put(z);
		linecolors.put(colorValue[0]);
		linecolors.put(colorValue[1]);
		linecolors.put(colorValue[2]);
		linecolors.put(1.0f);

		lastStoredX = x;
		lastStoredZ = z;
	}

	private FloatBuffer resize(FloatBuffer fb) {
		Log.i("", " ###### Resizing buffer " + fb + " ###### ");
		float[] f = new float[fb.position()];
		fb.position(0);
		fb.get(f);
		fb = Calc.alloc(fb.capacity() * 2);
		fb.put(f);
		return fb;
	}

	public void accelerate(float x, float y, float z) {
		xAccel += 0.02f * y;
		yAccel += 0.02f * x;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				100.0f);
		glMatrixMode(GL_MODELVIEW);

		try {
			enclosure = new Object3D(MarblePaint.getContext(), R.raw.box, -1);
			enclosure.setScale(0.45f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);

		glEnable(GL_NORMALIZE);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glEnable(GL_COLOR_MATERIAL);
		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		linecoords.put(new float[] { 0, 0.1f, 0 });
		linecolors.put(new float[] { 0.0f, 0.0f, 0.0f, 1.0f });
	}

	/**
	 * Enables orthographic (2d) projection
	 */
	private void orthoOn() {
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrthof(0, width, height, 0, 1, -1);

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glLoadIdentity();

		glDisable(GL_LIGHTING);
		glDisable(GL_COLOR_MATERIAL);
	}

	/**
	 * Disables orthographic projection
	 */
	private void orthoOff() {
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glPopMatrix();

		glEnable(GL_COLOR_MATERIAL);
		glEnable(GL_LIGHTING);
	}

	public void setColorValue(float r, float g, float b) {
		colorValue[0] = r;
		colorValue[1] = g;
		colorValue[2] = b;
	}

	public void resetLines() {
		linecoords.position(0);
		linecolors.position(0);
	}

	public void increaseSize() {
		if (linewidth < 10.0f) {
			linewidth += 1.0f;
			pointsize += 1.0f;
		}
	}
	
	public void decreaseSize() {
		if (linewidth > 2.0f) {
			linewidth -= 1.0f;
			pointsize -= 1.0f;
		}
	}
}
