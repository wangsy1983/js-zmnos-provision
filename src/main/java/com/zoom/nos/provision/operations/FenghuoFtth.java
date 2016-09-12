package com.zoom.nos.provision.operations;

import java.util.HashMap;
import java.util.List;

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
import com.zoom.nos.provision.tl1.message.FenghuoTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.FenghuoTl1Session;

public class FenghuoFtth extends AbstractOperations {

	private static Logger log = LoggerFactory.getLogger(FenghuoFtth.class);

	private FenghuoTl1Session session = null;

	public FenghuoFtth(WorkOrder wo) throws ZtlException {
		super(wo);
		session = new FenghuoTl1Session(wo.getTl1ServerIp(),
				wo.getTl1ServerPort(), "", 0, wo.getTl1User(),
				wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
		session.open();
	}

	/**
	 * 释放资源
	 */
	public void destruction() {
		session.close();
	}

	/**
	 * 
	 */
	public WoResult alterRate() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		// 限速
		// CFG-LANPORT::OLTID=198.132.0.30,PONID=NA-NA-2-1,ONUIDTYPE=LOID,
		// ONUID=02010200,ONUPORT=NA-NA-NA-1:CTAG::BW=8M;
		cmd = new StringBuffer();
		cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-1");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		if (wo.getAtucRate() < 1024) {
			cmd.append("BW=").append(wo.getAtucRate()).append("K");
		} else {
			cmd.append("BW=").append(wo.getAtucRate() / 1024).append("M");
		}
		cmd.append(";");

		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	/**
	 * 关宽带
	 */
	public WoResult close() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;
		// 关闭LAN端口
		// DACT-LANPORT::OLTID=198.132.0.30,PONID=NA-NA-2-1,ONUIDTYPE=LOID,
		// ONUID=02010200,ONUPORT=NA-NA-NA-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("DACT-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-1");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 如果最后一笔业务，注销
		if (wo.needDelOnu()) {
			this.delOnu();
		}
		
//		if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
//			return WoResult.rms_not_need_register;
//		}
		
