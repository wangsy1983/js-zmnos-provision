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
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.snmp.SnmpHandler;
import com.zoom.nos.provision.snmp.SnmpHandlerException;

public class HuanyuPonLan extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(HuanyuPonLan.class);

	// snmp handler
	private SnmpHandler snmpHandler;

	// snmp time out
	private long timeout = 5000;

	// retry times
	private int retries = 4;

	public HuanyuPonLan(WorkOrder wo) throws ZtlException {
		super(wo);
		this.timeout = 5000;
		this.retries = 4;
		try {
			snmpHandler = new SnmpHandler(InetAddress.getByName(wo.getNeIp()));

			// ��ʱ
			snmpHandler.setTimeout(timeout);
			// ���Դ���
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
	 * ����,NST4024B/NST2024B
	 * 
	 * @param atucRate
	 *            �����������,��λ"kbps"
	 * 
	 * @param aturRate
	 *            �����������,��λ"kbps"
	 * @return
	 */
	private WoResult openupNST4024B(int port, int atucRate, int aturRate) throws ZtlException {
		try {
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// ȡֵ�� 1= "Up" ; 2 = "Down"
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			// ��˿��������ƣ�
			// Leafs��rateLimitPortIngRate��OID .1.3.6.1.4.1.890.1.5.8.16.2.2.1.2��
			// ���˿��������ƣ�
			// Leafs��rateLimitPortEgrRate��OID .1.3.6.1.4.1.890.1.5.8.16.2.2.1.3��
			// ȡֵ����λ��Kbit/s��
			Map<OID, Variable> _ovmap2 = new HashMap<OID, Variable>();
			_ovmap2.put(new OID("1.3.6.1.4.1.890.1.5.8.16.2.2.1.2." + port),
					new Integer32(atucRate));
			_ovmap2.put(new OID("1.3.6.1.4.1.890.1.5.8.16.2.2.1.3." + port),
					new Integer32(aturRate));
			VariableBinding[] v2 = snmpHandler.set(_ovmap2);
			for (VariableBinding vb : v2) {
				log.debug(vb.toString());
			}
			log.debug("�޸��������������ok>");
			
			// 3.�򿪶˿ڹ���״̬����Up��
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			// 4.�ύcommit
			// =============================
			// ����Ϊconfig ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.890.1.5.8.16.9.1.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());
		} catch (ZtlException err) {
			throw err;
		}catch (SnmpHandlerException snmpHandlerErr) {
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

	/**
	 * ����,NST4016B
	 * 
	 * @param atucRate
	 *            �����������,��λ"kbps"
	 * 
	 * @param aturRate
	 *            �����������,��λ"kbps"
	 * @return
	 */
	private WoResult openupNST4016B(int port, int atucRate, int aturRate) throws ZtlException {
		try {
			// ��λ�̶�Ϊ0(��1)
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.3)
			// ȡֵ�� disable ( 0 ) , enable ( 1 ) , reset ( 2 )
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,0-down)=" + _v1.toInt());
			if (_v1.toInt() != 0) {
				log.debug("Update ifAdminStatus to 0");
				Variable v1 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port,
						new Integer32(0));
				log.debug("ifAdminStatus(1-up,0-down)=" + v1.toInt());
			}

			// ���У�onuUniEgressRateLimit (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.9)
			// ���У�onuUniIngressRateLimit (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.10)
			// ȡֵ�� ��λ"Kbit/s"
			Map<OID, Variable> _ovmap2 = new HashMap<OID, Variable>();
			_ovmap2.put(new OID("1.3.6.1.4.1.27608.1.2.3.1.2.1.9.1." + port), new Integer32(
					atucRate));
			_ovmap2.put(new OID("1.3.6.1.4.1.27608.1.2.3.1.2.1.10.1." + port), new Integer32(
					aturRate));
			VariableBinding[] v2 = snmpHandler.set(_ovmap2);
			for (VariableBinding vb : v2) {
				log.debug(vb.toString());
			}
			log.debug("�޸��������������ok>");

			// 3.�򿪶˿ڹ���״̬����Up��
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port,
					new Integer32(1));
			log.debug("ifAdminStatus(1-up,0-down)=" + v3.toInt());

			//4.�ύcommit
			// =============================
			// ����Ϊsave ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.1.4.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());
		} catch (ZtlException err) {
			throw err;
		} catch (SnmpHandlerException snmpHandlerErr) {
			try {
				snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port, new Integer32(1));
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


	/**
	 * ����,NST4016B
	 * 
	 * @param atucRate
	 *            �����������,��λ"kbps"
	 * 
	 * @param aturRate
	 *            �����������,��λ"kbps"
	 * @return
	 */
	private WoResult openupNST4016BW(int port, int atucRate, int aturRate) throws ZtlException {
		try {
			// ��λ�̶�Ϊ0(��1)
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.3)
			// ȡֵ�� disable ( 0 ) , enable ( 1 ) , reset ( 2 )
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,0-down)=" + _v1.toInt());
			if (_v1.toInt() != 0) {
				log.debug("Update ifAdminStatus to 0");
				Variable v1 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port,
						new Integer32(0));
				log.debug("ifAdminStatus(1-up,0-down)=" + v1.toInt());
			}

			// ���У�onuUniEgressRateLimit (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.9)
			// ���У�onuUniIngressRateLimit (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.10)
			// ȡֵ�� ��λ"Kbit/s"
			Map<OID, Variable> _ovmap2 = new HashMap<OID, Variable>();
			_ovmap2.put(new OID("1.3.6.1.4.1.27608.1.2.3.1.2.1.9." + port), new Integer32(
					atucRate));
			_ovmap2.put(new OID("1.3.6.1.4.1.27608.1.2.3.1.2.1.10." + port), new Integer32(
					aturRate));
			VariableBinding[] v2 = snmpHandler.set(_ovmap2);
			for (VariableBinding vb : v2) {
				log.debug(vb.toString());
			}
			log.debug("�޸��������������ok>");

			// 3.�򿪶˿ڹ���״̬����Up��
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port,
					new Integer32(1));
			log.debug("ifAdminStatus(1-up,0-down)=" + v3.toInt());

			//4.�ύcommit
			// =============================
			// ����Ϊsave ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.1.4.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());
		} catch (ZtlException err) {
			throw err;
		} catch (SnmpHandlerException snmpHandlerErr) {
			try {
				snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port, new Integer32(1));
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
	
	/**
	 * ��,NST4016B
	 * 
	 * @return
	 */
	private WoResult closedownNST4016B(int port) throws ZtlException {
		try {
			// ��λ�̶�Ϊ0(��1)
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.3)
			// ȡֵ�� disable ( 0 ) , enable ( 1 ) , reset ( 2 )
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,0-down)=" + _v1.toInt());
			if (_v1.toInt() != 0) {
				log.debug("Update ifAdminStatus to 0");
				Variable v1 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3.1." + port,
						new Integer32(0));
				log.debug("ifAdminStatus(1-up,0-down)=" + v1.toInt());
			}

			// 2.�ύcommit
			// =============================
			// ����Ϊsave ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.1.4.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());

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
	 * ��,NST4016B
	 * 
	 * @return
	 */
	private WoResult closedownNST4016BW(int port) throws ZtlException {
		try {
			// ��λ�̶�Ϊ0(��1)
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.4.1.27608.1.2.3.1.2.1.3)
			// ȡֵ�� disable ( 0 ) , enable ( 1 ) , reset ( 2 )
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,0-down)=" + _v1.toInt());
			if (_v1.toInt() != 0) {
				log.debug("Update ifAdminStatus to 0");
				Variable v1 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.3.1.2.1.3." + port,
						new Integer32(0));
				log.debug("ifAdminStatus(1-up,0-down)=" + v1.toInt());
			}

			// 2.�ύcommit
			// =============================
			// ����Ϊsave ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.27608.1.2.1.4.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());

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
	 * ��,NST4024B/NST2024B
	 * 
	 * @return
	 */
	private WoResult closedownNST4024B(int port) throws ZtlException {
		try {
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// ȡֵ�� 1= "Up" ; 2 = "Down"
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + port);
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + port, new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			// 4.�ύcommit
			// =============================
			// ����Ϊconfig ( 1 )
			Variable v4 = snmpHandler.set("1.3.6.1.4.1.890.1.5.8.16.9.1.0", new Integer32(1));
			log.debug("commit ok>" + v4.toInt());

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
	public WoResult alterRate() throws ZtlException {
		log.debug("alterRate() call open method...");
		return open();
	}
	
	/**
	 * 
	 */
	public WoResult open() throws ZtlException {
		log.debug(wo.getDeviceType() + ">open, ConfigWoId=" + wo.getConfigWoId() + ", OriginWoId="
				+ wo.getOriginWoId());
		if ("HUANYU-NST4016B".equalsIgnoreCase(wo.getDeviceType())) {
			return this.openupNST4016B(wo.getPortId(), wo.getAtucRate(), wo.getAtucRate());
		} else if ("HUANYU-NST4016B-W".equalsIgnoreCase(wo.getDeviceType())) {
			return this.openupNST4016BW(wo.getPortId(), wo.getAtucRate(), wo.getAtucRate());
		} else {
			// NST4024B/NST2024B
			return this.openupNST4024B(wo.getPortId(), wo.getAtucRate(), wo.getAtucRate());
		}
	}
	
	/**
	 * 
	 */
	public WoResult close() throws ZtlException {
		log.debug(wo.getDeviceType() + ">close, ConfigWoId=" + wo.getConfigWoId() + ", OriginWoId="
				+ wo.getOriginWoId());
		if ("HUANYU-NST4016B".equalsIgnoreCase(wo.getDeviceType())) {
			return this.closedownNST4016B(wo.getPortId());
		} else if ("HUANYU-NST4016B-W".equalsIgnoreCase(wo.getDeviceType())) {
			return this.closedownNST4016BW(wo.getPortId());
		} else {
			// NST4024B/NST2024B
			return this.closedownNST4024B(wo.getPortId());
		}
	}

	/**
	 * 
	 */
	public void destruction() {
		snmpHandler.close();
	}



}
