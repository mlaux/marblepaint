package com.codonforge.marblepaint;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class HelpMenu extends Menu {
	private Bitmap[] pages;

	private int pageNum = 0;

	public HelpMenu(int _x, int _y, int w, int h, Bitmap[] pids) {
		super(null, _x, _y, w, h, null);
		
		pages = pids;
	}

	public void render(Canvas c) {
		c.drawBitmap(pages[pageNum], null, area, null);
	}

	public boolean handleClick(int x, int y) {
		// Previous button
		if (x > this.x && x < this.x + 80 && y > this.y + this.height - 80
				&& y < this.y + this.height) {
			MarblePaint.getContext().vibrate();
			if(pageNum > 0) {
				prevPage();
				return true;
			}
		}
		
		// Next button
		if (x > this.x + this.width - 80 && x < this.x + this.width
				&& y > this.y + this.height - 80 && y < this.y + this.height) {
			MarblePaint.getContext().vibrate();
			if(pageNum < 7) {
				nextPage();
				return true;
			}
		}
		
		return false;
	}

	public void nextPage() {
		pageNum++;
	}

	public void prevPage() {
		pageNum--;
	}
}
