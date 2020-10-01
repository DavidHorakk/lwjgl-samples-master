package lvl2advanced.p02debug;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import lwjglutils.ShaderUtils;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLUtils;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * GLSL debug sample:<br/>
 * Requires LWJGL3
 * 
 * @author PGRF FIM UHK
 * @version 3.0
 * @since 2019-07-11
 */

public class HelloWorld {

	int width, height;

	// The window handle
	private long window;
	
	OGLBuffers buffers;
	
	int shaderProgram, locTime;
	
	float time = 0;
	private Callback debugProc;
	GLFWErrorCallback errCallback;
	
	boolean first = true;
	  
	enum DEBUGMODE {
		LWJGL, //using lwjgl debug mode
		GLFW, //user definition of handler 
		OpenGL, //using OpenGL debug mode by manually checking glError after calling GL method 
		OpenGL43, //using OpenGL debug mode by message callback 
		NONE,  //no special debug mode
		};
	DEBUGMODE debugMode = DEBUGMODE.NONE;
	
	GLDebugMessageCallback debugCallBack = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
	    System.err.println("Error source:" + OGLUtils.getDebugSource(source) + 
	    		" type:" + OGLUtils.getDebugType(type) + 
	    		" id:" + id + 
	    		" severity:" + OGLUtils.getDebugSeverity(severity) + 
	    		" " + memUTF8(memByteBuffer(message, length)) +
	    		" " + userParam);
	});

	
	private void init() {
		
		if (debugMode != DEBUGMODE.GLFW){
			// Setup an error callback. The default implementation
			// will print the error message in System.err.
			GLFWErrorCallback.createPrint(System.err).set();
			// or shortcut that throws an exception on error
			//GLFWErrorCallback.createThrow().set();
		} else
		glfwSetErrorCallback(errCallback = new GLFWErrorCallback() {
            GLFWErrorCallback delegate = GLFWErrorCallback.createPrint(System.err);

            @Override
            public void invoke(int error, long description) {
                if (error == GLFW_VERSION_UNAVAILABLE)
                    System.err.println("GLFW_VERSION_UNAVAILABLE: This demo requires OpenGL 2.0 or higher.");
                if (error == GLFW_NOT_INITIALIZED)
                    System.err.println("");
                if (error == GLFW_NO_CURRENT_CONTEXT)
                    System.err.println("GLFW_NO_CURRENT_CONTEXT");
			    if (error == GLFW_INVALID_ENUM)
			        System.err.println("GLFW_INVALID_ENUM");
			    if (error == GLFW_INVALID_VALUE)
			        System.err.println("GLFW_INVALID_VALUE");
			    if (error == GLFW_OUT_OF_MEMORY)
			        System.err.println("GLFW_OUT_OF_MEMORY");
			    if (error == GLFW_API_UNAVAILABLE)
			        System.err.println("GLFW_API_UNAVAILABLE");
			    if (error == GLFW_VERSION_UNAVAILABLE)
			        System.err.println("GLFW_VERSION_UNAVAILABLE");
			    if (error == GLFW_PLATFORM_ERROR)
			        System.err.println("GLFW_PLATFORM_ERROR");
			    if (error == GLFW_FORMAT_UNAVAILABLE)
			        System.err.println("GLFW_FORMAT_UNAVAILABLE");
			    if (error == GLFW_FORMAT_UNAVAILABLE)
			        System.err.println("GLFW_FORMAT_UNAVAILABLE");
    
                delegate.invoke(error, description);
            }

            @Override
            public void free() {
                delegate.free();
            }
        });
		
		/*other debug modes, some may have a very negative impact on performance
		 * see https://github.com/LWJGL/lwjgl3-wiki/wiki/2.5.-Troubleshooting
		 
		Configuration.DEBUG.set(true);
		Configuration.DEBUG_LOADER.set(true);
		Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);
		Configuration.DEBUG_STACK.set(true);
		Configuration.DEBUG_STREAM.set(true);
		*/
		
		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
		
		// Create the window
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (width > 0 && height > 0 && 
                		(HelloWorld.this.width != width || HelloWorld.this.height != height)) {
                	HelloWorld.this.width = width;
                	HelloWorld.this.height = height;
                }
            }
        });
		
		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		if (debugMode == DEBUGMODE.LWJGL)
			debugProc = GLUtil.setupDebugMessageCallback();
		
		
		if (debugMode == DEBUGMODE.OpenGL43){
			GLUtil.setupDebugMessageCallback();
			GL43.glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, true );
			//filtering of debug message control 
			//GL_DEBUG_SOURCE_API, GL_DEBUG_SOURCE_WINDOW_SYSTEM_, GL_DEBUG_SOURCE_SHADER_COMPILER, GL_DEBUG_SOURCE_THIRD_PARTY, GL_DEBUG_SOURCE_APPLICATION
			//GL_DEBUG_TYPE_ERROR, GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR, GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR, GL_DEBUG_TYPE_PORTABILITY, GL_DEBUG_TYPE_PERFORMANCE, GL_DEBUG_TYPE_MARKER, GL_DEBUG_TYPE_PUSH_GROUP, GL_DEBUG_TYPE_POP_GROUP
			//GL_DEBUG_SEVERITY_LOW, GL_DEBUG_SEVERITY_MEDIUM, or GL_DEBUG_SEVERITY_HIGH
			//GL43.glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH, (IntBuffer) null, true );
			
			glDebugMessageCallback(debugCallBack, NULL);
		}
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

	private void loop() {
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			
			glViewport(0, 0, width, height);
			
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			// set the current shader to be used, could have been done only once (in
			// init) in this sample (only one shader used)
			
			time += 0.1;
			
			//ERROR - GL_INVALID_OPERATION, no active program 
			glUniform1f(locTime, time); // correct shader must be set before this
			
			if (debugMode == DEBUGMODE.OpenGL && first)
				OGLUtils.checkGLError("[OpenGL] after setting uniform variable: " + this.getClass().getName() + "." +
							Thread.currentThread().getStackTrace()[1].getMethodName(), true);
			
			//ERROR - GL_INVALID_VALUE, handle does not refer to an object generated by OpenGL 
			glUseProgram(2); 
			
			//checking GLErrors 
			if (debugMode == DEBUGMODE.OpenGL && first)
				OGLUtils.checkGLError("[OpenGL] after setting shader program: " + this.getClass().getName() + "." +
							Thread.currentThread().getStackTrace()[1].getMethodName(), true);
			
			glUseProgram(shaderProgram); 
			
			// bind and draw
			buffers.draw(GL_TRIANGLES, shaderProgram);
			
			// end of debug mode after first run
			first = false;
			
			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}

	public void run() {
		try {
			System.out.println("Hello LWJGL " + Version.getVersion() + "!");
			init();

			loop();

			// Free the window callbacks and destroy the window
			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
			

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			// Terminate GLFW and free the error callback
			if (debugProc != null)
                debugProc.free(); 
			if (debugCallBack != null)
                debugCallBack.free(); 
			//glDeleteProgram(shaderProgram);
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}

	}

	public static void main(String[] args) {
		new HelloWorld().run();
	}

}