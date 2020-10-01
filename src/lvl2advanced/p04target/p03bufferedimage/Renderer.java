package lvl2advanced.p04target.p03bufferedimage;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
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
import lwjglutils.OGLRenderTarget;
import lwjglutils.OGLTexImageFloat;
import lwjglutils.OGLTextRenderer;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import lwjglutils.ToFloatArray;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Mat4Scale;
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
	
	int shaderProgram, locMat;
	
	OGLTexture2D texture, textureColor, textureDepth;
	
	Camera cam = new Camera();
	Mat4 proj = new Mat4PerspRH(Math.PI / 4, 1, 1, 10.0);

	OGLRenderTarget renderTarget;
	boolean draw = false;
	OGLTexture2D texRGB;
	OGLTexture2D.Viewer textureViewer;
	
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
					draw = false;
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
            	proj = new Mat4PerspRH(Math.PI / 4, height / (double) width, 1, 10.0);
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

	void createBuffers() {
		// vertices are not shared among triangles (and thus faces) so each face
		// can have a correct normal in all vertices
		// also because of this, the vertices can be directly drawn as GL_TRIANGLES
		// (three and three vertices form one face) 
		// triangles defined in index buffer
				float[] cube = {
						// bottom (z-) face
						1, 0, 0,	0, 0, -1, 	1, 0,
						0, 0, 0,	0, 0, -1,	0, 0, 
						1, 1, 0,	0, 0, -1,	1, 1, 
						0, 1, 0,	0, 0, -1,	0, 1, 
						// top (z+) face
						1, 0, 1,	0, 0, 1,	1, 0, 
						0, 0, 1,	0, 0, 1,	1, 1, 
						1, 1, 1,	0, 0, 1,	0, 0,
						0, 1, 1,	0, 0, 1,	0, 1,
						// x+ face
						1, 1, 0,	1, 0, 0,	1, 0,
						1, 0, 0,	1, 0, 0,	1, 1, 
						1, 1, 1,	1, 0, 0,	0, 0,
						1, 0, 1,	1, 0, 0,	0, 1,
						// x- face
						0, 1, 0,	-1, 0, 0,	1, 0,
						0, 0, 0,	-1, 0, 0,	0, 0, 
						0, 1, 1,	-1, 0, 0,	1, 1,
						0, 0, 1,	-1, 0, 0,	0, 1,
						// y+ face
						1, 1, 0,	0, 1, 0,	1, 0,
						0, 1, 0,	0, 1, 0,	0, 0, 
						1, 1, 1,	0, 1, 0,	1, 1,
						0, 1, 1,	0, 1, 0,	0, 1,
						// y- face
						1, 0, 0,	0, -1, 0,	1, 0,
						0, 0, 0,	0, -1, 0,	1, 1, 
						1, 0, 1,	0, -1, 0,	0, 0,
						0, 0, 1,	0, -1, 0,	0, 1
				};

				int[] indexBufferData = new int[36];
				for (int i = 0; i<6; i++){
					indexBufferData[i*6] = i*4;
					indexBufferData[i*6 + 1] = i*4 + 1;
					indexBufferData[i*6 + 2] = i*4 + 2;
					indexBufferData[i*6 + 3] = i*4 + 1;
					indexBufferData[i*6 + 4] = i*4 + 2;
					indexBufferData[i*6 + 5] = i*4 + 3;
				}
				
				
		OGLBuffers.Attrib[] attributes = {
				new OGLBuffers.Attrib("inPosition", 3),
				new OGLBuffers.Attrib("inNormal", 3),
				new OGLBuffers.Attrib("inTextureCoordinates", 2)
		};

		buffers = new OGLBuffers(cube, attributes, indexBufferData);

		System.out.println(buffers.toString());
	}

	@Override
	public void init() {

		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);

		createBuffers();
		
		shaderProgram = ShaderUtils.loadProgram("/lvl2advanced/p04target/p03bufferedimage/texture");
		
		glUseProgram(this.shaderProgram);
		
		locMat = glGetUniformLocation(shaderProgram, "mat");
		
		renderTarget = new OGLRenderTarget(width, height);

		cam = cam.withPosition(new Vec3D(5, 5, 2.5))
				.withAzimuth(Math.PI * 1.25)
				.withZenith(Math.PI * -0.125);
		
		try {
			texture = new OGLTexture2D("textures/bricks.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		textureViewer = new OGLTexture2D.Viewer();
		textRenderer = new OGLTextRenderer(width, height);
}
	
	@Override
	public void display() {
		glDisable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		
		glViewport(0, 0, width, height);
		
		
		// set the current shader to be used
		glUseProgram(shaderProgram); 
		
		// set our render target (texture)
		renderTarget.bind();

		glClearColor(0.7f, 1.0f, 1.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		texture.bind(shaderProgram, "textureID", 0);
		
		glUniformMatrix4fv(locMat, false,
				ToFloatArray.convert(cam.getViewMatrix().mul(proj).mul(new Mat4Scale((double)width / height, 1, 1))));
		
		// bind and draw
		buffers.draw(GL_TRIANGLES, shaderProgram);
		
		textureColor = renderTarget.getColorTexture();
		
		if (!draw) {
				draw = true;

				// process texture as RGB BufferedImage
				// get texture data as 4x float array
				OGLTexImageFloat imgFloat = textureColor.getTexImage(new OGLTexImageFloat.Format(4));

				// conversion to RGB texture 4x byte
				texRGB = new OGLTexture2D(imgFloat.toOGLTexImageByte(4));
				
				System.out.println(textureColor);
				System.out.println(imgFloat);
				System.out.println(texRGB);

				//flip y coordinate do to BufferedImage inverse orientation
				texRGB.flipY(imgFloat.getFormat());

				// get BufferedImage
				BufferedImage img = texRGB.toBufferedImage();
				
				// draw to BufferedImage
				Graphics gr = img.getGraphics();
				gr.setColor(new Color(0xffff));
				gr.fillOval(0, 0, texRGB.getWidth() / 10, texRGB.getHeight() / 10);
				gr.setColor(new Color(0xff00ff));
				gr.drawLine(texRGB.getWidth() / 2, 0, 0, texRGB.getHeight() / 2);
				gr.setColor(new Color(0xffff00));
				gr.drawRect(100, 100, 150, 100);
				
				gr.setColor(new Color(0xff0000));
				gr.setFont(new Font( "ariel", Font.BOLD, 40));
				gr.drawString("text", texRGB.getWidth() / 4, texRGB.getHeight()/4); ;
				
				// update texture data
				texRGB.fromBufferedImage(img);

				//flip y coordinate back
				texRGB.flipY(imgFloat.getFormat());

		}
		
		// set the default render target (screen)
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(0, 0, width, height);

		glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		// use the result of the previous draw as a texture for the next
		//renderTarget.bindColorTexture(shaderProgram, "textureID", 0);
		renderTarget.getColorTexture().bind(shaderProgram, "textureID", 0);
		// use the depth buffer from the previous draw as a texture for the next
		//renderTarget.bindDepthTexture(shaderProgram, "textureID", 0);
		
		texRGB.bind(shaderProgram, "textureID", 0);
		
		glUniformMatrix4fv(locMat, false,
				ToFloatArray.convert(cam.getViewMatrix().mul(proj)));
		buffers.draw(GL_TRIANGLES, shaderProgram);
		
		String text = new String(this.getClass().getName() + ": [LMB] camera, WSAD, N - draw to texture");
		
		textureViewer.view(textureColor, -1, -1, 1, height / (double) width);
		textureViewer.view(texRGB, -1, 0, 1, height / (double) width);
		
		textRenderer.clear();
		textRenderer.setColor(Color.RED);
		textRenderer.addStr2D(3, 20, text);
		textRenderer.addStr2D(width-90, height-3, " (c) PGRF UHK");
		textRenderer.draw();
	}
}