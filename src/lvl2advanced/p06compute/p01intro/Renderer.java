package lvl2advanced.p06compute.p01intro;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL30.glGetIntegeri_v;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
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
	
	int shaderProgram, locMode, computeShaderProgram;

	OGLTexture2D.Viewer textureViewer;
	
	int mode = 0;
	int tex_output;
	int tex_w = 256, tex_h = 256;
		
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_M:
					mode = (++mode) % 3;
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

	private void createBuffer() {
		float[] vertexBufferData = {
				-1, -1, 	0.7f, 0, 0, 	0.0f, 0.0f,
				 1,  0,		0, 0.7f, 0,		0, 1,
				 0,  1,		0, 0, 0.7f, 	1, 0
			};
			int[] indexBufferData = { 0, 1, 2 };

		OGLBuffers.Attrib[] attributes = { 
				new OGLBuffers.Attrib("inPosition", 2),
				new OGLBuffers.Attrib("inColor", 3),  
				new OGLBuffers.Attrib("inTexCoord", 2, 5)  };
		buffers = new OGLBuffers( vertexBufferData, 7, 
				attributes, indexBufferData);
		
		System.out.println(buffers);
	}
	
	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p06compute/p01intro/drawImage");
		computeShaderProgram = ShaderUtils.loadProgram(null, null, null, null, null, 
				"/lvl2advanced/p06compute/p01intro/computeImage"); 
		
		
		createBuffer();

		locMode = glGetUniformLocation(computeShaderProgram, "mode");
	
		textureViewer = new OGLTexture2D.Viewer();

		tex_output = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex_output);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, tex_w,
				tex_h, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		
		//Limits on work group size per dimension
		int[] val = new int[1];
		for (int dim = 0; dim < 3; dim++) {
			glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, dim, val);
			System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE [" + dim + "] : " + val[0]);
		}
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		glBindImageTexture (0, tex_output, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
						
		//fill texture in compute shader
		glUseProgram(computeShaderProgram);
		
		glUniform1i(locMode, mode%3); 
		
		glDispatchCompute(256/32, 256/32, 1);
		
		// make sure writing to image has finished before read
		glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		//show result texture by textureViewer
		textureViewer.view (tex_output, -1, 0, 0.5, 1., -1);
				
		//draw result texture by shader program
		glUseProgram(shaderProgram); 
		
		glBindImageTexture (0, tex_output, 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
		
		// draw
		buffers.draw(GL_TRIANGLES, shaderProgram);
		
		
		String text = new String(this.getClass().getName() + " [m]ode: " + mode);
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}