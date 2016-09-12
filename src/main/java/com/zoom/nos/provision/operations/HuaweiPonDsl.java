package com.zoom.nos.provision.operations;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.ServiceVlan;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.HwTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.HuaweiTl1Session;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class HuaweiPonDsl extends AbstractOperations {
	private static Logger log = LoggerFactory.getLogger(HuaweiPonDsl.class);

	private HuaweiTl1Session session = null;
	
	private static Short wotype_iptv = 31;

	public HuaweiPonDsl(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new HuaweiTl1Session(wo.getTl1ServerIp(), wo
				.getTl1ServerPort(), "", 0, wo.getTl1User(), wo
				.getTl1Password(), NosEnv.socket_timeout_tl1server);
		// open session
		session.open();
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException {

		StringBuffer cmd = null;

		// 取DID
		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}

		// 取速率模板
		String lineProfile = CoreService.ticketControlService
				.getLineProfileName(wo);
		if (StringUtils.isBlank(lineProfile)) {
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}

		// 操作前，关端口,不管是否成功
		close();
		// 设速率模板
		cmd = new StringBuffer();
		cmd.append("MOD-ADSLPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::LPROFID=").append(lineProfile);
		cmd.append(";");
		HwTL1ResponseMessage rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			log.debug("设速率模板失败，把端口再打开");
			cmd = new StringBuffer();
			cmd.append("ACT-ADSLPORT::DID=").append(did);
			cmd.append(",FN=").append(wo.getFrameId());
			cmd.append(",SN=").append(wo.getSlotId());
			cmd.append(",PN=").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::;");
			session.exeHwCmd(cmd.toString(), wo);
			return new WoResult(rm);
		}

		// 开端口
		cmd = new StringBuffer();
		cmd.append("ACT-ADSLPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::;");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	/**
	 * 销户
	 */
	public WoResult close() throws ZtlException {
		StringBuffer cmd = null; 
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			// LST-DSLPORT::ONUIP=10.71.227.51,ONUPORT=NA-0-2-5:CTAG::; XDSL
			// 检查端口状态
			String portStatus = "";
			cmd = new StringBuffer();
			cmd.append("LST-DSLPORT::ONUIP=").append(wo.getNeIp());
			cmd.append(",ONUPORT=NA");
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::;");

			HwTL1ResponseMessage rm = session.exeHwListCmd(cmd.toString(), wo);
			portStatus = rm.getResult().get("ADMINSTATUS");
			log.debug("portStatus=" + portStatus);
			if ("UP".equalsIgnoreCase(portStatus) || StringUtils.isBlank(portStatus)) {
				// 端口打开的，则关掉，其他情况忽略
				cmd = new StringBuffer();
				//DACT-DSLPORT::ONUIP=10.71.62.131,ONUPORT=NA-0-1-1:CTAG::;
				cmd.append("DACT-DSLPORT::ONUIP=").append(wo.getNeIp());
				cmd.append(",ONUPORT=NA");
				cmd.append("-").append(wo.getFrameId());
				cmd.append("-").append(wo.getSlotId());
				cmd.append("-").append(wo.getPortId());
				cmd.append(":");
				cmd.append(Ctag.getCtag());
				cmd.append("::;");
				rm = session.exeHwCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}
			}
			return WoResult.SUCCESS;
		}

		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}

//		// 检查端口状态
//		String portStatus = "";
//		cmd = new StringBuffer();
//		cmd.append("LST-DSLPORTDETAILINFO::DID=").append(did);
//		cmd.append(",FN=").append(wo.getFrameId());
//		cmd.append(",SN=").append(wo.getSlotId());
//		cmd.append(",PN=").append(wo.getPortId());
//		cmd.append(":");
//		cmd.append(Ctag.getCtag());
//		cmd.append("::;");
//
//		HwTL1ResponseMessage rm = session.exeHwListCmd(cmd.toString(), wo);
//		portStatus = rm.getResult().get("AtucStatus");
//		log.debug("portStatus=" + portStatus);
//		if ("Active".equalsIgnoreCase(portStatus)
//				|| "Activating".equalsIgnoreCase(portStatus)
//				|| StringUtils.isBlank(portStatus)) {
			// 端口打开的，则关掉，其他情况忽略
			cmd = new StringBuffer();
//			DACT-DSLPORT::ONUIP=10.71.62.131,ONUPORT=NA-0-1-1:CTAG::;
			cmd.append("DACT-ADSLPORT::DID=").append(did);
			cmd.append(",FN=").append(wo.getFrameId());
			cmd.append(",SN=").append(wo.getSlotId());
			cmd.append(",PN=").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::;");
			HwTL1ResponseMessage rm = session.exeHwCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
						+ rm.getEnDesc());
				return new WoResult(rm);
			}
