package LWJGLTesting;

import LWJGLTesting.Graphics.Actor;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class DisplayExample {

	/*
	 * Program metadata
	 */
	private int fps = 0;
	private long lastFPS;

	/*
	 * Window Data
	 */
	private final int WIDTH = 800;
	private final int HEIGHT = 600;

	List<Actor> actors;

	/*
	 * Temporary global vertex data
	 */
	private int vertexCount;
	private int vaoId;
	private int vboId;
	private int vsId;
	private int fsId;
	private int pId;
	private int indicesCount;
	private int vbocId;
	private int vboiId;

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
		setupShaders();

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
		destroyOpenGL();
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

		float[] vertices = {
				0.75f, 0.75f, 0f, 1f,
				0.75f, -0.75f, 0f, 1f,
				-0.75f, -0.75f, 0f, 1f
		};
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

//		float[] colors = {
//				1f, 0f, 0f, 1f,
//				0f, 1f, 0f, 1f,
//				0f, 0f, 1f, 1f,
//				1f, 1f, 1f, 1f,
//		};
//		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
//		colorsBuffer.put(colors);
//		colorsBuffer.flip();

		// Create a new Vertex Array Object in memory and select it (bind)
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind) - VERTICES
		vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - COLORS
//		vbocId = GL15.glGenBuffers();
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
//		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
//		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
//		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
	}

	public void loopCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

//		GL20.glUseProgram(pId);

		// Bind to the VAO that has all the information about the quad vertices
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);

		// Bind to the index VBO that has all the information about the order of the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Draw the vertices
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

		// Put everything back to default (deselect)
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
//		GL20.glUseProgram(0);
	}

	public void destroyOpenGL() {
		// Delete the shaders
		GL20.glUseProgram(0);
		GL20.glDetachShader(pId, vsId);
		GL20.glDetachShader(pId, fsId);

		GL20.glDeleteShader(vsId);
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId);

		// Select the VAO
		GL30.glBindVertexArray(vaoId);

		// Disable the VBO index from the VAO attributes list
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);

		// Delete the vertex VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);

		// Delete the color VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vbocId);

		// Delete the index VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboiId);

		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);

		Display.destroy();
	}

	public int loadShader(String filename, int type) {
		StringBuilder shaderSource = new StringBuilder();
		int shaderID = 0;

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not read file.");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Could not read file.");
			e.printStackTrace();
			System.exit(-1);
		} finally {

		}

		shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);

		return shaderID;
	}

	private void setupShaders() {

		// We need to load the shaders from external files
		vsId = this.loadShader("../vertex_shaders/" +
				"simple_shader.glsl",
				GL20.GL_VERTEX_SHADER);
		fsId = this.loadShader("../fragment_shaders/" +
				"simple_fragment.glsl",
				GL20.GL_FRAGMENT_SHADER);

		// Create a new shader program that links both shaders
		pId = GL20.glCreateProgram();
		GL20.glAttachShader(pId, vsId);
		GL20.glAttachShader(pId, fsId);
		GL20.glLinkProgram(pId);

		// Position information will be attribute 0
		GL20.glBindAttribLocation(pId, 0, "in_Position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(pId, 1, "in_Color");

		GL20.glValidateProgram(pId);

		int errorCheckValue = GL11.glGetError();
		if (errorCheckValue != GL11.GL_NO_ERROR) {
			System.out.println("ERROR - Could not create the shaders:" +
					GLU.gluErrorString(errorCheckValue));
			System.exit(-1);
		}

		// We don't need to create more programs from the shaders we loaded,
		// so we can free them
//		GL20.glDetachShader(pId, vsId);
//		GL20.glDeleteShader(vsId);
//		GL20.glDetachShader(pId, fsId);
//		GL20.glDeleteShader(fsId);
	}
}
