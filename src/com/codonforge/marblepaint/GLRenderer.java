package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f,
			0.0f, 1.0f);

	private int width;
	private int height;

	private Object3D enclosure;
	private int splatter;

	private float marblex;
	private float marbley = 1.0f;
	private float marblez;

	private float xAccel;
	private float yAccel;
	
	private float[] splatx = new float[256];
	private float[] splaty = new float[256];
	private int splatidx;

	public void onDrawFrame(GL10 gl) {
		// Clear the screen

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
		
		splatx[splatidx] = marblex;
		splaty[splatidx] = marblez;
		splatidx = (splatidx + 1) % 256;

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glLoadIdentity();
		GLU.gluLookAt(gl, 0, 25, 5, 0, 0, 0, 0, 0, -1);

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		enclosure.render();

		glColor4f(0.0f, 0.5f, 1.0f, 1.0f);

		glPushMatrix();
		glTranslatef(marblex, marbley, marblez);
		GLUT.glutSolidSphere(1.0f, 32, 32);
		glPopMatrix();

		glDisable(GL_LIGHTING);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		for(int k = 0; k < 256; k++)
			Rect.render(splatx[k] - 1, splaty[k] - 1, 2, 2, splatter);
		glEnable(GL_LIGHTING);
	}

	public void accelerate(float x, float y, float z) {
		xAccel += 0.02f * y;
		yAccel += 0.02f * x;

		MarblePaint.getContext().setOverlayText(2, "Marble: [x: " + marblex + ", y: " + marbley + ", z: " + marblez + "]");
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
		glMatrixMode(GL_MODELVIEW);

		try {
			splatter = Texture.loadTexture(MarblePaint.getContext(), R.drawable.paint);
			enclosure = new Object3D(MarblePaint.getContext(), R.raw.box, -1);
			enclosure.setScale(0.45f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

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
}
