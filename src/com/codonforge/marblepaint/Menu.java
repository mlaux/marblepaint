package com.codonforge.marblepaint;

public class Menu {
	private static final int ITEMS_PER_ROW = 4;
	private static final int ITEMS_PER_COL = 3;
	
	private Marble marble;
	
	private int x;
	private int y;
	
	private int width;
	private int height;
	
	private int texture;
	
	public Menu(Marble m, int _x, int _y, int w, int h, int tid) {
		marble = m;
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
		
		action((row * ITEMS_PER_ROW) + col);
		return true;
	}
	
	private void action(int id) {
		switch(id) {
			case 0: marble.setColor(0.0f, 0.0f, 0.0f); break; // black
			case 1: marble.setColor(1.0f, 0.0f, 0.0f); break; // red
			case 2: marble.setColor(0.0f, 1.0f, 0.0f); break; // green
			case 3: marble.setColor(0.0f, 0.0f, 1.0f); break; // blue
			case 4: marble.setColor(1.0f, 1.0f, 0.0f); break; // yellow
			case 5: marble.setColor(1.0f, 0.5f, 0.0f); break; // orange
			case 6: marble.setColor(0.5f, 0.0f, 1.0f); break; // purple
			case 7: marble.setColor(marble.getRainbowColor()[0],marble.getRainbowColor()[1],marble.getRainbowColor()[2]); break; // rainbow
			case 8: marble.increaseSize(); break;
			case 9: marble.decreaseSize(); break;
			case 10: marble.clear(); break;
			case 11: MarblePaint.getContext().finish();
		}
	}
}
