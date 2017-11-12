package fbos;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL30;

/**
 * A class which makes the construction of FBOs a bit easier.
 * 
 * @author Karl
 *
 */
public class FboBuilder {

	private final int width;
	private final int height;
	private final int samples;

	private Map<Integer, Attachment> colourAttachments = new HashMap<Integer, Attachment>();
	private Attachment depthAttachment;

	/**
	 * @param width - Width of the FBO in pixels.
	 * @param height - Height of the FBO in pixels.
	 * @param samples - The number of samples being used for multisampling (0 means no multisampling).
	 */
	protected FboBuilder(int width, int height, int samples) {
		this.width = width;
		this.height = height;
		this.samples = samples;
	}

	/**Adds a colour attachment to the FBO that is being created.
	 * @param index - The index of the colour buffer that this attachment should be attached to.
	 * @param attachment - The colour attachment.
	 * @return
	 */
	public FboBuilder addColourAttachment(int index, Attachment attachment) {
		colourAttachments.put(index, attachment);
		return this;
	}

	/**Adds a depth attachment to the FBO that is being created.
	 * @param attachment - The depth attachment.
	 * @return
	 */
	public FboBuilder addDepthAttachment(Attachment attachment) {
		this.depthAttachment = attachment;
		attachment.setAsDepthAttachment();
		return this;
	}

	/**
	 * Actually creates the FBO object and the attachments, and attaches the
	 * attachments to the FBO.
	 * 
	 * @return
	 */
	public Fbo init() {
		int fboId = createFbo();
		createColourAttachments();
		createDepthAttachment();
		return new Fbo(fboId, width, height, colourAttachments, depthAttachment);
	}

	/**
	 * Creates an new FBO object.
	 * 
	 * @return The newly created FBO.
	 */
	private int createFbo() {
		int fboId = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
		return fboId;
	}

	/**
	 * Initialises and attaches all the colour attachments for this FBO.
	 */
	private void createColourAttachments() {
		for (Entry<Integer, Attachment> entry : colourAttachments.entrySet()) {
			Attachment attachment = entry.getValue();
			int attachmentId = GL30.GL_COLOR_ATTACHMENT0 + entry.getKey();
			attachment.init(attachmentId, width, height, samples);
		}
	}

	/**
	 * Creates and attaches the depth attachment, if there is one.
	 */
	private void createDepthAttachment() {
		if (depthAttachment != null) {
			depthAttachment.init(GL30.GL_DEPTH_ATTACHMENT, width, height, samples);
		}
	}

}