		return WoResult.SUCCESS;
	}

	/**
	 * 注销ONT
	 */
	public WoResult delOnu() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		// DEL-ONU::OLTID=198.132.0.30,PONID=NA-NA-2-1:CTAG::ONUIDTYPE=LOID,
		// ONUTYPE=AN5006-04,ONUID=02010200;
		cmd = new StringBuffer();
		cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("ONUIDTYPE=LOID");
		cmd.append(",ONUTYPE=AN5006-04");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(";");

		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	/**
	 * 开通宽带
	 */
	public WoResult open() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		// CVLANID 不能为空（cvlan）
		if (wo.getCvlan().intValue() == -1) {
			throw new ZtlException(ErrorConst.cvlanNotBlank);
		}
		// SVLANID 不能为空（svlan）
		if (wo.getSvlan().intValue() == -1) {
			throw new ZtlException(ErrorConst.svlanNotBlank);
		}

		int needRegisterOnu = 0;
		// 注册
		WoResult _rwo = this.registerOnu();
		if (WoResult.not_need_register.equals(_rwo)) {
			// 接着往下走
			needRegisterOnu = 1;
		} else if (!WoResult.SUCCESS.equals(_rwo)) {
			return _rwo;
		}

		if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
			if (needRegisterOnu == 1) {
				return WoResult.rms_not_need_register;
			} else {
				return WoResult.rms_need_register;
			}
		}

		// 配置LAN端口VLAN信息,限速
		// CFG-LANPORT::OLTID=12.20.4.95,PONID=NA-NA-1-1,ONUIDTYPE=LOID,
		// ONUID=123,ONUPORT=NA-NA-NA-1:CTAG::BW=8M,PVID=1500,VLANMOD=Tag,PCOS=0;
		cmd = new StringBuffer();
		cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		// cmd.append(",ONUPORT=NA-NA-NA-1");
		// cmd.append(",ONUPORT=NA-NA-NA-1|NA-NA-NA-2");
		// 具体开哪个端口根据资源传值来截取
		cmd.append(",ONUPORT=NA-NA-NA-").append(
				CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
						false));
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		if (wo.getAtucRate() < 1024) {
			cmd.append("BW=").append(wo.getAtucRate()).append("K");
		} else {
			cmd.append("BW=").append(wo.getAtucRate() / 1024).append("M");
		}
		cmd.append(",PVID=").append(wo.getCvlan());
		cmd.append(",VLANMOD=Tag,PCOS=0");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		// OLT PON端口VLAN配置
		// ADD-PONVLAN::OLTID=12.20.4.95,PONID=NA-NA-1-1,ONUIDTYPE=LOID,
		// ONUID=123:CTAG::SVLAN=2919,CVLAN=1500[,UV=1500];
		cmd = new StringBuffer();
		cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("SVLAN=").append(wo.getSvlan());
		cmd.append(",CVLAN=").append(wo.getCvlan());
		cmd.append(",UV=").append(wo.getCvlan());
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		// 激活LAN端口
		// ACT-LANPORT::OLTID=12.20.4.95,PONID=NA-NA-1-1,ONUIDTYPE=LOID,
		// ONUID=123,ONUPORT=NA-NA-NA-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("ACT-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-1");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	/**
	 * 注册ONU
	 */
	public WoResult registerOnu() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		String resourceCode = wo.getResourceCode();
		if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON){
			int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (127 - wo.getOntKey().length()));
			resourceCode = resourceCode.substring(resourceCode.length() - limit);
		} else if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON){
			int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (127 - wo.getOntKey().length()));
			resourceCode = resourceCode.substring(resourceCode.length() - limit);
		}
		resourceCode = wo.getOntKey() + resourceCode;

		// 取ONT NAME
		String ontName = this.getOntName(wo.getNeIp(), wo.getShelfId(),
				wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId());
		if (ontName == null) {

			if ("192.168.61.110".equals(wo.getNeIp())) {
				// 没取到ont name，注册ont
				// ADD-ONU::OLTID=198.132.0.30,PONID=NA-NA-2-1:CTAG::
				// AUTHTYPE=LOID,ONUTYPE=AN5006-04,ONUID=02010200;
				cmd = new StringBuffer();
				// add new hgu handle
				if (StringUtils.isNotBlank(wo.getDeviceType())
						&& wo.getDeviceType().length() > 2
						&& !wo.getDeviceType().startsWith("0")
						&& (wo.getDeviceType().toLowerCase().charAt(2) == 'h')) {
					
					// new logic
					cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
					cmd.append(",PONID=NA-");
					if (wo.getFrameId() != null
							&& !wo.getFrameId().equals("-1")) {
						cmd.append(wo.getFrameId());
					} else {
						cmd.append("NA");
					}
					cmd.append("-").append(wo.getSlotId());
					cmd.append("-").append(wo.getPortId());
					cmd.append(":");
					cmd.append(Ctag.getCtag());
					cmd.append("::");
					cmd.append("AUTHTYPE=LOID");
					if (!StringUtils.isEmpty(wo.getDeviceType())
							&& !wo.getDeviceType().startsWith("0")) {
						cmd.append(",ONUTYPE=").append(wo.getDeviceType());
					} else {
						if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
							cmd.append(",ONUTYPE=AN5006-04");
						} else {
							cmd.append(",ONUTYPE=AN5506-04-B2");
						}

					}
					cmd.append(",NAME=").append(resourceCode);
					cmd.append(",ONUID=").append(wo.getOntKey());
				} else {
					// old logic
					cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
					cmd.append(",PONID=NA-");
					if (wo.getFrameId() != null
							&& !wo.getFrameId().equals("-1")) {
						cmd.append(wo.getFrameId());
					} else {
						cmd.append("NA");
					}
					cmd.append("-").append(wo.getSlotId());
					cmd.append("-").append(wo.getPortId());
					cmd.append(":");
					cmd.append(Ctag.getCtag());
					cmd.append("::");
					cmd.append("AUTHTYPE=LOID");
					if (!StringUtils.isEmpty(wo.getDeviceType())
							&& !wo.getDeviceType().startsWith("0")) {
						cmd.append(",ONUTYPE=").append(wo.getDeviceType());
					} else {
						if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
							cmd.append(",ONUTYPE=AN5006-04");
						} else {
							cmd.append(",ONUTYPE=AN5506-04-B2");
						}

					}
					cmd.append(",NAME=").append(resourceCode);
					cmd.append(",ONUID=").append(wo.getOntKey());
				}
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					// 注册失败，再查一次
					String ontName2 = this.getOntName(wo.getNeIp(),
							wo.getShelfId(), wo.getFrameId(), wo.getSlotId(),
							wo.getPortId(), wo.getOntId());
					if (ontName2 != null) {
						if (ontName2.equals(wo.getResourceCode())) {
							// ont存在，名字相同
							log.debug("equal ont name:[" + ontName2 + "]");
							return WoResult.SUCCESS;
						} else {
							// ont name不同，失败
							log.debug("Different ont name:[" + ontName2 + "]");
							throw new ZtlException(
									ErrorConst.repeatOntIdOnDevAdmin);
						}
					}
					// 再查一次也没有找到
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}
			} else {
				// 没取到ont name，注册ont
				// ADD-ONU::OLTID=198.132.0.30,PONID=NA-NA-2-1:CTAG::
				// AUTHTYPE=LOID,ONUTYPE=AN5006-04,ONUID=02010200;
				cmd = new StringBuffer();
				// add new hgu handle
				if (!StringUtils.isEmpty(wo.getDeviceType())
						&& !wo.getDeviceType().startsWith("0")
						&& !(wo.getDeviceType().toLowerCase().indexOf(2) == 'h')) {
					// new logic
					cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
					cmd.append(",PONID=NA-");
					if (wo.getFrameId() != null
							&& !wo.getFrameId().equals("-1")) {
						cmd.append(wo.getFrameId());
					} else {
						cmd.append("NA");
					}
					cmd.append("-").append(wo.getSlotId());
					cmd.append("-").append(wo.getPortId());
					cmd.append(":");
					cmd.append(Ctag.getCtag());
					cmd.append("::");
					cmd.append("AUTHTYPE=LOID");
					if (!StringUtils.isEmpty(wo.getDeviceType())
							&& !wo.getDeviceType().startsWith("0")) {
						cmd.append(",ONUTYPE=").append(wo.getDeviceType());
					} else {
						if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
							cmd.append(",ONUTYPE=AN5006-04");
						} else {
							cmd.append(",ONUTYPE=AN5506-04-B2");
						}

					}
					cmd.append(",NAME=").append(wo.getResourceCode());
					cmd.append(",ONUID=").append(wo.getOntKey());
				} else {
					// old logic
					cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
					cmd.append(",PONID=NA-");
					if (wo.getFrameId() != null
							&& !wo.getFrameId().equals("-1")) {
						cmd.append(wo.getFrameId());
					} else {
						cmd.append("NA");
					}
					cmd.append("-").append(wo.getSlotId());
					cmd.append("-").append(wo.getPortId());
					cmd.append(":");
					cmd.append(Ctag.getCtag());
					cmd.append("::");
					cmd.append("AUTHTYPE=LOID");
					if (!StringUtils.isEmpty(wo.getDeviceType())
							&& !wo.getDeviceType().startsWith("0")) {
						cmd.append(",ONUTYPE=").append(wo.getDeviceType());
					} else {
						if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
							cmd.append(",ONUTYPE=AN5006-04");
						} else {
							cmd.append(",ONUTYPE=AN5506-04-B2");
						}

					}
					cmd.append(",NAME=").append(wo.getResourceCode());
					cmd.append(",ONUID=").append(wo.getOntKey());
				}
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					// 注册失败，再查一次
					String ontName2 = this.getOntName(wo.getNeIp(),
							wo.getShelfId(), wo.getFrameId(), wo.getSlotId(),
							wo.getPortId(), wo.getOntId());
					if (ontName2 != null) {
						if (ontName2.equals(wo.getResourceCode())) {
							// ont存在，名字相同
							log.debug("equal ont name:[" + ontName2 + "]");
							return WoResult.SUCCESS;
						} else {
							// ont name不同，失败
							log.debug("Different ont name:[" + ontName2 + "]");
							throw new ZtlException(
									ErrorConst.repeatOntIdOnDevAdmin);
						}
					}
					// 再查一次也没有找到
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}
			}

			return WoResult.SUCCESS;
		} else {
			// ont 已存在
			if (ontName.equals(wo.getResourceCode())) {
				// ont存在，名字相同
				log.debug("equal ont name:[" + ontName + "]");
				return WoResult.SUCCESS;
			} else {
				// ont name不同，失败
				log.debug("Different ont name:[" + ontName + "]");
				throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
			}
		}
	}

	/**
	 * 开语音
	 */
	public WoResult openVoip() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		// 验证SbcIp、sbcIpReserve 不为空
		if (StringUtils.isBlank(wo.getSbcIp())) {
			throw new ZtlException(ErrorConst.sbcIpNotBlank);
		}
		if (StringUtils.isBlank(wo.getSbcIpReserve())) {
			throw new ZtlException(ErrorConst.sbcIpReserveNotBlank);
		}

		int voicePort = -1;
		int needRegisterOnu = 0;
		try {
			voicePort = Integer.parseInt(wo.getTid()) + 1;
		} catch (Exception e) {
			throw new ZtlException(ErrorConst.tidNeedNumber);
		}

		WoResult _rwo = this.registerOnu();
		if (WoResult.not_need_register.equals(_rwo)) {
			// 接着往下走
			needRegisterOnu = 1;
		} else if (!WoResult.SUCCESS.equals(_rwo)) {
			return _rwo;
		}

		if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
			if (needRegisterOnu == 1) {
				return WoResult.rms_not_need_register;
			} else {
				return WoResult.rms_need_register;
			}
		}

		// 激活VOIP端口
		// ACT-VOIPPORT::OLTID=198.132.0.30,PONID=NA-NA-2-1,ONUIDTYPE=LOID,
		// ONUID=02010200,ONUPORT=NA-NA-NA-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("ACT-VOIPPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-").append(voicePort);
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 配置ONU VOIP信息
		// CFG-VOIPSERVICE::OLTID=198.132.0.30,PONID=NA-NA-2-1,ONUIDTYPE=LOID,
		// ONUID=02010200,ONUPORT=NA-NA-NA-1:CTAG::PT=H.248,CCOS=7,
		// IPMODE=STATIC,VOIPVLAN=3000,EID=10.94.191.4,TID=A0,MGCIP1=10.64.7.97,
		// IP=10.94.191.4,IPMASK=255.255.255.0,IPGATEWAY=10.94.191.1;
		cmd = new StringBuffer();
		cmd.append("CFG-VOIPSERVICE::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-").append(voicePort);
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append("PT=H.248,CCOS=7,IPMODE=STATIC");
		cmd.append(",VOIPVLAN=").append(wo.getVoiceVLAN());
		cmd.append(",EID=").append(wo.getIadip());
		cmd.append(",TID=A").append(wo.getTid());
		cmd.append(",MGCIP1=").append(wo.getSbcIp());
		cmd.append(",MGCIP2=").append(wo.getSbcIpReserve());
		cmd.append(",IP=").append(wo.getIadip());
		cmd.append(",IPMASK=").append(wo.getIadipMask());
		cmd.append(",IPGATEWAY=").append(wo.getIadipGateway());
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		return WoResult.SUCCESS;
	}

	/**
	 * 关语音
	 */
	public WoResult closeVoip() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		int voicePort = -1;
		try {
			voicePort = Integer.parseInt(wo.getTid()) + 1;
		} catch (Exception e) {
			throw new ZtlException(ErrorConst.tidNeedNumber);
		}

		// 关闭VOIP端口
		// DACT-VOIPPORT::OLTID=198.132.0.30,PONID=NA-NA-2-1,ONUIDTYPE=LOID,
		// ONUID=02010200,ONUPORT=NA-NA-NA-1:CTAG::;
		cmd = new StringBuffer();
		cmd.append("DACT-VOIPPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-").append(voicePort);
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		// 如果最后一笔业务，注销
		if (wo.needDelOnu()) {
			this.delOnu();
		}
		
