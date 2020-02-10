package org.emau.icmvc.ttp.gics.emergency;
public class DecodingException extends Exception {

	private static final long serialVersionUID = 5883471481000632443L;

	public DecodingException() {
		super();
	}

	public DecodingException(String message, Throwable cause) {
		super(message, cause);
	}

	public DecodingException(String message) {
		super(message);
	}

	public DecodingException(Throwable cause) {
		super(cause);
	}
}
