package lvl2advanced.p01gui.p02threads;

import static org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Platform;

public class AppSingleThread {

	public static void main(String[] args) {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden
													// after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be
													// resizable
		if (Platform.get() == Platform.MACOSX) {
			glfwWindowHint(GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW_FALSE);
		}

		CountDownLatch quit = new CountDownLatch(1);

		LwjglWindowThread thread = new LwjglWindowThread(0, quit, new lvl2advanced.p01gui.p01simple.Renderer());
		long window = thread.getWindow();

		thread.start();

		out: while (true) {
			glfwWaitEvents();
			if (glfwWindowShouldClose(window)) {
				quit.countDown();
				break out;
			}
		}

		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		thread.dispose();
		System.out.println("App end");

		// Terminate GLFW and free the error callback
		glfwTerminate();
		Objects.requireNonNull(glfwSetErrorCallback(null)).free();
	}

}