package rendering;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.util.vector.Vector4f;

import display.Window;
import fbos.Attachment;
import fbos.Fbo;
import fbos.RenderBufferAttachment;
import fbos.TextureAttachment;
import terrains.Terrain;
import utils.OpenGlUtils;
import water.WaterTile;
import waterRendering.WaterRenderer;

/**
 * The overall rendering pipeline for rendering a frame. This is in charge of
 * setting up the display, rendering the scene, updating the display, and
 * closing the display when the game is closed.
 * 
 * @author Karl
 *
 */
public class RenderEngine {

	private static final float REFRACT_OFFSET = 1f;
	private static final float REFLECT_OFFSET = 0.1f;

	private final Window window;
	private final WaterRenderer waterRenderer;
	private final Fbo reflectionFbo;
	private final Fbo refractionFbo;

	/**
	 * Sets up the render engine and initialises the display. It also creates
	 * the FBOs for the reflection and refraction passes. These FBOs allow the
	 * scene to be rendered to textures which can then be sampled in the shader.
	 * The dimensions of the refraction FBO have been scaled down for
	 * performance reasons, although this results in a slight decrease in
	 * quality.
	 * 
	 * The refraction FBO is set up with a texture depth attachment, so that the
	 * image of the depth buffer can be sampled in the shaders. This is not
	 * necessary for the reflection pass.
	 * 
	 * @param fps
	 *            - The FPS cap.
	 * @param displayWidth
	 *            - The width of the display in pixels.
	 * @param displayHeight
	 *            - The height of the display in pixels.
	 */
	public RenderEngine(int fps, int displayWidth, int displayHeight) {
		this.window = Window.newWindow(displayWidth, displayHeight, fps).antialias(true).create();
		this.waterRenderer = new WaterRenderer();
		this.refractionFbo = createWaterFbo(displayWidth / 2, displayHeight / 2, true);
		this.reflectionFbo = createWaterFbo(displayWidth, displayHeight, false);
	}

	/**
	 * Carries out all the rendering for a frame. First the scene is rendered to
	 * the reflection texture and the refraction texture using the FBOs. This
	 * creates two images of the scene which can then be used to texture the
	 * water. The main render pass then takes place, rendering the scene
	 * (including the water) to the screen.
	 * 
	 * @param terrain
	 *            - The terrain in the scene.
	 * @param water
	 *            - The water in the scene.
	 * @param camera
	 *            - The scene's camera.
	 * @param light
	 *            - The light being used to illuminate the scene.
	 */
	public void render(Terrain terrain, WaterTile water, ICamera camera, Light light) {
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		doReflectionPass(terrain, camera, light, water.getHeight());
		doRefractionPass(terrain, camera, light, water.getHeight());
		GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		doMainRenderPass(terrain, water, camera, light);
	}

	/**
	 * @return The current display.
	 */
	public Window getWindow() {
		return window;
	}

	/**
	 * Deletes the FBOs and closes the display when the game closes.
	 */
	public void close() {
		reflectionFbo.delete();
		refractionFbo.delete();
		waterRenderer.cleanUp();
		window.destroy();
	}

	/**
	 * Prepares for a rendering pass. The depth and colour buffers of the
	 * current framebuffer are cleared and a few other default settings are set.
	 */
	private void prepare() {
		GL11.glClearColor(1f, 1f, 1f, 1f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL32.glProvokingVertex(GL32.GL_FIRST_VERTEX_CONVENTION);
		OpenGlUtils.cullBackFaces(true);
		OpenGlUtils.enableDepthTesting(true);
		OpenGlUtils.antialias(true);
	}

	/**
	 * Carries out the reflection pass, by rendering the scene to the reflection
	 * FBO. The camera is inverted before rendering to render the reflection of
	 * the scene. After rendering the camera is reverted to its original
	 * position. The clip plane used when rendering the scene ensures that only
	 * things above the water get rendered to the reflection texture, as things
	 * under the water shouldn't be getting reflected.
	 * 
	 * @param terrain
	 *            - The terrain in the scene.
	 * @param camera
	 *            - The scene's camera
	 * @param light
	 *            - The light in the scene.
	 * @param waterHeight
	 *            - The height of the water.
	 */
	private void doReflectionPass(Terrain terrain, ICamera camera, Light light, float waterHeight) {
		reflectionFbo.bindForRender(0);
		camera.reflect();
		prepare();
		terrain.render(camera, light, new Vector4f(0, 1, 0, -waterHeight + REFLECT_OFFSET));
		camera.reflect();
		reflectionFbo.unbindAfterRender();
	}

	/**
	 * Renders the scene to the refraction FBO. The scene is rendered from the
	 * normal camera position, and the result is stored in the refraction
	 * texture. A clipping plane is used to ensure that only parts of the scene
	 * that are under the water are rendered to the refraction FBO.
	 * 
	 * @param terrain
	 *            - The terrain.
	 * @param camera
	 *            - The camera being used in the scene.
	 * @param light
	 *            - The scene's light.
	 * @param waterHeight
	 *            - The height of the water in the world.
	 */
	private void doRefractionPass(Terrain terrain, ICamera camera, Light light, float waterHeight) {
		refractionFbo.bindForRender(0);
		prepare();
		terrain.render(camera, light, new Vector4f(0, -1, 0, waterHeight + REFRACT_OFFSET));
		refractionFbo.unbindAfterRender();
	}

	/**
	 * Renders the entire scene (terrain and water) to the screen. No clip plane
	 * is used here, so that the entire scene is rendered. Both the terrain and
	 * water are rendered during this pass.
	 * 
	 * @param terrain
	 *            - The terrain in the scene.
	 * @param water
	 *            - The water in the scene.
	 * @param camera
	 *            - The camera.
	 * @param light
	 *            - The light.
	 */
	private void doMainRenderPass(Terrain terrain, WaterTile water, ICamera camera, Light light) {
		prepare();
		terrain.render(camera, light, new Vector4f(0, 0, 0, 0));
		waterRenderer.render(water, camera, light, reflectionFbo.getColourBuffer(0), refractionFbo.getColourBuffer(0),
				refractionFbo.getDepthBuffer());
		window.update();
	}

	/**
	 * Sets up an FBO for one of the extra render passes. The FBO is initialised
	 * with a texture colour attachment, and can be initialised with either a
	 * render buffer or texture attachment for the depth buffer.
	 * 
	 * @param width
	 *            - The width of the FBO in pixels.
	 * @param height
	 *            - The height of the FBO in pixels.
	 * @param useTextureForDepth
	 *            - Whether the depth buffer attachment should be a texture or a
	 *            render buffer.
	 * @return The completed FBO.
	 */
	private static Fbo createWaterFbo(int width, int height, boolean useTextureForDepth) {
		Attachment colourAttach = new TextureAttachment(GL11.GL_RGBA8);
		Attachment depthAttach;
		if (useTextureForDepth) {
			depthAttach = new TextureAttachment(GL14.GL_DEPTH_COMPONENT24);
		} else {
			depthAttach = new RenderBufferAttachment(GL14.GL_DEPTH_COMPONENT24);
		}
		return Fbo.newFbo(width, height).addColourAttachment(0, colourAttach).addDepthAttachment(depthAttach).init();
	}

}
