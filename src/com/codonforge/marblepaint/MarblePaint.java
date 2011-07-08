package com.codonforge.marblepaint;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MarblePaint extends Activity implements SensorEventListener {
	public static final String VERSION = "2.1";
	
	private static MarblePaint context;

	private SurfaceView surface;
	private Renderer renderer;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	private	AlertDialog about;
	private	AlertDialog saveload;
	
	private EditText input;
	
	private boolean touchState;
	
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
				"http://www.codonforge.com\n" + 
				"http://www.facebook.com/codonforge");
		Linkify.addLinks(a, Linkify.WEB_URLS);
		aboutMessage.setText(a);
		aboutMessage.setMovementMethod(LinkMovementMethod.getInstance());
		
		String h = "Save your creation or\n" + "load an image to paint on!";
		helpMessage.setText(h);

		about = makeDialog(aboutMessage, "About MarblePaint (v" + VERSION + ")");
		
		saveload = makeSaveLoad(helpMessage, "Save/Load");
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
	
	private AlertDialog makeSaveLoad(View text, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setView(text);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				makeInput("Enter a name to save file as.", "Save as...", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String filename = MarblePaint.getContext().getInput().getText().toString();
						Renderer.save(filename);
					}
				});
			}
		});
		builder.setNegativeButton("Load", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				showGallery();
			}
		});
		return builder.create();
	}

	protected void onResume() {
		super.onResume();
		if(!sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)) {
			renderer.setTouch(true);
		}
	}

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}	
	
	public void makeInput(String text, String title, OnClickListener click) {
		input = new EditText(this);
		AlertDialog dialog;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(text);
		
		builder.setView(input);

		builder.setPositiveButton("Ok", click);

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg, int whichButton) {
				renderer.setTouch(touchState);
				dlg.dismiss();
			}
		});

		dialog = builder.create();
		touchState = renderer.isTouch();
		renderer.setTouch(true);
		dialog.show();
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
			return renderer.handleTap(e.getX(), e.getY());
		} else if(e.getAction() == MotionEvent.ACTION_MOVE) {
			return renderer.handleDrag(e.getX(), e.getY());
		} else if(e.getAction() == MotionEvent.ACTION_UP) {
			return renderer.handleRelease(e.getX(), e.getY());
		}
		return false;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        renderer.showhideMenu();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	
	public void alert(String text) { 
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
	
	public void showAbout() {
		about.show();
	}
	public void showSaveLoad() {
		saveload.show();
	}
	
	public EditText getInput() {
		return input;
	}
	
	public void showGallery() {
		startActivityForResult(new Intent(Intent.ACTION_PICK, 
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), 0xCAFE);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0xCAFE) {
			if (resultCode == Activity.RESULT_OK) {
				Uri selectedImage = data.getData();
				try {
					InputStream in = getContentResolver().openInputStream(selectedImage);
					renderer.loadBackground(in);
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void vibrate() {
		Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(v != null)
			v.vibrate(100);
	}
}