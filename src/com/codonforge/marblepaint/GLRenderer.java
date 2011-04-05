package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f, -1.0f, 0.0f);

	private int width;
	private int height;
	
	private int arrowtex;
	private boolean menuShown;

	private Marble marble;
	private Menu menu;

	public void onDrawFrame(GL10 gl) {
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		marble.update(width, height);
		marble.render();
		
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		if(menuShown)
			menu.render();
		Rect.render(menuShown ? 384 : 0, height - 64, 64, 64, arrowtex, menuShown);
	}

	public void accelerate(float x, float y, float z) {
		if(marble != null)
			marble.accelerate(x, y, z);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		if(marble == null)
			marble = new Marble(width / 2, height / 2);

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrthof(0.0f, width, height, 0.0f, -25.0f, 25.0f);
		glMatrixMode(GL_MODELVIEW);
		
		arrowtex = Texture.loadTexture(MarblePaint.getContext(), R.drawable.arrow);
		
		int uiTexture = Texture.loadTexture(MarblePaint.getContext(), R.drawable.ui);
		menu = new Menu(marble, 0, height - 384, 384, 384, uiTexture);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glDisable(GL_DEPTH_TEST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
		glEnable(GL_LIGHT0);
		glEnable(GL_COLOR_MATERIAL);
		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public boolean handleMenuClick(float x, float y) {
		if(menuShown) {
			if(x > 384 && x < 384 + 64 && y > height - 64 && y < height) {
				menuShown = !menuShown;
				return true;
			}
		} else {
			if(x > 0 && x < 64 && y > height - 64 && y < height) {
				menuShown = !menuShown;
				return true;
			}
		}
		return menu.handleClick((int) x, (int) y);
	}
}
