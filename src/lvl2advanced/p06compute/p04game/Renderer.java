package lvl2advanced.p06compute.p04game;


import static lwjglutils.ShaderUtils.COMPUTE_SHADER_SUPPORT_VERSION;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL30.glGetIntegeri_v;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import java.io.IOException;
import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture;
import lwjglutils.OGLTexture2D;
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
	int computeShaderProgram;

	OGLTexture2D texture;
	OGLTexture2D textureOut;
	OGLTexture2D textureIn;
	OGLTexture.Viewer textureViewer;
	
	boolean compute = true;
	boolean continues = true;
	boolean init = true, clear = false;
	int mouseX, mouseY;
	int mouseDown = 0;
		
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_M:
					compute = true;
					break;
				case GLFW_KEY_N:
					continues = !continues;
					break;
				case GLFW_KEY_I:
					init = true;
					break;
				case GLFW_KEY_C:
					clear = true;
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
    
    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
		
    	@Override
		public void invoke(long window, int button, int action, int mods) {
			if (button==GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS){
				mouseDown = 1;
			}
			
			if (button==GLFW_MOUSE_BUTTON_3 && action == GLFW_PRESS){
				mouseDown = 2;
			}
			
			if (mouseDown > 0){
				DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
				DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
				glfwGetCursorPos(window, xBuffer, yBuffer);
				double x = xBuffer.get(0);
				double y = yBuffer.get(0);
				mouseX = (int) x;
				mouseY = (int)y;
			}
			if (action == GLFW_RELEASE){
				mouseDown = 0;
        	}
		}
		
	};
	
	private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
		@Override
        public void invoke(long window, double x, double y) {
			if (mouseDown > 0){
				mouseX = (int) x;
				mouseY = (int) y;
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
		return mbCallback;
	}

	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return cpCallbacknew;
	}

	@Override
	public GLFWScrollCallback getScrollCallback() {
		return null;
	}

	void initTexture(boolean clear) {
		// load test texture
		try {
			texture = new OGLTexture2D("textures/mosaic.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OGLTexImageFloat texImageIn; 
		if (!clear)
			// create image as a copy of loaded texture, must have 4 components
			texImageIn = texture.getTexImage(new OGLTexImageFloat.Format(4));
		else{
			/// create empty image with size same as loaded texture
			texImageIn = new OGLTexImageFloat(texture.getWidth(),
				texture.getHeight(), 1, new OGLTexImageFloat.Format(4));
			// create input texture from the image
			int x = texImageIn.getWidth()/2;
			int y = texImageIn.getHeight()/2;
			//glider shape
			texImageIn.setPixel(x-1, y, 0, 1.0f); //only red color
			texImageIn.setPixel(x+1, y, 0, 1.0f); //only red color
			texImageIn.setPixel(x+1, y+1, 0, 1.0f); //only red color
			texImageIn.setPixel(x, y+1, 0, 1.0f); //only red color
			texImageIn.setPixel(x+1, y-1, 0, 1.0f); //only red color
		
		}
		textureIn = new OGLTexture2D(texImageIn);
		// create empty image with size same as loaded texture
		OGLTexImageFloat texImageOut = new OGLTexImageFloat(texture.getWidth(),
				texture.getHeight(), 1, new OGLTexImageFloat.Format(4));
		// create (empty) output texture from the image
		textureOut = new OGLTexture2D(texImageOut);
	}

	
	@Override
	public void init() {
		if ((OGLUtils.getVersionGLSL() < COMPUTE_SHADER_SUPPORT_VERSION)
				&& (OGLUtils.getExtensions().indexOf("compute_shader") == -1)){
			System.err.println("Compute shader is not supported"); 
			System.exit(0);
		}
		
		OGLUtils.printOGLparameters();
		
		//get limits of work group size per dimension
		int[] val = new int[1];
		for (int dim = 0; dim < 3; dim++) {
			glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, dim, val);
			System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE [" + dim + "] : " + val[0]);
		}
				
		computeShaderProgram = ShaderUtils.loadProgram(null, null, null, null, null, 
				"/lvl2advanced/p06compute/p04game/computeLife"); 
		
		
		
		textureViewer = new OGLTexture2D.Viewer();
		
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (init) {
			init = false;
			initTexture(false);
		}
		if (clear) {
			clear = false;
			initTexture(true);
		}
		
		
		int w = texture.getWidth();
		int h = texture.getHeight();
		
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// recompute? 
		if (compute || continues) {
			compute = false;
			
			glBindImageTexture(0, textureIn.getTextureId(), 0, false, 0, 
					GL_READ_ONLY, GL_RGBA32F);
			glBindImageTexture(1, textureOut.getTextureId(), 0, false, 0, 
					GL_WRITE_ONLY, GL_RGBA32F);

			// first step
			glUseProgram(computeShaderProgram);
			glDispatchCompute(w/16, h/16, 1);

			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);


			//change textures 
			glBindImageTexture(0, textureOut.getTextureId(), 0, false, 0, GL_READ_ONLY,
					GL_RGBA32F);
			glBindImageTexture(1, textureIn.getTextureId(), 0, false, 0, GL_WRITE_ONLY,
					GL_RGBA32F);
			
			//second step
			glUseProgram(computeShaderProgram);
			glDispatchCompute(w/16, h/16, 1);
			glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		}
		
		if (mouseDown>0) { //add new generator
			//System.out.println("[" + mouseX + "," + mouseY + "]");
			// get image as a copy of input texture
			OGLTexImageFloat texImageIn = textureIn.getTexImage(new OGLTexImageFloat.Format(4));
			int x = 2 * mouseX * texImageIn.getWidth() / width;
			int y = 2 * mouseY * texImageIn.getHeight() / height;
			if (x > 0 && x <texImageIn.getWidth()-1 && 
				y > 0 && y <texImageIn.getHeight()-1){
				if (mouseDown==2){//cross shape		
		  		texImageIn.setPixel(x, y, 0, 1.0f); //only red color
				texImageIn.setPixel(x+1, y, 0, 1.0f); //only red color
				texImageIn.setPixel(x, y+1, 0, 1.0f); //only red color
				texImageIn.setPixel(x-1, y, 0, 1.0f); //only red color
				texImageIn.setPixel(x, y-1, 0, 1.0f); //only red color
				}
				if (mouseDown==1){//glider shape
				texImageIn.setPixel(x-1, y, 0, 1.0f); //only red color
				texImageIn.setPixel(x+1, y, 0, 1.0f); //only red color
				texImageIn.setPixel(x+1, y+1, 0, 1.0f); //only red color
				texImageIn.setPixel(x, y+1, 0, 1.0f); //only red color
				texImageIn.setPixel(x+1, y-1, 0, 1.0f); //only red color
				}
			}
			// update input texture from the image
			textureIn.setTexImage(texImageIn);
		}
		
		//draw textures

		//show original texture in right up corner
		textureViewer.view(texture,0,0);
		//show input texture in left up corner
		textureViewer.view(textureIn,-1,0);
		//show output texture in right down corner
		textureViewer.view(textureOut, 0, -1);
		
		
		String text = new String(this.getClass().getName() + ": [LMB] new life, [n] -start/stop, [m] - step, [i] - reset, [c]lear, ESC - exit ");
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}