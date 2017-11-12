package waterRendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import rendering.ICamera;
import rendering.Light;
import utils.OpenGlUtils;
import water.WaterTile;

/**
 * Simple rendering code for rendering the water mesh. Most of the work is done
 * in the shaders, so the code here is relatively basic. It mostly just involves
 * loading up a load of uniform variables to the shaders before rendering the
 * mesh.
 * 
 * @author Karl
 *
 */
public class WaterRenderer {

	private static final float WAVE_SPEED = 0.002f;

	private final WaterShader shader;

	private float time = 0;

	/**
	 * Initialises the shader program that will be used to render the water.
	 */
	public WaterRenderer() {
		this.shader = new WaterShader();
	}

	/**
	 * Renders the water. The render call here is "glDrawArrays" instead of
	 * "glDrawElements" because no vertices are shared, so indices would be
	 * pointless.
	 * 
	 * @param water
	 *            - The water being rendered.
	 * @param camera
	 *            - The camera being used to render the water.
	 * @param light
	 *            - The light in the scene.
	 * @param reflectionTexture
	 *            - The reflection texture - an image of the scene taken with an
	 *            inverted camera. This will be applied to the water's surface
	 *            to simulate reflection.
	 * @param refractionTexture
	 *            - The refraction texture - and image of the scene from the
	 *            camera's current position that will be applied to the water
	 *            mesh to simulate refraction. A texture is used here so that it
	 *            can be distorted, but if no distortion is required then the
	 *            same effect could be achieved using alpha blending.
	 * @param depthTexture
	 *            - An image of the depth buffer for the scene. This is used to
	 *            apply depth effects to the water.
	 */
	public void render(WaterTile water, ICamera camera, Light light, int reflectionTexture, int refractionTexture,
			int depthTexture) {
		prepare(water, camera, light);
		bindTextures(reflectionTexture, refractionTexture, depthTexture);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, water.getVertexCount());
		finish(water);
	}

	/**
	 * Deletes the shader program after the game closes.
	 */
	public void cleanUp() {
		shader.cleanUp();
	}

	/**
	 * Binds the VAO, enables alpha blending (for the soft edges) and prepares
	 * the shader program.
	 * 
	 * @param water
	 *            - The water tile.
	 * @param camera
	 *            - The scene's camera.
	 * @param light
	 *            - The scene's light.
	 */
	private void prepare(WaterTile water, ICamera camera, Light light) {
		water.getVao().bind();
		OpenGlUtils.enableAlphaBlending();
		prepareShader(water, camera, light);
	}

	/**
	 * Binds the three textures to three separate texture units.
	 * 
	 * @param reflectionTexture
	 *            - Inverted image of the scene for simulating reflection.
	 * @param refractionTexture
	 *            - Image of the scene from cameras position to simulate
	 *            refraction.
	 * @param depthTexture
	 *            - Image of the depth buffer of the current scene.
	 */
	private void bindTextures(int reflectionTexture, int refractionTexture, int depthTexture) {
		bindTextureToUnit(reflectionTexture, WaterShader.REFLECT_TEX_UNIT);
		bindTextureToUnit(refractionTexture, WaterShader.REFRACT_TEX_UNIT);
		bindTextureToUnit(depthTexture, WaterShader.DEPTH_TEX_UNIT);
	}

	/**
	 * Binds a texture to a given unit.
	 * 
	 * @param textureId
	 *            - The ID of the texture object.
	 * @param textureUnit
	 *            - The index of the texture unit.
	 */
	private void bindTextureToUnit(int textureId, int textureUnit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	/**
	 * Finish rendering the water. Simply unbinds the VAO, stops the water
	 * shader program and disables alpha blending.
	 * 
	 * @param water
	 *            - The water that was being rendered.
	 */
	private void finish(WaterTile water) {
		water.getVao().unbind();
		shader.stop();
		OpenGlUtils.disableBlending();
	}

	/**
	 * Starts the shaders and loads up a load of uniform variables. Obviously if
	 * the uniform values aren't changing then you don't need to load them up
	 * every frame. E.G, if the light colour/position never changes then just
	 * load this up once when the game starts.
	 * 
	 * @param water
	 *            - The water being rendered.
	 * @param camera
	 *            - The scene's camera.
	 * @param light
	 *            - The light in the scene.
	 */
	private void prepareShader(WaterTile water, ICamera camera, Light light) {
		shader.start();
		updateTime();
		loadCameraVariables(camera);
		loadLightVariables(light);
		shader.height.loadFloat(water.getHeight());
	}

	/**
	 * Loads up uniform variables to do with the light source. If the light
	 * parameters aren't changing then don't bother doing this every frame!
	 * 
	 * @param light
	 *            - The light source.
	 */
	private void loadLightVariables(Light light) {
		shader.lightBias.loadVec2(light.getLightBias());
		shader.lightDirection.loadVec3(light.getDirection());
		shader.lightColour.loadVec3(light.getColour().getVector());
	}

	/**
	 * Loads up the camera uniform variables to the shader.
	 * 
	 * @param camera
	 *            - The scene's camera.
	 */
	private void loadCameraVariables(ICamera camera) {
		shader.projectionViewMatrix.loadMatrix(camera.getProjectionViewMatrix());
		shader.cameraPos.loadVec3(camera.getPosition());
		shader.nearFarPlanes.loadVec2(camera.getNearPlane(), camera.getFarPlane());
	}

	/**
	 * Updates the "waveTime" variables and loads it to the shader. This
	 * ever-increasing value is used in the distortion calculation. Because this
	 * value changes over time it allows the distortion to also change over
	 * time. Without this changing variable the water distortion would be still
	 * and un-moving.
	 */
	private void updateTime() {
		time += WAVE_SPEED;
		shader.waveTime.loadFloat(time);
	}

}
