package lvl2advanced.p05pipeline.p05subrutine;


import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL40.*;

import java.awt.event.KeyEvent;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Mat4Scale;
import transforms.Vec3D;


/**
 * GLSL sample:<br/>
 * Subroutine demonstration in both vertex and fragment shader<br/>
 * 
 * @author PGRF FIM UHK
 * @version 3.0
 * @since 2019-09-05
 */

public class Renderer extends AbstractRenderer{

	boolean mouseButton1 = false;
	double ox, oy;

	OGLBuffers buffers;
	OGLTextRenderer textRenderer;

	int shaderProgram, locMat, shaderProgramFunction, locTime;
	int mode = 3;
	int function1 = 0;
	int function2 = 0;
	
	Camera cam = new Camera();
	Mat4 proj; // created in reshape()
	Mat4 model = new Mat4Scale(5, 5, 1);

	long time;
	
	int[] subroutineColor = new int[3];
	int[] subroutineShape = new int[2];
	int locSub; 

	private GLFWWindowSizeCallback wsCallback = new GLFWWindowSizeCallback() {
	        @Override
	        public void invoke(long window, int w, int h) {
	        	if (w > 0 && h > 0) {
	            	width = w;
	            	height = h;
	            	proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
	            	if (textRenderer != null)
	            		textRenderer.resize(width, height);
	        	}
	        }
	    };
	    
	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				// We will detect this in our rendering loop
				glfwSetWindowShouldClose(window, true); 
			if (action == GLFW_PRESS || action == GLFW_REPEAT){
				switch (key) {
				case GLFW_KEY_W:
					cam = cam.forward(1);
					break;
				case GLFW_KEY_D:
					cam = cam.right(1);
					break;
				case GLFW_KEY_S:
					cam = cam.backward(1);
					break;
				case GLFW_KEY_A:
					cam = cam.left(1);
					break;
				case GLFW_KEY_LEFT_CONTROL:
					cam = cam.down(1);
					break;
				case GLFW_KEY_LEFT_SHIFT:
					cam = cam.up(1);
					break;
				case GLFW_KEY_SPACE:
					cam = cam.withFirstPerson(!cam.getFirstPerson());
					break;
				case GLFW_KEY_R:
					cam = cam.mulRadius(0.9f);
					break;
				case GLFW_KEY_F:
					cam = cam.mulRadius(1.1f);
					break;
				case KeyEvent.VK_M:
					mode++;
					break;

				case GLFW_KEY_KP_ADD:
				case GLFW_KEY_PAGE_DOWN:
						function1++;
					break;
				case GLFW_KEY_KP_SUBTRACT:
				case GLFW_KEY_PAGE_UP:
						function2++;
					break;
				}
			}
		}
	};
    
    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
		@Override
		public void invoke(long window, int button, int action, int mods) {
			if (button==GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS){
				mouseButton1 = true;
				DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
				DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
				glfwGetCursorPos(window, xBuffer, yBuffer);
				ox = xBuffer.get(0);
				oy = yBuffer.get(0);
			}
			
			if (button==GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE){
				mouseButton1 = false;
				DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
				DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
				glfwGetCursorPos(window, xBuffer, yBuffer);
				double x = xBuffer.get(0);
				double y = yBuffer.get(0);
				cam = cam.addAzimuth((double) Math.PI * (ox - x) / width)
        				.addZenith((double) Math.PI * (oy - y) / width);
				ox = x;
				oy = y;
        	}
		}
		
	};
	
    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
        	if (mouseButton1) {
				cam = cam.addAzimuth((double) Math.PI * (ox - x) / width)
						.addZenith((double) Math.PI * (oy - y) / width);
				ox = x;
				oy = y;
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

	
	void createBuffers(int width, int height) {
		// triangles defined in vertex buffer
		float[] cloud = new float [width*height*3];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int index = (i*height + j) * 3;
				cloud[index] = (float)j/(height-1); 
				cloud[index+1] = (float)i/(width-1); 
				cloud[index+2] = 0; 
			}
		}

		OGLBuffers.Attrib[] attributes = {
				new OGLBuffers.Attrib("inPosition", 3)
		};

		//create geometry without index buffer as the point list 
		buffers = new OGLBuffers(cloud, attributes, null);
		
		System.out.println(buffers.toString());
		
	}

	@Override
	public void init() {
		OGLUtils.shaderCheck();

		OGLUtils.printOGLparameters();

		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p05pipeline/p05subrutine/colored");
		
		if (OGLUtils.getExtensions()
				.contains("ARB_explicit_uniform_location")) 
				shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p05pipeline/p05subrutine/coloredLayout");
		
		createBuffers(100, 100);

		locMat = glGetUniformLocation(shaderProgram, "matMVP");
		locTime = glGetUniformLocation(shaderProgram, "time");

		cam = cam.withPosition(new Vec3D(5, 5, 2.5))
				.withAzimuth(Math.PI * 1.25)
				.withZenith(Math.PI * -0.125);
		
		//FS subroutine definition
		subroutineColor = new int[2];

		if (OGLUtils.getExtensions().contains("ARB_explicit_uniform_location")) {
			subroutineColor = new int[3];
			// option 3 of fragment shader
			subroutineColor[2] = 4; // specific layout in VS
			locSub = glGetSubroutineUniformLocation(shaderProgram, GL_FRAGMENT_SHADER, "mySelection");
		}

		// option 1 of fragment shader
		subroutineColor[0] = glGetSubroutineIndex(shaderProgram, GL_FRAGMENT_SHADER, "colorByColor");
		// option 2 of fragment shader
		subroutineColor[1] = glGetSubroutineIndex(shaderProgram, GL_FRAGMENT_SHADER, "colorByPossition");

		for (int i=0; i<subroutineColor.length; i++) {
			System.out.println("Fragment subroutineColor "+i+": "+ subroutineColor[i]);
		}
		
		// VS subroutine definition
		subroutineShape = new int[2];

		subroutineShape[0] = glGetSubroutineIndex(shaderProgram, GL_VERTEX_SHADER, "explicitFunction1");
		subroutineShape[1] = glGetSubroutineIndex(shaderProgram, GL_VERTEX_SHADER, "explicitFunction2");

		for (int i : subroutineShape) {
			System.out.println("Vertex subroutineShape "+i+": "+ subroutineShape[i]);
		}
		
		int[] maxSub = new int[1];
		int[] maxSubU = new int[1];
		glGetIntegerv(GL_MAX_SUBROUTINES, maxSub);
		glGetIntegerv(GL_MAX_SUBROUTINE_UNIFORM_LOCATIONS, maxSubU);
		System.out.println("Max Subroutines: " + maxSub[0]);
		System.out.println("Max Subroutine Uniforms: " + maxSubU[0]);

		time = System.currentTimeMillis();

		textRenderer = new OGLTextRenderer(width, height);
	}
	
	@Override
	public void display() {
		glEnable(GL_DEPTH_TEST);
		glViewport(0, 0, width, height);
		long currentTime = System.currentTimeMillis();
		
		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		glUseProgram(shaderProgram);
		
		glUniformSubroutinesui(GL_FRAGMENT_SHADER, subroutineColor[function1 % subroutineColor.length]);
		
		glUniformSubroutinesui(GL_VERTEX_SHADER, subroutineShape[function2 % subroutineShape.length]);
		
		glUniformMatrix4fv(locMat, false,
				ToFloatArray.convert(model.mul(cam.getViewMatrix()).mul(proj)));
		
		glUniform1i(locTime, (int)(time - currentTime));
		String text = new String("[LMB] camera, WSAD");
		
		glPointSize(1);

		switch(mode % 4){
		case 0:
			text +=", [m]ode: points";
			buffers.draw(GL_POINTS, shaderProgram);
		break;
		case 1:
			text +=", [m]ode: lines";
			buffers.draw(GL_LINES, shaderProgram);
		break;
		case 2:
			text +=", [m]ode: line strip";
			buffers.draw(GL_LINE_STRIP, shaderProgram);
		break;
		case 3:
			glPointSize(10);
			text +=", [m]ode: bigger points";
			buffers.draw(GL_POINTS, shaderProgram);
		break;
		}
		text +=", Num[+][-] change subrutine VS id: " + subroutineShape[function2 % subroutineShape.length] 
				+ " FS id: " + subroutineColor[function1 % subroutineColor.length];
			
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}