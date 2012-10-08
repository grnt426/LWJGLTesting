package LWJGLTesting;

import org.lwjgl.Sys;

/**
 * Author:      Grant Kurtz
 */
public class Time {

	private static int milliseconds;

	private static Time time;
	private static long lastTime;

	private Time() {
		milliseconds = 0;
	}

	public static Time createTime() {
		return time == null ? time : (time = new Time());
	}

	public static int getMilliseconds() {
		return milliseconds;
	}

	public static int getSeconds() {
		return milliseconds / 1000;
	}

	public static void updateTime(){
		milliseconds += getDelta();
	}

	private static int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastTime);
		lastTime = time;

		if (delta > 1000)
			return 1000;
		return delta;
	}

	protected static long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
}
