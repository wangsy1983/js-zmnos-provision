package com.zoom.nos.provision.exception;

public class ZtlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1359437577960972607L;
	
	private long errorcode;

	public ZtlException(long errorcode) {
		super("" + errorcode);
		this.errorcode = errorcode;
	}

	public ZtlException(long errorcode, String message) {
		super(message);
		this.errorcode = errorcode;
	}

	public ZtlException(long errorcode, Throwable cause) {
		super(cause);
		this.errorcode = errorcode;
	}

	public ZtlException(long errorcode, String message, Throwable cause) {
		super(message, cause);
		this.errorcode = errorcode;
	}

	/**
	 * @return the errorcode
	 */
	public long getErrorcode() {
		return errorcode;
	}

	/**
	 * @param errorcode
	 *            the errorcode to set
	 */
	public void setErrorcode(long errorcode) {
		this.errorcode = errorcode;
	}

}
