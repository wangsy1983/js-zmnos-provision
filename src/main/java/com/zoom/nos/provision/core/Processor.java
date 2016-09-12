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
			if ("华为".equals(wo.getDeviceMaker())
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
			} else if ("中兴".equals(wo.getDeviceMaker())
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
					|| "贝尔".equalsIgnoreCase(wo.getDeviceMaker())) {
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
				// 大连环宇
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
						log.error("HUANYU ftth 不支持");
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
			// case : 开通业务
			case WorkOrder.WOTYPE_OPEN:
				woResult = oper.open();
				break;
			// case : 移机-开
			case WorkOrder.WOTYPE_MOVE_OPEN:
				woResult = oper.open();
				break;
			// case : 销户业务
			case WorkOrder.WOTYPE_CLOSE:
				woResult = oper.close();
				break;
			// case : 注册ONU
			case WorkOrder.WOTYPE_REGISTER_ONU:
				woResult = oper.registerOnu();
				break;
			// case : 开宽带FTTH
			case WorkOrder.WOTYPE_ADD_LAN:
				woResult = oper.open();
				break;
			// case : 关宽带FTTH
			case WorkOrder.WOTYPE_DEL_LAN:
				woResult = oper.close();
				break;
			// case : 移机-关
			case WorkOrder.WOTYPE_MOVE_CLOSE:
				woResult = oper.close();
				break;
			// case : 复通
			case WorkOrder.WOTYPE_RESUME:
				log.warn("复通,？？？" + wo.getOriginWoId());
				throw new ZtlException(ErrorConst.wrongServiceStatus);
				// break;
				// case : 暂停
			case WorkOrder.WOTYPE_PAUSE:
				log.warn("暂停,？？？" + wo.getOriginWoId());
				throw new ZtlException(ErrorConst.wrongServiceStatus);
				// break;
				// case : 开IPTV
			case WorkOrder.WOTYPE_ADD_IPTV:
				woResult = oper.openIptv();
				break;
			// case : 关IPTV
			case WorkOrder.WOTYPE_DEL_IPTV:
				woResult = oper.closeIptv();
				break;
			// break;
			// case : 开宽带和IPTV
			case WorkOrder.WOTYPE_ADD_WBIPTV:
				woResult = oper.openWBIptv();
				break;
			// break;
			// case : 关宽带和IPTV
			case WorkOrder.WOTYPE_DEL_WBIPTV:
				woResult = oper.closeWBIptv();
				break;
			// case : 移机的开VOIP
			case WorkOrder.WOTYPE_MOVE_ADD_VOIP:
				woResult = oper.openVoip();
				break;
			// case : 开VOIP
			case WorkOrder.WOTYPE_ADD_VOIP:
				woResult = oper.openVoip();
				break;
			// case : 开VIDEO
			case WorkOrder.WOTYPE_ADD_VIDEO:
				woResult = oper.openVideo();
				break;

			// case : 移机的关VOIP
			case WorkOrder.WOTYPE_MOVE_DEL_VOIP:
				woResult = oper.closeVoip();
				break;
			// case : 关VOIP
			case WorkOrder.WOTYPE_DEL_VOIP:
				woResult = oper.closeVoip();
				break;
			// case : 关VIDEO
			case WorkOrder.WOTYPE_DEL_VIDEO:
				woResult = oper.closeVideo();
				break;
			// case : 修改速率
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
			// 厂家错误代码转换
			wo.setExeReturnMsg(woResult.getDescr());

			if (woResult.getCode() != null
					&& woResult.getCode().equals("rm_not_need_register")) {
				log.info("rms工单不需要注册");
				TicketControlFaced.sendMsg2RMSWebService(
						String.valueOf(wo.getConfigWoId()),
						String.valueOf(wo.getWoType()),
						String.valueOf(wo.getBuzType()), "1"); //TODO need update
				woResult = WoResult.SUCCESS;
//				return;
			} else if (woResult.getCode() != null
					&& woResult.getCode().equals("rms_need_register")) {
				log.info("rms工单需要注册");
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
			// 释放资源
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
			// 结果判定
			terminate(wo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 工单终结
	 * 
	 * @param wo
	 */
	private void terminate(WorkOrder wo) {
		if (wo.getExeResult().longValue() != ErrorConst.success) {
			// 工单执行失败
			String redoExpression = "";
			try {
				// 取得错误处理表达式
				redoExpression = CoreService.ticketControlService
						.getRedoExpression(wo.getExeResult());
			} catch (Exception e) {
				// 忽略异常
				log.error("取得错误处理表达式时发生异常", e);
			}
			try {
				if (StringUtils.isNotBlank(redoExpression)) {
					// 解析错误处理表达式(重做次数<空格>间隔毫秒)
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
							log.info("工单失败，可以重做 ConfigWoId="
									+ wo.getConfigWoId() + ",OriginWoId="
									+ wo.getOriginWoId() + ",ErrorId="
									+ wo.getExeResult() + ",passes="
									+ wo.getPasses() + ",nextStartTime="
									+ nextStartTime + ";redoPasses="
									+ redoPasses + ",interval=" + interval);
							// 终结处理结束，工单状态:等待执行
							return;
						}
					} else {
						log.warn("无效的错误处理表达式:" + redoExpression);
					}
				}
			} catch (Exception e) {
				// 忽略异常
				log.error(e.getMessage(), e);
			}
		}

		// System.out.println("\n wo.getConfigWoId()==="+wo.getConfigWoId());
		// System.out.println("\n wo.getOriginWoId()==="+wo.getOriginWoId());
		/*
		 * 执行成功 or 失败不能重做
		 */
		// 结束的处理 移机拆机不更改工单状态--无论宽带还是语音
		if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
			// if (wo.getWoType().shortValue() != WorkOrder.WOTYPE_MOVE_CLOSE &&
			// wo.getWoType().shortValue() != WorkOrder.WOTYPE_MOVE_DEL_VOIP) {
			try {
				boolean huilong = CoreService.ticketControlService
						.workOrderEnd(wo.getConfigWoId(), wo.getExeResult());
				if (huilong) {
					// 江苏回笼--把处理结果通知BSS接口
					if (SystemFlag.getSystemFlag() != null
							&& SystemFlag.getSystemFlag().equals(
									SystemFlag.JS_UNICOM)) {
						// JS工单处理结束后给主采机器发送消息,由主采程序通过axis向BSS发送消息
						log.debug("js 工单号为:" + wo.getConfigWoId()
								+ " 准备回笼,处理结果:"
								+ String.valueOf(wo.getExeResult()));
						TicketControlFaced.sendMsgToControl(
								String.valueOf(wo.getConfigWoId()),
								wo.getOntKey(),
								String.valueOf(wo.getExeResult()));
					} else {
						TicketControlFaced.sendMsgHuilong();
						//
						log.debug(wo.getOriginWoId() + "-EAI工单,等待回笼.");
					}
				} else {
					// 手工工单不发回笼消息
					log.debug(wo.getConfigWoId() + "-手工工单,结束.");
				}
				if (SystemFlag.getSystemFlag() != null
						&& SystemFlag.getSystemFlag().equals(
								SystemFlag.JS_UNICOM)) {
					// 江苏暂时不要
				} else {
					CoreService.workOrderHisService.createWorkOrderHis(wo);
				}
			} catch (Exception e) {
				// 忽略异常
				log.error(e.getMessage(), e);
			}
		} else {
			// localDB中工单，记入历史表
			CoreService.workOrderHisService.createWorkOrderHis(wo);
			log.debug(wo.getOriginWoId() + "移机拆执行完毕.");

		}
	}
}
