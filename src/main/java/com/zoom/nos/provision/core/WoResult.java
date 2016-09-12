package com.zoom.nos.provision.core;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.FenghuoTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.HwTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;

public class WoResult {
	/**
	 * 成功，没有错误
	 */
	public final static WoResult SUCCESS = new WoResult("success", "执行成功。");
	
	/**
	 * 不需要注册成功，没有错误
	 */
	public final static WoResult not_need_register = new WoResult("no_need_register", "之前注册过");
	
	
	/**
	 * RMS工单之前已经注册
	 */
	public final static WoResult rms_not_need_register = new WoResult("rm_not_need_register", "RMS工单之前已经注册");
	
	
	/**
	 * RMS工单需要注册
	 */
	public final static WoResult rms_need_register = new WoResult("rms_need_register", "RMS工单需要注册");

	// codeName
	private String code;

	// descr
	private String descr;

	// 类型
	private short type;

	/**
	 * 
	 * @param codeName
	 * @param descr
	 */
	public WoResult(String code, String descr) {
		this.code = code;
		this.descr = descr;
		this.type = 0;
	}

	/**
	 * 华为
	 * @param rm
	 */
	public WoResult(HwTL1ResponseMessage rm) {
		this.code = rm.getEn();
		this.descr = rm.getEnDesc();
		this.type = 11;
	}

	
	/**
	 * 中兴
	 * @param rm
	 */
	public WoResult(ZteTL1ResponseMessage rm) {
		this.code = rm.getEn();
		this.descr = rm.getEnDesc();
		this.type = 12;
	}
	
	/**
	 * Alcatel
	 * @param rm
	 */
	public WoResult(AlcatelTL1ResponseMessage rm) {
		this.code = rm.getEn();
		this.descr = rm.getEnDesc();
		this.type = 13;
	}
	
	/**
	 * 烽火
	 * @param rm
	 */
	public WoResult(FenghuoTL1ResponseMessage rm) {
		this.code = rm.getEn();
		this.descr = rm.getEnDesc();
		this.type = 14;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	// ----------------------------------

	/**
	 * @return the codeName
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param codeName
	 *            the codeName to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * @param descr
	 *            the descr to set
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * @return the type
	 */
	public short getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(short type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((descr == null) ? 0 : descr.hashCode());
		result = prime * result + type;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WoResult other = (WoResult) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (descr == null) {
			if (other.descr != null)
				return false;
		} else if (!descr.equals(other.descr))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
