package rendering;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a camera in the scene.
 * 
 * @author Karl
 *
 */
public interface ICamera {

	public Matrix4f getViewMatrix();

	public Vector3f getPosition();

	public Matrix4f getProjectionMatrix();

	public Matrix4f getProjectionViewMatrix();

	public float getNearPlane();

	public float getFarPlane();

	/**
	 * Inverts the camera for rendering a reflection texture. The y position
	 * needs to be changed to move it under the water (the same distance under
	 * the water as it was above the water) and the pitch is made negative.
	 */
	public void reflect();

}
