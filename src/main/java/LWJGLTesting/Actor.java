package LWJGLTesting;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

/**
 * Author:      Grant Kurtz
 */
public class Actor implements Drawable, Mass {

	/*
	 * Position Data
	 */
	private float x;
	private float y;

	/*
	 * To draw a circle efficiently we will store a buffer of all the
	 * vertex positions of a circle.  To achieve a circle of a given radius,
	 * multiply each value by the radius, and then add the desired amount
	 * to shift the circle to the correct position.
	 */
	private static float[][] CIRCLE_OBJECT;

	/*
	 * Kinematic Data
	 */
	private float xVelocity;
	private float yVelocity;
	private static final float GRAVITY = -9.8f;

	/*
	 * Aesthetic Data
	 */
	private float[] color;
	private float radius;

	private static Random generator = new Random();

	public Actor() {
		x = 0f;
		y = 0f;

		// We don't care what the initial velocity is so long as it is positive
		xVelocity = generator.nextInt(100) + 2;
		yVelocity = generator.nextInt(100) + 5;

		// Likewise, we don't care about the initial color so long as it is not
		// black (for then it would be invisible against the background)
		color = new float[3];
		for (int i = 0; i < color.length; i++) {
			color[i] = generator.nextFloat();
		}

		// Our radius can be any positive, non-zero, integer value
		radius = generator.nextInt(20) + 1;

		createCircleData();
	}

	private void createCircleData() {
		if(CIRCLE_OBJECT != null)
			return;
		CIRCLE_OBJECT = new float[2][360];
		for(int dims = 0; dims < 2; dims++){
			for(int angle = 0; angle < 360; angle+=5){
				if(dims == 0)
					CIRCLE_OBJECT[dims][angle] = (float) Math.sin(angle);
				else
					CIRCLE_OBJECT[dims][angle] = (float) Math.cos(angle);
			}
		}
	}

	@Override
	public void drawSelf() {

		// We need to set the color of ourselves
		glColor3f(color[0], color[1], color[2]);

		// Draw the projectile
		glBegin(GL_TRIANGLE_FAN);
		glVertex2f(x, y);
		for (int angle = 0; angle < 360; angle+=5) {
			glVertex2f(x + (CIRCLE_OBJECT[0][angle]  * radius),
					y + (CIRCLE_OBJECT[1][angle] * radius));
		}
		glEnd();
	}

	@Override
	public void updatePhysics() {
		float posTime = Time.getMilliseconds() / 1000f;
		x = xVelocity * posTime;
		y = (float) ((yVelocity * posTime) +
				(0.5f) * GRAVITY * Math.pow(posTime, 2));
	}
}
