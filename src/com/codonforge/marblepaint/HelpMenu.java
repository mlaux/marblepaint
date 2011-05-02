package com.codonforge.marblepaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class HelpMenu {
	private static final int ITEMS_PER_ROW = 5;
	private static final int ITEMS_PER_COL = 4;

	private MenuListener listener;

	private int x;
	private int y;

	private int width;
	private int height;
	
	private int totalwidth;
	private int totalheight;

	private Rect area;

	private Bitmap[] pages;
	private boolean visible;

	private int pageNum = 0;

	public HelpMenu(MenuListener ml, int _x, int _y, int w, int h, int tw, int th, Bitmap[] pids) {
		listener = ml;

		x = _x;
		y = _y;
		width = w;
		height = h;
		totalwidth = tw;
		totalheight = th;
		pages = pids;

		area = new Rect(x, y, x + width, y + height);
	}

	public void render(Canvas c) {
		c.drawBitmap(pages[pageNum], null, area, null);
	}

	public boolean handleClick(int x, int y) {
		if (x > totalwidth - 64 && x < totalheight && y > 0 && y < 64) { 
			setVisible(false);
			return true;
		}
		if (x < this.x || y < this.y || x > this.x + this.width || y > this.y + this.height) return false;

		x -= this.x;
		y -= this.y;

		// Over-parenthesized? probably.
		int row = (int) (((float) y / height) * ITEMS_PER_COL);
		int col = (int) (((float) x / width) * ITEMS_PER_ROW);

		listener.onAction((row * ITEMS_PER_ROW) + col);
		return true;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean b) {
		MarblePaint.getContext().findViewById(R.id.ads).setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		visible = b;
	}

	public void nextPage() {
		pageNum++;
	}

	public void prevPage() {
		pageNum--;
		Log.v("HelpMenu", "Attempted to open previous page");
	}

	public int getPage() {
		return pageNum;
	}
}
