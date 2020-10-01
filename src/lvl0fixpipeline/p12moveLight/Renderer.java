package lvl0fixpipeline.p12moveLight;

import lvl0fixpipeline.global.AbstractRenderer;
import lvl0fixpipeline.global.GLCamera;
import lwjglutils.OGLTexture2D;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static lvl0fixpipeline.global.GluUtils.gluPerspective;
import static lvl0fixpipeline.global.GlutUtils.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * It shows the possibilities of moving light sources in the scene
 *
 * @author PGRF FIM UHK
 * @version 3.1
 * @since 2020-01-20
 */
public class Renderer extends AbstractRenderer {
    private float dx, dy, ox, oy;
    private float zenit, azimut;

    private float trans, deltaTrans = 0;

    private float uhel = 0;

    private boolean mouseButton1 = false;
    private boolean per = true, move = true;
    private int sky = 0;
    private boolean light1 = true, light2 = true, light3 = true;
    private boolean light4 = true, light5 = true, light6 = true;
    private OGLTexture2D texture;

    private GLCamera camera;

    public Renderer() {
        super();

        /*used default glfwWindowSizeCallback see AbstractRenderer*/

        glfwKeyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                    // We will detect this in our rendering loop
                    glfwSetWindowShouldClose(window, true);
                if (action == GLFW_RELEASE) {
                    trans = 0;
                    deltaTrans = 0;
                }

                if (action == GLFW_PRESS) {
                    switch (key) {
                        case GLFW_KEY_P:
                            per = !per;
                            break;
                        case GLFW_KEY_M:
                            move = !move;
                            break;
                        case GLFW_KEY_K:
                            sky = (sky + 1) % 3;
                            break;
                        case GLFW_KEY_W:
                        case GLFW_KEY_S:
                        case GLFW_KEY_A:
                        case GLFW_KEY_D:
                            deltaTrans = 0.001f;
                            break;

                        case GLFW_KEY_1:
                            light1 = !light1;
                            break;
                        case GLFW_KEY_2:
                            light2 = !light2;
                            break;
                        case GLFW_KEY_3:
                            light3 = !light3;
                            break;
                        case GLFW_KEY_4:
                            light4 = !light4;
                            break;
                        case GLFW_KEY_5:
                            light5 = !light5;
                            break;
                        case GLFW_KEY_6:
                            light6 = !light6;
                            break;
                    }
                }
                switch (key) {
                    case GLFW_KEY_W:
                        camera.forward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_S:
                        camera.backward(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_A:
                        camera.left(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;

                    case GLFW_KEY_D:
                        camera.right(trans);
                        if (deltaTrans < 0.001f)
                            deltaTrans = 0.001f;
                        else
                            deltaTrans *= 1.02;
                        break;
                }
            }
        };

        glfwMouseButtonCallback = new GLFWMouseButtonCallback() {

            @Override
            public void invoke(long window, int button, int action, int mods) {
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);

                mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

                if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
                    ox = (float) x;
                    oy = (float) y;
                }
            }

        };

        glfwCursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double x, double y) {
                if (mouseButton1) {
                    dx = (float) x - ox;
                    dy = (float) y - oy;
                    ox = (float) x;
                    oy = (float) y;
                    zenit -= dy / width * 180;
                    if (zenit > 90)
                        zenit = 90;
                    if (zenit <= -90)
                        zenit = -90;
                    azimut += dx / height * 180;
                    azimut = azimut % 360;
                    dx = 0;
                    dy = 0;
                }
            }
        };

        glfwScrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                //do nothing
            }
        };
    }

    @Override
    public void init() {
        super.init();
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CW);
        glPolygonMode(GL_FRONT, GL_FILL);
        glPolygonMode(GL_BACK, GL_FILL);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        System.out.println("Loading texture...");
        try {
            texture = new OGLTexture2D("textures/globe.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        camera = new GLCamera();
        camera.setPosition(new Vec3D(11.62, 11.57, 11.60));
        azimut = (float) Math.toDegrees(-0.79);
        zenit = (float) Math.toDegrees(-0.65);
        camera.setFirstPerson(true);

        float[] mat_dif = new float[]{1, 1, 1, 1};
        float[] mat_spec = new float[]{1, 1, 1, 1};
        float[] mat_amb = new float[]{.1f, .1f, .1f, 1};

        glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, mat_amb);
        glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, mat_dif);
        glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, mat_spec);
        glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, new float[]{0, 0, 0, 1});

        scene();
        setLight(GL_LIGHT0, 1, 0, 0);
        setLight(GL_LIGHT1, 0, 1, 0);
        setLight(GL_LIGHT2, 0, 0, 1);
        setLight(GL_LIGHT3, 0, 1, 1);
        setLight(GL_LIGHT4, 1, 1, 0);
        setLight(GL_LIGHT5, 1, 0, 1);
    }

    private void setLight(int light, float r, float g, float b) {
        float[] light_amb = new float[]{0, 0, 0, 1};//{ 0.0f*r, .1f*g, .1f*b, 1 };
        float[] light_dif = new float[]{r, g, b, 1};//{ 0.1f*r, .2f*g, .2f*b, 1 };
        float[] light_spec = new float[]{0, 0, 0, 1};//{ 0.1f*r, .3f*g, .3f*b, 1 };

        glLightfv(light, GL_AMBIENT, light_amb);
        glLightfv(light, GL_DIFFUSE, light_dif);
        glLightfv(light, GL_SPECULAR, light_spec);
    }

    private void scene() {
        glNewList(1, GL_COMPILE);
        glColor3f(0, 0, 0);
        glPushMatrix();
        texture.bind();
        glutSolidSphere(5, 16, 16);
        glPopMatrix();
        glEndList();
    }

    private void drawBall(float r, float g, float b, float x, float y, float z) {
        glPushMatrix();
        glTranslatef(x, y, z);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glColor3f(r, g, b);
        glutWireSphere(1, 8, 8);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    @Override
    public void display() {
        camera.setAzimuth(Math.toRadians(azimut));
        camera.setZenith(Math.toRadians(zenit));
        glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glActiveTexture(GL_TEXTURE0);
        String text = this.getClass().getName() + ": [lmb] move";
        String textInfo = "Light: ";

        trans += deltaTrans;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        if (per)
            gluPerspective(45, width / (float) height, 0.1f, 500.0f);
        else
            glOrtho(-20 * width / (float) height,
                    20 * width / (float) height,
                    -20, 20, 0.1f, 500.0f);

        if (move) {
            uhel++;
        }

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glPushMatrix();
        glTranslatef(1, 1, -1);
        //drawBall(0,1,0,0,0,0);
        glPopMatrix();

        glDisable(GL_LIGHTING);

        if (light1) {
            // light moving (rotating) together with object
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glPushMatrix();
            camera.setMatrix();
            glRotatef(uhel, 0, 0, 1);
            float[] light_position = new float[]{8, 8, 8, 1};
            glLightfv(GL_LIGHT0, GL_POSITION, light_position);
            drawBall(1, 0, 0, 8, 8, 8);
            //glCallList(1);
            glPopMatrix();
            textInfo += "1";
        }

        if (light2) {
            //static light moving together with camera
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT1);
            glPushMatrix();

            glPushMatrix();
            glLoadIdentity();
            glTranslatef(-8f, -8f, -20);
            drawBall(0, 1, 0, 0, 0, 0);
            glLightfv(GL_LIGHT1, GL_POSITION, new float[]{0, 0, 0, 1});
            glPopMatrix();

            camera.setMatrix();
            glRotatef(uhel, 0, 0, 1);
            //glCallList(1);
            glPopMatrix();
            textInfo += "2";
        }

        if (light3) {
            // light moving together with camera and independently to object
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT2);
            glPushMatrix();
            glLoadIdentity();
            camera.setMatrix();

            glPushMatrix();
            glTranslatef((float) (10 * Math.sin(Math.toRadians(uhel * 5))), 0, 0);
            glLightfv(GL_LIGHT2, GL_POSITION, new float[]{0, 0, 0, 1});
            drawBall(0, 0, 1, 0, 0, 0);
            glPopMatrix();

            glRotatef(uhel, 0, 0, 1);
            //glCallList(1);

            glPopMatrix();
            textInfo += "3";
        }

        if (light4) {
            // light moving independently to object and camera
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT3);
            glPushMatrix();
            glLoadIdentity();
            camera.setMatrix();

            glPushMatrix();
            glLoadIdentity();
            glRotatef(uhel * 2, 0, 0, 1);
            glTranslatef(-8f, -8f, -20);
            glLightfv(GL_LIGHT3, GL_POSITION, new float[]{0, 0, 0, 1});
            drawBall(0, 1, 1, 0, 0, 0);
            glPopMatrix();

            glRotatef(uhel, 0, 0, 1);
            //glCallList(1);
            textInfo += "4";
            glPopMatrix();
        }

        if (light5) {
            // light with constant position relative to object
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT4);
            glPushMatrix();
            glLoadIdentity();
            camera.setMatrix();
            glPushMatrix();
            glTranslatef(6f, 0, 0);
            glLightfv(GL_LIGHT4, GL_POSITION, new float[]{0, 0, 0, 1});
            drawBall(1, 1, 0, 0, 0, 0);
            glPopMatrix();
            glRotatef(uhel, 0, 0, 1);
            //glCallList(1); //replaced with call after all conditions
            glPopMatrix();
            textInfo += "5";
        }

        if (light6) {
            // pulsing light with constant position relative to object
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT5);
            glPushMatrix();
            glLoadIdentity();
            camera.setMatrix();
            glPushMatrix();
            glTranslatef(0f, 7, 0);
            glLightfv(GL_LIGHT5, GL_POSITION, new float[]{0, 0, 0, 1});
            float intensity = (float) (Math.sin(Math.toRadians(uhel * 5)) * 0.5 + 0.5);
            setLight(GL_LIGHT5, intensity, 0, intensity);
            drawBall(intensity, 0, intensity, 0, 0, 0);
            glPopMatrix();
            glRotatef(uhel, 0, 0, 1);
            //glCallList(1); //replaced with call after all conditions
            glPopMatrix();
            textInfo += "6";
        }
        //draw scene (rotated sphere) - replacing calls in all conditions
        glPushMatrix();
        glLoadIdentity();
        camera.setMatrix();
        glRotatef(uhel, 0, 0, 1);
        glCallList(1);
        glPopMatrix();

        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
        glDisable(GL_LIGHT1);
        glDisable(GL_LIGHT2);
        glDisable(GL_LIGHT3);
        glDisable(GL_LIGHT4);
        glDisable(GL_LIGHT5);

        glPushMatrix();
        camera.setMatrix();
        drawBall(1, 1, 1, 0, 0, 0);
        glPopMatrix();

        if (per)
            text += ", [P]ersp ";
        else
            text += ", [p]ersp ";

        if (move)
            text += ", Ani[M] ";
        else
            text += ", Ani[m] ";

        //create and draw text
        textRenderer.clear();
        textRenderer.addStr2D(3, 20, text);
        textRenderer.addStr2D(3, 40, textInfo);
        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

}
