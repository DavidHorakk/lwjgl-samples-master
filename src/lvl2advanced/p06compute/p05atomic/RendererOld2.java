package lvl2advanced.p06compute.p05atomic;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.GL_SAMPLES_PASSED;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL15.glGetQueryObjectiv;
import static org.lwjgl.opengl.GL30.GL_PRIMITIVES_GENERATED;
import static org.lwjgl.opengl.GL42.*;

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
public class RendererOld2 extends AbstractRenderer{

	OGLBuffers buffers;
	
	int shaderProgram, locMode;

	int ac_buffer;
	int[] atomicData;
	
	int[] query, result;
	int numPrimitives, numSamples, locPrimitives, locSamples;
	
	boolean stop = false;
	int mode = 0;
	int tex_w = 256, tex_h = 256;
		
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_M:
					mode = (++mode) % 4;
					break;
				case GLFW_KEY_S:
					stop = !stop;
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
		
		int[] numAtomic = new int[1];
		
		glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_COMBINED_ATOMIC_COUNTERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_VERTEX_ATOMIC_COUNTERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_GEOMETRY_ATOMIC_COUNTERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTERS, numAtomic);
		System.out.println("GL_MAX_FRAGMENT_ATOMIC_COUNTERS  = " + numAtomic[0]);
		
		glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);
		glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS, numAtomic);
		System.out.println("GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS  = " + numAtomic[0]);

		
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p06compute/p05atomic/drawImageAtomic");
		
		
		createBuffer();

		locMode = glGetUniformLocation(shaderProgram, "mode");
		locPrimitives = glGetUniformLocation(shaderProgram, "numPrimitives");
		locSamples = glGetUniformLocation(shaderProgram, "numSamples");
		
		atomicData = new int[3];
		ac_buffer = glGenBuffers();
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
		glBufferData( GL_ATOMIC_COUNTER_BUFFER, atomicData, GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);		
		//initialization
		/*atomicData = new int[1];
		glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, ac_buffer);
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
		ByteBuffer dataBuffer =	glMapBufferRange(GL_ATOMIC_COUNTER_BUFFER, 0, atomicData.length,
				                                        GL_MAP_WRITE_BIT | 
				                                        GL_MAP_INVALIDATE_BUFFER_BIT | 
				                                        GL_MAP_UNSYNCHRONIZED_BIT);
		dataBuffer.rewind();		
		dataBuffer.put((byte)0);
		glUnmapBuffer(GL_ATOMIC_COUNTER_BUFFER);
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0); 
		*/
		
		query = new int[2];
		textRenderer = new OGLTextRenderer(width, height);
		
		glGenQueries(query);
		result = new int[1];
		
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (!stop){
		//draw result texture by shader program
		glUseProgram(shaderProgram); 
				
		// Query how many elements were drawn
		glBeginQuery(GL_PRIMITIVES_GENERATED, query[0]);

		// Query how samples (pixels) were rasterized
		glBeginQuery(GL_SAMPLES_PASSED, query[1]);
		
		
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
				
		
		glUniform1i(locMode, mode%4); 
		glUniform1i(locPrimitives, numPrimitives); 
		glUniform1i(locSamples, numSamples); 
		
		
		atomicData = new int[3];
		glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, ac_buffer);
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
		glBufferData( GL_ATOMIC_COUNTER_BUFFER, atomicData, GL_DYNAMIC_DRAW);
		
		// draw
		
		buffers.draw(GL_TRIANGLES, shaderProgram);
		
		glEndQuery(GL_PRIMITIVES_GENERATED);
		glGetQueryObjectiv(query[0], GL_QUERY_RESULT, result);

		numPrimitives = result[0];
		
		glEndQuery(GL_SAMPLES_PASSED);
		glGetQueryObjectiv(query[1], GL_QUERY_RESULT, result);

		numSamples = result[0];
		
		}
		String text = new String(this.getClass().getName() + " [s]top, [m]ode: " + mode);
		
		text += ", " + "Primitives " + numPrimitives;
		text += ", " + "Samples " + numSamples;
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}