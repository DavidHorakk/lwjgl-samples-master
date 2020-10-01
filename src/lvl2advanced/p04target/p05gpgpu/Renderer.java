package lvl2advanced.p04target.p05gpgpu;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.util.Random;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLRenderTarget;
import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTextRenderer;
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
	
	int shaderProgram;

	OGLTexture2D dataTexture;
	
	OGLRenderTarget renderTarget;
	OGLRenderTarget renderTarget2;
	
	boolean poprve = true, init = true;
	OGLTexImageFloat dataTexImage = null;
	int dataWidth = 512, dataHeight = 512;
	
	Random random = new Random();

	OGLTexture2D.Viewer textureViewer;
	
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_I:
					init = true;;
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

	void initData() {
		// create array of random data
		dataTexImage = new OGLTexImageFloat(dataWidth, dataHeight, 4);
		for (int i = 0; i < dataHeight; i++){
			for (int j = 0; j < dataWidth; j++) {
				dataTexImage.setPixel(j, i, 0, random.nextFloat()*random.nextFloat());
			}
			dataTexImage.setPixel(i, i, 0, 1.0f);
		}	
		// create texture
		dataTexture = new OGLTexture2D(dataTexImage);
	}

	void createBuffers() {
		// full-screen quad, just NDC positions are needed, texturing
		// coordinates can be calculated from them
		float[] triangleStrip = { 1, -1, 
				1, 1, 
				-1, -1, 
				-1, 1 };

		OGLBuffers.Attrib[] attributesStrip = {
				new OGLBuffers.Attrib("inPosition", 2)};

		buffers = new OGLBuffers(triangleStrip, attributesStrip, null);
		System.out.println(buffers.toString());
	}

	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		createBuffers();
		initData();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p04target/p05gpgpu/gpgpuRoll");
		//shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p04target/p05gpgpu/gpgpuMax");
		
		
		glUseProgram(this.shaderProgram);
		
		glDisable(GL_CULL_FACE); 
		glFrontFace(GL_CCW);
		glDisable(GL_DEPTH_TEST);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		renderTarget = new OGLRenderTarget(dataWidth, dataHeight);
		renderTarget2 = new OGLRenderTarget(dataWidth, dataHeight);

		textureViewer = new OGLTexture2D.Viewer();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glDisable(GL_CULL_FACE); 
		glFrontFace(GL_CCW);
		glDisable(GL_DEPTH_TEST);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
		renderTarget.bind();
		// set the current shader to be used
		glUseProgram(shaderProgram);

		glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer
		
		if (init) {
			init = false;
			initData();
			dataTexture.bind(shaderProgram, "textureID", 0);
		}
		else{
			renderTarget2.getColorTexture().bind(shaderProgram, "textureID", 0);
		}			
		
		buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);
		
		//ziskani textury, neni treba, pouzijeme primo renderTarget 
		//texture = new OGLTexture(gl,renderTarget.getColorTexture().getTexImage(new OGLTexImageByte.Format(4)));
		
		//vysledek predchoziho renderu pouzijeme jako texturu
		renderTarget.getColorTexture().bind(shaderProgram, "textureID", 0);
		
		
		// nastavime vychozi render target - kreslime do obrazovky
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, width, height);

		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT);

		buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

		//prehozeni renderTargetu
		OGLRenderTarget renderTargetHlp = renderTarget2;
		renderTarget2 = renderTarget;
		renderTarget = renderTargetHlp;
		
		//rendrujeme bez shaderu pro zobrazeni textury
		//puvodni textura
		textureViewer.view(dataTexture, -1, -1, 0.5, height / (double) width);
		//nova textura
		textureViewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5, height / (double) width);

		String text = new String(this.getClass().getName() + ": [I]nit");
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}