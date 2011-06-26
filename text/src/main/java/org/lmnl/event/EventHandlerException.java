package org.lmnl.event;

public class EventHandlerException extends Exception {

	private static final long serialVersionUID = 1L;

	public EventHandlerException() {
		super();
	}

	public EventHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventHandlerException(String message) {
		super(message);
	}

	public EventHandlerException(Throwable cause) {
		super(cause);
	}
}
