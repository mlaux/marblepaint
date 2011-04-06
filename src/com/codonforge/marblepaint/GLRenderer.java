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

	private boolean splash = true;
	private int splashtex;
	
	private int rgbtex;
	private int settingstex;
	private int arrowtex;
	
	private Marble marble;
	private Menu colors;
	private Menu settings;

	public void onDrawFrame(GL10 gl) {
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glLoadIdentity();

		if (splash) {
			Rect.render(width / 2 - 256, height / 2 - 128, 512, 512, splashtex, false);
			return;
		}

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		marble.update(width, height);
		marble.render();

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		if(colors.isVisible()) {
			colors.render();
			Rect.render(384, height - 64, 64, 64, arrowtex, true);
		} else if(settings.isVisible()) {
			settings.render();
			Rect.render(384, height - 64, 64, 64, arrowtex, true);
		} else {
			Rect.render(0, height - 64, 64, 64, rgbtex, false);
			Rect.render(64, height - 64, 64, 64, settingstex, false);
		}
	}

	public void accelerate(float x, float y, float z) {
		if (marble != null)
			marble.accelerate(x, y, z);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		if (marble == null)
			marble = new Marble(width / 2, height / 2);

		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrthof(0.0f, width, height, 0.0f, -25.0f, 25.0f);
		glMatrixMode(GL_MODELVIEW);

		splashtex = Texture.loadTexture(MarblePaint.getContext(), R.drawable.splash);
		rgbtex = Texture.loadTexture(MarblePaint.getContext(), R.drawable.rgb);
		settingstex = Texture.loadTexture(MarblePaint.getContext(), R.drawable.settings);
		arrowtex = Texture.loadTexture(MarblePaint.getContext(), R.drawable.arrow);

		int uiTexture = Texture.loadTexture(MarblePaint.getContext(), R.drawable.ui);
		colors = new Menu(new MenuListener() {
			public void onAction(int id) {
				switch(id) {
					case 0: marble.setColor(0.0f, 0.0f, 0.0f); break; // black
					case 1: marble.setColor(1.0f, 0.0f, 0.0f); break; // red
					case 2: marble.setColor(0.0f, 1.0f, 0.0f); break; // green
					case 3: marble.setColor(0.0f, 0.0f, 1.0f); break; // blue
					case 4: marble.setColor(1.0f, 1.0f, 0.0f); break; // yellow
					case 5: marble.setColor(1.0f, 0.5f, 0.0f); break; // orange
					case 6: marble.setColor(0.5f, 0.0f, 1.0f); break; // purple
					case 7: marble.toggleRainbow(); break; // rainbow
					case 8: marble.increaseSize(); break;
					case 9: marble.decreaseSize(); break;
					case 10: marble.clear(); break;
					case 11: MarblePaint.getContext().finish();
				}
			}
		}, 0, height - 384, 384, 384, uiTexture);

		int settingsTexture = Texture.loadTexture(MarblePaint.getContext(), R.drawable.ui2);
		settings = new Menu(new MenuListener() {
			public void onAction(int id) {
				// TODO implement
			}
		}, 0, height - 384, 384, 384, settingsTexture);
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

	public boolean handleTap(float x, float y) {
		if(colors.isVisible()) {
			if (x > 384 && x < 384 + 64 && y > height - 64 && y < height) {
				colors.setVisible(false);
				return true;
			}
			return colors.handleClick((int) x, (int) y);
		} else if(settings.isVisible()) {
			if (x > 384 && x < 384 + 64 && y > height - 64 && y < height) {
				settings.setVisible(false);
				return true;
			}
			return settings.handleClick((int) x, (int) y);
		} else {
			if (x > 0 && x < 64 && y > height - 64 && y < height) {
				colors.setVisible(true);
				return true;
			} else if (x > 64 && x < 128 && y > height - 64 && y < height) {
				settings.setVisible(true);
				return true;
			}
		}
		
		return false;
	}

	public void setSplash(boolean b) {
		splash = b;
	}

	public boolean getSplash() {
		return splash;
	}

	public void hideMenus() {
		settings.setVisible(false);
		colors.setVisible(false);
	}
}
