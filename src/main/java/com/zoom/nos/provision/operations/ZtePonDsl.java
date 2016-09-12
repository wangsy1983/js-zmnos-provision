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
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.ZteTl1Session;

public class ZtePonDsl extends AbstractOperations{
	private static Logger log = LoggerFactory.getLogger(ZtePonDsl.class);

	private ZteTl1Session session = null;
	
	private static Short wotype_iptv = 31;

	public ZtePonDsl(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new ZteTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "",0,wo
				.getTl1User(), wo.getTl1Password(),NosEnv.socket_timeout_tl1server);
		//open session
		session.open();
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException  {
		StringBuffer cmd = null;
		
		//取速率模板
		String lineProfile=CoreService.ticketControlService.getLineProfileName(wo);
		if(StringUtils.isBlank(lineProfile)){
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}
		
		// 设速率模板
		//CHG-PORTLPRF-DSL::DID=10.66.161.122,PID=2-0-12-13:ctag::LPRF=FAST2M.PRF;
		cmd = new StringBuffer();
		cmd.append("CHG-PORTLPRF-DSL::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::LPRF=").append(lineProfile);
		cmd.append(";");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
//		SaveFlash();
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 */
	public WoResult close()  throws ZtlException {
		StringBuffer cmd = null;
		
		
		// 关端口
		//CHG-PORT-STAT::DID=192.168.1.20,PID=2-0-12-13:ctag::STATUS=enable;
		cmd = new StringBuffer();
		cmd.append("CHG-PORT-STAT::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::STATUS=disable;");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
//		SaveFlash();
		return WoResult.SUCCESS;
	}

	public WoResult open() throws ZtlException  {
		StringBuffer cmd = null;
		
		//取速率模板
		String lineProfile=CoreService.ticketControlService.getLineProfileName(wo);
		if(StringUtils.isBlank(lineProfile)){
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}
		
		// 设速率模板
		//CHG-PORTLPRF-DSL::DID=10.66.161.122,PID=2-0-12-13:ctag::LPRF=FAST2M.PRF;
		cmd = new StringBuffer();
		cmd.append("CHG-PORTLPRF-DSL::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::LPRF=").append(lineProfile);
		cmd.append(";");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}

		// 开端口
		//CHG-PORT-STAT::DID=192.168.1.20,PID=2-0-12-13:ctag::STATUS=enable;
		cmd = new StringBuffer();
		cmd.append("CHG-PORT-STAT::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::STATUS=enable;");
		rm = session.exeZteCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
//		SaveFlash();
		return WoResult.SUCCESS;
	}

	private WoResult SaveFlash() throws ZtlException {
		StringBuffer cmd = null;

		// add vlan
		// SAV-FLASH::DID=10.61.94.33:5::;
		cmd = new StringBuffer();
		cmd.append("SAV-FLASH::DID=").append(wo.getNeIp());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::;");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
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
		ZteTL1ResponseMessage rm = null;		
		// 解业务VLAN绑定
		// UBND-PORT-VLAN::DID=192.168.1.2,PID=2-0-12-13:ctag::VLANFLAG=1,VLANID=200,PVCNO=4;
		cmd = new StringBuffer();
		cmd.append("UBND-PORT-VLAN::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANFLAG=1,VLANID=");
		cmd.append(serviceVlan);
		cmd.append(",PVCNO=").append(wo.getPvc());
		cmd.append(";");
		rm = session.exeZteCmd(cmd.toString(), wo);//???get pvc
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

//		SaveFlash();
		return WoResult.SUCCESS;
	
	}

