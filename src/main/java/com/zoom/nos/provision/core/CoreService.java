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

	// 自动取工单的任务
	private Timer autoTimer;

	public static String areacode = "";

	/**
	 * Core 初始化
	 * 
	 * @param areaCode
	 */
	public CoreService(String areacode) {
		CoreService.areacode = areacode;

		// create a Timer
		autoTimer = new Timer();
	}

	/**
	 * 启动自动取工单的任务
	 */
	public void startAutoSchedule() {
		// 1.取主DB
		autoTimer
				.schedule(new TimerTask() {
					public void run() {
						receiveWo();
					}
				}, NosEnv.takeMainDBWorkOrder_period,
						NosEnv.takeMainDBWorkOrder_period);

		// 2、取本地DB
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
			log.debug("取出失败工单处理间隔时间"+time_interval);
			autoTimer.schedule(new TimerTask() {
				public void run() {
					processFaildWo();
				}
			},time_interval,time_interval);
		}
	}

	/**
	 * 关闭排程计划
	 * 
	 */
	public void shutdownAutoSchedule() {
		autoTimer.cancel();
	}

	/**
	 * 从主DB中取工单，并执行
	 */
	public synchronized void receiveWo() {
		log.debug("从主DB中取工单，并执行...");
		List<WorkOrder> wolist = null;
		try {
			wolist = CoreService.ticketControlService
					.getProcessTicket(CoreService.areacode);
		} catch (Exception e) {
			log.error("从主DB中取工单，失败.");
			log.error(e.getMessage(), e);
		}
		
		if (wolist == null) {
			return;
		}
		
		for (WorkOrder wo : wolist) {
			try {
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
					if (!"华为".equals(wo.getDeviceMaker()) && !"HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
						Thread.sleep(3000);
					}
				}
				log.info("received, OriginWoId=" + wo.getOriginWoId()
						+ ", ConfigWoId=" + wo.getConfigWoId());
				// 塞入TL1、SNMP 登录信息
				try {
					CoreService.ticketControlService.getDeviceLoginInfo(wo,
							CoreService.areacode);
				} catch (Exception e) {
					log.warn("getDeviceLoginInfo error...");
					if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
						try {
							// 不是是移机拆的
							boolean huilong = CoreService.ticketControlService.workOrderEnd(wo.getConfigWoId(),
									ErrorConst.incompleteInfo);
							if (huilong) {
								TicketControlFaced.sendMsgHuilong();
								//
								log.debug(wo.getOriginWoId() + "-EAI工单,等待回笼.");
							} else {
								log.debug(wo.getConfigWoId() + "-手工工单,结束.");
							}
						} catch (Exception ex) {
							// 忽略异常
							log.error(e.getMessage(), ex);
						}
					} else {
						log.warn(wo.getOriginWoId() + "移机拆getDeviceLoginInfo error");
					}
					//没有设备登录信息，工单失败
					continue;
				}
				
				// 执行工单
				wo.setUuid(CoreService.workOrderService.getNextUuid());
				ThreadPoolExecutor threadPool = ThreadPoolFactory
						.getThreadPoolExecutor(wo.getTl1ServerIp());
				try {
					threadPool.execute(new Processor(wo));
				} catch (RejectedExecutionException e) {
					log.warn(wo.getConfigWoId() + ",TL1IP="
							+ wo.getTl1ServerIp() + "最大连接数已满.");
					// localDB,放入失败表
					CoreService.workOrderService.createWorkOrder(wo);
				}
				if (!WorkOrder.isMoveClose(wo.getWoType().shortValue())) {
					// 改主DB中，工单状态为正在执行-1
					CoreService.ticketControlService.setTicketStatus(wo
							.getConfigWoId(), 1);
				}else{
					// 移机拆，不改主DB中的工单状态
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				// 有失败，放弃此工单,下一轮再执行
			}
		}
	}

	/**
	 * 本地DB，取出失败工单，并执行
	 */
	public synchronized void processWo() {
//		log.debug("本地DB，取出失败工单，并执行...");
		List<WorkOrder> wolist = null;
		try {
			//得到重做的失败工单
			wolist = CoreService.workOrderService.getRedoWorkOrder();
		} catch (Exception e) {
			log.error("本地DB，取出失败工单，失败.");
			log.error(e.getMessage(), e);
		}
		for (WorkOrder wo : wolist) {
			try {
				if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
//					if (!"华为".equals(wo.getDeviceMaker()) && !"HUAWEI".equalsIgnoreCase(wo.getDeviceMaker())) {
//						Thread.sleep(3000);
//					}
				}
				log.info("重做工单,OriginWoId=" + wo.getOriginWoId()+
						", ConfigWoId=" + wo.getConfigWoId());
				// 执行工单
				ThreadPoolExecutor threadPool = ThreadPoolFactory
				.getThreadPoolExecutor(wo.getTl1ServerIp());
				threadPool.execute(new Processor(wo));
				CoreService.workOrderService.deleteWorkOrder(wo.getUuid());
			} catch (RejectedExecutionException e) {
				log.warn("OriginWoId=" + wo.getOriginWoId()+
						", ConfigWoId=" + wo.getConfigWoId()+
						", Tl1ServerIp=" + wo.getTl1ServerIp()+ 
						" TL1最大连接数已满.");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * 主DB，定时取出失败工单，并执行
	 */
	public synchronized void processFaildWo() {
		log.debug("主DB，定时取出失败工单，重做...");
		try {
			//得到重做的失败工单 
			CoreService.ticketControlService
					.modifyTicketStatusToWait(3,CoreService.areacode);
		} catch (Exception e) {
			log.error(" 主DB，定时取出失败工单重做，失败.");
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 第一次加载程序,工单恢复
	 * 
	 */
	public void WorkOrderReset() {

		log.debug("第一次加载程序,工单恢复");

		CoreService.workOrderService.deleteWorkOrders();

		// 取工单
		receiveWo();

		log.debug("第一次加载程序,工单恢复 End");
	}
}
