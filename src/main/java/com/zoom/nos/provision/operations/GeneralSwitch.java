package com.zoom.nos.provision.operations;

import java.net.InetAddress;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Variable;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.snmp.SnmpHandler;
import com.zoom.nos.provision.snmp.SnmpHandlerException;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class GeneralSwitch extends AbstractOperations{

	private static Logger log = LoggerFactory.getLogger(GeneralSwitch.class);

	// snmp handler
	private SnmpHandler snmpHandler;

	// snmp time out
	private long timeout = 5000;

	// retry times
	private int retries = 4;

	public GeneralSwitch(WorkOrder wo) throws ZtlException {
		super(wo);
		this.timeout = 5000;
		this.retries = 4;
		try {
			//
			snmpHandler = new SnmpHandler(InetAddress.getByName(wo.getNeIp()));

			// 超时
			snmpHandler.setTimeout(timeout);
			// 重试次数
			snmpHandler.setRetries(retries);
			// SnmpVersion is 2
			snmpHandler.setSnmpVersion(SnmpConstants.version2c);
			snmpHandler.setReadCommunity(wo.getSnmpReadCommunity());
			snmpHandler.setWriteCommunity(wo.getSnmpWriteCommunity());
			//
			snmpHandler.open();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.TCPErr);
		}
	}


	public WoResult cancellation(int port)throws ZtlException {
		try {
			/*
			 * 1.确保端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			// 索引： ADSL 物理接口(+4)
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler
						.get("1.3.6.1.2.1.2.2.1.7." + port);
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.info("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.info("Update ifAdminStatus to 2");
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
					Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(2));
					log.info("ifAdminStatus(1-up,2-down)=" + v1.toInt());
				} else {
					Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
					log.info("ifAdminStatus(1-up,2-down)=" + v1.toInt());
				}
			}
		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (Exception err) {
			// other error
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;
	}
	

	/**
	 * 复通
	 * 
	 * @return
	 */
	public WoResult resetOpen(int port)throws ZtlException {

		try {
			/*
			 * 1.确保端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			// 索引： ADSL 物理接口(+4)
			Integer32 _v1 = null;
			log.info("port="+port);
			try {
				_v1 = (Integer32) snmpHandler
						.get("1.3.6.1.2.1.2.2.1.7." + port);
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
					Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(2));
					log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
				}else{
					Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
					log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
				}
				
			}

			/*
			 * 2.打开端口管理状态到“Up”
			 */
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (Exception err) {
			// other error
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;
	}
	
	public WoResult alterRate() throws ZtlException {
		return this.resetOpen(wo.getPortId());
	}

	public WoResult close() throws ZtlException {
		return this.cancellation(wo.getPortId());
	}


	public WoResult open() throws ZtlException {
		return this.resetOpen(wo.getPortId());
	}

	public void destruction() {
		snmpHandler.close();		
	}
	
	protected WoResult closeService(int serviceVlan)throws ZtlException{
		return null;
	}

	protected WoResult openService(int defaultVlan, int serviceVlan)throws ZtlException{
		return null;
	}
}
