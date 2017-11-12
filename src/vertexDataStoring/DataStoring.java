package vertexDataStoring;

import java.nio.ByteBuffer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import utils.Colour;
import utils.DataUtils;

/**
 * This class is just used for converting vertex data into a load of bytes which
 * can then be stored in a VBO. It uses ByteBuffers for convenience, as these
 * are basically just a byte[] with a pointer. Previously we've just used
 * FloatBuffers for storing vertex data, but here I take a little bit more care
 * to pack the vertex data tightly. I haven't covered some of the data formats
 * that I use here, but it's not relevant to this tutorial. You can store the
 * vertex data in the VBO in whatever format you want - the low poly stuff will
 * still work the same.
 * 
 * @author Karl
 *
 */
public class DataStoring {

	public static void packVertexData(Vector3f position, Vector3f normal, Colour colour, ByteBuffer buffer) {
		packVertexData(position.x, position.y, position.z, normal, colour, buffer);
	}

	/**
	 * Simply stores the two position floats and the 4 indicator bytes for a
	 * vertex into the byte buffer (which is basically just a fancy byte[]).
	 * This the the buffer of data that will get stored in the VAO for the
	 * water.
	 * 
	 * @param position
	 *            - The x,z position of the vertex.
	 * @param indicators
	 *            - The 4 indicator bytes which contain the offsets of the two
	 *            other vertices in the triangle in relation to this vertex.
	 * @param buffer
	 *            - The byte buffer containing the vertex data for the water.
	 *            Will get stored in the VAO.
	 */
	public static void packVertexData(Vector2f position, byte[] indicators, ByteBuffer buffer) {
		buffer.putFloat(position.x);
		buffer.putFloat(position.y);
		buffer.put(indicators);
	}

	public static void packVertexData(float x, float y, float z, Vector3f normal, Colour colour, ByteBuffer buffer) {
		store3Floats(buffer, x, y, z);
		storeNormal(buffer, normal);
		storeColour(buffer, colour);
	}

	public static void packVertexData(float x, float y, float z, Colour colour, ByteBuffer buffer) {
		store3Floats(buffer, x, y, z);
		storeColour(buffer, colour);
	}

	private static void store3Floats(ByteBuffer buffer, float a, float b, float c) {
		buffer.putFloat(a);
		buffer.putFloat(b);
		buffer.putFloat(c);
	}

	private static void storeNormal(ByteBuffer buffer, Vector3f normal) {
		int packedInt = DataUtils.pack_2_10_10_10_REV_int(normal.x, normal.y, normal.z, 0);
		buffer.putInt(packedInt);
	}

	private static void storeColour(ByteBuffer buffer, Colour colour) {
		byte[] colourBytes = colour.getAsBytes();
		buffer.put(colourBytes);
	}

}
