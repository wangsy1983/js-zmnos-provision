package com.zoom.nos.provision.operations;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import com.zoom.nos.provision.CodeUtils;
import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.snmp.SnmpHandler;
import com.zoom.nos.provision.snmp.SnmpHandlerException;
import com.zoom.nos.provision.util.gongdanutil;

public class HuanyuAdsl extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(HuanyuAdsl.class);

	// snmp handler
	private SnmpHandler snmpHandler;

	// snmp time out
	private long timeout = 5000;

	// retry times
	private int retries = 4;

	public HuanyuAdsl(WorkOrder wo) throws ZtlException {
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
			// ����Ŀ��������port�йأ�call buildCommunity ��װ
			int slot_t = wo.getSlotId();
			snmpHandler.setReadCommunity(buildCommunity(slot_t, wo.getSnmpReadCommunity(), wo
					.getDeviceType()));
			snmpHandler.setWriteCommunity(buildCommunity(slot_t, wo.getSnmpWriteCommunity(), wo
					.getDeviceType()));
			//
			snmpHandler.open();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.TCPErr);
		}
	}

	/**
	 * ����,����ģʽ
	 * 
	 * @param atucRate
	 *            �����������,��λ"bps"
	 * 
	 * @param aturRate
	 *            �����������,��λ"bps"
	 * @return
	 */
	private WoResult openupFast(int port, int atucRate, int aturRate) throws ZtlException {

		try {
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// ȡֵ�� 1= "Up" ; 2 = "Down"
			// ������ ADSL ����ӿ�(+4)
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler
						.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			/*
			 * ����ADSL����·����,����ģʽ
			 */
			Integer32 _v2 = null;
			try {
				_v2 = (Integer32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)));
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ�����
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("Line type(2-Fast,3-Interleave)=" + _v2.toInt());
			if (_v2.toInt() != 2) {
				log.debug("Update line type to 2");
				// ����ADSL����·����:��2��=�����٣���3��=����֯
				Variable v2 = snmpHandler.set("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)), new Integer32(2));
				log.debug("Line type(2-Fast,3-Interleave)=" + v2.toInt());
			}

			/*
			 * 2.�޸��û�ҵ�����к�����������ʣ�����ģʽ�£�
			 */
			// =============================
			// ������ ADSL ����ӿ�,��ʽ
			// "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
			// ȡֵ�� ��λ"bps"
			// ���� / ���٣�
			// adslAtucChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.13)
			// ���� / ���٣�
			// adslAturChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.27)
			Map<OID, Variable> _ovmap2 = new HashMap<OID, Variable>();
			_ovmap2.put(
					new OID("1.3.6.1.2.1.10.94.1.1.14.1.13." + (toIndexAsciiFormate(port + 4))),
					new UnsignedInteger32((long) atucRate));
			_ovmap2.put(
					new OID("1.3.6.1.2.1.10.94.1.1.14.1.27." + (toIndexAsciiFormate(port + 4))),
					new UnsignedInteger32((long) aturRate));
			VariableBinding[] v2 = snmpHandler.set(_ovmap2);
			for (VariableBinding vb : v2) {
				log.debug(vb.toString());
			}
			log.debug("�޸��������������ok>");

			/*
			 * 3.�򿪶˿ڹ���״̬����Up��
			 */
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			/*
			 * 4.�ύcommit
			 */
			// =============================
			// ȡ��������ֵ
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("������=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			// commit
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// �������ۼ�1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("�������ۼ�ok>");

		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (ZtlException ztlException) {
			throw ztlException;
		} catch (Exception err) {
			// other error
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;
	}

	/**
	 * ����,��֯ģʽ
	 * 
	 * @param atucRate
	 *            �����������,��λ"bps"
	 * 
	 * @param aturRate
	 *            �����������,��λ"bps"
	 * @return
	 */
	private WoResult openupInterleave(int port, int atucRate, int aturRate) throws ZtlException {

		try {
			/*
			 * 1.�رն˿ڹ���״̬����Down��
			 */
			// =============================
			// Leaf�� ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// ȡֵ�� 1= "Up" ; 2 = "Down"
			// ������ ADSL ����ӿ�(+4)
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("ifAdminStatus(1-up,2-down)=" + _v1.toInt());
			if (_v1.toInt() != 2) {
				log.debug("Update ifAdminStatus to 2");
				Variable v1 = snmpHandler
						.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
				log.debug("ifAdminStatus(1-up,2-down)=" + v1.toInt());
			}

			/*
			 * ����ADSL����·����,��֯ģʽ
			 */
			Integer32 _v2 = null;
			try {
				_v2 = (Integer32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)));
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ�����
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("Line type(2-Fast,3-Interleave)=" + _v2.toInt());
			if (_v2.toInt() != 3) {
				log.debug("Update line type to 3");
				// ����ADSL����·����:��2��=�����٣���3��=����֯
				Variable v2 = snmpHandler.set("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)), new Integer32(3));
				log.debug("Line type(2-Fast,3-Interleave)=" + v2.toInt());
			}

			/*
			 * 2.�޸��û�ҵ�����к�����������ʣ�����ģʽ�£�
			 */
			// =============================
			// ������ ADSL ����ӿ�,��ʽ
			// "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
			// ȡֵ�� ��λ"bps"
			// ���� / ��֯��
			// adslAtucChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.14)
			// ���� / ��֯��
			// adslAturChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.28)
			Map<OID, Variable> _ovmap2 = new HashMap<OID, Variable>();
			_ovmap2.put(
					new OID("1.3.6.1.2.1.10.94.1.1.14.1.14." + (toIndexAsciiFormate(port + 4))),
					new UnsignedInteger32((long) atucRate));
			_ovmap2.put(
					new OID("1.3.6.1.2.1.10.94.1.1.14.1.28." + (toIndexAsciiFormate(port + 4))),
					new UnsignedInteger32((long) aturRate));

			// debug
			VariableBinding[] v2 = snmpHandler.set(_ovmap2);
			for (VariableBinding vb : v2) {
				log.debug(vb.toString());
			}
			log.debug("�޸��������������ok>");

			/*
			 * 3.�򿪶˿ڹ���״̬����Up��
			 */
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			/*
			 * 4.�ύcommit
			 */
			// =============================
			// ȡ��������ֵ
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("������=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			// commit
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// �������ۼ�1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("�������ۼ�ok>");

		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (ZtlException ztlException) {
			throw ztlException;
		} catch (Exception err) {
			// other error
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;
	}

	/**
	 * ����
	 * 
	 * @return
	 */
	private WoResult cancellation(int port) throws ZtlException {

		try {
			// 1.�رն˿ڹ���״̬����Down��
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get�������ת��ΪInteger32���˿ڲ����ڣ�ȡ�˿ڹ���״̬ʧ��
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			if (_v1.toInt() != 2) {
				Variable v1 = snmpHandler
						.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
				log.debug(v1 + "�رն˿�ok>");
			} else {
				log.debug("�˿��Ѿ��ǹر�״̬>");
			}

			// ��������2.�޸��û�ҵ�����к�����������ʣ�����ģʽ�£���ȱʡ����

			// 3.�ύcommit
			// ȡ��������ֵ
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("������=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			//
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// �������ۼ�1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("�������ۼ�ok>");

		} catch (SnmpHandlerException snmpHandlerErr) {
			log.error(snmpHandlerErr.toString());
			if (snmpHandlerErr.getErrorStatus() == ErrorConst.SNMP_TIMEOUT) {
				throw new ZtlException(ErrorConst.SNMP_TIMEOUT);
			} else {
				throw new ZtlException(ErrorConst.SNMP_ERROR);
			}
		} catch (ZtlException ztlException) {
			throw ztlException;
		} catch (Exception err) {
			// other error
			log.error(ExceptionUtils.getFullStackTrace(err));
			throw new ZtlException(ErrorConst.UnknowError);
		}
		// success
		return WoResult.SUCCESS;
	}

	/**
	 * ��װ������
	 * 
	 */
	private String buildCommunity(int slot, String community, String devType) {
		if ("HUANYUBA1000A".equals(devType)) {
			// BA1000A�ͺ� ��λ��+12
			slot = slot + 12;
		}
		// SNMP Communities:
		// GET : proxy@v2@public@2.2.2.[��λ��]@161
		// SET : proxy@v2@private@2.2.2.[��λ��]@161
		// (����:��λ��Ϊ7��ADSL�忨 "proxy@v2@public@2.2.2.7@161")
		StringBuffer sb = new StringBuffer("proxy@v2@").append(community).append("@2.2.2.").append(
				slot).append("@161");
		return sb.toString();
	}

	/**
	 * ת������ΪASCII���ʽ �C
	 * <p>
	 * "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
	 * 
	 * @param index
	 * @return
	 */
	private String toIndexAsciiFormate(int index) {
		// ���룬29���ַ�
		return StringUtils.leftPad(CodeUtils.toAsciiDec(index, "."), 29, "48.");
	}

	/**
	 * 
	 */
	public WoResult open() throws ZtlException {
		if (wo.getAdslLineType() == WorkOrder.ADSLLINETYPE_FASTONLY) {
			// ����
			return this.openupFast(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		} else {
			// ��֯
			return this.openupInterleave(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		}
	}

	public WoResult alterRate() throws ZtlException {
		if (wo.getAdslLineType() == WorkOrder.ADSLLINETYPE_FASTONLY) {
			// ����
			return this.openupFast(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		} else {
			// ��֯
			return this.openupInterleave(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		}
	}

	public WoResult close() throws ZtlException {
		return this.cancellation(wo.getPortId());
	}

	public void destruction() {
		snmpHandler.close();
	}

	protected WoResult closeService(int serviceVlan) throws ZtlException {
		return null;
	}

	protected WoResult openService(int defaultVlan, int serviceVlan) throws ZtlException {
		return null;
	}
}
