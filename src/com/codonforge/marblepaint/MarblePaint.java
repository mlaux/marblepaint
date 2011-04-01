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
import android.view.Window;
import android.widget.TextView;

public class MarblePaint extends Activity implements SensorEventListener {
	private static MarblePaint context;

	private GLSurfaceView glSurface;
	private GLRenderer glRenderer;

	private TextView overlayText1;
	private TextView overlayText2;

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
		glSurface = (GLSurfaceView) findViewById(R.id.glSurfaceView);
		glSurface.setRenderer(glRenderer);

		// If we ever need more, I'll convert them to be in an array
		overlayText1 = (TextView) findViewById(R.id.debugLine1);
		overlayText2 = (TextView) findViewById(R.id.debugLine2);
	}

	public void setOverlayText(int line, String s) {
		if (line == 1)
			overlayText1.setText(s);
		else if (line == 2)
			overlayText2.setText(s);
		else
			throw new IllegalArgumentException();
	}

	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		float[] v = event.values;
		setOverlayText(1, "Accelerometer [x: " + v[0] + ", y: " + v[1] + ", z: " + v[2] + "]");
		glRenderer.accelerate(v[0], v[1], v[2]);
	}

	public static final MarblePaint getContext() {
		return context;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
}