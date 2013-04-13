package com.precog.client.rest;

public class HttpException extends Exception {
	private static final long serialVersionUID = 2676948062081356496L;

	public HttpException() {
		super();
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}
}
