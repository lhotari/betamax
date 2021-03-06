package betamax.util.servlet

import javax.servlet.ServletOutputStream
import javax.servlet.http.*

class MockHttpServletResponse extends AbstractMockServletMessage implements HttpServletResponse {

	int status
	String message

	private boolean written = false
	private ServletOutputStream outputStream
	private PrintWriter writer

	void sendError(int sc, String msg) {
		status = sc
		message = msg
	}

	void sendError(int sc) {
		status = sc
	}

	void setStatus(int sc, String sm) {
		status = sc
		message = sm
	}

	ServletOutputStream getOutputStream() {
		if (!outputStream) {
			if (written) {
				throw new IllegalStateException()
			} else {
				written = true
				outputStream = new MockServletOutputStream(this)
			}
		}
		outputStream
	}

	PrintWriter getWriter() {
		if (!writer) {
			if (written) {
				throw new IllegalStateException()
			} else {
				written = true
				writer = new PrintWriter(new MockServletOutputStream(this), characterEncoding)
			}
		}
		writer
	}

	void addCookie(Cookie cookie) {
		throw new UnsupportedOperationException()
	}

	String encodeURL(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeRedirectURL(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeUrl(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeRedirectUrl(String url) {
		throw new UnsupportedOperationException()
	}

	void sendRedirect(String location) {
		throw new UnsupportedOperationException()
	}

	void setContentLength(int len) {
		throw new UnsupportedOperationException()
	}

	void setBufferSize(int size) {
		throw new UnsupportedOperationException()
	}

	int getBufferSize() {
		throw new UnsupportedOperationException()
	}

	void flushBuffer() {
		throw new UnsupportedOperationException()
	}

	void resetBuffer() {
		throw new UnsupportedOperationException()
	}

	boolean isCommitted() {
		throw new UnsupportedOperationException()
	}

	void reset() {
		throw new UnsupportedOperationException()
	}

}
