package com.codonforge.marblepaint;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MarblePaint extends Activity implements SensorEventListener {
	private static MarblePaint context;

	private GLSurfaceView glSurface;
	private GLRenderer glRenderer;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	private	AlertDialog about;
	private	AlertDialog help;
	
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
		
		//Notifications
		final TextView aboutMessage = new TextView(this);
		final TextView helpMessage = new TextView(this);
		
		final SpannableString a = new SpannableString("Created by Codonforge\nProgramming by Matt Laux\nArt and Physics by Jeff Bell\nhttp://www.codonforge.com");
		Linkify.addLinks(a, Linkify.WEB_URLS);
		aboutMessage.setText(a);
		aboutMessage.setMovementMethod(LinkMovementMethod.getInstance());
		
		String h = "Tilt your phone to move the ball\nClick the wrench for settings\nClick the three balls for marble options\nChose the touch option to drag the ball";
		helpMessage.setText(h);

		about = makeDialog(aboutMessage, "About MarblePaint");
		
		help = makeDialog(helpMessage, "Help");
	}
	
	private AlertDialog makeDialog(View text, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setView(text);
		builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		return builder.create();
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
	
	public void alert(String text) { 
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public void showAbout() {
		about.show();
	}
	public void showHelp() {
		help.show();
	}
}