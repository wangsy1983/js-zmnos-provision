package com.zoom.nos.provision.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;

public abstract class AbstractOperations implements IOperations {
	private static Logger log = LoggerFactory
			.getLogger(AbstractOperations.class);
	protected WorkOrder wo;

	protected AbstractOperations(WorkOrder wo) {
		this.wo = wo;
	}

	/*
	 * =====================
	 */

	public WoResult openIptv() throws ZtlException {
//		throw new ZtlException(ErrorConst.wrongServiceStatus);
		return WoResult.SUCCESS;
	}

	public WoResult closeIptv() throws ZtlException {
//		throw new ZtlException(ErrorConst.wrongServiceStatus);
		return WoResult.SUCCESS;
	}
	 
	public WoResult openWBIptv()throws ZtlException {
//		throw new ZtlException(ErrorConst.wrongServiceStatus);
		return WoResult.SUCCESS;
	}
	 
	public WoResult closeWBIptv()throws ZtlException {
//		throw new ZtlException(ErrorConst.wrongServiceStatus);
		return WoResult.SUCCESS;
	}

	public WoResult openVoip() throws ZtlException {
		log.warn("null method");
		return WoResult.SUCCESS;
	}

	public WoResult closeVoip() throws ZtlException {
		log.warn("null method");
		return WoResult.SUCCESS;
	}

	public WoResult openVideo() throws ZtlException {
		throw new ZtlException(ErrorConst.wrongServiceStatus);
	}

	public WoResult closeVideo() throws ZtlException {
		throw new ZtlException(ErrorConst.wrongServiceStatus);
	}

	public WoResult registerOnu() throws ZtlException {
		log.warn("null method");
		return WoResult.SUCCESS;
	}
	
	public WoResult delOnu() throws ZtlException {
		log.warn("null method");
		return WoResult.SUCCESS;
	}
}
