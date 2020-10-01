package lvl2advanced.p06compute.p05atomic;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;


/**
* 
* @author PGRF FIM UHK
* @version 2.0
* @since 2019-10-21
*/
public class RendererOld extends AbstractRenderer{

	double ox, oy;
	boolean mouseButton1 = false;
	
	int computeShaderProgram;
	int locOffset;
	int[] locBuffer = new int[2];
    	
	//size = numberOfItems * [key(float) + paddingKey(3xfloat) + value(vec3) + paddingValue(float)]
	final int dataSize = 8*(1+3+3+1); 
	
	FloatBuffer data = ByteBuffer.allocateDirect(dataSize * 4)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer();
	FloatBuffer dataOut = ByteBuffer.allocateDirect(dataSize * 4)
			.order(ByteOrder.nativeOrder())
			.asFloatBuffer();;
	
	int offset = 4, compute = 0;

		
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
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

	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		int[] numAtomic = new int[1];
		
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
		
		
		computeShaderProgram = ShaderUtils.loadProgram(null, null, null, null, null, 
				"/lvl2advanced/p06compute/p05atomic/computeAtomic"); 
		
		
		locOffset = glGetUniformLocation(computeShaderProgram, "offset");

		// buffer initialization
		data.rewind();
		Random r = new Random();
		for (int i = 0; i < dataSize; i++) {
			data.put(i, r.nextFloat());
		}	
		
		System.out.print("Input Data values ");
		for (int i = 0; i < dataSize; i++) {
			if (i % 8 == 0)
				System.out.println();
			System.out.print(String.format("%3.2f ", data.get(i)));
		}
		System.out.println();

		// declare and generate a buffer object name
		glGenBuffers(locBuffer);
		
		// bind the buffer and define its initial storage capacity
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[0]);
		glBufferData(GL_SHADER_STORAGE_BUFFER, data, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, locBuffer[0]);
		
		// bind the buffer and define its initial storage capacity
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[1]);
		glBufferData(GL_SHADER_STORAGE_BUFFER, dataOut, GL_DYNAMIC_DRAW);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, locBuffer[1]);
		
		// unbind the buffer
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
		
		//assign the index of shader storage block to the binding point (see shader)  
		glShaderStorageBlockBinding(computeShaderProgram, 0, 0); //input buffer
		glShaderStorageBlockBinding(computeShaderProgram, 1, 1); //output buffer
		
		System.out.print("key values: ");
		for (int i = 0; i < dataSize; i += 8) {
			System.out.print(String.format("%4.2f ", data.get(i)));
		}
		System.out.println();
		
		String text = new String(this.getClass().getName() + " nothing to render, see console output");
		
		
		System.out.println(text);
		
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (offset>0) {
			glUseProgram(computeShaderProgram);

			glUniform1i(locOffset, offset);
					
			//set input and output buffer
			if (compute % 2 == 0) {
				//bind input buffer
				glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[0]);
		    	glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, locBuffer[0]);
				
		    	//bind output buffer
				glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[1]);
				glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, locBuffer[1]);
				
			}else{
				//bind input buffer
				glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[1]);
				glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, locBuffer[1]);
		    	//bind output buffer
				glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[0]);
				glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, locBuffer[0]);
			}
			
			glDispatchCompute(offset, 1, 1);
			
			// make sure writing to image has finished before read
			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
			
			{// just for print after one 
				if (compute % 2 == 0) {
					glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[1]);
					glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, locBuffer[1]);
				} else {
					glBindBuffer(GL_SHADER_STORAGE_BUFFER, locBuffer[0]);
					glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, locBuffer[0]);
				}

				glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, dataOut);

				System.out.print("Output data values after iteration " + (compute+1) + " offset " + offset);
				dataOut.rewind();
				for (int i = 0; i < dataSize; i++) {
					if (i % 8 == 0)
						System.out.println();
					System.out.print(String.format("%4.2f ", dataOut.get(i)));
				}
				System.out.println();

				if (offset <= 1) {
					System.out.println(String.format("minimal key value is %3.2f", dataOut.get(0)));
				}
			}
			
			compute ++;
			offset = offset/2;
			
		}
		
		glUseProgram(0);
		
		glClearColor(0.5f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		String text = new String(this.getClass().getName() + " nothing to render, see console output");
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}