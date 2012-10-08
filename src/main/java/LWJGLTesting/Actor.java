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
	 * Kinematic Data
	 */
	private float xVelocity;
	private float yVelocity;
	private static final float GRAVITY = -9.8f;

	/*
	 * Aesthetic Data
	 */
	private float[] color;

	Random generator;

	public Actor() {
		x = 0f;
		y = 0f;
		generator = new Random();

		// We don't care what the initial velocity is so long as it is positive
		xVelocity = generator.nextInt(100) + 1;
		yVelocity = generator.nextInt(100) + 2;

		// Likewise, we don't care about the initial color so long as it is not
		// black (for then it would be invisible against the background)
		color = new float[3];
		for (int i = 0; i < color.length; i++) {
			color[i] = generator.nextFloat();
		}
	}

	@Override
	public void drawSelf() {

		// We need to set the color of ourselves
		glColor3f(color[0], color[1], color[2]);

		// Draw the projectile
		glBegin(GL_TRIANGLE_FAN);
		float radius = 10;
		glVertex2f(x, y);
		for (float angle = 0; angle < 360; angle += 1) {
			glVertex2f(x + ((float) Math.sin(angle) * radius),
					y + ((float) Math.cos(angle) * radius));
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
