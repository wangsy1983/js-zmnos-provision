package com.zoom.nos.provision.operations;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.snmp.SnmpHandler;
import com.zoom.nos.provision.snmp.SnmpHandlerException;

public class HuanyuPonDsl extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(HuanyuPonDsl.class);

	// ascii码常量
	private static final String ASCII_M_fast = ".77.95.102.97.115.116";

	private static final String ASCII_M_Interleave = ".77.95.73.110.116.101.114.108.101.97.118.101";

	// snmp handler
	private SnmpHandler snmpHandler;

	// snmp time out
	private long timeout = 5000;

	// retry times
	private int retries = 4;

	public HuanyuPonDsl(WorkOrder wo) throws ZtlException {
		super(wo);
		this.timeout = 5000;
		this.retries = 4;
		try {
			snmpHandler = new SnmpHandler(InetAddress.getByName(wo.getNeIp()));

			// 超时
			snmpHandler.setTimeout(timeout);
			// 重试次数
			snmpHandler.setRetries(retries);
			// SnmpVersion is 2
			snmpHandler.setSnmpVersion(SnmpConstants.version2c);
			// 
			snmpHandler.setReadCommunity(wo.getSnmpReadCommunity());
			snmpHandler.setWriteCommunity(wo.getSnmpWriteCommunity());
			//
			snmpHandler.open();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.TCPErr);
		}
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException {
		return this.open();
	}

	/**
	 * 
	 */
	public WoResult close() throws ZtlException {
		try {
			/*
			 * 1.关闭端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + wo.getPortId());
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + wo.getPortId(),
						new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			/*
			 * 2.提交commit
			 */
			// =============================
			// 1. 配置保存：
			// Leaf：iesMaintenanceTarget (OID .1.3.6.1.4.1.890.1.5.13.1.3.2.2.)
			// index (index=0, setvalue=0)
			// Leaf：iesMaintenanceOps (OID .1.3.6.1.4.1.890.1.5.13.1.3.2.1.)
			// index (index=0, setvalue=1)
			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			_ovmap4.put(new OID("1.3.6.1.4.1.890.1.5.13.1.3.2.2.0"), new Integer32(0));
			_ovmap4.put(new OID("1.3.6.1.4.1.890.1.5.13.1.3.2.1.0"), new Integer32(1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("commit ok>");

		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (Exception err) {
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;

	}

	/**
	 * 
	 */
	public void destruction() {
		snmpHandler.close();
	}

	/**
	 * 
	 */
	public WoResult open() throws ZtlException {

		int port = wo.getPortId();
		// 下行最大速率
		int atucRate = wo.getAtucRate() / 1024;
		// int aturRate=wo.getAturRate()/1024;

		String profileName = "";
		if (wo.getAdslLineType() == WorkOrder.ADSLLINETYPE_FASTONLY) {
			// 快速
			profileName = atucRate + "M_fast";
			// (atucRate + 48) + ASCII_M_fast;
		} else {
			// 交织
			profileName = atucRate + "M_Interleave";
			// profileName = (atucRate + 48) + ASCII_M_Interleave;
		}

		try {
			/*
			 * 1.关闭端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + port);
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			// ADSL速率模板
			// Leaf：adslLineConfProfile (OID 1.3.6.1.2.1.10.94.1.1.1.1.4)
			Variable v2 = snmpHandler.set("1.3.6.1.2.1.10.94.1.1.1.1.4." + port, new OctetString(
					profileName));
			log.debug("修改上下行最大速率ok>" + v2.toString());

			// 3.打开端口管理状态到“Up”
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			// 4.提交commit
			// =============================
			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			_ovmap4.put(new OID("1.3.6.1.4.1.890.1.5.13.1.3.2.2.0"), new Integer32(0));
			_ovmap4.put(new OID("1.3.6.1.4.1.890.1.5.13.1.3.2.1.0"), new Integer32(1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("commit ok>");

		} catch (ZtlException err) {
			throw err;
		} catch (SnmpHandlerException snmpHandlerErr) {
			try {
				snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(1));
			} catch (Exception e) {
				log.error(e.toString(),e);
			}
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (Exception err) {
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;

	}

}