	protected WoResult openService(int defaultVlan, int serviceVlan)throws ZtlException{

		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null;

		// add 上网 vlan
		this.createVlan(wo.getNeIp(), defaultVlan);
	
		// add service vlan
		this.createVlan(wo.getNeIp(), serviceVlan);
		
		// 绑定VLAN
		// BND-PORT-VLAN::DID=192.168.1.2,PID=2-0-12-13:ctag::VLANFLAG=1,VLANID=200,PVID=200,PVCNO=1;
		cmd = new StringBuffer();
		cmd.append("BND-PORT-VLAN::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANFLAG=1,VLANID=");
		cmd.append(defaultVlan);
		cmd.append(",PVID=").append(defaultVlan);
		cmd.append(",PVCNO=").append(wo.getPvc());
		cmd.append(";");
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		
		//取得业务的PVC
		int servicePvc = CoreService.ticketControlService.getServiceVlan(
				wo.getWoType(), "0" + wo.getCityId()).getPvc();
		// BND-PORT-VLAN::DID=192.168.1.2,PID=2-0-12-13:ctag::VLANFLAG=1,VLANID=200,PVID=200,PVCNO=4;
		cmd = new StringBuffer();
		cmd.append("BND-PORT-VLAN::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANFLAG=1,VLANID=");
		cmd.append(serviceVlan);
		cmd.append(",PVID=").append(serviceVlan);
		cmd.append(",PVCNO=").append(servicePvc);
		cmd.append(";");
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 开端口
		// CHG-PORT-STAT::DID=192.168.1.10,PID=1-1-2-2:ctag::STATUS=enable;
		cmd = new StringBuffer();
		cmd.append("CHG-PORT-STAT::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::STATUS=enable;");
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
//		SaveFlash();
		return WoResult.SUCCESS;
	
	}
	
