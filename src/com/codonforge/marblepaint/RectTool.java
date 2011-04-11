package com.codonforge.marblepaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class RectTool {
	private static RectF rect = new RectF();
	private static Paint paint = new Paint();
	
	public static void render(Canvas c, Bitmap b, float x, float y, float w, float h) {
		rect.left = x;
		rect.top = y;
		rect.right = x + w;
		rect.bottom = y + h;
		
		if(b != null) {
			c.drawBitmap(b, null, rect, null);
		} else {
			c.drawRect(rect, paint);
		}
	}
}
