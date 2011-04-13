package com.codonforge.marblepaint;

import android.content.res.Resources;
import android.graphics.*;
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
	
	private Paint m_paint;
	private int m_baseX;
	private float[] m_color = { 0.0f, 1.0f, 1.0f };
	
	private boolean running;
	
	public Renderer() {
		m_paint = new Paint();
		m_paint.setTextSize(20.0f);
	}
	
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
			for(int k = -200 + m_baseX; k < m_width; k += 20) {
				m_paint.setColor(Color.HSVToColor(m_color));
				c.drawLine(k + 200, 0, k, m_height, m_paint);
			}
			m_color[0] = (m_color[0] + 1.0f) % 360.0f;
			m_baseX = (m_baseX + 1) % 20;
			
			m_paint.setColor(Color.LTGRAY);
			c.drawText(MarblePaint.VERSION, 0, m_height - 5, m_paint);
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
		if (marble != null && !splash && !touch) {
			marble.accelerate(x, y);
		}
	}

	public boolean handleTap(float x, float y) {
		if(splash) {
			splash = false;
			return true;
		}
		
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
			} else {
				if(touch) {
					marble.startDrag(x, y, m_width, m_height);
				} else { 
					marble.setMakeTrail(false);
					marble.setPos(x, y, m_width, m_height);
				}
				return true;
			}
		}
	}
	
	public boolean handleDrag(float x, float y) {
		marble.setPos(x, y, m_width, m_height);
		return true;
	}

	public boolean handleRelease(float x, float y) {
		if(!touch) marble.setMakeTrail(true);
		return true;
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
					marble.stop();
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
		Bitmap settingsTexture = BitmapFactory.decodeResource(r, R.drawable.ui2);
		
		int mh = Math.min(384, h - 64);
		int my = h - mh;
		
		colors = new Menu(new ColorMenuListener(), 0, my, mh, mh, uiTexture);
		settings = new Menu(new SettingsMenuListener(), 0, my, mh, mh, settingsTexture);
		
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
	}

	public void requestClear() {
		marble.clear();
	}
}
