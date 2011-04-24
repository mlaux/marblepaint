package com.codonforge.marblepaint;

import java.io.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapScaler {
	/**
	 * So bad, but it works lol
	 */
	public static Bitmap decodeFile(InputStream in, int w) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int k;
		byte[] b = new byte[1024];
		while ((k = in.read(b, 0, 1024)) != -1)
			baos.write(b, 0, k);

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, o);

		int scale = 1;
		if (o.outHeight > w || o.outWidth > w) {
			scale = (int) Math.pow(2.0d,
					Math.round(Math.log(w / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, o2);
	}

}
