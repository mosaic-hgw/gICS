package org.emau.icmvc.ttp.gics.emergency;
public class MissingPropertyException extends Exception {

	private static final long serialVersionUID = 1317308411168936884L;

	public MissingPropertyException() {
		super();
	}

	public MissingPropertyException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingPropertyException(String message) {
		super(message);
	}

	public MissingPropertyException(Throwable cause) {
		super(cause);
	}
}
