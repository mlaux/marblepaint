package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class GLRenderer implements GLSurfaceView.Renderer {
	private static final FloatBuffer lightPos = Calc.wrapDirect(0.0f, 0.0f, 2.0f, 1.0f);
	
	private int width;
	private int height;
	
	private int rotation;
	
	public void onDrawFrame(GL10 gl) {
		// Clear the screen
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);  
		glLoadIdentity();
		
		// Set a blueish color
		// The parameters go R, G, B, A, with each being between 0 and 1
		glColor4f(0.0f, 0.5f, 1.0f, 1.0f);
		
		glPushMatrix();
			// Move away from the origin
			glTranslatef(0, 0.0f, -10.0f);
			glPushMatrix();
				// Rotate the light
				glRotatef(rotation, 1, 0, 0);
				glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
			glPopMatrix();
			// Draw the sphere
			GLUT.glutSolidSphere(1.0f, 32, 32);
		glPopMatrix();
		
		// Increment the rotation
		rotation = (rotation + 1) % 360;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		
		glViewport(0, 0, width, height);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f, 100.0f);
		
		glMatrixMode(GL_MODELVIEW);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		glLightfv(GL_LIGHT0, GL_POSITION, lightPos);
		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glEnable(GL_COLOR_MATERIAL);
	}
}
