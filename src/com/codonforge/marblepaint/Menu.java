package com.codonforge.marblepaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class Menu {
	private int itemsPerRow;
	private int itemsPerCol;
	
	protected Rect area;

	protected int x;
	protected int y;
	
	protected int width;
	protected int height;
	
	private MenuListener listener;
	
	private Bitmap texture;
	private boolean visible;
	
	public Menu(MenuListener ml, int _x, int _y, int w, int h, int ipr, int ipc, Bitmap tid) {
		listener = ml;
		
		x = _x;
		y = _y;
		width = w;
		height = h;
		itemsPerRow = ipr;
		itemsPerCol = ipc;

		texture = tid;
		
		area = new Rect(x, y, x + width, y + height);
	}
	
	public Menu(MenuListener ml, int _x, int _y, int w, int h, Bitmap tid) {
		listener = ml;
		
		x = _x;
		y = _y;
		width = w;
		height = h;
		texture = tid;
		
		itemsPerRow = itemsPerCol = 4;
		
		area = new Rect(x, y, x + width, y + height);
	}
	
	/* private static Paint pt = new Paint();
	
	static {
		pt.setStyle(Paint.Style.STROKE);
	} */
	
	public void render(Canvas c) {
		c.drawBitmap(texture, null, area, null);
		
		// Uncomment this to render a box around each menu item
		// to check if the boundaries are right.
		
		/* for(int j = 0; j < itemsPerRow; j++) {
			for(int k = 0; k < itemsPerCol; k++) {
				int w = width / itemsPerRow;
				int h = height / itemsPerCol;
				int dx = x + j * w;
				int dy = y + k * h;
				c.drawRect(dx, dy, dx + w, dy + h, pt);
			}
		} */
	}
	
	public boolean handleClick(int x, int y) {
		if(x < this.x || y < this.y || x > this.x + this.width || y > this.y + this.height)
			return false;
		
		x -= this.x;
		y -= this.y;
		
		// Over-parenthesized? probably.
		int row = (int) (((float) y / height) * itemsPerCol);
		int col = (int) (((float) x / width)  * itemsPerRow);
		
		MarblePaint.getContext().vibrate();
		listener.onAction((row * itemsPerRow) + col);
		return true;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean b) {
		MarblePaint.getContext().findViewById(R.id.ads).setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		visible = b;
	}
}
