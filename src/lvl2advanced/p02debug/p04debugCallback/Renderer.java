package lvl2advanced.p02debug.p04debugCallback;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DONT_CARE;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memByteBuffer;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.IntBuffer;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;

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
	
	float time = 0;
	
	boolean debug1 = true, debug2=true, debug3=true, debug4=true, debug5=true;
	
	@Override
	public GLFWCursorPosCallback getCursorCallback() {
		return null;
	}

	GLDebugMessageCallback debugCallBack = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
	    System.err.println("Error source:" + OGLUtils.getDebugSource(source) + 
	    		" type:" + OGLUtils.getDebugType(type) + 
	    		" id:" + id + 
	    		" severity:" + OGLUtils.getDebugSeverity(severity) + 
	    		" " + memUTF8(memByteBuffer(message, length)) +
	    		" " + userParam);
	});
	
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_1:
					debug1 = !debug1;
					break;
				case GLFW_KEY_2:
					debug2 = !debug2;
					break;
				case GLFW_KEY_3:
					debug3 = !debug3;
					break;
				case GLFW_KEY_4:
					debug4 = !debug4;
					break;
				case GLFW_KEY_5:
					debug5 = !debug5;
					break;
				}
			}
		};
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

	void createBuffers() {
		float[] vertexBufferData = {
			-1, -1, 	0.7f, 0, 0, 
			 1,  0,		0, 0.7f, 0,
			 0,  1,		0, 0, 0.7f 
		};
		int[] indexBufferData = { 0, 1, 2 };

		// vertex binding description, concise version
		OGLBuffers.Attrib[] attributes = {
				new OGLBuffers.Attrib("inPosition", 2), // 2 floats
				new OGLBuffers.Attrib("inColor", 3) // 3 floats
		};
		buffers = new OGLBuffers(vertexBufferData, attributes,
				indexBufferData);
	}

	
	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		OGLUtils.printLWJLparameters();
		OGLUtils.printJAVAparameters();
		
		// Set the clear color
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

		createBuffers();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p02debug/start");
		//sample shader files with many errors - try to find and correct them
		//shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p02debug/startError"); 
		
		
		// internal OpenGL ID of a shader uniform (constant during one draw call
		// - constant value for all processed vertices or pixels) variable
		locTime = glGetUniformLocation(shaderProgram, "time");
		
		textRenderer = new OGLTextRenderer(width, height);
		glDebugMessageCallback(debugCallBack, NULL);
		
	}
	
	@Override
	public void display() {
		
		
		//GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL43.GL_DEBUG_TYPE_ERROR, GL43.GL_DEBUG_SEVERITY_HIGH , (IntBuffer) null, debug1);
		GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_API, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, debug1);
		String text1 = "[1] "+OGLUtils.getDebugSource(GL43.GL_DEBUG_SOURCE_API)+":"+debug1;

		GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, debug2);
		String text2 = "[2] "+OGLUtils.getDebugSource(GL43.GL_DEBUG_SOURCE_API)+":"+debug2;

		GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_SHADER_COMPILER, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, debug3);
		String text3 = "[3] "+OGLUtils.getDebugSource(GL43.GL_DEBUG_SOURCE_API)+":"+debug3;

		GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_THIRD_PARTY, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, debug4);
		String text4 = "[4] "+OGLUtils.getDebugSource(GL43.GL_DEBUG_SOURCE_API)+":"+debug4;

		GL43.glDebugMessageControl(GL43.GL_DEBUG_SOURCE_APPLICATION, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, debug5);
		String text5 = "[5] "+OGLUtils.getDebugSource(GL43.GL_DEBUG_SOURCE_API)+":"+debug5;
		
		//filtering of debug message control 
		//GL_DEBUG_SOURCE_API, GL_DEBUG_SOURCE_WINDOW_SYSTEM_, GL_DEBUG_SOURCE_SHADER_COMPILER, GL_DEBUG_SOURCE_THIRD_PARTY, GL_DEBUG_SOURCE_APPLICATION
		//GL_DEBUG_TYPE_ERROR, GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR, GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR, GL_DEBUG_TYPE_PORTABILITY, GL_DEBUG_TYPE_PERFORMANCE, GL_DEBUG_TYPE_MARKER, GL_DEBUG_TYPE_PUSH_GROUP, GL_DEBUG_TYPE_POP_GROUP
		//GL_DEBUG_SEVERITY_LOW, GL_DEBUG_SEVERITY_MEDIUM, or GL_DEBUG_SEVERITY_HIGH
		//GL43.glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, (IntBuffer) null, true );

		glViewport(0, 0, width, height);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

		// set the current shader to be used, could have been done only once (in
		// init) in this sample (only one shader used)
		
		time += 0.1;
		
		//ERROR - GL_INVALID_OPERATION, no active program 
		glUniform1f(locTime, time); // correct shader must be set before this
		
		//ERROR - GL_INVALID_VALUE, handle does not refer to an object generated by OpenGL 
		glUseProgram(10); 
		
		
		glUseProgram(shaderProgram); 
		
		// bind and draw
		buffers.draw(GL_TRIANGLES, shaderProgram);
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text1);
		textRenderer.addStr2D(3, 40, text2);
		textRenderer.addStr2D(3, 60, text3);
		textRenderer.addStr2D(3, 80, text4);
		textRenderer.addStr2D(3, 100, text5);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
	
	@Override
	public void dispose(){
		if (debugCallBack != null)
            debugCallBack.free(); 
	}
}