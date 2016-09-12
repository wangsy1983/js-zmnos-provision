package com.zoom.nos.provision.snmp;

import org.snmp4j.PDU;

import com.zoom.nos.provision.ErrorConst;

public class  SnmpHandlerException extends Exception {

	private static final long serialVersionUID = -2158007129679255005L;

	/*
	 * noError(0),tooBig(1),noSuchName(2),badValue(3),readOnly(4),genErr(5)
	 */
	private long errorStatus = ErrorConst.UnknowError;

	private String errorStatusText;

	private int errorIndex;

	public SnmpHandlerException(String message) {
		super(message);
	}

	/**
	 * pdu errorStatus, errorIndex add to exception
	 * 
	 * @param pdu
	 */
	public SnmpHandlerException(PDU pdu) {
		this(pdu.getErrorStatus(), pdu.getErrorIndex());
	}

	/**
	 * 
	 * @param errorStatus
	 * @param errorIndex
	 */
	public SnmpHandlerException(int errorStatus, int errorIndex) {
		super("errorStatusText=" + PDU.toErrorStatusText(errorStatus)
				+ "; errorStatus=" + errorStatus + "; errorIndex=" + errorIndex);
		this.errorStatusText = PDU.toErrorStatusText(errorStatus);
		this.errorStatus = errorStatus;
		this.errorIndex = errorIndex;
	}

	/**
	 * ²úÉú³¬Ê±´íÎó
	 * 
	 * @return
	 */
	public static SnmpHandlerException generateTimeOutSnmpHandlerException() {
		SnmpHandlerException e = new SnmpHandlerException("time out");
		e.setErrorStatus(ErrorConst.SNMP_TIMEOUT);
		e.setErrorStatusText("time out");
		e.setErrorIndex(0);
		return e;
	}

	/**
	 * @return the errorStatus
	 */
	public long getErrorStatus() {
		return errorStatus;
	}

	/**
	 * @param errorStatus the errorStatus to set
	 */
	public void setErrorStatus(long errorStatus) {
		this.errorStatus = errorStatus;
	}

	/**
	 * @return the errorStatusText
	 */
	public String getErrorStatusText() {
		return errorStatusText;
	}

	/**
	 * @param errorStatusText the errorStatusText to set
	 */
	public void setErrorStatusText(String errorStatusText) {
		this.errorStatusText = errorStatusText;
	}

	/**
	 * @return the errorIndex
	 */
	public int getErrorIndex() {
		return errorIndex;
	}

	/**
	 * @param errorIndex the errorIndex to set
	 */
	public void setErrorIndex(int errorIndex) {
		this.errorIndex = errorIndex;
	}

}
