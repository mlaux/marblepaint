package com.codonforge.marblepaint;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class Renderer implements SurfaceHolder.Callback, Runnable {
	private SurfaceHolder m_surfaceHolder;
	private Thread m_renderThread;
	
	private int m_width;
	private int m_height;
	
	private boolean splash = true;
	private boolean touch = false;
	private Bitmap splashtex;

	private Bitmap rgbtex;
	private Bitmap settingstex;

	private Marble marble;
	private Menu colors;
	private Menu settings;
	
	private boolean running;
	
	public void run() {
		running = true;
		while(running) {
			Canvas c = null;
			try {
				c = m_surfaceHolder.lockCanvas();
				synchronized (m_surfaceHolder) {
					if(c != null) {
						render(c);
					}
				}
			} finally {
				if (c != null) {
					m_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
	
	private void render(Canvas c) {
		c.drawColor(0xFFFFFFFF);
		
		if (splash) {
			RectTool.render(c, splashtex, m_width / 2 - 256, m_height / 2 - 128, 512, 512);
			return;
		}

		marble.update(m_width, m_height);
		marble.render(c);

		if (colors.isVisible())
			colors.render(c);
		else if (settings.isVisible())
			settings.render(c);
		else {
			RectTool.render(c, rgbtex, 0, m_height - 64, 64, 64);
			RectTool.render(c, settingstex, 64, m_height - 64, 64, 64);
		}
	}


	public void accelerate(float x, float y, float z) {
		if (marble != null && !splash && !touch)
			marble.accelerate(x, y, z);
	}

	public boolean handleTap(float x, float y) {
		if (colors.isVisible()) {
			if (x > 384 && x < 384 + 64 && y > m_height - 64 && y < m_height) {
				colors.setVisible(false);
				return true;
			}
			return colors.handleClick((int) x, (int) y);
		} else if (settings.isVisible()) {
			if (x > 384 && x < 384 + 64 && y > m_height - 64 && y < m_height) {
				settings.setVisible(false);
				return true;
			}
			return settings.handleClick((int) x, (int) y);
		} else {
			if (x > 0 && x < 64 && y > m_height - 64 && y < m_height) {
				colors.setVisible(true);
				return true;
			} else if (x > 64 && x < 128 && y > m_height - 64 && y < m_height) {
				settings.setVisible(true);
				return true;
			}
		}

		return false;
	}
	
	public boolean handleDrag(float x, float y) {
		if(touch) {
			marble.setPos(x, y);
			return true;
		}
		return false;
	}

	public void setSplash(boolean b) {
		splash = b;
	}

	public boolean getSplash() {
		return splash;
	}

	class ColorMenuListener implements MenuListener {
		public void onAction(int id) {
			switch (id) {
				case 0: marble.setColor(0x00, 0x00, 0x00); colors.setVisible(false); break; // black
				case 1: marble.setColor(0xFF, 0x00, 0x00); colors.setVisible(false); break; // red
				case 2: marble.setColor(0x00, 0xFF, 0x00); colors.setVisible(false); break; // green
				case 3: marble.setColor(0x00, 0x00, 0xFF); colors.setVisible(false); break; // blue
				case 4: marble.setColor(0xFF, 0xFF, 0x00); colors.setVisible(false); break; // yellow
				case 5: marble.setColor(0xFF, 0x7F, 0x00); colors.setVisible(false); break; // orange
				case 6: marble.setColor(0x7F, 0x00, 0xFF); colors.setVisible(false); break; // purple
				case 7: marble.setRainbow(true); colors.setVisible(false); break; // rainbow
				case 8: marble.increaseSize(); break;
				case 9: marble.decreaseSize(); break;
				case 10: marble.clear(); colors.setVisible(false); break;
				case 11: colors.setVisible(false);
			}
		}
	}
	
	class SettingsMenuListener implements MenuListener {
		public void onAction(int id) {
			switch(id) {
				case 0:
					touch = false;
					break;
				case 1: 
					touch = true;
					break;
				case 2: // save
					MarblePaint.getContext().alert("Coming soon!");
					break;
				case 3: // load
					MarblePaint.getContext().alert("Coming soon!");
					break;
				case 4: // about
					MarblePaint.getContext().showAbout();
					settings.setVisible(false);
					break;
				case 5: // help
					MarblePaint.getContext().showHelp();
					break;
				case 8: // exit
					MarblePaint.getContext().finish();
					break;
				case 11: // back
					break;
			}

			settings.setVisible(false);
		}
	}

	public void surfaceChanged(SurfaceHolder arg0, int form, int w, int h) {
		m_width = w;
		m_height = h;
		
		if (marble == null)
			marble = new Marble(w / 2, h / 2, w, h);
		
		Resources r = MarblePaint.getContext().getResources();
		splashtex = BitmapFactory.decodeResource(r, R.drawable.splash);
		rgbtex = BitmapFactory.decodeResource(r, R.drawable.rgb);
		settingstex = BitmapFactory.decodeResource(r, R.drawable.settings);

		Bitmap uiTexture = BitmapFactory.decodeResource(r, R.drawable.ui);
		colors = new Menu(new ColorMenuListener(), 0, h - 384, 384, 384, uiTexture);

		Bitmap settingsTexture = BitmapFactory.decodeResource(r, R.drawable.ui2);
		settings = new Menu(new SettingsMenuListener(), 0, h - 384, 384, 384, settingsTexture);
		
		m_renderThread = new Thread(this);
		m_renderThread.start();
	}

	public void surfaceCreated(SurfaceHolder arg0) {
		m_surfaceHolder = arg0;
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		running = false;
		try {
			m_renderThread.join();
		} catch (InterruptedException e) { }
		
		marble.destroy();
	}
}
