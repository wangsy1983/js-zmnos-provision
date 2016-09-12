package com.zoom.nos.provision.tl1.message;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.exception.TL1MessageParserException;

public class TL1Header {
	private static Logger log = LoggerFactory.getLogger(TL1Header.class);
	private String sid;
	private String date;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * 
	 * @param text
	 * @throws TL1MessageParserException
	 */
	public static TL1Header paser(String text) throws TL1MessageParserException {
		TL1Header tl1Header = new TL1Header();
		int i = text.indexOf(" ");
		if (i == -1) {
			log.error("paser:" + text);
			throw new TL1MessageParserException();
		} else {
			tl1Header.sid = text.substring(0, i);
			tl1Header.date = text.substring(i + 1, text.length());
		}
		return tl1Header;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
