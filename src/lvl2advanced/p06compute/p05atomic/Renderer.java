package lvl2advanced.p06compute.p05atomic;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL30.glGetIntegeri_v;
import static org.lwjgl.opengl.GL43.*;

import java.nio.ByteBuffer;

import org.lwjgl.glfw.*;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.*;
import transforms.Vec2D;


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
	
	int ac_buffer;
	int[] atomicData;
	int[] wgSize = new int[3];
	
	int[] query, result;
	int numPrimitives, numSamples, locPrimitives, locSamples;
	int locSizeComp, locModComp;
	
	boolean step = false, fill=true;
	int first = 0;
	
	int mode = 0;
	int count = 256;
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
					mode = (++mode) % 4;
					first = 0;
					break;
				case GLFW_KEY_S:
					step = !step;
					first = 0;
					break;
				case GLFW_KEY_F:
					fill = !fill;
					first = 0;
					break;
				case GLFW_KEY_PAGE_UP:
					count =count/2;
					if (count<2) count =2;
					first = 0;
					break;
				case GLFW_KEY_PAGE_DOWN:
					count =count*2;
					if (count>32*2048) count =32*2048;
					first = 0;
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
            	first = 0;
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
	
	private void createBuffer2() {
		int n = count;
		float[] vertexBufferData = new float[(n*3)*7];
		int[] indexBufferData = new int[3*n];
		
		for(int i = 0; i<n; i++){
			vertexBufferData[3*7*i+0+0] =  0;
			vertexBufferData[3*7*i+0+1] =  0;
			vertexBufferData[3*7*i+0+2] =  0;
			vertexBufferData[3*7*i+0+3] =  0;
			vertexBufferData[3*7*i+0+4] =  0;
			vertexBufferData[3*7*i+0+5] =  0;
			vertexBufferData[3*7*i+0+6] =  0;

			vertexBufferData[3*7*i+7+0] =  (float)Math.cos(2*Math.PI/n*i);
			vertexBufferData[3*7*i+7+1] =  (float)Math.sin(2*Math.PI/n*i);
			vertexBufferData[3*7*i+7+2] =  0xF<<i;
			vertexBufferData[3*7*i+7+3] =  0xF<<i;
			vertexBufferData[3*7*i+7+4] =  0xF<<i;
			vertexBufferData[3*7*i+7+5] =  (float)Math.cos(2*Math.PI/n*i);
			vertexBufferData[3*7*i+7+6] =  (float)Math.sin(2*Math.PI/n*i);

			vertexBufferData[3*7*i+14+0] =  (float)Math.cos(2*Math.PI/n*(i+1));
			vertexBufferData[3*7*i+14+1] =  (float)Math.sin(2*Math.PI/n*(i+1));
			vertexBufferData[3*7*i+14+2] =  0xF<<i;
			vertexBufferData[3*7*i+14+3] =  0xF<<i;
			vertexBufferData[3*7*i+14+4] =  0xF<<i;
			vertexBufferData[3*7*i+14+5] =  (float)Math.cos(2*Math.PI/n*(i+1));
			vertexBufferData[3*7*i+14+6] =  (float)Math.sin(2*Math.PI/n*(i+1));
		}
		for(int i = 0; i<n*3; i++){
			indexBufferData[i] =  i;
		}
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
		
		int[] val = new int[1];
		for (int dim = 0; dim < 3; dim++) {
			glGetIntegeri_v(GL_MAX_COMPUTE_WORK_GROUP_SIZE, dim, val);
			wgSize[dim]=val[0];
			System.out.println("GL_MAX_COMPUTE_WORK_GROUP_SIZE [" + dim + "] : " + val[0]);
		}
		
		shaderProgram = ShaderUtils.loadProgram(
				"/lvl2advanced/p06compute/p05atomic/drawImage");
		computeShaderProgram = ShaderUtils.loadProgram(null, null, null, null, null, 
				"/lvl2advanced/p06compute/p05atomic/computeImage"); 
		
		
		createBuffer2();

		textureViewer = new OGLTexture2D.Viewer();

		tex_output = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex_output);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, tex_w,
				tex_h, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		
		locModComp = glGetUniformLocation(computeShaderProgram, "mode");
		locSizeComp = glGetUniformLocation(computeShaderProgram, "size");
		
		
		locMode = glGetUniformLocation(shaderProgram, "mode");
		locPrimitives = glGetUniformLocation(shaderProgram, "numPrimitives");
		locSamples = glGetUniformLocation(shaderProgram, "numSamples");
		
		atomicData = new int[3];
		ac_buffer = glGenBuffers();
		glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
		glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 1, ac_buffer);
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
		glGenQueries(query);
		result = new int[1];
		
		textRenderer = new OGLTextRenderer(width, height);
		
		
}
	
	@Override
	public void display() {
	int wgX = count, wgY = count;
		
	glViewport(0, 0, width, height);
	if (fill)
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		else
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		
		if (!step || first<2){
			glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
						
			atomicData = new int[3];
			if (mode <= 4){
				//glDispatchCompute(256/32, 256/32, 1);
				wgX = count;
				wgY = count;
				if (wgX > wgSize[0])
					wgX = wgSize[0];
				if (wgY > wgSize[1])
					wgY = wgSize[1];
				
				tex_output = glGenTextures(); //TODO move to init
				glBindTexture(GL_TEXTURE_2D, tex_output);
				//glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, tex_w,
				//		tex_h, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
				glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, wgX,
						wgY, 0, GL_RGBA, GL_FLOAT, (ByteBuffer) null);
				
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
				
				//----------------------------on compute shader
				//fill texture in compute shader
				glUseProgram(computeShaderProgram);
				glBindImageTexture (0, tex_output, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);
				glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
				glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 1, ac_buffer);
				glBufferData( GL_ATOMIC_COUNTER_BUFFER, atomicData, GL_DYNAMIC_DRAW);
				
				glUniform1i(locModComp, mode%4); 
				
				//glUniform2fv(locSizeComp, ToFloatArray.convert(new Vec2D(tex_w,tex_h))); 
				glUniform2fv(locSizeComp, ToFloatArray.convert(new Vec2D(wgX,wgY))); 
				glDispatchCompute(wgX, wgY, 1);
				
				// make sure writing to image has finished before read
				glMemoryBarrier(GL_ALL_BARRIER_BITS | GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL_ATOMIC_COUNTER_BARRIER_BIT);
				
				glBindImageTexture (0, tex_output, 0, false, 0, GL_READ_ONLY, GL_RGBA32F);
				textureViewer.view(tex_output, -1, -1,1.5);
				
			
			}else{
				if (first<2)
					createBuffer2();
				//----------------------------to FrameBuffer
				
				glUseProgram(shaderProgram); 
				
				glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
				glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 1, ac_buffer);
				
				//----------------------------Queries initialization
				// Query how many elements were drawn
				glBeginQuery(GL_PRIMITIVES_GENERATED, query[0]);
		
				// Query how samples (pixels) were rasterized
				glBeginQuery(GL_SAMPLES_PASSED, query[1]);
				
				
				glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				
				glUniform1i(locMode, mode%4); 
				glUniform1i(locPrimitives, numPrimitives); 
				glUniform1i(locSamples, numSamples); 
				
				glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, ac_buffer);
				glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, ac_buffer);
				glBufferData( GL_ATOMIC_COUNTER_BUFFER, atomicData, GL_DYNAMIC_DRAW);
				
				// draw
				buffers.draw(GL_TRIANGLES, shaderProgram);
				
				//----------------------------Queries results
				
				glEndQuery(GL_PRIMITIVES_GENERATED);
				glEndQuery(GL_SAMPLES_PASSED);
				glGetQueryObjectiv(query[0], GL_QUERY_RESULT, result);
		
				numPrimitives = result[0];
				
				glGetQueryObjectiv(query[1], GL_QUERY_RESULT, result);
		
				numSamples = result[0];
			}
			first++;
		}
		
		String text = new String(this.getClass().getName() + "[F]ill");
		if (step)
			text += "[S]tart, ";
		else
			text += "[S]top, ";

		text += ", " + "Primitives " + numPrimitives;
		text += ", " + "Samples " + numSamples;
		
		switch (mode){
			case 0:
				text += ", [m]ode: CS";
				text += ", " + "workgroup [Page up/down] " + wgX+" X "+wgY;
							break;
			case 1:
				text += ", [m]ode: FS";
				text += ", " + "triangles [Page up/down] " + count;
						break;
			case 2:
				text += ", [m]ode: VS";
				text += ", " + "triangles [Page up/down] " + count;
						break;
			case 3:
				text += ", [m]ode: VS+image";
				text += ", " + "triangles [Page up/down] " + count;
						break;
		}
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}