	/**
	 * 
	 * @param ip
	 * @param vlanid
	 * @return
	 * @throws ZtlException
	 */
	private WoResult createVlan(String ip, int vlanid)throws ZtlException{
		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null;

		// add vlan
		// CRT-VLAN::DID=192.168.1.10:catg::VLANID=200;
		cmd = new StringBuffer();
		cmd.append("CRT-VLAN::DID=").append(ip);
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::VLANID=").append(vlanid);
		cmd.append(";");
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
					+ rm.getEnDesc());
			return new WoResult(rm);
		}
		//不保存flash
		return WoResult.SUCCESS;
	}
	
	
	/***
	 * closeIptv
	 */
	public WoResult closeIptv() throws ZtlException{
		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null;
		//IPTV查询PVC 
		ServiceVlan serVlan =  CoreService.ticketControlService.getServiceVlan(
					2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			close();
			return WoResult.SUCCESS;
		}
		
		//UBND-PORT-VLAN::DID=10.1.34.10,PID=1-1-15-26:5::VLANID=4000,PVCNO=2;
		cmd = new StringBuffer();
		cmd.append("UBND-PORT-VLAN::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("VLANID=").append(serVlan.getVlan());
		cmd.append(",PVCNO=").append(serVlan.getPvcNo());
		cmd.append(";");
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
					+ rm.getEnDesc());
			return new WoResult(rm);
		}
		
		//DACT-PVC-IP::DID=10.1.34.10,PID=1-1-15-26:5::PVCNO=2,VPI=8,VCI=35;
		cmd = new StringBuffer();
		cmd.append("DACT-PVC-IP::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("PVCNO=").append(serVlan.getPvcNo());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
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
		ZteTL1ResponseMessage rm = null; 
		//IPTV查询PVC(如果只有一条PVC,开IPTV当作开宽带一样处理)
		ServiceVlan serVlan =  CoreService.ticketControlService
				.getServiceVlan(2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			open();
			return WoResult.SUCCESS;
		}

		//MOD-PORTPVC-IP::DID=10.1.34.10,PID=1-1-15-26:5::PVCNO=2,VPI=8,VCI=35; 
		cmd = new StringBuffer();
		cmd.append("MOD-PORTPVC-IP::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::PVCNO=").append(serVlan.getPvcNo());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(";"); 
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		//ACT-PVC-IP::DID=10.1.34.10,PID=1-1-15-26:5::PVCNO=2,VPI=8,VCI=35;
		cmd = new StringBuffer();
		cmd.append("ACT-PVC-IP::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("PVCNO=").append(serVlan.getPvcNo());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(";");  
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		
//		创建vlan
		createVlan(wo.getNeIp(),serVlan.getVlan());
		
//		BND-PORT-VLAN::DID=10.1.34.10,PID=1-1-15-26:5::VLANID=4000,VLANFLAG=2,PVCNO=2,PVID=4000;
		cmd = new StringBuffer();
		cmd.append("BND-PORT-VLAN::DID").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::"); 
		cmd.append("VLANID=").append(serVlan.getVlan());
		cmd.append(",VLANFLAG=").append(serVlan.getPvcNo());
		cmd.append(",PVCNO=2");//.append(serVlan.getPvcNo());
		cmd.append(",PVID=").append(serVlan.getVlan());
		cmd.append(";"); 
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		 
		//取速率模板
		String lineProfile=CoreService.ticketControlService.getLineProfileName(wo);
		if(StringUtils.isBlank(lineProfile)){
			throw new ZtlException(ErrorConst.lineProfileNoutFound);
		}
		
//		// 设速率模板
//		//CHG-PORTLPRF-DSL::DID=10.66.161.122,PID=2-0-12-13:ctag::LPRF=FAST2M.PRF;
//		cmd = new StringBuffer();
//		cmd.append("CHG-PORTLPRF-DSL::DID=").append(wo.getNeIp());
//		cmd.append(",PID=").append(wo.getShelfId());
//		cmd.append("-").append(wo.getFrameId());
//		cmd.append("-").append(wo.getSlotId());
//		cmd.append("-").append(wo.getPortId());
//		cmd.append(":");
//		cmd.append(Ctag.getCtag());
//		cmd.append("::LPRF=").append(lineProfile);
//		cmd.append(";"); 
// 
//		rm = session.exeZteCmd(cmd.toString(), wo);
//		if (rm.isFailed()) {
//			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
//			return new WoResult(rm);
//		}
//		 
//		//CHG-PORT-STAT::DID=10.1.34.10,PID=1-1-15-26:134827::STATUS=1; 
//		cmd = new StringBuffer();
//		cmd.append("CHG-PORT-STAT::DID=").append(wo.getNeIp());
//		cmd.append(",PID=").append(wo.getShelfId());
//		cmd.append("-").append(wo.getFrameId());
//		cmd.append("-").append(wo.getSlotId());
//		cmd.append("-").append(wo.getPortId());
//		cmd.append(":");
//		cmd.append(Ctag.getCtag());
//		cmd.append("::STATUS=1");
//		cmd.append(";");
//		rm = session.exeZteCmd(cmd.toString(), wo);
//		if (rm.isFailed()) {
//			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
//			return new WoResult(rm);
//		}
//		端口配置速率并开通
		open();
//		SaveFlash();
		return WoResult.SUCCESS;
	}
	
	/**
	 * 多业务,同是开宽带和IPTV
	 * 
	 */
	public WoResult openWBIptv()throws ZtlException {
		open();
		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null; 
		//IPTV查询PVC 
		ServiceVlan serVlan =  CoreService.ticketControlService.getServiceVlan(
					2,wotype_iptv, "0" + wo.getCityId()); 
		if(serVlan == null){
			open();
			return WoResult.SUCCESS;
		}
		//MOD-PORTPVC-IP::DID=10.1.34.10,PID=1-1-15-26:5::PVCNO=2,VPI=8,VCI=35; 
		cmd = new StringBuffer();
		cmd.append("MOD-PORTPVC-IP::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::PVCNO=").append(serVlan.getPvcNo());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(";"); 
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		//ACT-PVC-IP::DID=10.1.34.10,PID=1-1-15-26:5::PVCNO=2,VPI=8,VCI=35;
		cmd = new StringBuffer();
		cmd.append("ACT-PVC-IP::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("PVCNO=").append(serVlan.getPvcNo());
		cmd.append(",VPI=").append(serVlan.getVpi());
		cmd.append(",VCI=").append(serVlan.getVci());
		cmd.append(";");  
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		 
//		BND-PORT-VLAN::DID=10.1.34.10,PID=1-1-15-26:5::VLANID=4000,VLANFLAG=2,PVCNO=2,PVID=4000;
		cmd = new StringBuffer();
		cmd.append("BND-PORT-VLAN::DID").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::"); 
		cmd.append("VLANID=").append(serVlan.getVlan());
		cmd.append(",VLANFLAG=").append(serVlan.getPvcNo());
		cmd.append(",PVCNO=2");//.append(serVlan.getPvcNo());
		cmd.append(",PVID=").append(serVlan.getVlan());
		cmd.append(";"); 
		rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	} 
}
