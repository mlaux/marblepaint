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
import android.view.View;
import android.view.Window;
import android.widget.Button;

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
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		setContentView(R.layout.main);

		glRenderer = new GLRenderer();
		glSurface = (GLSurfaceView) findViewById(R.id.glSurfaceView);
		glSurface.setRenderer(glRenderer);

		// Set color button variables
		Button red = (Button) findViewById(R.id.buttonRed);
		Button blue = (Button) findViewById(R.id.buttonBlue);
		Button green = (Button) findViewById(R.id.buttonGreen);
		Button yellow = (Button) findViewById(R.id.buttonYellow);
		Button orange = (Button) findViewById(R.id.buttonOrange);
		Button purple = (Button) findViewById(R.id.buttonPurple);
		Button black = (Button) findViewById(R.id.buttonBlack);

		// Add color button listeners
		addColorListener(1.0f, 0.0f, 0.0f, red);
		addColorListener(0.0f, 0.0f, 1.0f, blue);
		addColorListener(0.0f, 1.0f, 0.0f, green);
		addColorListener(1.0f, 1.0f, 0.0f, yellow);
		addColorListener(1.0f, 0.5f, 0.0f, orange);
		addColorListener(0.5f, 0.0f, 1.0f, purple);
		addColorListener(0.0f, 0.0f, 0.0f, black);
		
		// Clear button
		Button clear = (Button) findViewById(R.id.buttonClear);
		clear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				glRenderer.resetLines();
			}
		});
		
		// Size buttons
		Button sizeInc = (Button) findViewById(R.id.buttonSizeInc);
		sizeInc.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				glRenderer.increaseSize();
			}
		});
		Button sizeDec = (Button) findViewById(R.id.buttonSizeDec);
		sizeDec.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				glRenderer.decreaseSize();
			}
		});
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
		glRenderer.accelerate(v[0], v[1], v[2]);
	}

	public static final MarblePaint getContext() {
		return context;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	private void addColorListener(final float r, final float g, final float b, Button button) {
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				glRenderer.setColorValue(r, g, b);
			}
		});
	}
}