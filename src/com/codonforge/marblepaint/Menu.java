package com.codonforge.marblepaint;

import android.view.View;

public class Menu {
	private static final int ITEMS_PER_ROW = 4;
	private static final int ITEMS_PER_COL = 3;
	
	private MenuListener listener;
	
	private int x;
	private int y;
	
	private int width;
	private int height;
	
	private int texture;
	private boolean visible;
	
	public Menu(MenuListener ml, int _x, int _y, int w, int h, int tid) {
		listener = ml;
		
		x = _x;
		y = _y;
		width = w;
		height = h;
		texture = tid;
	}
	
	public void render() {
		Rect.render(x, y, width, height, texture, false);
	}
	
	public boolean handleClick(int x, int y) {
		if(x < this.x || y < this.y || x > this.x + this.height || y > this.y + this.height)
			return false;
		
		x -= this.x;
		y -= this.y;
		
		// Over-parenthesized? probably.
		int row = (int) (((float) y / height) * ITEMS_PER_COL);
		int col = (int) (((float) x / width)  * ITEMS_PER_ROW);
		
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
}
