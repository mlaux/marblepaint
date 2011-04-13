package com.codonforge.marblepaint;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MarblePaint extends Activity implements SensorEventListener {
	private static final String VERSION = "1.2b";
	
	private static MarblePaint context;

	private SurfaceView surface;
	private Renderer renderer;

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
		
		renderer = new Renderer();
		surface = (SurfaceView) this.findViewById(R.id.surfaceView);
		surface.getHolder().addCallback(renderer);
		
		AdView adView = (AdView) this.findViewById(R.id.ads);
		adView.loadAd(new AdRequest());
		adView.setVisibility(View.INVISIBLE);
		
		//Notifications
		final TextView aboutMessage = new TextView(this);
		final TextView helpMessage = new TextView(this);
		
		final SpannableString a = new SpannableString("Created by Codonforge\n" +
				"Programming by Matt Laux\n" +
				"Art and Physics by Jeff Bell\n" +
				"http://www.codonforge.com");
		Linkify.addLinks(a, Linkify.WEB_URLS);
		aboutMessage.setText(a);
		aboutMessage.setMovementMethod(LinkMovementMethod.getInstance());
		
		String h = "Tilt your phone to move the marble.\n" +
				"Tap the wrench for settings.\n" +
				"Tap the three marbles for marble options.\n" +
				"Chose the touch option to drag the marble.";
		helpMessage.setText(h);

		about = makeDialog(aboutMessage, "About MarblePaint (v" + VERSION + ")");
		
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
		if(!sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
			// TODO Inform the user that the accelerometer isn't supported and
			// make touch the default for them
		}
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	public void onSensorChanged(SensorEvent event) {
		float[] v = event.values;
		float x = v[1], y = v[0], z = v[2];
		if(x * x + y * y + z * z > 400) {
			renderer.requestClear();
		} else renderer.accelerate(x, y, z);
	}

	public static final MarblePaint getContext() {
		return context;
	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}
	
	public boolean onTouchEvent(MotionEvent e) {
		if(e.getAction() == MotionEvent.ACTION_DOWN) {
			if(renderer.getSplash()) {
				renderer.setSplash(false);
				return true;
			} else {
				return renderer.handleTap(e.getX(), e.getY());
			}
		} else if(e.getAction() == MotionEvent.ACTION_MOVE) {
			return renderer.handleDrag(e.getX(), e.getY());
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