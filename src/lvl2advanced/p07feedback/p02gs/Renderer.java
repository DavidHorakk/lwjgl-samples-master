package lvl2advanced.p07feedback.p02gs;


import static lwjglutils.ShaderUtils.COMPUTE_SHADER_SUPPORT_VERSION;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_QUERY_RESULT;
import static org.lwjgl.opengl.GL15.glBeginQuery;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glEndQuery;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenQueries;
import static org.lwjgl.opengl.GL15.glGetBufferSubData;
import static org.lwjgl.opengl.GL15.glGetQueryObjectiv;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_INTERLEAVED_ATTRIBS;
import static org.lwjgl.opengl.GL30.GL_PRIMITIVES_GENERATED;
import static org.lwjgl.opengl.GL30.GL_RASTERIZER_DISCARD;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN;
import static org.lwjgl.opengl.GL30.glBeginTransformFeedback;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glEndTransformFeedback;
import static org.lwjgl.opengl.GL30.glGetIntegeri_v;
import static org.lwjgl.opengl.GL30.glTransformFeedbackVaryings;
import static org.lwjgl.opengl.GL43.GL_MAX_COMPUTE_WORK_GROUP_SIZE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
		
	int shaderProgram, shaderProgramPre;
	int buffer_name;
	boolean compute = true, first = true;
	int[] query, result;
	
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
				new OGLBuffers.Attrib("inColor", 3),
		};
		buffers = new OGLBuffers(vertexBufferDataPos1, attributesPos,
				indexBufferData);
		System.out.println(buffers);
	}
	
	private void initTransformFeedback() {		
		buffer_name = glGenBuffers();
		
		// Create transform feedback output buffer
		glBindBuffer(GL_ARRAY_BUFFER, buffer_name);
		glBufferData(GL_ARRAY_BUFFER, 3*4*4*(2 + 3), GL_DYNAMIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
        result = new int[1];
        query = new int[2];
        glGenQueries(query);
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
				
		if (OGLUtils.getVersionGLSL() < 450)
			shaderProgramPre = ShaderUtils.loadProgram("/lvl2advanced/p07feedback/p02gs/feedbackGS");
		else
			shaderProgramPre = ShaderUtils.loadProgram("/lvl2advanced/p07feedback/p02gs/feedbackGSLayout");
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p07feedback/p02gs/feedbackDraw");
		
		glTransformFeedbackVaryings( shaderProgramPre, new String[] {"outData"} , GL_INTERLEAVED_ATTRIBS);
		
		initTransformFeedback();
		createInputBuffer();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (compute) {
			compute = false;
		
			glUseProgram(shaderProgramPre);

			glEnable(GL_RASTERIZER_DISCARD);

			glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, buffer_name);

			glBeginTransformFeedback(GL_POINTS);

			// Query- how many elements were transformed
			glBeginQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN, query[0]);

			// Draw buffer to get transform feedback
			buffers.draw(GL_POINTS, shaderProgramPre);

			// Query how many elements were transformed
			glEndQuery(GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN);
			glGetQueryObjectiv(query[0], GL_QUERY_RESULT, result);
			System.out.println("GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN: " + result[0]);

			glEndTransformFeedback();
			// Unbind the transform feedback for safety
			glBindBufferBase(GL_TRANSFORM_FEEDBACK_BUFFER, 0, 0);

			// Disable the rasterizer discard, because we need rasterization in drawing.
			glDisable(GL_RASTERIZER_DISCARD);

			glFlush();

			// Print transform feedback buffer to check
			FloatBuffer f = ByteBuffer.allocateDirect(4*3 * 4 * (2 + 3))
					.order(ByteOrder.nativeOrder())
					.asFloatBuffer();
			glBindBuffer(GL_ARRAY_BUFFER, buffer_name);
			glGetBufferSubData(GL_ARRAY_BUFFER, 0, f);

			for (int i = 0; i < f.limit(); i += 5) {
				System.out.println(" XY:" + f.get(i) + ", " + f.get(i + 1) + " RGB:" + f.get(i + 2) + "," + f.get(i + 3)
						+ "," + f.get(i + 4));
			}

		}

		// drawing pipeline
		glUseProgram(shaderProgram);

		// draw original buffer
		glPointSize(5f);
		buffers.draw(GL_POINTS, shaderProgram);

		// Query- how many elements were drawn
		if (first) 
			glBeginQuery(GL_PRIMITIVES_GENERATED, query[1]);
		
		// draw date get from transform feedback
		glPointSize(10f);
		glBindBuffer(GL_ARRAY_BUFFER, buffer_name);
		glEnableVertexAttribArray(0); // attribute inPossition
		// first attribute, size=2 floats, stride=20 bytes, offset=0 bytes
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 20, 0); 
		
		glEnableVertexAttribArray(1); // attribute inColor
		// second attribute, size=2 floats, stride=20 bytes, offset=8 bytes
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 20, 8);
		
		glDrawArrays(GL_POINTS, 0, 3*4); // number of elements

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		
		
		if (first) {
			first = false;
			// Query how many elements were transformed
			glEndQuery(GL_PRIMITIVES_GENERATED);
			glGetQueryObjectiv(query[1], GL_QUERY_RESULT, result);
			System.out.println("GL_PRIMITIVES_GENERATED: " + result[0]);

		}
		
		String text = new String(this.getClass().getName() + " transform feedback, see console output");
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}