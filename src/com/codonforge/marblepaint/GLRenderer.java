package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f, 1.0f, 0.0f);

	private int width;
	private int height;

	private Marble marble;
	private int uiTexture;

	private transient boolean needClear;

	public void onDrawFrame(GL10 gl) {
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glLoadIdentity();

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		marble.update(width, height);
		marble.render();
		
		if(needClear) {
			marble.clear();
			needClear = false;
		}
		
		glDisable(GL_LIGHTING);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Rect.render(0, 0, 256, 256, uiTexture);
		glEnable(GL_LIGHTING);
	}

	public void accelerate(float x, float y, float z) {
		marble.accelerate(x, y, z);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		marble = new Marble(width / 2, height / 2);

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrthof(0.0f, width, height, 0.0f, -25.0f, 25.0f);
		glMatrixMode(GL_MODELVIEW);
		
		uiTexture = Texture.loadTexture(MarblePaint.getContext(), R.drawable.ui);
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
	}

	public void setColorValue(float r, float g, float b) {
		marble.setColor(r, g, b);
	}

	public void resetLines() {
		needClear = true;
	}

	public void increaseSize() {
		marble.increaseSize();
	}
	
	public void decreaseSize() {
		marble.decreaseSize();
	}
}
