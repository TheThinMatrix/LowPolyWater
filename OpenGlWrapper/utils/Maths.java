package utils;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Maths {
	
	public static float clamp(float value, float min, float max){
		return Math.max(Math.min(value, max), min);
	}

	/**
	 * Calculates the normal of the triangle made from the 3 vertices. The vertices must be specified in counter-clockwise order.
	 * @param vertex0
	 * @param vertex1
	 * @param vertex2
	 * @return
	 */
	public static Vector3f calcNormal(Vector3f vertex0, Vector3f vertex1, Vector3f vertex2) {
		Vector3f tangentA = Vector3f.sub(vertex1, vertex0, null);
		Vector3f tangentB = Vector3f.sub(vertex2, vertex0, null);
		Vector3f normal = Vector3f.cross(tangentA, tangentB, null);
		normal.normalise();
		return normal;
	}
	
	public static void updateViewMatrix(Matrix4f viewMatrix, float x, float y, float z, float pitch, float yaw){
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Vector3f negativeCameraPos = new Vector3f(-x, -y, -z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
	}
	
}
