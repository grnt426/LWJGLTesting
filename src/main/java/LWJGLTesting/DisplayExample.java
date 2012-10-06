package LWJGLTesting;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class DisplayExample {

	private float frames = 0f;
	private long lastTime = 0;

	public static void main(String[] args) {
		DisplayExample d = new DisplayExample();
		d.start();
	}

	private void start() {
		try {
			DisplayMode mode = new DisplayMode(800, 600);
			Display.setDisplayMode(mode);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		// init OpenGL
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 0, 600, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glDisable(GL_DEPTH_TEST); // Disable the depth buffer
		glTranslatef(0.375f, 0.375f, 0);


		float time = 0f;

		float initXVelocity = 50f;
		float initYVelocity = 75f;
		float gravity = -9.8f;

		while (!Display.isCloseRequested()) {
			float posTime = time / 1000f;
			System.out.println("Time: " + time + "ms (" + posTime +"s), Frame: " + frames);

			// Clear the screen
			glClear(GL_COLOR_BUFFER_BIT);

			// I want blue
			glColor3f(0.5f, 0.5f, 1.0f);

			// Draw the projectile
			glBegin(GL_TRIANGLE_FAN);
			float x = initXVelocity * posTime;
			float y = (float) ((initYVelocity * posTime) +
					(0.5f) * gravity * Math.pow(posTime, 2));
			float radius = 10;
			glVertex2f(x, y);
			for (float angle = 0; angle < 360; angle += 1) {
				glVertex2f(x + ((float) Math.sin(angle) * radius),
						y + ((float) Math.cos(angle) * radius));
			}
			glEnd();

			Display.update();
			Display.sync(60);
			frames++;
			time += getDelta();
		}

		Display.destroy();
	}

	public DisplayExample() {

	}

	private int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastTime);
	    lastTime = time;

		if(delta > 1000)
			return 1000;
	    return delta;
	}

	private long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
}
