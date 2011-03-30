package com.codonforge.marblepaint;

import static android.opengl.GLES10.GL_BLEND;
import static android.opengl.GLES10.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES10.GL_COLOR_MATERIAL;
import static android.opengl.GLES10.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES10.GL_DEPTH_TEST;
import static android.opengl.GLES10.GL_LEQUAL;
import static android.opengl.GLES10.GL_LIGHT0;
import static android.opengl.GLES10.GL_LIGHTING;
import static android.opengl.GLES10.GL_MODELVIEW;
import static android.opengl.GLES10.GL_NICEST;
import static android.opengl.GLES10.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES10.GL_PERSPECTIVE_CORRECTION_HINT;
import static android.opengl.GLES10.GL_POSITION;
import static android.opengl.GLES10.GL_PROJECTION;
import static android.opengl.GLES10.GL_SRC_ALPHA;
import static android.opengl.GLES10.glBlendFunc;
import static android.opengl.GLES10.glClear;
import static android.opengl.GLES10.glClearColor;
import static android.opengl.GLES10.glColor4f;
import static android.opengl.GLES10.glDepthFunc;
import static android.opengl.GLES10.glEnable;
import static android.opengl.GLES10.glHint;
import static android.opengl.GLES10.glLightfv;
import static android.opengl.GLES10.glLoadIdentity;
import static android.opengl.GLES10.glMatrixMode;
import static android.opengl.GLES10.glPopMatrix;
import static android.opengl.GLES10.glPushMatrix;
import static android.opengl.GLES10.glTranslatef;
import static android.opengl.GLES10.glViewport;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f,
			2.0f, 1.0f);

	private int width;
	private int height;

	private float marblex;
	private float marbley;
	private float marblez;

	public void onDrawFrame(GL10 gl) {
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glLoadIdentity();
		GLU.gluLookAt(gl, 0, 25, 0, 0, 0, 0, 0, 0, -1);

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLUT.glutSolidSphere(0.1f, 16, 16);

		// Set a blueish color
		// The parameters go R, G, B, A, with each being between 0 and 1
		glColor4f(0.0f, 0.5f, 1.0f, 1.0f);

		glPushMatrix();
		// Put the light at 0, 0, 0
		glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

		// Move away from the origin -- anything after this line will be
		// translated
		// Note that you could combine the next 2 lines like this:
		// glTranslatef(0.0f, marbley, -20.0f);
		glTranslatef(marblex, marbley, marblez);

		// Draw sphere
		GLUT.glutSolidSphere(1.0f, 32, 32);
		glPopMatrix();

	}

	public void accelerate(float x, float y, float z) {

		marblex += 1.5 * ((y + 0.7f) * .10f);// for calibration we can trade
		// decimals for variables which change to current v values. just done
		// for example.
		marblez += 1.5 * ((x - 0.2f) * .10f);

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
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glEnable(GL_COLOR_MATERIAL);
	}
}
