package com.codonforge.marblepaint;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MarblePaint extends Activity implements SensorEventListener {
	private static MarblePaint context;

	private GLSurfaceView glSurface;
	private GLRenderer glRenderer;

	private SensorManager sensorManager;
	private Sensor accelerometer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		setContentView(R.layout.main);
		
		glRenderer = new GLRenderer();
		glSurface = (GLSurfaceView) this.findViewById(R.id.glSurfaceView);
		glSurface.setRenderer(glRenderer);
		
		AdView adView = (AdView) this.findViewById(R.id.ads);
		adView.loadAd(new AdRequest());
	}

	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		float[] v = event.values;
		glRenderer.accelerate(v[0], v[1], v[2]);
	}

	public static final MarblePaint getContext() {
		return context;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
	
	public boolean onTouchEvent(MotionEvent e) {
		if(e.getAction() == MotionEvent.ACTION_DOWN) {
			if(glRenderer.getSplash()) {
				glRenderer.setSplash(false);
				return true;
			} else {
				return glRenderer.handleTap(e.getX(), e.getY());
			}
		}
		return false;
	}
}