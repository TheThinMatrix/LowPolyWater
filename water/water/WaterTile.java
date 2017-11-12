package water;

import openglObjects.Vao;

/**
 * Represents the entire water mesh in the world. It contains the VAO containing
 * the vertex data, and holds the number of vertices in the mesh, along with the
 * height of the water in the world.
 * 
 * @author Karl
 *
 */
public class WaterTile {

	private final Vao vao;
	private final int vertexCount;
	private final float height;

	protected WaterTile(Vao vao, int vertexCount, float height) {
		this.vao = vao;
		this.height = height;
		this.vertexCount = vertexCount;
	}

	/**
	 * @return the Vao containing all the vertex data for the water mesh.
	 */
	public Vao getVao() {
		return vao;
	}

	/**
	 * @return The height that the water should be rendered at.
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @return The total number of vertices in the water mesh.
	 */
	public int getVertexCount() {
		return vertexCount;
	}

	/**
	 * Deletes the VAO when the game closes.
	 */
	public void delete() {
		vao.delete(true);
	}

}
