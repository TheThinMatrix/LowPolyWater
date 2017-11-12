package fbos;

import org.lwjgl.opengl.GL30;

/**Represents a render buffer attachment.
 * @author Karl
 *
 */
public class RenderBufferAttachment extends Attachment {

	private final int format;

	public RenderBufferAttachment(int format) {
		this.format = format;
	}

	@Override
	public void init(int attachment, int width, int height, int samples) {
		int buffer = GL30.glGenRenderbuffers();
		super.setBufferId(buffer);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, buffer);
		GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, format, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_RENDERBUFFER, buffer);
	}

	@Override
	public void delete() {
		GL30.glDeleteRenderbuffers(super.getBufferId());
	}

}