//		}
		return WoResult.SUCCESS;
	}

	/**
	 * 开户
	 */
	public WoResult open() throws ZtlException {

		StringBuffer cmd = null;
		
		// 取速率模板
		String lineProfile = CoreService.ticketControlService
				.getLineProfileName(wo);
		if (StringUtils.isBlank(lineProfile)) {
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}
		
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			// 操作前，关端口,不管是否成功
			close();

			// 设速率模板  CFG-DSLPORTBW::ONUIP=10.71.62.131,ONUPORT=NA-0-1-1:CTAG::BW=6M;
			cmd = new StringBuffer();
			cmd.append("CFG-DSLPORTBW=").append(wo.getNeIp());
			cmd.append(",ONUPORT=NA");
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::BW=").append(lineProfile);
			cmd.append(";");
			HwTL1ResponseMessage rm = session.exeHwCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
			
			// 开端口 ACT-DSLPORT::ONUIP=10.71.62.131,ONUPORT=NA-0-1-1:CTAG::;
			cmd = new StringBuffer();
			cmd.append("ACT-DSLPORT::ONUIP=").append(wo.getNeIp());
			cmd.append(",ONUPORT=NA");
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::;");
			rm = session.exeHwCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
			return WoResult.SUCCESS;
		}
		
		// 取DID
		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
 
		// 操作前，关端口,不管是否成功
		close();
		// 设速率模板
		cmd = new StringBuffer();
		cmd.append("MOD-ADSLPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::LPROFID=").append(lineProfile);
		cmd.append(";");
		HwTL1ResponseMessage rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 开端口
		cmd = new StringBuffer();
		cmd.append("ACT-ADSLPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::;");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
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


	/**
	 * 关多业务
	 */
	protected WoResult closeService(int serviceVlan) throws ZtlException {
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;

		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}

		// 解绑定VLAN
		// DEL-SERVICEPORT::DID=12345,FN=0,SN=0,PN=0:ctag::VPI=8,VCI=35,MAVLANID=100;
		cmd = new StringBuffer();
		cmd.append("SERVICEPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VPI=").append(wo.getVpi());
		cmd.append(",VCI=").append(wo.getVci());
		cmd.append(",MAVLANID=").append(serviceVlan);
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
	 * 开多业务
	 */
	protected WoResult openService(int defaultVlan, int serviceVlan)
			throws ZtlException {
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;

		// get did
		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}

		// 取速率模板
		String lineProfile = CoreService.ticketControlService
				.getLineProfileName(wo);
		if (StringUtils.isBlank(lineProfile)) {
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}

		// add service vlan
		this.createVlan(did, serviceVlan);

		// 绑定VLAN
		// MOD-SERVICEPORT::DID=12345,FN=0,SN=0,PN=0:ctag::VPI=8,VCI=35,MTX=7,MRX=7,MAVLANID=100;
		cmd = new StringBuffer();
		cmd.append("MOD-SERVICEPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VPI=").append(wo.getVpi());
		cmd.append(",VCI=").append(wo.getVci());
		cmd.append(",MTX=").append(lineProfile);
		cmd.append(",MRX=").append(lineProfile);
		cmd.append(",MAVLANID=").append(serviceVlan);
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
	/**
	 * closeIptv
	 * 
	 * 
	 */
	public WoResult closeIptv() throws ZtlException{
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;
		// 取DID
		String did = CoreService.ticketControlService.getDeviceDid(wo.getTl1ServerIp(), wo
				.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
		
		//IPTV查询PVC 
		ServiceVlan serVlan =  CoreService.ticketControlService.getServiceVlan(
					2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			close();
			return WoResult.SUCCESS;
		}
		
//		;QUIT-NTV::DID=7340672,FN=0,SN=14,PN=21,VPI=0,VCI=35:CTAG::;
//		QUIT-NTV::DID=7366362,FN=0,SN=4,PN=31,VPI=0,VCI=35:CTAG::;
		cmd = new StringBuffer();
		cmd.append("QUIT-NTV::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
					+ rm.getEnDesc());
			return new WoResult(rm);
		}
//		DEL-SERVICEPORT::DID=7340672,FN=0,SN=14,PN=21:CTAG::VLANID=3999;
//		DEL-SERVICEPORT::DID=7366362,FN=0,SN=4,PN=31:CTAG::VLANID=3999;	
		cmd = new StringBuffer();
		cmd.append("DEL-SERVICEPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId()); 
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("VLANID=").append(serVlan.getVlan());
		cmd.append(";");
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
					+ rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}
	/**
	 * 开IPTV
	 */
	public WoResult openIptv() throws ZtlException {
		StringBuffer cmd = null;
		HwTL1ResponseMessage rm = null;
		//5600 CRT-SERVICEPORT::DID=7340672,FN=0,SN=14,PN=21:CTAG::VLANID=3999,VPI=0,VCI=35,TX=xdsl,RX=xdsl;
		//     CRT-SERVICEPORT::DID=7366362,FN=0,SN=4,PN=31:CTAG::VLANID=3999,VPI=0,VCI=35,TX=HW-1M-LAN,RX=HW-1M-LAN;
		
//		JOIN-NTVUSR::DID=7340672,FN=0,SN=14,PN=21:CTAG::VPI=0,VCI=35;
//		JOIN-NTVUSR::DID=7366362,FN=0,SN=4,PN=31:CTAG::AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,VPI=0,VCI=35,RCVGLBLV=ON,IGMPVLAN=4011,MAXBANDWIDTH=-1;
			
//		DACT-ADSLPORT::DID=7340672,FN=0,SN=14,PN=21:4186::;
//		DACT-ADSLPORT::DID=7366362,FN=0,SN=4,PN=31:6195::;
 
//		MOD-ADSLPORT::DID=7340672,FN=0,SN=14,PN=21:4185::LPROFID=HW-4M-FAST;
//		MOD-ADSLPORT::DID=7366362,FN=0,SN=4,PN=31:6196::LPROFID=HW-1M-FAST;
		
//		大连组播以下六句
//		1.LST-SERVICEPORT::DID=7340672,FN=0,SN=14,PN=21:CTAG::VLANID=3999;
//		2.CRT-SERVICEPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::VLANID=$vlan,VPI=$vpi,VCI=$vci,TX=$profilename,RX=$profilename;
//		3.JOIN-NTVUSR::DID=7366362,FN=0,SN=4,PN=31:CTAG::AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,VPI=0,VCI=35,RCVGLBLV=ON,IGMPVLAN=4011,MAXBANDWIDTH=-1;
//		其它地市以下三句
//		//DACT-ADSLPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::;
//		5.MOD-ADSLPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::LPROFID=$profilename;
//		6.ACT-ADSLPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::;

		
		
		// 取速率模板
		String lineProfile = CoreService.ticketControlService.getLineProfileName(wo);
		if (StringUtils.isBlank(lineProfile)) {
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}
		
		//IPTV查询PVC 
		ServiceVlan serVlan =  CoreService.ticketControlService.getServiceVlan(2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			open();
			return WoResult.SUCCESS;
		}
		
		// 取DID
		String did = CoreService.ticketControlService.getDeviceDid(wo.getTl1ServerIp(), wo
				.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
		cmd = new StringBuffer();
		cmd.append("CRT-SERVICEPORT::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANID=").append(serVlan.getVlan());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(",TX=").append(lineProfile);
		cmd.append(",RX=").append(lineProfile);
//		cmd.append(",TX=xdsl,RX=xdsl");
		cmd.append(";"); 
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		
		//沈阳		JOIN-NTVUSR::DID=7340672,FN=0,SN=14,PN=21:CTAG::VPI=0,VCI=35;	
		//大连等其它	JOIN-NTVUSR::DID=7366362,FN=0,SN=4,PN=31:CTAG::AUTH=2,MAXGRP=8,
		//			QCKLEV=IMMEDIATE,VPI=0,VCI=35,RCVGLBLV=ON,IGMPVLAN=4011,MAXBANDWIDTH=-1;
		 
		cmd = new StringBuffer();
		cmd.append("JOIN-NTVUSR::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		
		//AreaCode在取出来处理时会去掉区号前面的0,比如沈阳区号'024' 直接存'24'到工单对象
		//沈阳与其它地市ADSL版本不一样
		if (wo.getCityId() != 24 ) {
			cmd.append(",AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,RCVGLBLV=ON");
			cmd.append(",IGMPVLAN=").append(serVlan.getIgmpVlan());
			cmd.append(",MAXBANDWIDTH=-1");
		}
		
//		if (wo.getCityId() != 24 ) {
//			cmd.append(",AUTH=2,QCKLEV=1");
//		} else {
//			cmd.append(",AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,RCVGLBLV=ON");
//			cmd.append(",IGMPVLAN=").append(serVlan.getIgmpVlan());
//			cmd.append(",MAXBANDWIDTH=-1");
//		}
		cmd.append(";"); 
		 
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		
//		DACT-ADSLPORT::DID=7340672,FN=0,SN=14,PN=21:4186::;
//		cmd = new StringBuffer();
//		cmd.append("DACT-ADSLPORT::DID=").append(did);
//		cmd.append(",FN=").append(wo.getFrameId());
//		cmd.append(",SN=").append(wo.getSlotId());
//		cmd.append(",PN=").append(wo.getPortId());
//		cmd.append(":");
//		cmd.append(Ctag.getCtag());
//		cmd.append("::"); 
//		cmd.append(";"); 
//		rm = session.exeHwCmd(cmd.toString(), wo);
//		if (rm.isFailed()) {
//			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
//			return new WoResult(rm);
//		}
		
		//MOD-ADSLPORT::DID=7340672,FN=0,SN=14,PN=21:4185::LPROFID=HW-4M-FAST;
//		cmd = new StringBuffer();
//		cmd.append("MOD-ADSLPORT::DID=").append(did);
//		cmd.append(",FN=").append(wo.getFrameId());
//		cmd.append(",SN=").append(wo.getSlotId());
//		cmd.append(",PN=").append(wo.getPortId());
//		cmd.append(":");
//		cmd.append(Ctag.getCtag());
//		cmd.append("::");
//		cmd.append("LPROFID=").append(lineProfile);
//		cmd.append(";"); 
//		rm = session.exeHwCmd(cmd.toString(), wo);
//		if (rm.isFailed()) {
//			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
//			return new WoResult(rm);
//		}
		open();
		return WoResult.SUCCESS;
	}
	
	/**
	 * 多业务,同是开宽带和IPTV
	 * 
	 */
	public WoResult openWBIptv()throws ZtlException {
//		1.LST-DSLPORTDETAILINFO::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::;                         3
//		2.DACT-ADSLPORT::DID=7340529,FN=3,SN=12,PN=4:4183::;                                               6
//		3.MOD-ADSLPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::LPROFID=$profilename;              4
//		4.ACT-ADSLPORT::DID=$deviceid,FN=$frame,SN=$slot,PN=$port:CTAG::;                                  5
//
//		5.CRT-SERVICEPORT::DID=7340672,FN=0,SN=14,PN=21:CTAG::VLANID=3999,VPI=0,VCI=35,TX=xdsl,RX=xdsl;    9
//		JOIN-NTVUSR::DID=7366362,FN=0,SN=4,PN=31:CTAG::AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,VPI=0,VCI=35,RCVGLBLV=ON,IGMPVLAN=4011,MAXBANDWIDTH=-1;
//		JOIN-NTVUSR::DID=7340672,FN=0,SN=14,PN=21:CTAG::VPI=0,VCI=35;
 
		open();
		HwTL1ResponseMessage rm = null;
		// 取DID
		String did = CoreService.ticketControlService.getDeviceDid(wo
				.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
		if (StringUtils.isBlank(did)) {
			throw new ZtlException(ErrorConst.noSuchDid);
		}
		//IPTV查询PVC 
		ServiceVlan serVlan =  CoreService.ticketControlService.getServiceVlan(
					2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			open();
			return WoResult.SUCCESS;
		}
				
		StringBuffer cmd = new StringBuffer();
		cmd.append("JOIN-NTVUSR::DID=").append(did);
		cmd.append(",FN=").append(wo.getFrameId());
		cmd.append(",SN=").append(wo.getSlotId());
		cmd.append(",PN=").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		
		//AreaCode在取出来处理时会去掉区号前面的0,比如沈阳区号'024' 直接存'24'到工单对象
		//沈阳与其它地市ADSL版本不一样
		if (wo.getCityId() != 24 ) {
			cmd.append(",AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,RCVGLBLV=ON");
			cmd.append(",IGMPVLAN=").append(serVlan.getIgmpVlan());
			cmd.append(",MAXBANDWIDTH=-1");
		}
		cmd.append(";"); 
//		if (wo.getCityId() != 24 ) {
//			cmd.append(",AUTH=2,QCKLEV=1");
//		} else {
//			cmd.append(",AUTH=2,MAXGRP=8,QCKLEV=IMMEDIATE,RCVGLBLV=ON");
//			cmd.append(",IGMPVLAN=").append(serVlan.getIgmpVlan());
//			cmd.append(",MAXBANDWIDTH=-1");
//		}
		
		rm = session.exeHwCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}
	
}