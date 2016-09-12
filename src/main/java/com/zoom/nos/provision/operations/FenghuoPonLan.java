package com.zoom.nos.provision.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.FenghuoTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.FenghuoTl1Session;

public class FenghuoPonLan extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(FenghuoPonLan.class);

	private FenghuoTl1Session session = null;

	public FenghuoPonLan(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new FenghuoTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "", 0, wo
				.getTl1User(), wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
		// open session
		session.open();
	}

	public WoResult alterRate() throws ZtlException {
		StringBuffer cmd = null;
		// 限速
		// CFG-LANPORT::OLTID=198.142.0.22,PONID=NA-NA-1-2,ONUIDTYPE=MAC,
		// ONUID=01020100,ONUPORT=NA-NA-NA-1:CTAG::BW=2M;
		cmd = new StringBuffer();
		cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-NA-");
		if (wo.getShelfId()!=null && !wo.getShelfId().equals("-1")) {
			cmd.append(wo.getShelfId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getFrameId());
		cmd.append(",ONUIDTYPE=MAC");
		cmd.append(",ONUID=").append(wo.getMacAddress());
		cmd.append(",ONUPORT=NA-NA-NA-");
		cmd.append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		if (wo.getAtucRate() < 1024) {
			cmd.append("BW=").append(wo.getAtucRate()).append("K");
		} else {
			cmd.append("BW=").append(wo.getAtucRate() / 1024).append("M");
		}
		cmd.append(";");

		FenghuoTL1ResponseMessage rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	/**
	 * 
	 */
	public WoResult close() throws ZtlException {
		StringBuffer cmd = null;
		// 关端口
		// DACT-LANPORT::OLTID=198.142.0.22,PONID=NA-NA-1-2,ONUIDTYPE=MAC,
		//   ONUID=01020100,ONUPORT=NA-NA-NA-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("DACT-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-NA-");
//		if (wo.getShelfId()!=-1) {
		if (wo.getShelfId()!=null && !wo.getShelfId().equals("-1")) {
			cmd.append(wo.getShelfId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getFrameId());
		cmd.append(",ONUIDTYPE=MAC");
		cmd.append(",ONUID=").append(wo.getMacAddress());
		cmd.append(",ONUPORT=NA-NA-NA-");
		cmd.append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		FenghuoTL1ResponseMessage rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 */
	public WoResult open() throws ZtlException {
		StringBuffer cmd = null;
		// 开端口
		//ACT-LANPORT::OLTID=198.142.0.22,PONID=NA-NA-1-2,ONUIDTYPE=MAC,
		//  ONUID=01020100,ONUPORT=NA-NA-NA-1:CTAG::;
		
		//ACT-LANPORT::OLTID=10.230.7.2,PONID=NA-NA-1-1,ONUIDTYPE=MAC,ONUID=FHTT00080624,ONUPORT=NA-NA-NA-1:145251::;
		cmd = new StringBuffer();
		cmd.append("ACT-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-NA-");
//		if (wo.getShelfId()!=-1) {
		if (wo.getShelfId()!=null && !wo.getShelfId().equals("-1")) {
			cmd.append(wo.getShelfId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getFrameId());
		cmd.append(",ONUIDTYPE=MAC");
		cmd.append(",ONUID=").append(wo.getMacAddress());
		cmd.append(",ONUPORT=NA-NA-NA-");
		cmd.append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		FenghuoTL1ResponseMessage rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn()
							+ rm.getEnDesc());
			return new WoResult(rm);
		}
		//限速
		return this.alterRate();
	}

	/**
	 * 释放资源
	 */
	public void destruction() {
		session.close();
	}

}