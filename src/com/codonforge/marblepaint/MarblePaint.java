package com.codonforge.marblepaint;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class MarblePaint extends Activity implements SensorEventListener {
	private static MarblePaint context;

	private GLSurfaceView glSurface;
	private GLRenderer glRenderer;
	private TextView overlayText;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		setContentView(R.layout.main);

		glRenderer = new GLRenderer();
		glSurface = (GLSurfaceView) findViewById(R.id.surfaceView1);
		glSurface.setRenderer(glRenderer);

		overlayText = (TextView) findViewById(R.id.textView1);
	}

	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_GAME);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		float[] v = event.values;
		overlayText.setText("x: " + v[0] + ", y: " + v[1] + ", z: " + v[2]);
		glRenderer.accelerate(v[0], v[1], v[2]);
	}

	public static final MarblePaint getContext() {
		return context;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
	}
}