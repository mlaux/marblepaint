package com.codonforge.marblepaint;

import android.graphics.*;

public class Marble {
	private static final float radius = 25.0f;
	private static final float[] SIN_TBL = new float[256];
	private static final float[] COS_TBL = new float[256];

	private float x;
	private float y;

	private float xAccel;
	private float yAccel;

	private float lastStoredX;
	private float lastStoredY;

	private float linewidth;

	private boolean rainbowMode;

	private Bitmap m_drawBuffer;
	private Canvas m_drawCanvas;
	
	private Paint m_linePaint;
	
	private int r1, g1, b1;
	private int colorpos, counter;

	public Marble(int x, int y, int sw, int sh) {
		this.x = this.lastStoredX = x;
		this.y = this.lastStoredY = y;

		m_drawBuffer = Bitmap.createBitmap(sw, sh, Bitmap.Config.ARGB_8888);
		m_drawCanvas = new Canvas(m_drawBuffer);
		
		linewidth = 4.0f;

		m_linePaint = new Paint();
		m_linePaint.setARGB(0xFF, 0x00, 0x00, 0x00);
		m_linePaint.setStrokeWidth(linewidth);
	}

	public void render(Canvas c) {
		if(rainbowMode)
			updateRainbow();
		
		c.drawBitmap(m_drawBuffer, 0, 0, null);
		c.drawCircle(x, y, radius, m_linePaint);
	}

	public void accelerate(float x, float y, float z) {
		xAccel += ((0.5f * y) - (4.472135955e-1 * ((1.26f * y) * (.1f / .5f))));
		yAccel += ((0.5f * x) - (4.472135955e-1 * ((1.26f * x) * (.1f / .5f))));
		// Acceleration - Coefficient of Rotational Friction * Normal Force *
		// Force of Rolling Resistance
	}

	public void setPos(float x, float y) {
		xAccel = yAccel = 0;
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

		if (Calc.distanceSquared(lastStoredX, lastStoredY, x, y) > 750.0f) {
			m_drawCanvas.drawLine(lastStoredX, lastStoredY, x, y, m_linePaint);

			lastStoredX = x;
			lastStoredY = y;
		}
	}

	public void setColor(int r, int g, int b) {
		rainbowMode = false;
		m_linePaint.setARGB(0xFF, r, g, b);
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
		/*int a = colorpos & 0xff, b = (colorpos + 64) & 0xff, c = (colorpos + 128) & 0xff;
		r1 = (int) (SIN_TBL[a] * 127 + 128);
		g1 = (int) (SIN_TBL[b] * 127 + 128);
		b1 = (int) (SIN_TBL[c] * 127 + 128);
		m_linePaint.setARGB(0xFF, r1, g1, b1);
		
		colorpos++;
		colorpos &= 0xff;
		*/
		
		// red
		if (counter == 0)
			m_linePaint.setARGB(0xFF, 0xFF, 0x00, 0x00);
		// green
		else if (counter == 20)
			m_linePaint.setARGB(0xFF, 0x00, 0xFF, 0x00);
		// blue
		else if (counter == 40)
			m_linePaint.setARGB(0xFF, 0x00, 0x00, 0xFF);
		// yellow
		else if (counter == 60)
			m_linePaint.setARGB(0xFF, 0xFF, 0xFF, 0x00);
		// orange
		else if (counter == 80)
			m_linePaint.setARGB(0xFF, 0xFF, 0x7F, 0x00);
		// purple
		else if (counter == 100)
			m_linePaint.setARGB(0xFF, 0x7F, 0x00, 0xFF);

		counter = (counter + 1) % 120;
	}

	public void setRainbow(boolean b) {
		rainbowMode = b;
	}
	
	public void destroy() {
		m_drawBuffer.recycle();
	}
	
	static {
		float dt = (float) Math.PI * 2.0f / 256.0f;
		for(int k = 0; k < 256; k++) {
			SIN_TBL[k] = (float) Math.sin(k * dt);
			COS_TBL[k] = (float) Math.cos(k * dt);
		}
	}
}
