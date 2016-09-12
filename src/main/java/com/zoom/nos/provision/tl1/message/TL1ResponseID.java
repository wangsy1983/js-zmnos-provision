package com.zoom.nos.provision.tl1.message;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.zoom.nos.provision.exception.TL1MessageParserException;

public class TL1ResponseID {
    private String ctag = null;
    private String completionCode = null;
    
	public String getCtag() {
		return ctag;
	}
	public void setCtag(String ctag) {
		this.ctag = ctag;
	}
	
	public String getCompletionCode() {
		return completionCode;
	}
	public void setCompletionCode(String completionCode) {
		this.completionCode = completionCode;
	}
	
	/**
	 * 
	 * @param text
	 * @return
	 * @throws TL1MessageParserException
	 */
	public static TL1ResponseID paser(String text) throws TL1MessageParserException {
		TL1ResponseID tl1ResponseID = new TL1ResponseID();
		int i = text.indexOf(" ");
		if (i == -1) {
			throw new TL1MessageParserException();
		} else {
			tl1ResponseID.ctag = text.substring(0, i);
			tl1ResponseID.completionCode = text.substring(i+1, text.length());
		}
		return tl1ResponseID;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
