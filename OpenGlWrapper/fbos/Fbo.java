package fbos;

import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Represents an Frame Buffer Object. Holds the ID of the object, and the
 * width/height of the FBO.
 * 
 * @author Karl
 *
 */
public class Fbo {

	private final int fboId;
	private final int width;
	private final int height;

	private final Map<Integer, Attachment> colourAttachments;
	private final Attachment depthAttachment;

	protected Fbo(int fboId, int width, int height, Map<Integer, Attachment> attachments, Attachment depthAttachment) {
		this.fboId = fboId;
		this.width = width;
		this.height = height;
		this.colourAttachments = attachments;
		this.depthAttachment = depthAttachment;
	}

	/**
	 * Copy the contents of a colour attachment of this FBO to the screen.
	 * 
	 * @param colourIndex
	 *            - The index of the colour buffer that should be blitted.
	 */
	public void blitToScreen(int colourIndex) {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		GL11.glDrawBuffer(GL11.GL_BACK);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboId);
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0 + colourIndex);
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, Display.getWidth(), Display.getHeight(),
				GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	/**
	 * Copy the contents of this FBO to another FBO. This can be used to resolve
	 * multisampled FBOs.
	 * 
	 * @param srcColourIndex
	 *            - Index of the colour buffer in this (the source) FBO.
	 * @param target
	 *            - The target FBO.
	 * @param targetColourIndex
	 *            - The index of the target colour buffer in the target FBO.
	 */
	public void blitToFbo(int srcColourIndex, Fbo target, int targetColourIndex) {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.fboId);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + targetColourIndex);

		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fboId);
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0 + srcColourIndex);

		int bufferBit = depthAttachment != null && target.depthAttachment != null
				? GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT : GL11.GL_COLOR_BUFFER_BIT;
		GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, target.width, target.height, bufferBit, GL11.GL_NEAREST);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	/**
	 * Gets the ID of the buffer being used as the colour buffer (either a
	 * render buffer or a texture).
	 * 
	 * @param colourIndex
	 *            - The index of the colour attachment.
	 * @return The ID of the buffer.
	 */
	public int getColourBuffer(int colourIndex) {
		return colourAttachments.get(colourIndex).getBufferId();
	}

	/**
	 * @return The ID of the buffer containing the depth buffer (either a render
	 *         buffer or a texture).
	 */
	public int getDepthBuffer() {
		return depthAttachment.getBufferId();
	}

	/**
	 * @return True if this framebuffer has been correctly set up and is
	 *         complete.
	 */
	public boolean isComplete() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
		boolean complete = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		return complete;
	}

	/**
	 * Bind the FBO so that it can be rendered to. Anything rendered while the
	 * FBO is bound will be rendered to the FBO.
	 * 
	 * @param colourIndex
	 *            - The index of the colour buffer that should be drawn to.
	 */
	public void bindForRender(int colourIndex) {
		// should add support for binding multiple colour attachments
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0 + colourIndex);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fboId);
		GL11.glViewport(0, 0, width, height);
	}

	/**
	 * Switch back to the default frame buffer.
	 */
	public void unbindAfterRender() {
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
		GL11.glDrawBuffer(GL11.GL_BACK);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	/**
	 * Delete the FBO and attachments.
	 */
	public void delete() {
		for (Attachment attachment : colourAttachments.values()) {
			attachment.delete();
		}
		if (depthAttachment != null) {
			depthAttachment.delete();
		}
	}

	/**
	 * Starts the creation of a new FBO (without multisampling)
	 * 
	 * @param width
	 *            - The width in pixels of the FBO.
	 * @param height
	 *            - The height in pixels.
	 * @return The builder object for the FBO.
	 */
	public static FboBuilder newFbo(int width, int height) {
		return new FboBuilder(width, height, 0);
	}

	/**
	 * Starts the creation of a new multisampled FBO.
	 * 
	 * @param width
	 *            - Width in pixels.
	 * @param height
	 *            - Height in pixels.
	 * @param samples
	 *            - Number of samples being used for multisampling.
	 * @return The builder object.
	 */
	public static FboMsBuilder newMultisampledFbo(int width, int height, int samples) {
		return new FboMsBuilder(new FboBuilder(width, height, samples));
	}

}
