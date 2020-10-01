package lvl2advanced.p05pipeline.p01geometryshader;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL32.GL_LINE_STRIP_ADJACENCY;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import static lwjglutils.ShaderUtils.GEOMETRY_SHADER_SUPPORT_VERSION;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import lwjglutils.ToIntArray;
import transforms.Vec2D;
import transforms.Vec3D;


/**
* 
* @author PGRF FIM UHK
* @version 2.0
* @since 2019-09-02
*/
public class Renderer extends AbstractRenderer{

	OGLBuffers buffers;
	
	int shaderProgram;

	List<Integer> indexBufferData;
	List<Vec2D> vertexBufferDataPos;
	List<Vec3D> vertexBufferDataCol;
	
	boolean update = true, mode = false;
	
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_R:
					initBuffers();
					update = true;
					break;
				case GLFW_KEY_M:
					mode = !mode;
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
				DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
				DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
				glfwGetCursorPos(window, xBuffer, yBuffer);
				double mouseX = (xBuffer.get(0) / (double) width) * 2 - 1;
				double mouseY = ((height - yBuffer.get(0)) / (double) height) * 2 - 1;
				indexBufferData.add(indexBufferData.size());
				vertexBufferDataPos.add(new Vec2D(mouseX, mouseY));
				vertexBufferDataCol.add(new Vec3D(mouseX / 2 + 0.5, mouseY / 2 + 0.5, 1));
				update = true;
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
		return null;
	}

	@Override
	public GLFWScrollCallback getScrollCallback() {
		return null;
	}

	void initBuffers() {
		indexBufferData = new ArrayList<>();
		vertexBufferDataPos = new ArrayList<>();
		vertexBufferDataCol = new ArrayList<>();
		
		vertexBufferDataPos.add(new Vec2D(-0.5f, 0.0f));
		vertexBufferDataPos.add(new Vec2D(0.0f, 0.5));
		vertexBufferDataPos.add(new Vec2D(0.0f, -0.5f));
		vertexBufferDataPos.add(new Vec2D(0.5f, 0.0f));
		vertexBufferDataPos.add(new Vec2D(0.7f, 0.5f));
		vertexBufferDataPos.add(new Vec2D(0.9f, -0.7f));
		
		Random r = new Random();
		for(int i = 0; i < vertexBufferDataPos.size(); i++){
			indexBufferData.add(i);
			vertexBufferDataCol.add(new Vec3D(r.nextDouble(),r.nextDouble(),r.nextDouble()));
		}
	}
	
	void updateBuffers() {
		OGLBuffers.Attrib[] attributesPos = { 
				new OGLBuffers.Attrib("inPosition", 2), };
		OGLBuffers.Attrib[] attributesCol = {
				new OGLBuffers.Attrib("inColor", 3)
		};
		
		buffers = new OGLBuffers(ToFloatArray.convert(vertexBufferDataPos), attributesPos,
				ToIntArray.convert(indexBufferData));
		buffers.addVertexBuffer(ToFloatArray.convert(vertexBufferDataCol), attributesCol);

	}
	
	@Override
	public void init() {
		OGLUtils.shaderCheck();
		if (OGLUtils.getVersionGLSL() < GEOMETRY_SHADER_SUPPORT_VERSION){
			System.err.println("Geometry shader is not supported"); 
			System.exit(0);
		}
		
		OGLUtils.printOGLparameters();
		
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		String extensions = glGetString(GL_EXTENSIONS);
		if (extensions.indexOf("GL_ARB_enhanced_layouts") == -1)
			shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p05pipeline/p01geometryshader/geometry_OlderSM");
		else
			shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p05pipeline/p01geometryshader/geometry");
		
		initBuffers();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glViewport(0, 0, width, height);
		
		if (update) {
			updateBuffers();
			update = false;
			System.out.println(indexBufferData.size());
		}
		
		if (mode) 
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		else
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		glUseProgram(shaderProgram);

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
		
		buffers.draw(GL_LINE_STRIP_ADJACENCY, shaderProgram,indexBufferData.size());

		String text = new String(this.getClass().getName() + ": [I]nit. [M]ode");
		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}