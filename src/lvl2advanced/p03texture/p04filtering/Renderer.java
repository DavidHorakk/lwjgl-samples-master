package lvl2advanced.p03texture.p04filtering;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL14.GL_MIRRORED_REPEAT;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.io.IOException;
import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import lvl2advanced.p01gui.p01simple.AbstractRenderer;
import lwjglutils.OGLBuffers;
import lwjglutils.OGLTexImageByte;
import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import lwjglutils.OGLUtils;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;


/**
* 
* @author PGRF FIM UHK
* @version 2.0
* @since 2019-09-02
*/
public class Renderer extends AbstractRenderer{

	double ox, oy;
	boolean mouseButton1 = false;
	
	OGLBuffers buffers;
	
	int shaderProgram, locMat, locHeight;

	OGLTexture2D texture, textureGrid;

	boolean depthTest = true, cCW = true, renderLine = false;
	Camera cam = new Camera();
	Mat4 proj = new Mat4PerspRH(Math.PI / 4, 1, 0.01, 1000.0);
	OGLTexture2D.Viewer textureViewer;
	
	int mode = 1, locMode, modeTex = 2, modeInter = 5, texSource;
	int level, locLevel;

	int size = 1024;
	int maxLevel;

	private GLFWKeyCallback   keyCallback = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
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
				case GLFW_KEY_N:
					texSource = (texSource + 1) % 2;
					break;
				case GLFW_KEY_M:
					mode = (mode + 1) % 4;
					break;
				case GLFW_KEY_L:
					level = (level + 1) % maxLevel;
					break;
				case GLFW_KEY_T:
					modeTex = (modeTex + 1) % 4;
					break;
				case GLFW_KEY_I:
					modeInter = (modeInter + 1) % 6;
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
            	proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 0.01, 1000.0);
            	if (textRenderer != null)
            		textRenderer.resize(width, height);

            }
        }
    };
    
    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
    	@Override
		public void invoke(long window, int button, int action, int mods) {
			mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;
			
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
    
    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override public void invoke (long window, double dx, double dy) {
        if (dy<0)
        	cam = cam.mulRadius(0.9f);
        else
        	cam = cam.mulRadius(1.1f);
			
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
		return scrollCallback;
	}

	OGLTexImageFloat addAxes(OGLTexImageFloat image){
		int bold = image.getWidth()/8;
		//draw axes to texture
		for (int i = 0; i<image.getWidth(); i++)
			for(int j=0; j<bold; j++){
				image.setPixel(i, j, 0, 1.0f); //red
			image.setPixel(i, j, 1, 0.0f); //green
			image.setPixel(i, j, 2, 0.0f); //blue
			}
		for (int i = 0; i<image.getHeight(); i++)
			for(int j=0; j<bold; j++){
			image.setPixel(j, i, 0, 0.0f); //red
			image.setPixel(j, i, 1, 1.0f); //green
			image.setPixel(j, i, 2, 0.0f); //blue
		}
		for (int i = 0; i<bold; i++)
			for(int j=0; j<bold; j++){
			image.setPixel(j, i, 0, 0.0f); //red
			image.setPixel(j, i, 1, 0.0f); //green
			image.setPixel(j, i, 2, 1.0f); //blue
		}
		//update image
		return image;
	}
	
	OGLTexImageByte addDiagonal(OGLTexImageByte image){
		int bold = image.getWidth()/8;
		//draw diagonal to texture
		for (int i = image.getWidth()-1; i>0; i--){
			image.setPixel(i, i, 0, (byte) 255); //red
			image.setPixel(i, i, 1, (byte) 255); //green
			image.setPixel(i, i, 2, (byte) 255); //blue
		}
		for (int i = image.getWidth()-1; i>image.getWidth()-bold; i--)
			for(int j=image.getHeight()-1; j>image.getHeight()-bold; j--){
			image.setPixel(j, i, 0, (byte) 255); //red
			image.setPixel(j, i, 1, (byte) 255); //green
			image.setPixel(j, i, 2, (byte) 255); //blue
		}
		//update image
		return image;
	}
	
	void createBuffers() {
		// vertices are not shared among triangles (and thus faces) so each face
		// can have a correct normal in all vertices
		// also because of this, the vertices can be directly drawn as GL_TRIANGLES
		// (three and three vertices form one face) 
		// triangles defined in index buffer
		float[] cube = {
						// bottom (z-) face
						1, 0, 0,	0, 0, -1,	1, 0, 
						0, 0, 0,	0, 0, -1,	0, 0, 
						1, 1, 0,	0, 0, -1,	1, 1, 
						0, 1, 0,	0, 0, -1,	0, 1
						
		};
		int[] indexBufferData = {0, 1, 2, 1, 2, 3};		
		OGLBuffers.Attrib[] attributes = {
				new OGLBuffers.Attrib("inPosition", 3),
				new OGLBuffers.Attrib("inNormal", 3),
				new OGLBuffers.Attrib("inTextureCoordinates", 2)
		};

		buffers = new OGLBuffers(cube, attributes, indexBufferData);
	}

	@Override
	public void init() {
		OGLUtils.printOGLparameters();
		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		createBuffers();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p03texture/p04filtering/textureInterpolation");
		
		
		glUseProgram(this.shaderProgram);
		
		locMat = glGetUniformLocation(shaderProgram, "mat");
		locMode = glGetUniformLocation(shaderProgram, "mode");
		locLevel = glGetUniformLocation(shaderProgram, "level");

		try {
			texture = new OGLTexture2D("textures/testTexture.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		texture.bind();
		texture.flipY(new OGLTexImageFloat.Format(4));

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glGenerateMipmap(GL_TEXTURE_2D);
		// glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP,
		// GL_TRUE); //removed from GL 3.1 and above.

		OGLTexImageByte imageGrid = new OGLTexImageByte(size, size, new OGLTexImageByte.Format(4));
		for (int i = 0; i < size; i += 20)
			for (int j = 0; j < size; j += 20)
				for (int m = 0; m < 10; m++)
					for (int n = 0; n < 10; n++) {
						imageGrid.setPixel(i + m, j + n, 1, (byte) 0xff);
						imageGrid.setPixel(i + m, j + n, 2, (byte) 0xff);
						imageGrid.setPixel(i + m, j + n, (byte) 0xff);
					}

		textureGrid = new OGLTexture2D(imageGrid);
		textureGrid.bind();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glGenerateMipmap(GL_TEXTURE_2D);

		// coloring first MIP level
		imageGrid = textureGrid.getTexImage(new OGLTexImageByte.Format(4), 1);
		for (int i = 0; i < imageGrid.getWidth(); i++)
			for (int j = 0; j < imageGrid.getHeight(); j++) {
				imageGrid.setPixel(i, j, 1, (byte) 0xff);
			}
		textureGrid.setTexImage(imageGrid, 1);

		// coloring second MIP level
		imageGrid = textureGrid.getTexImage(new OGLTexImageByte.Format(4), 2);
		for (int i = 0; i < imageGrid.getWidth(); i++)
			for (int j = 0; j < imageGrid.getHeight(); j++) {
				imageGrid.setPixel(i, j, 2, (byte) 0xff);
			}
		textureGrid.setTexImage(imageGrid, 2);

		// coloring third MIP level
		imageGrid = textureGrid.getTexImage(new OGLTexImageByte.Format(4), 3);
		for (int i = 0; i < imageGrid.getWidth(); i++)
			for (int j = 0; j < imageGrid.getHeight(); j++) {
				imageGrid.setPixel(i, j, 0, (byte) 0xff);
			}
		textureGrid.setTexImage(imageGrid, 3);

		cam = cam.withPosition(new Vec3D(5, 5, 2.5))
				.withAzimuth(Math.PI * 1.25)
				.withZenith(Math.PI * -0.125);
		
		textureViewer = new OGLTexture2D.Viewer();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		String text = new String(this.getClass().getName() + ": [LMB] camera, WSAD");

		glViewport(0, 0, width, height);
		glDisable(GL_CULL_FACE); 
		glFrontFace(GL_CCW);
		glEnable(GL_DEPTH_TEST);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_TEXTURE_2D);
		
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		// set the current shader to be used
		glUseProgram(shaderProgram); 
		
		OGLTexture2D testTexture;
		
		if (texSource == 1)
			testTexture = texture;
		else
			testTexture = textureGrid;
		
		testTexture.bind();
		maxLevel = 1 + (int) Math.floor((Math.log(Math.max(testTexture.getHeight(), testTexture.getWidth())) / Math.log(2)));

		switch (modeTex) {
		case 0:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			text += ", [t]exMode: GL_CLAMP_TO_EDGE";
			break;
		case 1:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
			text += ", [t]exMode: GL_CLAMP_TO_BORDER";
			break;
		case 2:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
			text += ", [t]exMode: GL_REPEAT";
			break;
		case 3:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
			text += ", [t]exMode: GL_MIRRORED_REPEAT";
		}

		switch (modeInter) {
		case 0:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			text += ", [i]nterpolation: GL_NEAREST";
			break;
		case 1:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			text += ", [i]nterpolation: GL_LINEAR";
			break;
		case 2:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			text += ", [i]nterpolation: GL_NEAREST_MIPMAP_NEAREST";
			break;
		case 3:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			text += ", [i]nterpolation: GL_NEAREST_MIPMAP_LINEAR";
			break;
		case 4:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			text += ", [i]nterpolation: GL_LINEAR_MIPMAP_NEAREST";
			break;
		case 5:
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			text += ", [i]nterpolation: GL_LINEAR_MIPMAP_LINEAR";
		}

		switch (mode) {
		case 1:
			text += ", [m]ode: smooth";
			break;
		case 2:
			text += ", [m]ode: flat";
			break;
		case 3:
			text += ", [m]ode: noperspective";
			break;
		default:
			text += ", [m]ode: default";
		}

		text += ", [n] texture source";

		text += ", [l]evel of MIP " + level + "/" + maxLevel;

		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		glUseProgram(shaderProgram);
		glUniformMatrix4fv(locMat, false,
				ToFloatArray.convert(cam.getViewMatrix().mul(proj)));
		
		glUniform1i(locMode, mode);
		glUniform1i(locLevel, level);

		buffers.draw(GL_TRIANGLE_STRIP, shaderProgram);

		//show loaded texture and its mip layers
		textureViewer.view(texture, 0.25, -1, 0.5);
		for(int i= 0; i<8; i++)
			textureViewer.view(texture, 0.75, i*0.25-1, 0.25, 1.0, i);
		
		//show made texture and its mip layers
		textureViewer.view(textureGrid, -0.75, -1, 0.5);
		for(int i= 0; i<8; i++)
			textureViewer.view(textureGrid, -1, i*0.25-1, 0.25, 1.0, i);

		
		textRenderer.clear();
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}