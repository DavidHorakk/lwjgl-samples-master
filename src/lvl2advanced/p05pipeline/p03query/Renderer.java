package lvl2advanced.p05pipeline.p03query;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.GL_SAMPLES_PASSED;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glDeleteQueries;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL15.glGetQueryObjectiv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_PRIMITIVES_GENERATED;
import static org.lwjgl.opengl.GL33.GL_TIMESTAMP;
import static org.lwjgl.opengl.GL33.GL_TIME_ELAPSED;
import static org.lwjgl.opengl.GL33.glGetQueryObjecti64v;
import static org.lwjgl.opengl.GL33.glQueryCounter;

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
	
	int shaderProgram;
	int[] query, result;
	long[] resultLong;
	int mode = 0;
		
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_E:
					mode ++;
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
		
	private void createInputBuffer() {
		int[] indexBufferData = { 0, 1, 2, 3 };
	
		float[] vertexBufferDataPos1 = {
				-.5f, -.1f,  0.0f, 1.0f, 0.1f,
				-.3f, .5f,  0.0f, 1.0f, 1.0f,
				.2f, -.4f,  0.0f, 0.5f, 0.5f,
				.3f, .8f,  0.0f, 0.1f, 1.0f, 
					};
			
		OGLBuffers.Attrib[] attributesPos = { 
				new OGLBuffers.Attrib("inPosition", 2),
				new OGLBuffers.Attrib("inColor", 3), };
		buffers = new OGLBuffers(vertexBufferDataPos1, attributesPos, indexBufferData);
	}
	
	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p05pipeline/p03query/feedbackDraw");
		
		createInputBuffer();
		result = new int[4];
		resultLong = new long[1];
		query = new int[4];
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		glGenQueries(query);

		// Query how many elements were drawn
		glBeginQuery(GL_PRIMITIVES_GENERATED, query[0]);

		// Query how samples (pixels) were rasterized
		glBeginQuery(GL_SAMPLES_PASSED, query[1]);
					
		// Query time counter of rendering
		glBeginQuery(GL_TIME_ELAPSED, query[2]);

		glPointSize(5f);
		glUseProgram(shaderProgram);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		String text = new String(this.getClass().getName() );
		
		switch(mode%6){
		case 0: 
			text += ": " + "[E]lements GL_POINTS" ;
			buffers.draw(GL_POINTS, shaderProgram);
			break;
		case 1: 
			text += ": " + "[E]lements GL_LINES" ;
			buffers.draw(GL_LINES, shaderProgram);
			break;
		case 2: 
			text += ": " + "[E]lements GL_LINE_LOOP" ;
			buffers.draw(GL_LINE_LOOP, shaderProgram);
			break;
		case 3: 
			text += ": " + "[E]lements GL_TRIANGLES" ;
			buffers.draw(GL_TRIANGLES, shaderProgram);
			break;
		case 4: 
			text += ": " + "[E]lements GL_TRIANGLE_STRIP" ;
			buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);
			break;
		case 5: 
			text += ": " + "[E]lements NONE" ;
			break;
		}
		
		
		glEndQuery(GL_PRIMITIVES_GENERATED);
		glGetQueryObjectiv(query[0], GL_QUERY_RESULT, result);

		text += ", " + "Primitives " + result[0];
		
		glEndQuery(GL_SAMPLES_PASSED);
		glGetQueryObjectiv(query[1], GL_QUERY_RESULT, result);

		text += ", " + "Samples " + result[0];
		
		glEndQuery(GL_TIME_ELAPSED);
		glGetQueryObjectiv(query[2], GL_QUERY_RESULT, result);

		text += ", " + "Pass time " + String.format("%4.2f ms", result[2]/1e6);		
		
		glQueryCounter(query[3], GL_TIMESTAMP);
		glGetQueryObjecti64v(query[3], GL_QUERY_RESULT, resultLong);

		glDeleteQueries(query);
		
		text += ", " + "Time stamp " +  String.format("%4.1f s", resultLong[0]/1e9);
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}