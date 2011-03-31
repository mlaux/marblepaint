package com.codonforge.marblepaint;

import static android.opengl.GLES10.*;
import static android.opengl.GLES11.glIsTexture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * This is really messy (the loading especially) but it works
 * Supports loading, transformation, and rendering of 3D models in .obj format.
 * Has support for texture coordinates and vertex normals.
 * 
 * @author Matt
 *
 */
public class Object3D {
	public float baseX;
	public float baseY;
	public float baseZ;
	
	public float rotationX;
	public float rotationY;
	public float rotationZ;
	
	public float scaleX = 1.0f;
	public float scaleY = 1.0f;
	public float scaleZ = 1.0f;
	
	private Context ctx;
	
	private FloatBuffer vertices;
	private FloatBuffer textCoords;
	private FloatBuffer normals;

	private int textureID = -1;
	private boolean hasNormals;
	
	private int gltexturehandle;
	
	public Object3D(Context ctx, int objID, int texID) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(objID)));
		
		List<float[]> verts = new ArrayList<float[]>();
		List<float[]> texverts = new ArrayList<float[]>();
		List<float[]> normverts = new ArrayList<float[]>();
		
		List<Float> allvert = new ArrayList<Float>();
		List<Float> alltext = new ArrayList<Float>();
		List<Float> allnorm = new ArrayList<Float>();
		
		String line;
		while((line = br.readLine()) != null) {
			// Ignore empty lines and comments
			if(line.length() == 0 || line.startsWith("#"))
				continue;
			
			// Replace sequences of more than one space with just one space
			line = line.replaceAll("( )+", " ");
			
			String[] parts = line.split(" ");
			if (parts[0].equals("v")) {
				// Vertex
				verts.add(new float[] { Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]) });
			} else if (parts[0].equals("vt")) {
				// Texture coordinate
				texverts.add(new float[] { Float.parseFloat(parts[1]), 1 - Float.parseFloat(parts[2]), 0.0f });
			} else if(parts[0].equals("vn")) {
				// Normal
				normverts.add(new float[] { Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]) });
			} else if(parts[0].equals("f")) {
				// Face
				for(int k = 1; k < parts.length; k++) {
					// Vertex index/texture coord index/normal index
					String[] v = parts[k].split("/");
					if(v.length > 1 && v[1].length() != 0) {
						// Has texture coordinates in the .obj
						if(texID == -1)
							throw new IllegalArgumentException("obj file has texcoords but no texture specified");
						textureID = texID;
					}
					
					if(v.length > 2) {
						hasNormals = true;
					}
					
					float[] vc = verts.get(Integer.parseInt(v[0]) - 1);
					allvert.add(vc[0]); allvert.add(vc[1]); allvert.add(vc[2]);
					if(textureID != -1) {
						float[] vt = texverts.get(Integer.parseInt(v[1]) - 1);
						alltext.add(vt[0]); alltext.add(vt[1]); alltext.add(vt[2]);
					}
					if(hasNormals) {
						float[] vn = normverts.get(Integer.parseInt(v[2]) - 1);
						allnorm.add(vn[0]); allnorm.add(vn[1]); allnorm.add(vn[2]);
					}
				}
			}
		}
		
		this.ctx = ctx;
		this.vertices = Calc.wrapDirect(allvert);
		this.textCoords = Calc.wrapDirect(alltext);
		this.normals = Calc.wrapDirect(allnorm);
	}
	
	public Object3D setScale(float sf) {
		scaleX = scaleY = scaleZ = sf;
		return this;
	}
	
	public Object3D setScale(float x, float y, float z) {
		scaleX = x;
		scaleY = y;
		scaleZ = z;
		return this;
	}
	
	public Object3D setRotation(float x, float y, float z) {
		rotationX = x;
		rotationY = y;
		rotationZ = z;
		return this;
	}
	
	public Object3D setPosition(float x, float y, float z) {
		baseX = x;
		baseY = y;
		baseZ = z;
		return this;
	}
	
	public void render() {
		glPushMatrix();
		
		glTranslatef(baseX, baseY, baseZ);
		
		glRotatef(rotationZ, 0, 0, 1);
		glRotatef(rotationY, 0, 1, 0);
		glRotatef(rotationX, 1, 0, 0);

		glScalef(scaleX, scaleY, scaleZ);
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, vertices);
		
		if(textureID != -1) {
			if(!glIsTexture(gltexturehandle))
				gltexturehandle = Texture.loadTexture(ctx, textureID);
			
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, gltexturehandle);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(3, GL_FLOAT, 0, textCoords);
		}
		
		if(hasNormals) {
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(GL_FLOAT, 0, normals);
		}
		
		// TODO Triangulate larger polygons instead of assuming triangles
		glDrawArrays(GL_TRIANGLES, 0, vertices.capacity() / 3);
		
		glDisableClientState(GL_VERTEX_ARRAY);
		if(textureID != -1) {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glDisable(GL_TEXTURE_2D);
		}
		if (hasNormals)
			glDisableClientState(GL_NORMAL_ARRAY);
		
		glPopMatrix();
	}
}
