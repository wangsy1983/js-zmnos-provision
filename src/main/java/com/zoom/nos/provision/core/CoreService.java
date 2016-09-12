package com.zoom.nos.provision.core;

//import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.service.CmdLogService;
import com.zoom.nos.provision.service.WorkOrderHisService;
import com.zoom.nos.provision.service.WorkOrderService;
import com.zoom.nos.provision.ticketControl.TicketControlFaced;
import com.zoom.nos.provision.ticketControl.service.TicketControlService;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class CoreService {
	private static Logger log = LoggerFactory.getLogger(CoreService.class);

	// Service
	public static WorkOrderService workOrderService;
	
	public static WorkOrderHisService workOrderHisService;

	public static TicketControlService ticketControlService; 
	
	public static CmdLogService cmdLogService;
	static {
		// init service
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"applicationContext.xml");

		CoreService.workOrderService = (WorkOrderService) context
				.getBean("workOrderService");

		CoreService.workOrderHisService = (WorkOrderHisService) context
				.getBean("workOrderHisService");

		CoreService.cmdLogService = (CmdLogService) context
				.getBean("cmdLogService");

		//
		ApplicationContext ticketControlContext = new ClassPathXmlApplicationContext(
				"ticketControlApplicationContext.xml");

		CoreService.ticketControlService = (TicketControlService) ticketControlContext
				.getBean("ticketControlService");
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			
		}
		log.info("get LOCALSTATE :::"+SystemFlag.getSystemFlag());
	}
	// ------------------------------------------------

	// �Զ�ȡ����������
	private Timer autoTimer;

	public static String areacode = "";

	/**
	 * Core ��ʼ��
	 * 
	 * @param areaCode
	 */
	public CoreService(String areacode) {
		CoreService.areacode = areacode;

		// create a Timer
		autoTimer = new Timer();
	}

	/**
	 * �����Զ�ȡ����������
	 */
	public void startAutoSchedule() {
		// 1.ȡ��DB
		autoTimer
				.schedule(new TimerTask() {
					public void run() {
						receiveWo();
					}
				}, NosEnv.takeMainDBWorkOrder_period,
						NosEnv.takeMainDBWorkOrder_period);

		// 2��ȡ����DB
		autoTimer.schedule(new TimerTask() {
			public void run() {
				processWo();
			}
		}, NosEnv.takeLocalDBWorkOrder_period,
				NosEnv.takeLocalDBWorkOrder_period);
		
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			int time_interval = 60 * 60 * 1000;
			String tmp = CoreService.ticketControlService.getContiunTime("1", CoreService.areacode);
			if (tmp != null && Integer.parseInt(tmp) > 0) {
				time_interval = Integer.parseInt(tmp) * 60 * 1000;
			} 
			log.debug("ȡ��ʧ�ܹ���������ʱ��"+time_interval);
			autoTimer.schedule(new TimerTask() {
				public void run() {
					processFaildWo();
				}
			},time_interval,time_interval);
		}
	}

	/**
	 * �ر��ų̼ƻ�
	 * 
	 */
	public void shutdownAutoSchedule() {
		autoTimer.cancel();
	}

	/**
	 * ����DB��ȡ��������ִ��
	 */
	public synchronized void receiveWo() {
		log.debug("����DB��ȡ��������ִ��...");
		List<WorkOrder> wolist = null;
		try {
			wolist = CoreService.ticketControlService
					.getProcessTicket(CoreService.areacode);
		} catch (Exception e) {
			log.error("����DB��ȡ������ʧ��.");
			log.error(e.getMessage(), e);
		}
		
		if (wolist == null) {
			return;
		}
		
		for (WorkOrder wo : wolist) {
			try {
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
					if (!"��Ϊ".equals(wo.getDeviceMaker()) && !"HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
						Thread.sleep(3000);
					}
				}
				log.info("received, OriginWoId=" + wo.getOriginWoId()
						+ ", ConfigWoId=" + wo.getConfigWoId());
				// ����TL1��SNMP ��¼��Ϣ
				try {
					CoreService.ticketControlService.getDeviceLoginInfo(wo,
							CoreService.areacode);
				} catch (Exception e) {
					log.warn("getDeviceLoginInfo error...");
					if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
						try {
							// �������ƻ����
							boolean huilong = CoreService.ticketControlService.workOrderEnd(wo.getConfigWoId(),
									ErrorConst.incompleteInfo);
							if (huilong) {
								TicketControlFaced.sendMsgHuilong();
								//
								log.debug(wo.getOriginWoId() + "-EAI����,�ȴ�����.");
							} else {
								log.debug(wo.getConfigWoId() + "-�ֹ�����,����.");
							}
						} catch (Exception ex) {
							// �����쳣
							log.error(e.getMessage(), ex);
						}
					} else {
						log.warn(wo.getOriginWoId() + "�ƻ���getDeviceLoginInfo error");
					}
					//û���豸��¼��Ϣ������ʧ��
					continue;
				}
				
				// ִ�й���
				wo.setUuid(CoreService.workOrderService.getNextUuid());
				ThreadPoolExecutor threadPool = ThreadPoolFactory
						.getThreadPoolExecutor(wo.getTl1ServerIp());
				try {
					threadPool.execute(new Processor(wo));
				} catch (RejectedExecutionException e) {
					log.warn(wo.getConfigWoId() + ",TL1IP="
							+ wo.getTl1ServerIp() + "�������������.");
					// localDB,����ʧ�ܱ�
					CoreService.workOrderService.createWorkOrder(wo);
				}
				if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
					// ����DB�У�����״̬Ϊ����ִ��-1
					CoreService.ticketControlService.setTicketStatus(wo
							.getConfigWoId(), 1);
				}else{
					// �ƻ��𣬲�����DB�еĹ���״̬
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				// ��ʧ�ܣ������˹���,��һ����ִ��
			}
		}
	}

	/**
	 * ����DB��ȡ��ʧ�ܹ�������ִ��
	 */
	public synchronized void processWo() {
//		log.debug("����DB��ȡ��ʧ�ܹ�������ִ��...");
		List<WorkOrder> wolist = null;
		try {
			//�õ�������ʧ�ܹ���
			wolist = CoreService.workOrderService.getRedoWorkOrder();
		} catch (Exception e) {
			log.error("����DB��ȡ��ʧ�ܹ�����ʧ��.");
			log.error(e.getMessage(), e);
		}
		for (WorkOrder wo : wolist) {
			try {
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
//					if (!"��Ϊ".equals(wo.getDeviceMaker()) && !"HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
//						Thread.sleep(3000);
//					}
				}
				log.info("��������,OriginWoId=" + wo.getOriginWoId()+
						", ConfigWoId=" + wo.getConfigWoId());
				// ִ�й���
				ThreadPoolExecutor threadPool = ThreadPoolFactory
				.getThreadPoolExecutor(wo.getTl1ServerIp());
				threadPool.execute(new Processor(wo));
				CoreService.workOrderService.deleteWorkOrder(wo.getUuid());
			} catch (RejectedExecutionException e) {
				log.warn("OriginWoId=" + wo.getOriginWoId()+
						", ConfigWoId=" + wo.getConfigWoId()+
						", Tl1ServerIp=" + wo.getTl1ServerIp()+ 
						" TL1�������������.");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * ��DB����ʱȡ��ʧ�ܹ�������ִ��
	 */
	public synchronized void processFaildWo() {
		log.debug("��DB����ʱȡ��ʧ�ܹ���������...");
		try {
			//�õ�������ʧ�ܹ��� 
			CoreService.ticketControlService
					.modifyTicketStatusToWait(3,CoreService.areacode);
		} catch (Exception e) {
			log.error(" ��DB����ʱȡ��ʧ�ܹ���������ʧ��.");
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * ��һ�μ��س���,�����ָ�
	 * 
	 */
	public void WorkOrderReset() {

		log.debug("��һ�μ��س���,�����ָ�");

		CoreService.workOrderService.deleteWorkOrders();

		// ȡ����
		receiveWo();

		log.debug("��һ�μ��س���,�����ָ� End");
	}
}
