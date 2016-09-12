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

public class FenghuoPonDsl extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(FenghuoPonDsl.class);

	private FenghuoTl1Session session = null;

	public FenghuoPonDsl(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new FenghuoTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "", 0, wo
				.getTl1User(), wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
		// open session
		session.open();
	}

	public WoResult alterRate() throws ZtlException {
		StringBuffer cmd = null;
		// 限速
		// CFG-DSLPORTBW::ONUIP=198.132.12.2,ONUPORT=NA-NA-3-1:CTAG::BW=4M;
		cmd = new StringBuffer();
		cmd.append("CFG-DSLPORTBW::ONUIP=").append(wo.getNeIp());
		cmd.append(",ONUPORT=NA-NA-");
		if (wo.getSlotId() != -1) {
			cmd.append(wo.getSlotId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-");
		if (wo.getPortId()!=-1) {
			cmd.append(wo.getPortId());
		} else {
			cmd.append("NA");
		}
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
		// DACT-DSLPORT::ONUIP=198.132.12.2,ONUPORT=NA-NA-3-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("DACT-DSLPORT::ONUIP=").append(wo.getNeIp());
		cmd.append(",ONUPORT=NA-NA-");
		if (wo.getSlotId() != -1) {
			cmd.append(wo.getSlotId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-");
		if (wo.getPortId()!=-1) {
			cmd.append(wo.getPortId());
		} else {
			cmd.append("NA");
		}
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
		//ACT-DSLPORT::ONUIP=198.132.12.2,ONUPORT=NA-NA-3-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("ACT-DSLPORT::ONUIP=").append(wo.getNeIp());
		cmd.append(",ONUPORT=NA-NA-");
		if (wo.getSlotId() != -1) {
			cmd.append(wo.getSlotId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-");
		if (wo.getPortId()!=-1) {
			cmd.append(wo.getPortId());
		} else {
			cmd.append("NA");
		}
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