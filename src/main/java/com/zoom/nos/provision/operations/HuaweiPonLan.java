package com.zoom.nos.provision.operations;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.HwTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.HuaweiTl1Session;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class HuaweiPonLan extends AbstractOperations{
	private static Logger log = LoggerFactory.getLogger(HuaweiPonLan.class);

	private HuaweiTl1Session session = null;
	
	public HuaweiPonLan(WorkOrder wo) throws ZtlException {
		super(wo);
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			wo.setTl1ServerPort(9819);
		}
		session = new HuaweiTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "",0,wo
				.getTl1User(), wo.getTl1Password(),NosEnv.socket_timeout_tl1server);
		//open session
		session.open();
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException  {
		return open();
	}

	/**
	 * 
	 */
	public WoResult close() throws ZtlException  {
		StringBuffer cmd = null;
		//关端口
		cmd = new StringBuffer();
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
//			DACT-LANPORT::ONUIP=172.16.16.203,ONUPORT=NA-0-1-1:111::;
			cmd.append("DACT-ETHPORT::DEV=").append(wo.getNeIp());
		} else{
			String did=CoreService.ticketControlService.
			getDeviceDid(wo.getTl1ServerIp(), wo.getNeIp(), "0"+wo.getCityId());
			if(StringUtils.isBlank(did)){
				throw new ZtlException(ErrorConst.noSuchDid);
			}
			cmd.append("DACT-ETHPORT::DID=").append(did);
		}
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::;");
		HwTL1ResponseMessage rm = session.exeHwCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	public WoResult open() throws ZtlException  {

		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;
		//取速率模板
		String lineProfile=CoreService.ticketControlService.getLineProfileName(wo);
		if(StringUtils.isBlank(lineProfile)){
			if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
				
			}else{
				throw new ZtlException(ErrorConst.lineProfileNoutFound); 
			}
		}
		cmd = new StringBuffer();
		String tmpHead = "";
 		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
 			tmpHead="DEV="+wo.getNeIp(); 
 		} else {
			//取DID
			String did=CoreService.ticketControlService.
			getDeviceDid(wo.getTl1ServerIp(), wo.getNeIp(), "0"+wo.getCityId());
			if(StringUtils.isBlank(did)){
				throw new ZtlException(ErrorConst.noSuchDid);
			}
			//设速率模板
			//MOD-SERVICEPORT::DEV=10.100.11.129,FN=0,SN=1,PN=1:ctag::UV=untagged,MTX=6,MRX=6;
			tmpHead="DID="+did; 
		}
 		cmd.append("MOD-SERVICEPORT::").append(tmpHead);
 		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			
		} else if(SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)){
			Short line = 3;
			wo.setLineType(line);
			//取速率模板
			wo.setUpOrDown("up");
			String upLineProfile=CoreService.ticketControlService.getLineProfileName(wo);
			if(StringUtils.isBlank(upLineProfile)){
				throw new ZtlException(ErrorConst.lineProfileNoutFound); 
			}
			
			wo.setUpOrDown("down");
			String downLineProfile=CoreService.ticketControlService.getLineProfileName(wo);
			if(StringUtils.isBlank(downLineProfile)){
				throw new ZtlException(ErrorConst.lineProfileNoutFound); 
			}
		
			line = 2;
			wo.setLineType(line);
			cmd.append("UV=untagged");
			cmd.append(",MTX=").append(upLineProfile);
			cmd.append(",MRX=").append(downLineProfile); 
		} else {
			cmd.append("UV=untagged");
			cmd.append(",MTX=").append(lineProfile);
			cmd.append(",MRX=").append(lineProfile); 
		}
		cmd.append(";");  
		rm = session.exeHwCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}

		//开端口
		cmd = new StringBuffer();
		cmd.append("ACT-ETHPORT::").append(tmpHead);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::;"); 
		rm = session.exeHwCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}


	/**
	 * 释放资源
	 */
	public void destruction() {
		session.close();
	}
	
	protected WoResult closeService(int serviceVlan)throws ZtlException{
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;

		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
			
		// 绑定VLAN
		// DASS-ETHPORTANDVLAN::DID=123456,FN=0,SN=0,PN=1:ctag::VLANID=100;
		cmd = new StringBuffer();
		cmd.append("DASS-ETHPORTANDVLAN::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANID=");
		cmd.append(serviceVlan);
		cmd.append(";");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	protected WoResult openService(int defaultVlan, int serviceVlan)throws ZtlException{
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;

		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
			
		// add service vlan
		this.createVlan(did, serviceVlan);
		
		// 绑定VLAN
		// ASS-ETHPORTANDVLAN::DID=123456,FN=0,SN=0,PN=1:ctag::VLANID=100;
		cmd = new StringBuffer();
		cmd.append("ASS-ETHPORTANDVLAN::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANID=");
		cmd.append(serviceVlan);
		cmd.append(";");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}
	
	/**
	 * 
	 * @param did
	 * @param vlanid
	 * @return
	 * @throws ZtlException
	 */
	private WoResult createVlan(String did, int vlanid)throws ZtlException{
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;

		// add vlan
		//ADD-VLAN::DID=123456:ctag::VLANID=100,VLANTYPE=SMART,VLANATTR=COMMON;
		cmd = new StringBuffer();
		cmd.append("ADD-VLAN::DID=").append(did);
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANID=").append(vlanid);
		cmd.append(",VLANTYPE=SMART,VLANATTR=COMMON;");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
					+ rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}
}
