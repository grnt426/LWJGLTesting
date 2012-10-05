package LWJGLTesting;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class DisplayExample {

	public static void main(String[] args){
		DisplayExample d = new DisplayExample();
		d.start();
	}

	private void start() {
		try {
			DisplayMode mode = new DisplayMode(640, 480);
			Display.setDisplayMode(mode);
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		while (!Display.isCloseRequested()) {

			// render OpenGL here

			Display.update();
		}

		Display.destroy();
	}

	public DisplayExample(){

	}
}
