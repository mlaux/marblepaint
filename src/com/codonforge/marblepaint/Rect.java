package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

public class Rect {
	private static FloatBuffer data = Calc.alloc(4 * 2);
	
	public static void render(float x, float y, float w, float h, int tex) {
		glPushMatrix();
		glTranslatef(x, y, 0);
		glScalef(w, h, 0);
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(2, GL_FLOAT, 0, data);
		if(tex != -1) {
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, tex);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, 0, data);
		}
		glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
		if(tex != -1) {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glDisable(GL_TEXTURE_2D);
		}
		glDisableClientState(GL_VERTEX_ARRAY);
		glPopMatrix();
	}
	
	static {
		data.put(new float[] { 0, 0 });
		data.put(new float[] { 0, 1 });
		data.put(new float[] { 1, 1 });
		data.put(new float[] { 1, 0 });
		data.flip();
	}
}
