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
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

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
	private final int HEIGHT = 800;

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

	private int START_TIME;

	/*
	 * To draw a circle efficiently we will store a buffer of all the
	 * vertex positions of a circle.  To achieve a circle of a given radius,
	 * multiply each value by the radius, and then add the desired amount
	 * to shift the circle to the correct position.
	 */
	private static float[][] CIRCLE_OBJECT;
	private int uniformTimeLoc;
	private boolean notFirst = true;
	private int uniformHeightLoc;
	private int uniformWidthLoc;

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
		uniformTimeLoc = glGetUniformLocation(pId, "time");
		uniformHeightLoc = glGetUniformLocation(pId, "height");
		uniformWidthLoc = glGetUniformLocation(pId, "width");
		START_TIME = Time.getMilliseconds() / 1000;

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

		float[] circle = new float[360*4+4];
		int counter = 4; // TODO: fix this later
		createCircleData();
		circle[0] = -1.0f;
		circle[1] = -1.0f;
		circle[2] = 0f;
		circle[3] = 1.0f;

		for (int angle = 0; angle < 360; angle+=1) {
			circle[counter] = CIRCLE_OBJECT[0][angle] * .08f + -1.0f; // X
			circle[counter + 1] = CIRCLE_OBJECT[1][angle] * .08f + -1.0f; // Y
			circle[counter + 2] = 0.0f; // Z
			circle[counter + 3] = 1.0f; // W
			counter += 4;
		}

		System.out.println("CIRCLE VERTICES: " + circle.length);

		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(circle.length);
		verticesBuffer.put(circle);
		verticesBuffer.flip();

		// Create a new Vertex Buffer Object in memory and select it (bind) - VERTICES
		vboId = GL15.glGenBuffers();
		glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		glBindVertexArray(0);

		exitOnGLError("sdfjdklsjf");
	}

	public void loopCycle() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		glUseProgram(pId);

		float timeDiff = Time.getMilliseconds() / 1000.0f - START_TIME;
		glUniform1f(uniformTimeLoc, timeDiff);
		glUniform1f(uniformHeightLoc, HEIGHT);
		glUniform1f(uniformWidthLoc, WIDTH);

		// Bind to the index VBO that has all the information about the order of the vertices
		glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

		// Draw the vertices
		glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 361);

		// Put everything back to default (deselect)
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(0);
		glUseProgram(0);

		exitOnGLError("sdfjdklsjf");
	}

	public void destroyOpenGL() {
		// Delete the shaders
		glUseProgram(0);
		GL20.glDetachShader(pId, vsId);
		GL20.glDetachShader(pId, fsId);

		GL20.glDeleteShader(vsId);
		GL20.glDeleteShader(fsId);
		GL20.glDeleteProgram(pId);

		// Select the VAO
		glBindVertexArray(vaoId);

		// Disable the VBO index from the VAO attributes list
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		// Delete the vertex VBO
		glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);

		// Delete the color VBO
		glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vbocId);

		// Delete the index VBO
		glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboiId);

		// Delete the VAO
		glBindVertexArray(0);
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

		exitOnGLError("sdfjdklsjf");

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

		exitOnGLError("sdfjdklsjf");

		// We don't need to create more programs from the shaders we loaded,
		// so we can free them
//		GL20.glDetachShader(pId, vsId);
//		GL20.glDeleteShader(vsId);
//		GL20.glDetachShader(pId, fsId);
//		GL20.glDeleteShader(fsId);
	}

	public void exitOnGLError(String errorMessage) {
		int errorValue = GL11.glGetError();

		if (errorValue != GL11.GL_NO_ERROR) {
			String errorString = GLU.gluErrorString(errorValue);
			System.err.println("ERROR - " + errorMessage + ": " + errorString);

			if (Display.isCreated()) Display.destroy();
			System.exit(-1);
		}
	}

	private void createCircleData() {
		if(CIRCLE_OBJECT != null)
			return;
		CIRCLE_OBJECT = new float[2][360];
		for(int dims = 0; dims < 2; dims++){
			for(int angle = 0; angle < 360; angle+=1){
				if(dims == 1)
					CIRCLE_OBJECT[dims][angle] = (float) Math.sin(angle);
				else
					CIRCLE_OBJECT[dims][angle] = (float) Math.cos(angle);
			}
		}
	}
}
