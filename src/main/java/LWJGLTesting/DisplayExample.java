package LWJGLTesting;

import LWJGLTesting.Graphics.Actor;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class DisplayExample {

	private int fps = 0;
	private long lastFPS;

	private final int WIDTH = 800;
	private final int HEIGHT = 600;

	List<Actor> actors;
	private int vertexCount;
	private int vaoId;
	private int vboId;

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

		if (actors == null)
			actors = new ArrayList<Actor>();

		while (i > 0) {
			i--;
			actors.add(new Actor());
		}
	}

	private void start() {

		// init OpenGL
		setupOpenGL();

		// Necessary for computing accurate FPS data as startup times cause
		// the initial read to be incorrect
		lastFPS = Time.getTime();

		setupQuad();

		// Main render loop for handling all draw activity
		while (!Display.isCloseRequested()) {

			// Clear the screen
			glClear(GL_COLOR_BUFFER_BIT);

			// We need some peeps to fill this screen!
//			drawActors();
			loopCycle();

			// Need to keep that counter relevant
			updateFPS();

			// handle all the timing stuff
			Display.sync(60);
			Display.update();
			Time.updateTime();
		}

		// Clean up everything to avoid memory leaks
		Display.destroy();
	}

	private void setupOpenGL() {

		// Setup an OpenGL context with API version 3.2
		try {
			PixelFormat pixelFormat = new PixelFormat();
			ContextAttribs contextAttributes = new ContextAttribs(3, 2);
			contextAttributes.withForwardCompatible(true);
			contextAttributes.withProfileCore(true);

			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
			Display.setTitle("OpenGL Fun!");
			Display.create(pixelFormat, contextAttributes);

			GL11.glViewport(0, 0, WIDTH, HEIGHT);
		} catch (LWJGLException e) {
			System.err.println("Woah! Something happened while initializing " +
					"OpenGL!");
			e.printStackTrace();
			System.exit(-1);
		}

		glDisable(GL_DEPTH_TEST); // Disable the depth buffer

		// We want a black background
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0f);

		// Map the internal OpenGL coordinate system to the entire screen
		GL11.glViewport(0, 0, WIDTH, HEIGHT);
	}

	private void drawActors() {
		for (Actor a : actors) {
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

	public void setupQuad() {
		// OpenGL expects vertices to be defined counter clockwise by default
		float[] vertices = {
				// Left bottom triangle
				-0.5f, 0.5f, 0f,
				-0.5f, -0.5f, 0f,
				0.5f, -0.5f, 0f,
				// Right top triangle
				0.5f, -0.5f, 0f,
				0.5f, 0.5f, 0f,
				-0.5f, 0.5f, 0f
		};
		// Sending data to OpenGL requires the usage of (flipped) byte buffers
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		vertexCount = 6;

		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind)
		// A VBO is a collection of Vectors which in this case resemble the location of each vertex.
		vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
	}

	public void loopCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		// Bind to the VAO that has all the information about the quad vertices
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);

		// Draw the vertices
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

		// Put everything back to default (deselect)
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}

	public void destroyOpenGL() {
		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);

		// Delete the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);

		Display.destroy();
	}
}
