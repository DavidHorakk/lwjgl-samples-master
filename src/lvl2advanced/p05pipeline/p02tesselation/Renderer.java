package lvl2advanced.p05pipeline.p02tesselation;


import static lwjglutils.ShaderUtils.GEOMETRY_SHADER_SUPPORT_VERSION;
import static lwjglutils.ShaderUtils.TESSELATION_SUPPORT_VERSION;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL40.GL_MAX_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40.GL_PATCHES;
import static org.lwjgl.opengl.GL40.GL_PATCH_VERTICES;
import static org.lwjgl.opengl.GL40.glPatchParameteri;


import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;


/**
* 
* @author PGRF FIM UHK
* @version 2.0
* @since 2019-09-02
*/
public class Renderer extends AbstractRenderer{

	OGLBuffers buffers;
	
	int shaderProgram, locTime;

	float time = 1;
	
	int demoType = 0;
	boolean demoTypeChanged = true;
	boolean stop = false;
	
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_S:
					stop = !stop;
					break;
				case GLFW_KEY_M:
					demoType = (demoType+1) % 4;
					demoTypeChanged = true;
					break;
				}
			}
		}
	};
    
    private GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
    	@Override
    	public void invoke(long window, int w, int h) {
            if (w > 0 && h > 0 && 
            		(w != width || h != height)) {
            	width = w;
            	height = h;
            	if (textRenderer != null)
            		textRenderer.resize(width, height);

            }
        }
    };
    
  	@Override
	public GLFWKeyCallback getKeyCallback() {
		return keyCallback;
	}

	@Override
	public GLFWWindowSizeCallback getWsCallback() {
		return wsCallback;
	}

	@Override
	public GLFWMouseButtonCallback getMouseCallback() {
		return null;
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return null;
	}

	@Override
	public GLFWScrollCallback getScrollCallback() {
		return null;
	}

	void createBuffers() {
		int[] indexBufferData = { 0,1,2};
		
		float[] vertexBufferDataPos = {
			-0.8f, -0.9f, 
			-0.8f, 0.6F,
			0.6f, 0.8f, 
		};
		
		float[] vertexBufferDataCol = {
			0, 1, 0, 
			1, 0, 0,
			1, 1, 0,
		};
		
		OGLBuffers.Attrib[] attributesPos = {
				new OGLBuffers.Attrib("inPosition", 2),
		};
		OGLBuffers.Attrib[] attributesCol = {
				new OGLBuffers.Attrib("inColor", 3)
		};
		buffers = new OGLBuffers(vertexBufferDataPos, attributesPos,
				indexBufferData);
		buffers.addVertexBuffer(vertexBufferDataCol, attributesCol);
		
		System.out.println(buffers);
	}

	
	private int init(int demoType){
		String extensions = OGLUtils.getExtensions();
		int newShaderProgram = 0;
		switch (demoType){
		case 0: //only VS a FS
			System.out.println("Pipeline: VS + FS");
			if (extensions.indexOf("GL_ARB_enhanced_layouts") == -1)
				newShaderProgram = ShaderUtils.loadProgram( 
						"/lvl2advanced/p05pipeline/p02tesselation/tessel_OlderSM_WithoutGS",
						"/lvl2advanced/p05pipeline/p02tesselation/tessel_OlderSM_WithoutGS",
						null,null,null,null); 
			else
				newShaderProgram = ShaderUtils.loadProgram( 
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						null,null,null,null); 
			break;
		case 1: // only VS, FS and GS
			System.out.println("Pipeline: VS + GS + FS");
			if (OGLUtils.getVersionGLSL() >= GEOMETRY_SHADER_SUPPORT_VERSION) {
				if (extensions.indexOf("GL_ARB_enhanced_layouts") == -1)
					newShaderProgram = ShaderUtils.loadProgram( 
							"/lvl2advanced/p05pipeline/p02tesselation/tessel_OlderSM_OnlyGS",
							"/lvl2advanced/p05pipeline/p02tesselation/tessel_OlderSM_OnlyGS", 
							"/lvl2advanced/p05pipeline/p02tesselation/tessel_OlderSM_OnlyGS", 
							null, null,null); 
				else 
					newShaderProgram = ShaderUtils.loadProgram( 
							"/lvl2advanced/p05pipeline/p02tesselation/tessel", 
							"/lvl2advanced/p05pipeline/p02tesselation/tessel",
							"/lvl2advanced/p05pipeline/p02tesselation/tessel",
							null, null, null); 
			} else
				System.out.println("Geometry shader is not supported");
			break;
		case 2: //VS, FS and tess
			System.out.println("Pipeline: VS + tess + FS");
			if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
				newShaderProgram = ShaderUtils.loadProgram( 
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						null,
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						"/lvl2advanced/p05pipeline/p02tesselation/tessel",
						null); 
				}	
			else
				System.out.println("Tesselation is not supported");
			break;
		default: //VS, FS, GS and tess
			System.out.println("Pipeline: VS + tess + GS + FS");
			if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
				newShaderProgram = ShaderUtils.loadProgram( 
						"/lvl2advanced/p05pipeline/p02tesselation/tessel");
			}	
			else
				System.out.println("Tesselation is not supported");
		}
		
		return newShaderProgram;	
		
	}
	
	@Override
	public void init() {
		if (OGLUtils.getVersionGLSL() >= TESSELATION_SUPPORT_VERSION) {
			int[] maxPatchVertices = new int[1];
			glGetIntegerv(GL_MAX_PATCH_VERTICES, maxPatchVertices);
			System.out.println("Max supported patch vertices "	+ maxPatchVertices[0]);
		}
		
		createBuffers();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (demoTypeChanged) {
			int oldShaderProgram = shaderProgram;
			shaderProgram = init(demoType);
			if (shaderProgram>0) {
				glDeleteProgram(oldShaderProgram);
			} else {
				shaderProgram = oldShaderProgram;
			}
			locTime = glGetUniformLocation(shaderProgram, "time");
			demoTypeChanged = false;
		}
		
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		glClearColor(0.2f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		if (!stop) time *= 1.01;
		time =  time % 100;
		
		//System.out.println(time);
		
		glUseProgram(shaderProgram); 
		glUniform1f(locTime, time); 
		
		switch (demoType){
		case 1: //points VS+GS+FS
			if (OGLUtils.getVersionGLSL() >= 300){
				buffers.draw(GL_TRIANGLES, shaderProgram);
			}	
			break;		
		case 2: //tessellation VS+TCS+TES+FS
		case 3: //points VS+TCS+TES+GS+FS
			if (OGLUtils.getVersionGLSL() >= 400){
				glPatchParameteri(GL_PATCH_VERTICES, 3);
				buffers.draw(GL_PATCHES, shaderProgram);
			}
			break;		
		default: //triangle VS+FS
			buffers.draw(GL_TRIANGLES, shaderProgram); 
			break;		
		}
		
		String text = new String(this.getClass().getName() + ": [I]nit, [M]ode");
		text += String.format("%5.1f", time);
		
		if (stop) text += " [S]tart";
		else text += " [S]top";
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}