package LWJGLTesting;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class DisplayExample {

	private float frames = 0f;
	private long lastTime = 0;
	private int fps = 0;
	private long lastFPS;

	List<Actor> actors;

	public static void main(String[] args) {

		// We need to load our natives for OpenGL to work.  They should be
		// right here next to us.
		System.out.print("Loading LWJGL Natives...");
		System.setProperty("org.lwjgl.librarypath",
				System.getProperty("user.dir") +
						System.getProperty("file.separator") + "natives");
		System.out.println("Done");
		System.out.println("Launching!");
		Time.createTime();
		DisplayExample d = new DisplayExample();
		d.createActors(1000);
		d.start();
	}

	private void createActors(int i) {

		if(actors == null)
			actors = new ArrayList<Actor>();

		while(i > 0){
			i--;
			actors.add(new Actor());
		}
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

		// Necessary for computing accurate FPS data as startup times cause
		// the initial read to be incorrect
		lastFPS = Time.getTime();

		// Main render loop for handling all draw activity
		while (!Display.isCloseRequested()) {

			// Clear the screen
			glClear(GL_COLOR_BUFFER_BIT);

			// We need some peeps to fill this screen!
			drawActors();

			// Need to keep that counter relevant
			updateFPS();

			// handle all the timing stuff
			Display.sync(60);
			Display.update();
			frames++;
			Time.updateTime();
		}

		// Clean up everything to avoid memory leaks
		Display.destroy();
	}

	private void drawActors() {
		for(Actor a : actors){
			a.updatePhysics(); // maybe make separate?
			a.drawSelf();
		}
	}

	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (Time.getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
}
