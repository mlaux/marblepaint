package com.codonforge.marblepaint;

import java.io.InputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

public class Renderer implements SurfaceHolder.Callback, Runnable {
	private final static int MAX_FPS = 50;
	private final static int MAX_FRAME_SKIPS = 5;
	private final static int FRAME_PERIOD = 1000 / MAX_FPS;
	
	private SurfaceHolder m_surfaceHolder;
	private Thread m_renderThread;
	
	private int m_width;
	private int m_height;
	
	private boolean splash = true;
	private boolean touch = false;
	private Bitmap splashtex;
	private Bitmap logotex;

	private Bitmap menuclosedtex;
	private Bitmap helptex;
	private Bitmap closetex;

	private Menu menu;
	private static Marble marble;
	private Menu colors;
	private HelpMenu help;
	private Menu options;
	
	private Paint m_paint;
	private int m_baseX;
	private float[] m_color = { 0.0f, 1.0f, 1.0f };
	
	private boolean running;
	
	public Renderer() {
		m_paint = new Paint();
		m_paint.setTextSize(20.0f);
		m_paint.setStrokeWidth(12.5f);
	}
	
	public void run() {
		running = true;
		Canvas canvas;

		long beginTime; // the time when the cycle begun
		long timeDiff; // the time it took for the cycle to execute
		int sleepTime; // ms to sleep (<0 if we're behind)
		int framesSkipped; // number of frames being skipped

		sleepTime = 0;

		while (running) {
			canvas = null;
			// try locking the canvas for exclusive pixel editing
			// in the surface
			try {
				canvas = m_surfaceHolder.lockCanvas();
				synchronized (m_surfaceHolder) {
					beginTime = System.currentTimeMillis();
					framesSkipped = 0; // resetting the frames skipped
					// update game state
					update();
					// render state to the screen
					// draws the canvas on the panel
					render(canvas);
					// calculate how long did the cycle take
					timeDiff = System.currentTimeMillis() - beginTime;
					// calculate sleep time
					sleepTime = (int) (FRAME_PERIOD - timeDiff);

					if (sleepTime > 0) {
						try {
							Thread.sleep(sleepTime);
						} catch (Exception e) {
							
						}
					}

					while (sleepTime < 0 && framesSkipped < MAX_FRAME_SKIPS) {
						update();
						// add frame period to check if in next frame
						sleepTime += FRAME_PERIOD;
						framesSkipped++;
					}
				}
			} finally {
				if (canvas != null) {
					m_surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}
	
	private void update() {
		if(!splash) {
			marble.update(m_width, m_height);
		}
	}
	
	private void render(Canvas c) {
		c.drawColor(0xFFFFFFFF);
		
		if (splash) {
			m_color[1] = 0.1f;
			c.drawColor(Color.HSVToColor(m_color));
			
			for(int k = -200 + m_baseX; k < m_width; k += 50) {
				m_color[1] = 0.25f;
				m_paint.setColor(Color.HSVToColor(m_color));
				c.drawLine(k + 200, 0, k, m_height, m_paint);
			}
			m_color[0] = (m_color[0] + 1.0f) % 360.0f;
			m_baseX = (m_baseX + 1) % 50;
			
			m_paint.setColor(Color.LTGRAY);
			c.drawText(MarblePaint.VERSION, 0, m_height - 5, m_paint);
			RectTool.render(c, splashtex, m_width / 2 - 256, m_height / 2 - 128, 512, 512);
			RectTool.render(c, logotex, m_width - 150, m_height - 25, 150, 25);
			return;
		}
		
		marble.render(c);

		if (help.isVisible()) {
			help.render(c);
			RectTool.render(c, closetex, m_width - 64, 0, 64, 64);
		} else if (colors.isVisible()) {
			colors.render(c);
			RectTool.render(c, closetex, m_width - 64, 0, 64, 64);
		} else if (menu.isVisible()) {
			menu.render(c);
		} else if (options.isVisible()) {
			options.render(c);
			RectTool.render(c, closetex, m_width - 64, 0, 64, 64);
		} else {
			RectTool.render(c, menuclosedtex, ((m_width - (m_width - 200)) / 2), m_height - (m_height / 4), m_width - 200, (m_height / 4));
			RectTool.render(c, helptex, m_width - 64, 0, 64, 64);
		}
	}


	public void accelerate(float x, float y, float z) {
		if (marble != null && !splash && !touch && marble.isMakingTrail()) {
			marble.accelerate(x, y);
		}
	}

	public boolean handleTap(float x, float y) {
		if(splash) {
			splash = false;
			return true;
		}
		
		if (menu.isVisible())
			return menu.handleClick((int) x, (int) y);
		else if (colors.isVisible()) {
			if (x > m_width - 64 && x < m_width && y > 0 && y < 64) {
				MarblePaint.getContext().vibrate();
				colors.setVisible(false);
				return true;
			} else return colors.handleClick((int) x, (int) y); 
		}
		else if (options.isVisible()) {
			if (x > m_width - 64 && x < m_width && y > 0 && y < 64) {
				MarblePaint.getContext().vibrate();
				options.setVisible(false);
				return true;
			} else return options.handleClick((int) x, (int) y); 
		}
		else if (help.isVisible()) {
			if (x > m_width - 64 && x < m_width && y > 0 && y < 64) {
				MarblePaint.getContext().vibrate();
				help.setVisible(false);
				return true;
			} else return help.handleClick((int) x, (int) y);
		}
		else {
			if (x > 64 && x < m_width - 64 && y > m_height - 64 && y < m_height) {
				MarblePaint.getContext().vibrate();
				menu.setVisible(true);			
				return true;
			} else if (x > m_width - 64 && x < m_width && y > 0 && y < 64) {
				MarblePaint.getContext().vibrate();
				help.setVisible(true);
				return true;
			} else {
				if(touch) {
					marble.startDrag(x, y, m_width, m_height);
				} else { 
					marble.setMakeTrail(false);
					marble.stop();
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
				case 5: marble.setColor(0xFF, 0x00, 0x00); colors.setVisible(false); break; // red
				case 6: marble.setColor(0xFF, 0x7F, 0x00); colors.setVisible(false); break; // orange
				case 7: marble.setColor(0xFF, 0xFF, 0x00); colors.setVisible(false); break; // yellow
				case 8: marble.setColor(0x7F, 0xFF, 0x00); colors.setVisible(false); break; // light green
				case 9: marble.setColor(0x00, 0xFF, 0x00); colors.setVisible(false); break; // green
				case 10: marble.setColor(0x00, 0x00, 0xFF); colors.setVisible(false); break; // blue
				case 11: marble.setColor(0x00, 0x7F, 0xFF); colors.setVisible(false); break; // light blue
				case 12: marble.setColor(0x00, 0xFF, 0xFF); colors.setVisible(false); break; // sky blue/turquoise
				case 13: marble.setColor(0xFF, 0x00, 0xFF); colors.setVisible(false); break; // pink/purple color
				case 14: marble.setColor(0x7F, 0x00, 0xFF); colors.setVisible(false); break; // purple
				case 15: marble.setColor(0x00, 0x00, 0x00); colors.setVisible(false); break; // black
				case 16: marble.setColor(0x59, 0x59, 0x59); colors.setVisible(false); break; // dark gray
				case 17: marble.setColor(0xA1, 0xA1, 0xA1); colors.setVisible(false); break; // gray
				case 18: marble.setColor(0xE6, 0xE6, 0xE6); colors.setVisible(false); break; // light gray
				case 19: marble.setRainbow(true); colors.setVisible(false); break; // rainbow
			}
		}
	}
	
	class MainMenuListener implements MenuListener {
		public void onAction(int id) {
			switch(id) {
			case 2:
				menu.setVisible(false);
				break;
			case 5:
				setTouch(!touch);
				break;
			case 6: 
				colors.setVisible(true);
				break;
			case 7: // options
				options.setVisible(true);
				break;
			case 8: // save/load
				MarblePaint.getContext().showSaveLoad();
				break;
			case 9: // about
				MarblePaint.getContext().showAbout();
				break;
			
			}

			menu.setVisible(false);
		}
	}
	
	class OptionsMenuListener implements MenuListener {
		public void onAction(int id) {
			switch(id) {
			case 10:
				marble.decreaseSize();
				break;
			case 11:
				marble.increaseSize();
				break;
			case 13:
				requestClear();
				options.setVisible(false);
				break;		
			}
		}
	}

	public void surfaceChanged(SurfaceHolder arg0, int form, int w, int h) {
		m_width = w;
		m_height = h;
		
		if (marble == null)
			marble = new Marble(w / 2, h / 2, w, h);
		
		Resources r = MarblePaint.getContext().getResources();
		
		splashtex = BitmapFactory.decodeResource(r, R.drawable.splash);
		logotex = BitmapFactory.decodeResource(r, R.drawable.codonforge);
		menuclosedtex = BitmapFactory.decodeResource(r, R.drawable.menu_closed);
		helptex = BitmapFactory.decodeResource(r, R.drawable.help);
		closetex = BitmapFactory.decodeResource(r, R.drawable.close);
		
		Bitmap optionsTexture = BitmapFactory.decodeResource(r, R.drawable.options);
		Bitmap menuTexture = BitmapFactory.decodeResource(r, R.drawable.menu);
		Bitmap colorsTexture = BitmapFactory.decodeResource(r, R.drawable.colors);
		Bitmap[] helpTextures = { BitmapFactory.decodeResource(r, R.drawable.help0), BitmapFactory.decodeResource(r, R.drawable.help1), 
				BitmapFactory.decodeResource(r, R.drawable.help2), BitmapFactory.decodeResource(r, R.drawable.help3), 
				BitmapFactory.decodeResource(r, R.drawable.help4), BitmapFactory.decodeResource(r, R.drawable.help5), 
				BitmapFactory.decodeResource(r, R.drawable.help6), BitmapFactory.decodeResource(r, R.drawable.help7), 
				BitmapFactory.decodeResource(r, R.drawable.help8) }; 
		
		int mw = w - 128;
		int mh = Math.min(384, h - 64);
		int my = (h - mh) / 2;
		int mx = (w - mw) / 2;
		int height = (int) Math.round(h / 2.6);
		
		menu = new Menu (new MainMenuListener(), (w - (w - 200)) / 2, h - height, w - 200, height, 5, 2, menuTexture);
		colors = new Menu(new ColorMenuListener(), mx, my, mw, mh, 5, 4, colorsTexture);
		options = new Menu(new OptionsMenuListener(), mx, my, mw, mh, 5, 4, optionsTexture);
		help = new HelpMenu(mx, my, mw, mh, helpTextures);
		
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
		if(marble != null)
			marble.clear();
	}

	public void loadBackground(InputStream inputStream) throws Exception {
		marble.load(BitmapScaler.decodeFile(inputStream, Math.min(m_width, m_height)));
	}
	
	public boolean isTouch() {
		return touch;
	}

	public void setTouch(boolean b) {
		marble.stop();
		touch = b;
	}
	
	public void showhideMenu() {
		if (menu.isVisible() == true) {
			menu.setVisible(false);
		} else {
			menu.setVisible(true);
		}
		MarblePaint.getContext().vibrate();
	}
	
	public static void save(String filename) {
		marble.save(filename);
	}
}
