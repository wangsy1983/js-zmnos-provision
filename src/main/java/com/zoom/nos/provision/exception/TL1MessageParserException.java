package com.zoom.nos.provision.exception;

import com.zoom.nos.provision.ErrorConst;

public class TL1MessageParserException extends ZtlException {
	/**
	 * 
	 */
	public TL1MessageParserException() {
		super(ErrorConst.TL1MessageParserErr);
	}

	/**
	 * @param message
	 */
	public TL1MessageParserException(String message) {
		super(ErrorConst.TL1MessageParserErr, message);
	}

	/**
	 * @param cause
	 */
	public TL1MessageParserException(Throwable cause) {
		super(ErrorConst.TL1MessageParserErr, cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TL1MessageParserException(String message, Throwable cause) {
		super(ErrorConst.TL1MessageParserErr, message, cause);
	}
}
