package com.zoom.nos.provision.core;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.operations.AlcatelFtth;
import com.zoom.nos.provision.operations.AlcatelPonDsl;
import com.zoom.nos.provision.operations.AlcatelPonLan;
import com.zoom.nos.provision.operations.FenghuoFtth;
import com.zoom.nos.provision.operations.FenghuoPonDsl;
import com.zoom.nos.provision.operations.FenghuoPonLan;
import com.zoom.nos.provision.operations.GeneralSwitch;
import com.zoom.nos.provision.operations.HuanyuAdsl;
import com.zoom.nos.provision.operations.HuanyuPonDsl;
import com.zoom.nos.provision.operations.HuanyuPonLan;
import com.zoom.nos.provision.operations.HuaweiFtth;
import com.zoom.nos.provision.operations.HuaweiPonDsl;
import com.zoom.nos.provision.operations.HuaweiPonLan;
import com.zoom.nos.provision.operations.IOperations;
import com.zoom.nos.provision.operations.ZteFtth;
import com.zoom.nos.provision.operations.ZtePonDsl;
import com.zoom.nos.provision.operations.ZtePonLan;
import com.zoom.nos.provision.ticketControl.TicketControlFaced;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class Processor implements Runnable {
	private static Logger log = LoggerFactory.getLogger(Processor.class);

	private WorkOrder wo;

	/**
	 * 
	 * @param wo
	 */
	public Processor(WorkOrder wo) {
		this.wo = wo;
	}

	public void run() {
		log.debug("Process start:ConfigWoId=" + wo.getConfigWoId()
				+ ", OriginWoId=" + wo.getOriginWoId());

		IOperations oper = null;
		WoResult woResult = null;
		Date disposeTime = null;
		try {
			if ("��Ϊ".equals(wo.getDeviceMaker())
					|| "HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
				if (wo.getLineType().shortValue() == 1) {
					// dsl
					oper = new HuaweiPonDsl(wo);
				} else if (wo.getLineType().shortValue() == 2) {
					// lan
					oper = new HuaweiPonLan(wo);
				} else if (wo.getLineType().shortValue() == 3) {
					// ftth
					oper = new HuaweiFtth(wo);
				} else {
					log.error("LineType wrong:" + wo.getLineType());
					throw new ZtlException(ErrorConst.UnknowError);
				}
			} else if ("����".equals(wo.getDeviceMaker())
					|| "ZTE".equalsIgnoreCase(wo.getDeviceMaker())) {
				if (wo.getLineType().shortValue() == 1) {
					// dsl
					oper = new ZtePonDsl(wo);
				} else if (wo.getLineType().shortValue() == 2) {
					// lan
					oper = new ZtePonLan(wo);
				} else if (wo.getLineType().shortValue() == 3) {
					// ftth
					oper = new ZteFtth(wo);
				} else {
					log.error("LineType wrong:" + wo.getLineType());
					throw new ZtlException(ErrorConst.UnknowError);
				}
			} else if ("Alcatel".equalsIgnoreCase(wo.getDeviceMaker())
					|| "����".equalsIgnoreCase(wo.getDeviceMaker())) {
				if (wo.getLineType().shortValue() == 1) {
					// dsl
					oper = new AlcatelPonDsl(wo);
				} else if (wo.getLineType().shortValue() == 2) {
					// lan
					oper = new AlcatelPonLan(wo);
				} else if (wo.getLineType().shortValue() == 3) {
					// ftth
					oper = new AlcatelFtth(wo);
				} else {
					log.error("LineType wrong:" + wo.getLineType());
					throw new ZtlException(ErrorConst.UnknowError);
				}
			} else if ("HUANYU".equalsIgnoreCase(wo.getDeviceMaker())) {
				// ��������
				if (wo.getPonLineType().shortValue() == 0) {
					oper = new HuanyuAdsl(wo);
				} else {
					// PON
					if (wo.getLineType().shortValue() == 1) {
						// dsl
						oper = new HuanyuPonDsl(wo);
					} else if (wo.getLineType().shortValue() == 2) {
						// lan
						oper = new HuanyuPonLan(wo);
					} else if (wo.getLineType().shortValue() == 3) {
						// ftth
						log.error("HUANYU ftth ��֧��");
					} else {
						log.error("LineType wrong:" + wo.getLineType());
						throw new ZtlException(ErrorConst.UnknowError);
					}
				}
			} else if ("Fenghuo".equalsIgnoreCase(wo.getDeviceMaker())) {
				if (wo.getLineType().shortValue() == 1) {
					// dsl
					oper = new FenghuoPonDsl(wo);
				} else if (wo.getLineType().shortValue() == 2) {
					// lan
					oper = new FenghuoPonLan(wo);
				} else if (wo.getLineType().shortValue() == 3) {
					// ftth
					oper = new FenghuoFtth(wo);
				} else {
					log.error("LineType wrong:" + wo.getLineType());
					throw new ZtlException(ErrorConst.UnknowError);
				}
			} else if ("switch".equalsIgnoreCase(wo.getDeviceMaker())) {
				// lan
				oper = new GeneralSwitch(wo);
			} else if ("".equalsIgnoreCase(wo.getDeviceMaker())) {
				log.error("not found device maker");
				throw new ZtlException(ErrorConst.incompleteInfo);
			} else {
				log.error("DeviceMaker wrong:[" + wo.getDeviceMaker() + "]");
				throw new ZtlException(ErrorConst.wrongDeviceMaker);
			}

			// --------------------------------------
			switch (wo.getWoType().shortValue()) {
			// case : ��ͨҵ��
			case WorkOrder.WOTYPE_OPEN:
				woResult = oper.open();
				break;
			// case : �ƻ�-��
			case WorkOrder.WOTYPE_MOVE_OPEN:
				woResult = oper.open();
				break;
			// case : ����ҵ��
			case WorkOrder.WOTYPE_CLOSE:
				woResult = oper.close();
				break;
			// case : ע��ONU
			case WorkOrder.WOTYPE_REGISTER_ONU:
				woResult = oper.registerOnu();
				break;
			// case : �����FTTH
			case WorkOrder.WOTYPE_ADD_LAN:
				woResult = oper.open();
				break;
			// case : �ؿ��FTTH
			case WorkOrder.WOTYPE_DEL_LAN:
				woResult = oper.close();
				break;
			// case : �ƻ�-��
			case WorkOrder.WOTYPE_MOVE_CLOSE:
				woResult = oper.close();
				break;
			// case : ��ͨ
			case WorkOrder.WOTYPE_RESUME:
				log.warn("��ͨ,������" + wo.getOriginWoId());
				throw new ZtlException(ErrorConst.wrongServiceStatus);
				// break;
				// case : ��ͣ
			case WorkOrder.WOTYPE_PAUSE:
				log.warn("��ͣ,������" + wo.getOriginWoId());
				throw new ZtlException(ErrorConst.wrongServiceStatus);
				// break;
				// case : ��IPTV
			case WorkOrder.WOTYPE_ADD_IPTV:
				woResult = oper.openIptv();
				break;
			// case : ��IPTV
			case WorkOrder.WOTYPE_DEL_IPTV:
				woResult = oper.closeIptv();
				break;
			// break;
			// case : �������IPTV
			case WorkOrder.WOTYPE_ADD_WBIPTV:
				woResult = oper.openWBIptv();
				break;
			// break;
			// case : �ؿ����IPTV
			case WorkOrder.WOTYPE_DEL_WBIPTV:
				woResult = oper.closeWBIptv();
				break;
			// case : �ƻ��Ŀ�VOIP
			case WorkOrder.WOTYPE_MOVE_ADD_VOIP:
				woResult = oper.openVoip();
				break;
			// case : ��VOIP
			case WorkOrder.WOTYPE_ADD_VOIP:
				woResult = oper.openVoip();
				break;
			// case : ��VIDEO
			case WorkOrder.WOTYPE_ADD_VIDEO:
				woResult = oper.openVideo();
				break;

			// case : �ƻ��Ĺ�VOIP
			case WorkOrder.WOTYPE_MOVE_DEL_VOIP:
				woResult = oper.closeVoip();
				break;
			// case : ��VOIP
			case WorkOrder.WOTYPE_DEL_VOIP:
				woResult = oper.closeVoip();
				break;
			// case : ��VIDEO
			case WorkOrder.WOTYPE_DEL_VIDEO:
				woResult = oper.closeVideo();
				break;
			// case : �޸�����
			case WorkOrder.WOTYPE_ALTER_RATE:
				woResult = oper.alterRate();
				break;
			//
			default:
				log.error(wo.getOriginWoId() + ",wrong WokerOrder Type:"
						+ wo.getWoType());
				throw new ZtlException(ErrorConst.wrongServiceStatus);
				// break;
			}

			disposeTime = new Date();
			// --------------------------------------
			// ���Ҵ������ת��
			wo.setExeReturnMsg(woResult.getDescr());

			if (woResult.getCode() != null
					&& woResult.getCode().equals("rm_not_need_register")) {
				log.info("rms��������Ҫע��");
				TicketControlFaced.sendMsg2RMSWebService(
						String.valueOf(wo.getConfigWoId()),
						String.valueOf(wo.getWoType()),
						String.valueOf(wo.getBuzType()), "1"); //TODO need update
				woResult = WoResult.SUCCESS;
//				return;
			} else if (woResult.getCode() != null
					&& woResult.getCode().equals("rms_need_register")) {
				log.info("rms������Ҫע��");
				TicketControlFaced.sendMsg2RMSWebService(
						String.valueOf(wo.getConfigWoId()),
						String.valueOf(wo.getWoType()),
						String.valueOf(wo.getBuzType()), "1");
				woResult = WoResult.SUCCESS;
//				return;
			}

			if (!"success".equals(woResult.getCode())) {
				long errorId = CoreService.ticketControlService
						.getErrorIdForCodeName(woResult);
				wo.setExeResult(errorId);
			} else {
				wo.setExeResult(ErrorConst.success);
			}

			// --------------------------------------
		} catch (ZtlException e) {
			wo.setExeResult(e.getErrorcode());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			wo.setExeResult(ErrorConst.UnknowError);
		} finally {
			// �ͷ���Դ
			if (oper != null) {
				try {
					oper.destruction();
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}

		try {
			log.debug("Process end:ConfigWoId=" + wo.getConfigWoId()
					+ ", OriginWoId=" + wo.getOriginWoId() + ", ExeResult="
					+ wo.getExeResult());
			// ����ж�
			terminate(wo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * �����ս�
	 * 
	 * @param wo
	 */
	private void terminate(WorkOrder wo) {
		if (wo.getExeResult().longValue() != ErrorConst.success) {
			// ����ִ��ʧ��
			String redoExpression = "";
			try {
				// ȡ�ô�������ʽ
				redoExpression = CoreService.ticketControlService
						.getRedoExpression(wo.getExeResult());
			} catch (Exception e) {
				// �����쳣
				log.error("ȡ�ô�������ʽʱ�����쳣", e);
			}
			try {
				if (StringUtils.isNotBlank(redoExpression)) {
					// ������������ʽ(��������<�ո�>�������)
					String[] _pre = redoExpression.split(" ");
					if (_pre.length == 2 && StringUtils.isNumeric(_pre[0])
							&& StringUtils.isNumeric(_pre[1])) {
						short redoPasses = Short.parseShort(_pre[0]);
						int interval = Integer.parseInt(_pre[1]);
						if (wo.getPasses() <= redoPasses) {
							Date nextStartTime = new Date(
									System.currentTimeMillis() + interval);
							wo.setStartExeTime(nextStartTime);
							wo.setPasses((short) (wo.getPasses().shortValue() + 1));
							CoreService.workOrderService.createWorkOrder(wo);

							CoreService.ticketControlService.setTicketFailed(
									wo.getConfigWoId(), wo.getExeResult());
							log.info("����ʧ�ܣ��������� ConfigWoId="
									+ wo.getConfigWoId() + ",OriginWoId="
									+ wo.getOriginWoId() + ",ErrorId="
									+ wo.getExeResult() + ",passes="
									+ wo.getPasses() + ",nextStartTime="
									+ nextStartTime + ";redoPasses="
									+ redoPasses + ",interval=" + interval);
							// �սᴦ�����������״̬:�ȴ�ִ��
							return;
						}
					} else {
						log.warn("��Ч�Ĵ�������ʽ:" + redoExpression);
					}
				}
			} catch (Exception e) {
				// �����쳣
				log.error(e.getMessage(), e);
			}
		}

		// System.out.println("\n wo.getConfigWoId()==="+wo.getConfigWoId());
		// System.out.println("\n wo.getOriginWoId()==="+wo.getOriginWoId());
		/*
		 * ִ�гɹ� or ʧ�ܲ�������
		 */
		// �����Ĵ��� �ƻ���������Ĺ���״̬--���ۿ����������
		if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
			// if (wo.getWoType().shortValue() != WorkOrder.WOTYPE_MOVE_CLOSE &&
			// wo.getWoType().shortValue() != WorkOrder.WOTYPE_MOVE_DEL_VOIP) {
			try {
				boolean huilong = CoreService.ticketControlService
						.workOrderEnd(wo.getConfigWoId(), wo.getExeResult());
				if (huilong) {
					// ���ջ���--�Ѵ�����֪ͨBSS�ӿ�
					if (SystemFlag.getSystemFlag() != null
							&& SystemFlag.getSystemFlag().equals(
									SystemFlag.JS_UNICOM)) {
						// JS�����������������ɻ���������Ϣ,�����ɳ���ͨ��axis��BSS������Ϣ
						log.debug("js ������Ϊ:" + wo.getConfigWoId()
								+ " ׼������,������:"
								+ String.valueOf(wo.getExeResult()));
						TicketControlFaced.sendMsgToControl(
								String.valueOf(wo.getConfigWoId()),
								wo.getOntKey(),
								String.valueOf(wo.getExeResult()));
					} else {
						TicketControlFaced.sendMsgHuilong();
						//
						log.debug(wo.getOriginWoId() + "-EAI����,�ȴ�����.");
					}
				} else {
					// �ֹ���������������Ϣ
					log.debug(wo.getConfigWoId() + "-�ֹ�����,����.");
				}
				if (SystemFlag.getSystemFlag() != null
						&& SystemFlag.getSystemFlag().equals(
								SystemFlag.JS_UNICOM)) {
					// ������ʱ��Ҫ
				} else {
					CoreService.workOrderHisService.createWorkOrderHis(wo);
				}
			} catch (Exception e) {
				// �����쳣
				log.error(e.getMessage(), e);
			}
		} else {
			// localDB�й�����������ʷ��
			CoreService.workOrderHisService.createWorkOrderHis(wo);
			log.debug(wo.getOriginWoId() + "�ƻ���ִ�����.");

		}
	}
}
