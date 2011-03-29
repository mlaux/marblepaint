package com.codonforge.marblepaint;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class MarblePaint extends Activity {
	private static MarblePaint context;
	
	private GLSurfaceView glSurface;
	private GLRenderer glRenderer;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = this;
        
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
    	getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
        
        glRenderer = new GLRenderer();
        
        glSurface = new GLSurfaceView(this);
        glSurface.setRenderer(glRenderer);
        
        setContentView(glSurface);
        
    }
    
    public static final MarblePaint getContext() {
    	return context;
    }
}