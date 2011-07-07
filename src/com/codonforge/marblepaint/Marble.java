package com.codonforge.marblepaint;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

public class Marble {
	private static final int radius = 25;

	private float x;
	private float y;

	private float xAccel;
	private float yAccel;

	private float lastStoredX;
	private float lastStoredY;

	private float linewidth;

	private boolean rainbowMode;
	private boolean makeTrail;
	
	private Bitmap m_marbleImage;

	private Bitmap m_drawBuffer;
	private Canvas m_drawCanvas;
	
	private Paint m_linePaint;
	private Paint m_ballPaint;
	
	private float[] colorValue;

	public Marble(int x, int y, int sw, int sh) {
		this.x = this.lastStoredX = x;
		this.y = this.lastStoredY = y;
		
		m_marbleImage = BitmapFactory.decodeResource(MarblePaint.getContext().getResources(), R.drawable.marble);
		m_marbleImage = Bitmap.createScaledBitmap(m_marbleImage, radius * 2, radius * 2, false);

		m_drawBuffer = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
		m_drawCanvas = new Canvas(m_drawBuffer);

		clear();
		
		linewidth = 6.0f;
		colorValue = new float[] { 0.0f, 1.0f, 1.0f };

		m_linePaint = new Paint();
		m_linePaint.setARGB(0xFF, 0x00, 0x00, 0x00);
		m_linePaint.setStrokeWidth(linewidth);
		m_linePaint.setStrokeCap(Cap.ROUND);
		m_linePaint.setStrokeJoin(Join.ROUND);
		
		m_ballPaint = new Paint();
		m_ballPaint.setARGB(0xFF, 0x00, 0x00, 0x00);
		m_ballPaint.setColorFilter(new LightingColorFilter(0, 0));
	}

	public void render(Canvas c) {
		if(rainbowMode)
			updateRainbow();
		
		c.drawBitmap(m_drawBuffer, 0, 0, null);
		c.drawBitmap(m_marbleImage, x - radius, y - radius, m_ballPaint);
	}

	public void accelerate(float x, float y) {
		if(!makeTrail) return;
		xAccel += x * .7f - .5;
		yAccel += y * .7f - .5;
		// NEW PHYSICS :D
	}

	public void setPos(float x, float y, int w, int h) {
		if(x <= radius) x = radius;
		if(y <= radius) y = radius;
		if(x >= w - radius) x = w - radius;
		if(y >= h - radius) y = h - radius;
		this.x = x;
		this.y = y;
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

		if(makeTrail)
			m_drawCanvas.drawLine(lastStoredX, lastStoredY, x, y, m_linePaint);

		lastStoredX = x;
		lastStoredY = y;
	}

	public void setColor(int r, int g, int b) {
		rainbowMode = false;
		int col = (0xFF << 24) | (r << 16) | (g << 8) | b;
		m_linePaint.setColor(col);
		
		m_ballPaint.setColorFilter(new LightingColorFilter(col, 0));
	}

	public void clear() {
		m_drawBuffer.eraseColor(0xFFFFFFFF);
	}

	public void increaseSize() {
		if (linewidth < 10.0f)
			linewidth += 1.0f;
		m_linePaint.setStrokeWidth(linewidth);
	}

	public void decreaseSize() {
		if (linewidth > 2.0f)
			linewidth -= 1.0f;
		m_linePaint.setStrokeWidth(linewidth);
	}

	private void updateRainbow() {
		int c = Color.HSVToColor(colorValue);
		m_linePaint.setColor(c);
		m_ballPaint.setColorFilter(new LightingColorFilter(c, 0));
		
		colorValue[0] = (colorValue[0] + 1.0f) % 360.0f;
	}

	public void setRainbow(boolean b) {
		rainbowMode = b;
	}
	
	public void destroy() {
		m_drawBuffer.recycle();
	}
	
	public float getRadius() {
		return radius;
	}
	
	public void startDrag(float x2, float y2, int w, int h) {
		setPos(x2, y2, w, h);
		lastStoredX = x2;
		lastStoredY = y2;
	}
	
	public void setMakeTrail(boolean b) {
		makeTrail = b;
	}
	
	public boolean isMakingTrail() {
		return makeTrail;
	}
	
	public void stop() {
		xAccel = yAccel = 0;
	}
	
	public void load(Bitmap bitmap) {
		if(bitmap == null)
			return;
		int x = m_drawBuffer.getWidth() / 2 - bitmap.getWidth() / 2;
		int y = m_drawBuffer.getHeight() / 2 - bitmap.getHeight() / 2;
		m_drawCanvas.drawBitmap(bitmap, x, y, null);
	}
	
	public void save(String name) {
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/MarblePaint/";
			File dir = new File(path);
			
			if(!dir.exists() && !dir.mkdirs()) {
				MarblePaint.getContext().alert("Error creating MarblePaint pictures folder");
				return;
			}
			if (!name.endsWith(".png"))
				name += ".png";
			FileOutputStream out = new FileOutputStream(new File(dir, name));
			m_drawBuffer.compress(Bitmap.CompressFormat.PNG, 100, out);
			
			MarblePaint.getContext().alert("Saved to " + path + name);
			
			MediaConnectionClient client = new MediaConnectionClient(path + name);
			MediaScannerConnection mc = new MediaScannerConnection(MarblePaint.getContext(), client);
			client.setScanner(mc);
			
			mc.connect();
		} catch (Exception e) {
			MarblePaint.getContext().alert(e.toString());
			e.printStackTrace();
		}
	}
	
	static class MediaConnectionClient implements MediaScannerConnection.MediaScannerConnectionClient {
		private String filename;
		private MediaScannerConnection scanner;
		
		public MediaConnectionClient(String fn) {
			filename = fn;
		}
		
		private void setScanner(MediaScannerConnection con) {
			scanner = con;
		}
		
		public void onMediaScannerConnected() {
			scanner.scanFile(filename, null);
		}
		
		public void onScanCompleted(String path, Uri uri) {
			scanner.disconnect();
		}
	}
}
