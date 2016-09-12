package com.zoom.nos.provision.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.AlcatelTl1Session;
import com.zoom.nos.provision.tl1.session.Ctag;

public class AlcatelPonDsl extends AbstractOperations{

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(AlcatelPonDsl.class);

	private AlcatelTl1Session session = null;
	
	public AlcatelPonDsl(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new AlcatelTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "",0,wo
				.getTl1User(), wo.getTl1Password(),NosEnv.socket_timeout_tl1server);
		//open session
		session.open();
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException  {

		StringBuffer cmd = null;
		
		//ED-HSI:ONUIP=10.1.44.50:HSIPORT-1-2:Ctag::BWPROFUP=1M,BWPROFDN=1M;
		cmd = new StringBuffer();
		cmd.append("ED-HSI:ONUIP=").append(wo.getNeIp());
		cmd.append(":HSIPORT-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
//		cmd.append("::BWPROFUP=").append(wo.getAtucRate() / 1024).append("M");
//		cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024).append("M");
		
		if (wo.getAtucRate() < 1024) {
			cmd.append("::BWPROFUP=").append(wo.getAtucRate()).append("K");
			cmd.append(",BWPROFDN=").append(wo.getAtucRate()).append("K");
		}else{
			cmd.append("::BWPROFUP=").append(wo.getAtucRate() / 1024).append("m");
			cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024).append("m");
		}
		
		cmd.append(";");
		AlcatelTL1ResponseMessage rm = session.exeAlcatelCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
		
		return WoResult.SUCCESS;
	
	}

	/**
	 * 
	 */
	public WoResult close() throws ZtlException  {
		StringBuffer cmd = null;
	
		//暂停端口
		//ED-HSI:ONUIP=135.251.201.115:HSIPORT-4-1:ontedhsi::STATE=OOS,STYPE=HSI;
		cmd = new StringBuffer();
		cmd.append("ED-HSI:ONUIP=").append(wo.getNeIp());
		cmd.append(":HSIPORT-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
//		cmd.append("::STATE=OOS,STYPE=HSI;");
		cmd.append("::STATE=OOS;");
		AlcatelTL1ResponseMessage rm = session.exeAlcatelCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 */
	public WoResult open() throws ZtlException  {
		StringBuffer cmd = null;
		
		//ED-HSI:ONUIP=10.1.44.50:HSIPORT-1-2:Ctag::BWPROFUP=1M,BWPROFDN=1M;
		cmd = new StringBuffer();
		cmd.append("ED-HSI:ONUIP=").append(wo.getNeIp());
		cmd.append(":HSIPORT-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
//		cmd.append("::BWPROFUP=").append(wo.getAtucRate() / 1024).append("M");
//		cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024).append("M");
		if (wo.getAtucRate() < 1024) {
			cmd.append("::BWPROFUP=").append(wo.getAtucRate()).append("K");
			cmd.append(",BWPROFDN=").append(wo.getAtucRate()).append("K");
		} else {
			cmd.append("::BWPROFUP=").append(wo.getAtucRate() / 1024).append("m");
			cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024).append("m");
		}
		cmd.append(";");
		AlcatelTL1ResponseMessage rm = session.exeAlcatelCmd(cmd.toString(),wo);
		if (rm.isFailed()) {
			log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
			return new WoResult(rm);
		}
		
		//恢复端口
		cmd = new StringBuffer();
		cmd.append("ED-HSI:ONUIP=").append(wo.getNeIp());
		cmd.append(":HSIPORT-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::STATE=IS;");
		rm = session.exeAlcatelCmd(cmd.toString(),wo);
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
		return null;
	}

	protected WoResult openService(int defaultVlan, int serviceVlan)throws ZtlException{
		return null;
	}

}
