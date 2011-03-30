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
		return loadTexture(BitmapFactory.decodeResource(res, id));
	}

	public static int loadTexture(Bitmap bitmap) {
		int[] buf = new int[1];
		glGenTextures(1, buf, 0);

		int id = buf[0];

		glBindTexture(GL_TEXTURE_2D, id);

		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameterx(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

		return id;
	}
}
