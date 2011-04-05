package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

/**
 * Loads images from PNG/JPG/whatever files for use as OpenGL textures.
 * 
 * @author Matt
 * 
 */
public class Texture {
	public static int loadTexture(Context ctx, int id) {
		Resources res = ctx.getResources();
		Bitmap bmp = BitmapFactory.decodeResource(res, id);
		int w = bmp.getWidth(), h = bmp.getHeight();
		if(w != h || (w & (w - 1)) != 0 || (h & (h - 1)) != 0) {
			int k = nextPow2(w < h ? w : h);
			bmp = Bitmap.createScaledBitmap(bmp, k, k, false);
		}
		return loadTexture(bmp);
	}
	
	private static int nextPow2(int k) {
		k -= 1;
		k |= k >> 1;
		k |= k >> 2;
		k |= k >> 4;
		k |= k >> 8;
		k |= k >> 16;
		return k + 1;
	}

	public static int loadTexture(Bitmap bitmap) {
		int[] buf = new int[1];
		glGenTextures(1, buf, 0);

		int id = buf[0];

		glBindTexture(GL_TEXTURE_2D, id);
		glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		
		GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
		return id;
	}
}
