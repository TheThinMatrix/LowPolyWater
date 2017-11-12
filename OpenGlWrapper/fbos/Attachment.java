package fbos;

/**
 * Represents an FBO attachment.
 * 
 * @author Karl
 *
 */
public abstract class Attachment {

	private int bufferId;
	private boolean isDepthAttach = false;

	public int getBufferId() {
		return bufferId;
	}

	/**
	 * Creates the attachment by initializing the necessary storage, and then
	 * attaches it to the FBO. This method also needs to set the bufferId after
	 * the storage has been initialized.
	 * 
	 * @param attachment
	 *            - The type of attachment, e.g. GL_COLOR_ATTACHMENT0, or
	 *            GL_DEPTH_ATTACHMENT, etc.
	 * @param width
	 *            - The width of the FBO in pixels
	 * @param height
	 *            - The height of the FBO in pixels
	 * @param samples
	 *            - The number of samples that this FBO uses (for
	 *            multisampling). This is 0 if multisampling is not used.
	 */
	public abstract void init(int attachment, int width, int height, int samples);

	public abstract void delete();

	/**
	 * Sets the ID of the storage being used for this attachment.
	 * 
	 * @param id
	 *            - The ID of either the texture or the render buffer being used
	 *            for this attachment.
	 */
	protected void setBufferId(int id) {
		this.bufferId = id;
	}

	/**
	 * Indicate that this attachment is a depth buffer attachment.
	 */
	protected void setAsDepthAttachment() {
		this.isDepthAttach = true;
	}

	/**
	 * @return True if this attachment is being used as a depth attachment.
	 */
	protected boolean isDepthAttachment() {
		return isDepthAttach;
	}

}
