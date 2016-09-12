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

			// 超时
			snmpHandler.setTimeout(timeout);
			// 重试次数
			snmpHandler.setRetries(retries);
			// SnmpVersion is 2
			snmpHandler.setSnmpVersion(SnmpConstants.version2c);
			// 环宇的口令字与口port有关，call buildCommunity 组装
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
	 * 开户,快速模式
	 * 
	 * @param atucRate
	 *            下行最大速率,单位"bps"
	 * 
	 * @param aturRate
	 *            上行最大速率,单位"bps"
	 * @return
	 */
	private WoResult openupFast(int port, int atucRate, int aturRate) throws ZtlException {

		try {
			/*
			 * 1.关闭端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			// 索引： ADSL 物理接口(+4)
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
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
			 * 更改ADSL的线路类型,快速模式
			 */
			Integer32 _v2 = null;
			try {
				_v2 = (Integer32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)));
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("Line type(2-Fast,3-Interleave)=" + _v2.toInt());
			if (_v2.toInt() != 2) {
				log.debug("Update line type to 2");
				// 更改ADSL的线路类型:“2”=仅快速，“3”=仅交织
				Variable v2 = snmpHandler.set("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)), new Integer32(2));
				log.debug("Line type(2-Fast,3-Interleave)=" + v2.toInt());
			}

			/*
			 * 2.修改用户业务上行和下行最大速率（快速模式下）
			 */
			// =============================
			// 索引： ADSL 物理接口,格式
			// "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
			// 取值： 单位"bps"
			// 下行 / 快速：
			// adslAtucChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.13)
			// 上行 / 快速：
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
			log.debug("修改上下行最大速率ok>");

			/*
			 * 3.打开端口管理状态到“Up”
			 */
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			/*
			 * 4.提交commit
			 */
			// =============================
			// 取计数器的值
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("计数器=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			// commit
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// 计数器累加1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("计数器累加ok>");

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
	 * 开户,交织模式
	 * 
	 * @param atucRate
	 *            下行最大速率,单位"bps"
	 * 
	 * @param aturRate
	 *            上行最大速率,单位"bps"
	 * @return
	 */
	private WoResult openupInterleave(int port, int atucRate, int aturRate) throws ZtlException {

		try {
			/*
			 * 1.关闭端口管理状态到“Down”
			 */
			// =============================
			// Leaf： ifAdminStatus (OID 1.3.6.1.2.1.2.2.1.7)
			// 取值： 1= "Up" ; 2 = "Down"
			// 索引： ADSL 物理接口(+4)
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
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
			 * 更改ADSL的线路类型,交织模式
			 */
			Integer32 _v2 = null;
			try {
				_v2 = (Integer32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)));
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			log.debug("Line type(2-Fast,3-Interleave)=" + _v2.toInt());
			if (_v2.toInt() != 3) {
				log.debug("Update line type to 3");
				// 更改ADSL的线路类型:“2”=仅快速，“3”=仅交织
				Variable v2 = snmpHandler.set("1.3.6.1.4.1.4900.1.2.10.2.11.1.3."
						+ (toIndexAsciiFormate(port + 4)), new Integer32(3));
				log.debug("Line type(2-Fast,3-Interleave)=" + v2.toInt());
			}

			/*
			 * 2.修改用户业务上行和下行最大速率（快速模式下）
			 */
			// =============================
			// 索引： ADSL 物理接口,格式
			// "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
			// 取值： 单位"bps"
			// 下行 / 交织：
			// adslAtucChanConfFastMaxTxRate (OID 1.3.6.1.2.1.10.94.1.1.14.1.14)
			// 上行 / 交织：
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
			log.debug("修改上下行最大速率ok>");

			/*
			 * 3.打开端口管理状态到“Up”
			 */
			// =============================
			Variable v3 = snmpHandler.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(1));
			log.debug("ifAdminStatus(1-up,2-down)=" + v3.toInt());

			/*
			 * 4.提交commit
			 */
			// =============================
			// 取计数器的值
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("计数器=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			// commit
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// 计数器累加1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("计数器累加ok>");

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
	 * 销户
	 * 
	 * @return
	 */
	private WoResult cancellation(int port) throws ZtlException {

		try {
			// 1.关闭端口管理状态到“Down”
			Integer32 _v1 = null;
			try {
				_v1 = (Integer32) snmpHandler.get("1.3.6.1.2.1.2.2.1.7." + (port + 4));
			} catch (ClassCastException e) {
				// get结果不能转换为Integer32，端口不存在，取端口管理状态失败
				// org.snmp4j.smi.Null
				throw new ZtlException(ErrorConst.portNotExist);
			}
			if (_v1.toInt() != 2) {
				Variable v1 = snmpHandler
						.set("1.3.6.1.2.1.2.2.1.7." + (port + 4), new Integer32(2));
				log.debug(v1 + "关闭端口ok>");
			} else {
				log.debug("端口已经是关闭状态>");
			}

			// （不做）2.修改用户业务上行和下行最大速率（快速模式下）到缺省设置

			// 3.提交commit
			// 取计数器的值
			Counter32 v = (Counter32) snmpHandler.get("1.3.6.1.4.1.4900.1.2.10.6.1.11.0");
			log.debug("计数器=" + v);

			Map<OID, Variable> _ovmap4 = new HashMap<OID, Variable>();
			//
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.6.4.0"), new Integer32(2));
			// 计数器累加1
			_ovmap4.put(new OID("1.3.6.1.4.1.4900.1.2.10.6.1.11.0"),
					new Counter32(v.getValue() + 1));
			VariableBinding[] v4 = snmpHandler.set(_ovmap4);
			for (VariableBinding vb : v4) {
				log.debug(vb.toString());
			}
			log.debug("计数器累加ok>");

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
	 * 组装口令字
	 * 
	 */
	private String buildCommunity(int slot, String community, String devType) {
		if ("HUANYUBA1000A".equals(devType)) {
			// BA1000A型号 槽位号+12
			slot = slot + 12;
		}
		// SNMP Communities:
		// GET : proxy@v2@public@2.2.2.[槽位号]@161
		// SET : proxy@v2@private@2.2.2.[槽位号]@161
		// (例如:槽位号为7的ADSL板卡 "proxy@v2@public@2.2.2.7@161")
		StringBuffer sb = new StringBuffer("proxy@v2@").append(community).append("@2.2.2.").append(
				slot).append("@161");
		return sb.toString();
	}

	/**
	 * 转换索引为ASCII码格式 –
	 * <p>
	 * "00000000XX" (eg. "0000000005" -48.48.48.48.48.48.48.48.48.53 )
	 * 
	 * @param index
	 * @return
	 */
	private String toIndexAsciiFormate(int index) {
		// 补齐，29个字符
		return StringUtils.leftPad(CodeUtils.toAsciiDec(index, "."), 29, "48.");
	}

	/**
	 * 
	 */
	public WoResult open() throws ZtlException {
		if (wo.getAdslLineType() == WorkOrder.ADSLLINETYPE_FASTONLY) {
			// 快速
			return this.openupFast(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		} else {
			// 交织
			return this.openupInterleave(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		}
	}

	public WoResult alterRate() throws ZtlException {
		if (wo.getAdslLineType() == WorkOrder.ADSLLINETYPE_FASTONLY) {
			// 快速
			return this.openupFast(wo.getPortId(), gongdanutil.getQuickenRate(wo.getAtucRate()) * NosEnv.ratek_unit, wo
					.getAturRate()
					* NosEnv.ratek_unit);
		} else {
			// 交织
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
