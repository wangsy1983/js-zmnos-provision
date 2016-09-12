package com.zoom.nos.provision.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.SystemFlag;
import com.zoom.nos.provision.tl1.session.ZteTl1Session;

public class ZtePonLan extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(ZtePonLan.class);

	private ZteTl1Session session = null;

	public ZtePonLan(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new ZteTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "", 0, wo.getTl1User(), wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
		// open session
		session.open();
	}

	public WoResult alterRate() throws ZtlException {
		StringBuffer cmd = null;
		// 限速
		// CHG-ETHUNIPORT::DID=192.168.1.10,PID=1-1-2-2:ctag::USPIR=2048,DSPIR=1024;
		cmd = new StringBuffer();
		cmd.append("CHG-ETHUNIPORT::DID=").append(wo.getNeIp());
		cmd.append(",PID=").append(wo.getShelfId());
		cmd.append("-").append(wo.getFrameId());
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::USPIR=");
		cmd.append(wo.getAtucRate());
		cmd.append(",DSPIR=");
		cmd.append(wo.getAtucRate());
		cmd.append(";");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// SaveFlash();
		return WoResult.SUCCESS;
	}

	public WoResult close() throws ZtlException {
		StringBuffer cmd = null;

		// 关端口
		// CHG-PORT-STAT::DID=192.168.1.10,PID=1-1-2-2:ctag::STATUS=disable;
		String[] oltponID = null;
		try {
			log.info("wo.getOltPonID()=" + wo.getOltPonID());
			oltponID = wo.getOltPonID().split("-");
			if (oltponID.length != 5) {
				log.debug(wo.getOltPonID());
				log.debug("zte 1江苏联通BSS侧传来的参数,缺少OLT的部分");
				throw new ZtlException(ErrorConst.IMP);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZtlException(ErrorConst.IMP);
		}

		cmd = new StringBuffer();

		if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
			// DACT-LANPORT::OLTID=192.168.200.2,PONID=1-1-2-8,ONUIDTYPE=ONU_NUMBER,ONUID=1,ONUPORT=1-1-1-15:CTAG::;
			cmd.append("DACT-LANPORT::OLTID=").append(oltponID[0]);
			cmd.append(",PONID=").append(oltponID[1]);
			cmd.append("-").append(oltponID[2]);
			cmd.append("-").append(oltponID[3]);
			cmd.append("-").append(oltponID[4]);
			cmd.append(",ONUIDTYPE=ONU_NUMBER");
			cmd.append(",ONUID=").append(wo.getOntId());
			cmd.append(",ONUPORT=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::");
			cmd.append(";");
		} else {
			// CHG-ONUUNI-PON::DID=172.16.243.210,OID=1-1-1-1-1,PORT=1:3344::STATUS=disable,ACCESSMODE=oam;

			cmd.append("CHG-ONUUNI-PON::DID=").append(oltponID[0]);
			cmd.append(",OID=").append(oltponID[1]);
			cmd.append("-").append(oltponID[2]);
			cmd.append("-").append(oltponID[3]);
			cmd.append("-").append(oltponID[4]);
			cmd.append("-").append(wo.getOntId());
			cmd.append(",PORT=");
			if (wo.getSlotId() != null && wo.getSlotId() > 0) {
				cmd.append(wo.getSlotId() + "-");
			}
			cmd.append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::STATUS=disable");
			cmd.append(",MODE=1");
			cmd.append(",ACCESSMODE=oam;");
		}

		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);

		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 江苏联通中兴保存在ONU上面,其它按原来的保存在OLT
		if (!(wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON)) {
			SaveONUFlash();
		}
		return WoResult.SUCCESS;
	}

	public WoResult open() throws ZtlException {
		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null;

		String[] oltponID = null;
		try {
			oltponID = wo.getOltPonID().split("-");
			if (oltponID.length != 5) {
				log.debug("江苏联通BSS侧传来的参数,缺少OLT的部分");
				throw new ZtlException(ErrorConst.wrongDeviceMaker);
			}
		} catch (Exception e) {
			log.debug("江苏联通BSS侧传来的参数,缺少OLT的部分");
			throw new ZtlException(ErrorConst.wrongDeviceMaker);
		}
		cmd = new StringBuffer();

		if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
			// ACT-LANPORT::OLTID=192.168.200.2,PONID=1-1-2-8,ONUIDTYPE=ONU_NUMBER,ONUID=1,ONUPORT=1-1-1-15:CTAG::;
			cmd.append("ACT-LANPORT::OLTID=").append(oltponID[0]);
			cmd.append(",PONID=").append(oltponID[1]);
			cmd.append("-").append(oltponID[2]);
			cmd.append("-").append(oltponID[3]);
			cmd.append("-").append(oltponID[4]);
			cmd.append(",ONUIDTYPE=ONU_NUMBER");
			cmd.append(",ONUID=").append(wo.getOntId());
			cmd.append(",ONUPORT=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::");
			cmd.append(";");
		} else {
			// CHG-ONUUNI-PON::DID=172.16.243.210,OID=1-1-1-1-1,PORT=1:3344::STATUS=1,MODE=1,PVLAN=0,CVLAN=3033,ACCESSMODE=oam;
			cmd.append("CHG-ONUUNI-PON::DID=").append(oltponID[0]);
			cmd.append(",OID=").append(oltponID[1]);
			cmd.append("-").append(oltponID[2]);
			cmd.append("-").append(oltponID[3]);
			cmd.append("-").append(oltponID[4]);
			cmd.append("-").append(wo.getOntId());
			cmd.append(",PORT=");
			if (wo.getSlotId() != null && wo.getSlotId() > 0) {
				cmd.append(wo.getSlotId() + "-");
			}
			cmd.append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::STATUS=1");
			cmd.append(",MODE=1");
			cmd.append(",PVLAN=0");
			// 12.19去掉此项
			// cmd.append(",CVLAN=").append(wo.getCvlan());
			cmd.append(",ACCESSMODE=oam;");
		}
		rm = session.exeZteCmd(cmd.toString(), wo);

		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// SAV-FLASH::DID=172.16.243.210:5::;
		if (!(wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON)) {
			SaveONUFlash();
		}
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
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	private WoResult SaveONUFlash() throws ZtlException {
		StringBuffer cmd = null;
		String[] oltponID = wo.getOltPonID().split("-");
		if (oltponID.length != 5) {
			log.debug("SAVE ONU FLASH 江苏联通BSS侧传来的参数,缺少OLT的部分");
			throw new ZtlException(ErrorConst.IMP);
		}
		// SAV-ONU-FLASH-PON::DID=10.63.196.246,OID=1-1-5-2-18:Ctag001::OAM=true;
		cmd = new StringBuffer();
		cmd.append("SAV-ONU-FLASH-PON::DID=").append(oltponID[0]);
		cmd.append(",OID=").append(oltponID[1]);
		cmd.append("-").append(oltponID[2]);
		cmd.append("-").append(oltponID[3]);
		cmd.append("-").append(oltponID[4]);
		cmd.append("-").append(wo.getOntId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::OAM=true;");
		ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
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
	 * 开多业务
	 * 
	 * @param defaultVlan
	 *            上网VLAN
	 * @param serviceVlan
	 *            业务VLAN
	 * @return
	 * @throws ZtlException
	 */
	protected WoResult openService(int defaultVlan, int serviceVlan) throws ZtlException {
		StringBuffer cmd = null;
		ZteTL1ResponseMessage rm = null;

		// add 上网 vlan
		this.createVlan(wo.getNeIp(), defaultVlan);

		// add service vlan
		this.createVlan(wo.getNeIp(), serviceVlan);

		// 绑定VLAN
		if (wo.getDeviceTypeId().intValue() == WorkOrder.DEVICETYPE_ZTE_F820) {
			// F820
			// CHG-PORTVLAN-PON::DID=12.4.6.235,PID=1-1-1-1:CTAG::OPERATION=1,VLANTYPE=1,VLANID=2000;
			cmd = new StringBuffer();
			cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
			cmd.append(",PID=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::OPERATION=1,VLANTYPE=1,VLANID=");
			cmd.append(defaultVlan);
			cmd.append(";");
			rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
			// CHG-PORTVLAN-PON::DID=12.4.6.235,PID=1-1-1-1:CTAG::OPERATION=1,VLANTYPE=1,VLANID=4000;
			cmd = new StringBuffer();
			cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
			cmd.append(",PID=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::OPERATION=1,VLANTYPE=1,VLANID=");
			cmd.append(serviceVlan);
			cmd.append(";");
			rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
		} else {
			// F822
			// BND-PORT-VLAN::DID=12.4.6.234,PID=1-1-2-9:5::VLANFLAG=1,VLANID=2000;
			cmd = new StringBuffer();
			cmd.append("BND-PORT-VLAN::DID=").append(wo.getNeIp());
			cmd.append(",PID=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::VLANFLAG=1,VLANID=");
			cmd.append(wo.getVlan());
			cmd.append(";");
			rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
			// BND-PORT-VLAN::DID=12.4.6.234,PID=1-1-2-8:5::VLANFLAG=1,VLANID=4000;
			cmd = new StringBuffer();
			cmd.append("BND-PORT-VLAN::DID=").append(wo.getNeIp());
			cmd.append(",PID=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::VLANFLAG=1,VLANID=");
			cmd.append(wo.getIptvvlan());
			cmd.append(";");
			rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
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
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		// SaveFlash();
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 * @param serviceVlan
	 *            业务VLAN
	 * @return
	 * @throws ZtlException
	 */
	protected WoResult closeService(int serviceVlan) throws ZtlException {
		StringBuffer cmd = null;

		// 解绑定VLAN
		if (wo.getDeviceTypeId().intValue() == WorkOrder.DEVICETYPE_ZTE_F820) {
			// F820
			cmd = new StringBuffer();
			cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
			cmd.append(",PID=").append(wo.getShelfId());
			cmd.append("-").append(wo.getFrameId());
			cmd.append("-").append(wo.getSlotId());
			cmd.append("-").append(wo.getPortId());
			cmd.append(":");
			cmd.append(Ctag.getCtag());
			cmd.append("::OPERATION=2,VLANTYPE=1,VLANID=");
			cmd.append(serviceVlan);
			cmd.append(";");
			ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
		} else {
			// F822
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
			cmd.append(";");
			ZteTL1ResponseMessage rm = session.exeZteCmd(cmd.toString(), wo);
			if (rm.isFailed()) {
				log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
				return new WoResult(rm);
			}
		}

		// SaveFlash();
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 * @param ip
	 * @param vlanid
	 * @return
	 * @throws ZtlException
	 */
	private WoResult createVlan(String ip, int vlanid) throws ZtlException {
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
			log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		// 不保存flash
		return WoResult.SUCCESS;
	}
}