//		if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
//			return WoResult.rms_not_need_register;
//		}
		
		return WoResult.SUCCESS;
	}

	/**
	 * 开IPTV
	 */
	public WoResult openIptv() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;
		// IPTV查询PVC(如果只有一条PVC,开IPTV当作开宽带一样处理)
		ServiceVlan serVlan = CoreService.ticketControlService.getServiceVlan(
				2, wo.getWoType(), "0" + wo.getCityId());
		if (serVlan == null) {
			throw new ZtlException(ErrorConst.configProfileNoutFound);
		}

		String ontName2 = this.getOntName(wo.getNeIp(), wo.getShelfId(),
				wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId());
		if (ontName2 != null) {
			if (ontName2.equals(wo.getResourceCode())) {
				cmd = new StringBuffer();
				cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
				cmd.append(",PONID=NA-");
				if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
					cmd.append(wo.getFrameId());
				} else {
					cmd.append("NA");
				}
				cmd.append("-").append(wo.getSlotId());
				cmd.append("-").append(wo.getPortId());
				cmd.append(",ONUIDTYPE=LOID");
				cmd.append(",ONUID=").append(wo.getOntKey());
				cmd.append(",ONUPORT=NA-NA-NA-4");
				cmd.append(":");
				cmd.append(Ctag.getCtag());
				cmd.append("::");
				cmd.append("BW=8M");
				cmd.append(",VLANMOD=Tag");
				cmd.append(",PVID=").append(serVlan.getVlan());
				cmd.append(",PCOS=4");
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}

				cmd = new StringBuffer();
				cmd.append("ADD-LANIPTVPORT::OLTID=").append(wo.getNeIp());
				cmd.append(",PONID=NA-");
				if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
					cmd.append(wo.getFrameId());
				} else {
					cmd.append("NA");
				}
				cmd.append("-").append(wo.getSlotId());
				cmd.append("-").append(wo.getPortId());
				cmd.append(",ONUIDTYPE=LOID");
				cmd.append(",ONUID=").append(wo.getOntKey());
				cmd.append(",ONUPORT=NA-NA-NA-4");
				cmd.append(":");
				cmd.append(Ctag.getCtag());
				cmd.append("::");
				cmd.append("MVLAN=").append(serVlan.getIgmpVlan());
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}

				cmd = new StringBuffer();
				cmd.append("CFG-LANIPTVPORT::OLTID=").append(wo.getNeIp());
				cmd.append(",PONID=NA-");
				if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
					cmd.append(wo.getFrameId());
				} else {
					cmd.append("NA");
				}
				cmd.append("-").append(wo.getSlotId());
				cmd.append("-").append(wo.getPortId());
				cmd.append(",ONUIDTYPE=LOID");
				cmd.append(",ONUID=").append(wo.getOntKey());
				cmd.append(",ONUPORT=NA-NA-NA-4");
				cmd.append(":");
				cmd.append(Ctag.getCtag());
				cmd.append("::");
				cmd.append("FLMODE=Enabled");
				cmd.append(",MAXGRP=130");
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}

				cmd = new StringBuffer();
				cmd.append("ACT-LANPORT::OLTID=").append(wo.getNeIp());
				cmd.append(",PONID=NA-");
				if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
					cmd.append(wo.getFrameId());
				} else {
					cmd.append("NA");
				}
				cmd.append("-").append(wo.getSlotId());
				cmd.append("-").append(wo.getPortId());
				cmd.append(",ONUIDTYPE=LOID");
				cmd.append(",ONUID=").append(wo.getOntKey());
				cmd.append(",ONUPORT=NA-NA-NA-4");
				cmd.append(":");
				cmd.append(Ctag.getCtag());
				cmd.append("::");
				cmd.append(";");
				rm = session.exeFenghuoCmd(cmd.toString(), wo);
				if (rm.isFailed()) {
					log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
							+ rm.getEn() + rm.getEnDesc());
					return new WoResult(rm);
				}
				return WoResult.SUCCESS;
			} else {
				// ont name不同，失败
				log.debug("Different ont name:[" + ontName2 + "]");
				throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
			}
		} else {
			throw new ZtlException(ErrorConst.iptvNotFindONU);
		}
	}

	/**
	 * 多业务,同是开宽带和IPTV
	 * 
	 */
	public WoResult openWBIptv() throws ZtlException {
		WoResult open = open();
		if (!"success".equals(open.getCode())) {
			return open;
		}
		WoResult iptv = openIptv();
		if (!"success".equals(iptv.getCode())) {
			return iptv;
		}
		return WoResult.SUCCESS;
	}

	public WoResult closeIPTV() throws ZtlException {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;

		// IPTV查询PVC
		ServiceVlan serVlan = CoreService.ticketControlService.getServiceVlan(
				2, wo.getWoType(), "0" + wo.getCityId());
		if (serVlan == null) {
			throw new ZtlException(ErrorConst.configProfileNoutFound);
		}

		cmd = new StringBuffer();
		cmd.append("DACT-LANPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-4");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		cmd = new StringBuffer();
		cmd.append("DEL-LANPORTVLAN::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-4");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::UV=").append(serVlan.getVlan());
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}

		cmd = new StringBuffer();
		cmd.append("DEL-LANIPTVPORT::OLTID=").append(wo.getNeIp());
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(",ONUPORT=NA-NA-NA-4");
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::MVLAN=").append(serVlan.getIgmpVlan());
		cmd.append(";");
		rm = session.exeFenghuoCmd(cmd.toString(), wo);
		if (rm.isFailed()) {
			log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
					+ rm.getEn() + rm.getEnDesc());
			return new WoResult(rm);
		}
		return WoResult.SUCCESS;
	}

	/**
	 * 多业务,同是关宽带和IPTV
	 * 
	 */
	public WoResult closeWBIptv() throws ZtlException {
		WoResult iptv = closeIptv();
		if (!"success".equals(iptv.getCode())) {
			return iptv;
		}
		WoResult close = close();
		if (!"success".equals(close.getCode())) {
			return close;
		}
		return WoResult.SUCCESS;
	}

	/**
	 * 
	 * @param ip
	 * @param rn
	 * @param fn
	 * @param sn
	 * @param pn
	 * @param ontId
	 * @return
	 * @throws ZtlException
	 */
	private String getOntName(String ip, String rn, String fn, Short sn,
			Integer pn, String ontId) {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;
		String ontName = "";// , ontKey = "";

		// LST-ONU::OLTID=10.250.18.102,PONID=NA-NA-4-1,ONUIDTYPE=LOID,ONUID=whdx04:CTAG::;
		cmd = new StringBuffer();
		cmd.append("LST-ONU::OLTID=").append(ip);
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		try {
			rm = session.exeFhListCmd(cmd.toString(), wo);
		} catch (ZtlException e) {
			log.debug("get ontName failed:" + e.getErrorcode() + ","
					+ e.getMessage());
			log.debug(e.toString(), e);
			return null;
		}

		ontName = rm.getResult().get("NAME");

		// ontKey = rm.getResult().get("ONUID");
		// log.info("fenghuo ontkey="+ontKey);
		// if (ontKey != null && ontKey.compareTo("") > 0) {
		// ontName += "@@@@" + ontKey;
		// }
		return ontName;
	}

	private boolean hasNotServiceport(String ip, Short rn, Short fn, Short sn,
			Short pn, String ontId) {
		StringBuffer cmd = null;
		FenghuoTL1ResponseMessage rm = null;
		String rs = "";

		// LST-POTS::OLTID=172.26.6.2,PONID=NA-NA-11-03,ONUIDTYPE=LOID,ONUID=11030001:1::;
		cmd = new StringBuffer();
		cmd.append("LST-ONU::OLTID=").append(ip);
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");

		try {
			rm = session.exeFhMuListCmd(cmd.toString(), wo);
		} catch (ZtlException e) {
			log.debug("fenghuo-hasNotServiceport failed:" + e.getErrorcode()
					+ "," + e.getMessage());
			log.debug(e.toString(), e);
			return false;
		}

		List<HashMap<String, String>> muResult = rm.getMuResult();
		for (HashMap<String, String> rsmap : muResult) {
			rs = rsmap.get("TID");

			if (StringUtils.isNotBlank(rs) && !"--".equals(rs)) {
				log.debug("TID =" + rs);
				return false;
			}
		}

		// LST-PORTVLAN::OLTID=172.26.6.2,PONID=NA-NA-11-03,ONUIDTYPE=LOID,ONUID=11030001:1::;
		cmd = new StringBuffer();
		cmd.append("LST-PORTVLAN::OLTID=").append(ip);
		cmd.append(",PONID=NA-");
		if (wo.getFrameId() != null && !wo.getFrameId().equals("-1")) {
			cmd.append(wo.getFrameId());
		} else {
			cmd.append("NA");
		}
		cmd.append("-").append(wo.getSlotId());
		cmd.append("-").append(wo.getPortId());
		cmd.append(",ONUIDTYPE=LOID");
		cmd.append(",ONUID=").append(wo.getOntKey());
		cmd.append(":");
		cmd.append(Ctag.getCtag());
		cmd.append("::");
		cmd.append(";");
		try {
			rm = session.exeFhListCmd(cmd.toString(), wo);
		} catch (ZtlException e) {
			log.debug("fenghuo-hasNotServiceport failed:" + e.getErrorcode()
					+ "," + e.getMessage());
			log.debug(e.toString(), e);
			return false;
		}

		muResult = rm.getMuResult();
		for (HashMap<String, String> rsmap : muResult) {
			rs = rsmap.get("CVLAN");

			if (StringUtils.isNotBlank(rs) && !"--".equals(rs)) {
				log.debug("fenghuo CVLAN =" + rs);
				return false;
			}
		}

		return true;
	}

}