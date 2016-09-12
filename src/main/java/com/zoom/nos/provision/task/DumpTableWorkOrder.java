package com.zoom.nos.provision.task;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.zoom.nos.provision.service.WorkOrderService;

/**
 * 表WorkOrder转储任务
 * @author zm
 *
 */
public class DumpTableWorkOrder extends QuartzJobBean {
	private static Logger log = LoggerFactory
			.getLogger(DumpTableWorkOrder.class);

	private WorkOrderService workOrderService;

	public void executeInternal(JobExecutionContext ctx)
			throws JobExecutionException {
		log.info("-- start task DumpTableWorkOrder --");
		try {
//			int c = workOrderService.dump();
//			log.info("dump record count = " + c);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		log.info("-- end task DumpTableWorkOrder --");
	}

	public void setWorkOrderService(WorkOrderService workOrderService) {
		this.workOrderService = workOrderService;
	}
}
