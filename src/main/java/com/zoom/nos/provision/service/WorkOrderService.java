package com.zoom.nos.provision.service;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.DerbyMaxValueIncrementer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.util.orm.springjdbc.SimpleJdbcSupport;

@Service
@Transactional
public class WorkOrderService extends SimpleJdbcSupport {
	private static Logger log = LoggerFactory.getLogger(WorkOrderService.class);
	
	private static final String ALL_FEILD = " UUID,originWoId,configWoId,woType,woState,acceptTime,startExeTime,endExeTime,cityId,countyId,bureauId,priority,deviceTypeId,neIp,shelfId,frameId,slotId,portId,adslLineType,atucRate,aturRate,snmpReadCommunity,snmpWriteCommunity,tl1ServerIp,tl1ServerPort,tl1User,tl1Password,exeResult,exeReturnMsg,passes,vlan,iptvvlan,voipvlan,videovlan,macAddress,ponLineType,lineType,deviceMaker,originWoUuid,pvc,vci,vpi,nextWorkOrderUuid,ontId,deviceType,ontLanNumber,ontPotsNumber,mgcIp,sbcIp,iadip,iadipMask,iadipGateway,svlan,cvlan,ontKey,tid,useStatus,voiceVLAN,sbcIpReserve,resourceCode,specLineNum ";
    private static final String QUERY_ALL_SQL = "select "+ALL_FEILD+" from WorkOrder";

    private static final String QUERY_SQL = "select "+ALL_FEILD+" from WorkOrder where  UUID=? ";

    private static final String INSERT_SQL = "insert into WorkOrder ("+ALL_FEILD+") values (:uuid,:originWoId,:configWoId,:woType,:woState,:acceptTime,:startExeTime,:endExeTime,:cityId,:countyId,:bureauId,:priority,:deviceTypeId,:neIp,:shelfId,:frameId,:slotId,:portId,:adslLineType,:atucRate,:aturRate,:snmpReadCommunity,:snmpWriteCommunity,:tl1ServerIp,:tl1ServerPort,:tl1User,:tl1Password,:exeResult,:exeReturnMsg,:passes,:vlan,:iptvvlan,:voipvlan,:videovlan,:macAddress,:ponLineType,:lineType,:deviceMaker,:originWoUuid,:pvc,:vci,:vpi,:nextWorkOrderUuid,:ontId,:deviceType,:ontLanNumber,:ontPotsNumber,:mgcIp,:sbcIp,:iadip,:iadipMask,:iadipGateway,:svlan,:cvlan,:ontKey,:tid,:useStatus,:voiceVLAN,:sbcIpReserve,:resourceCode,:specLineNum)";

    private static final String DELETE_SQL = "delete from WorkOrder where  UUID=? ";

      


	private DerbyMaxValueIncrementer maxValueIncrementer = null;

	@Autowired
	public void init(DataSource dataSource) {
		super.init(dataSource);
		maxValueIncrementer = new DerbyMaxValueIncrementer(dataSource,
				"WORKORDER_SEQUENCE", "uuid");
		// 设置取唯一key的缓存大小，程序重启时会浪费一些缓存中的key
		maxValueIncrementer.setCacheSize(NosEnv.incrementerCacheSize);
	}
	
	public long getNextUuid(){
		// get max uuid
		return maxValueIncrementer.nextLongValue();
	}

	/**
	 * get all WorkOrder
	 */
	@Transactional(readOnly = true)
	public List<WorkOrder> getAllWorkOrder() {
		return jdbcTemplate.query(QUERY_ALL_SQL,
				resultBeanMapper(WorkOrder.class));
	}

	/**
	 * get a WorkOrder by uuid
	 */
	@Transactional(readOnly = true)
	public WorkOrder getWorkOrder(Long uuid) {
		return jdbcTemplate.queryForObject(QUERY_SQL,
				resultBeanMapper(WorkOrder.class), uuid);
	}


	/**
	 * create a WorkOrder
	 * 
	 * @param entity
	 * @return uuid
	 */
	public void createWorkOrder(WorkOrder entity) {
		jdbcTemplate.update(INSERT_SQL, paramBeanMapper(entity));
	}

	/**
	 * delete a WorkOrder by uuid
	 */
	public void deleteWorkOrder(Long uuid) {
		jdbcTemplate.update(DELETE_SQL, uuid);
	}
	
	/**
	 * delete a WorkOrder by uuid
	 */
	public void deleteWorkOrders() {
		String sql = "delete from WorkOrder";
		jdbcTemplate.update(sql);
	}
	

//	/**
//	 * 查某种执行状态的工单集合
//	 * 
//	 * @param state   工单状态
//	 * @return
//	 */
//	@Transactional(readOnly = true)
//	public List<WorkOrder> getWorkOrderForState(Short state) {
//		return jdbcTemplate.query(QUERY_A_STATE_SQL,
//				resultBeanMapper(WorkOrder.class), state);
//	}
//	private static final String QUERY_A_STATE_SQL =
//		"select "+ALL_FEILD+ "from WORKORDER where WOSTATE=? ";
//	
//	/**
//	 * 取得其他相同配置工单号的工单,不含工单状态为WOSTATE_END的
//	 * 
//	 * @param uuid
//	 * @param configWoId
//	 * @return
//	 */
//	public List<WorkOrder> getOtherWoExeResult(Long uuid, Integer configWoId) {
//		return jdbcTemplate.query(GET_OTHER_WO_EXERESULT,
//				resultBeanMapper(WorkOrder.class), uuid, configWoId);
//	}
//	private static final String GET_OTHER_WO_EXERESULT = 
//		"select "+ALL_FEILD+" from WORKORDER " 
//		+ " where UUID!=? and configWoId=? and WOSTATE!=" + WorkOrder.WOSTATE_END;
	
	/**
	 * 取出可以立即处理的失败工单
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<WorkOrder> getRedoWorkOrder() {
		String sql = 
			"select "+ALL_FEILD+" from WORKORDER a "
			+ "where a.startExeTime<=? " 
			+ "order by endExeTime";
		return jdbcTemplate.query(sql,
				resultBeanMapper(WorkOrder.class), new Date());
	}


//	/**
//	 * 更改工单状态为：等待
//	 * 
//	 * @param uuid
//	 * @param nextStartTime
//	 * @return
//	 */
//	public int updateWoStateToWait(Long uuid, Date nextStartTime) {
//		return jdbcTemplate
//				.update(UPDATE_WOSTATE_WAIT_SQL, nextStartTime, uuid);
//	}
//	private static final String UPDATE_WOSTATE_WAIT_SQL = "update WORKORDER "
//		+ "set WOSTATE=" + WorkOrder.WOSTATE_WAIT
//		+ ", STARTEXETIME=? where UUID=? ";
//	
//	/**
//	 * 更改工单状态为：等待,并设置已执行次数
//	 * 
//	 * @param uuid
//	 * @param nextStartTime
//	 * @param passes
//	 * @return
//	 */
//	public int updateWoStateToWait(Long uuid, Date nextStartTime, int passes) {
//		return jdbcTemplate.update(UPDATE_WOSTATE_WAIT_SQL_2, nextStartTime,
//				(short) passes, uuid);
//	}
//	private static final String UPDATE_WOSTATE_WAIT_SQL_2 = "update WORKORDER "
//		+ "set WOSTATE=" + WorkOrder.WOSTATE_WAIT
//		+ ", STARTEXETIME=?, passes=? where UUID=? ";
	

//	/**
//	 * 更改工单状态为：执行
//	 * 
//	 * @param uuid
//	 * @return
//	 */
//	public int updateWoStateToRun(Long uuid) {
//		return jdbcTemplate.update(UPDATE_WOSTATE_RUN_SQL, new Date(), uuid);
//	}
//	private static final String UPDATE_WOSTATE_RUN_SQL = "update WORKORDER "
//		+ "set WOSTATE=" + WorkOrder.WOSTATE_RUN
//		+ ", STARTEXETIME=? where UUID=? ";
	
//
//	/**
//	 * 更改工单状态为：执行结束
//	 * 
//	 * @param uuid
//	 * @return
//	 */
//	public int updateWoStateToProcessEnd(Long uuid,Long exeResult) {
//		return jdbcTemplate.update(UPDATE_WOSTATE_PROCESSEND_SQL,exeResult, new Date(), uuid);
//	}
//	private static final String UPDATE_WOSTATE_PROCESSEND_SQL = "update WORKORDER "
//		+ "set WOSTATE=" + WorkOrder.WOSTATE_PROCESSEND
//		+ ",exeResult=?, ENDEXETIME=? where UUID=? ";
	

//	/**
//	 * 更改工单状态为：完全结束
//	 * 
//	 * @param uuid
//	 * @return
//	 */
//	public int updateWoStateToEnd(Long uuid,Long exeResult) {
//		int c = jdbcTemplate.update(UPDATE_WOSTATE_END_SQL,exeResult, new Date(), uuid);
//		log.info(uuid + ", process End.");
//		return c;
//	}
//	private static final String UPDATE_WOSTATE_END_SQL = "update WORKORDER "
//		+ "set WOSTATE=" + WorkOrder.WOSTATE_END
//		+ ",exeResult=?, ENDEXETIME=? where UUID=? ";
	

//	/**
//	 * 转储
//	 * 
//	 * @param uuid
//	 * @return
//	 */
//	public int dump() {
//		int c1 = jdbcTemplate.update(DUMP_WORKORDER_INSERT);
//		int c2 = jdbcTemplate.update(DUMP_WORKORDER_DELETE);
//
//		if (c1 != c2) {
//			log.error("dump error: insert count=" + c1 + 
//					", delete count=" + c2);
//		}
//		return c2;
//	}
//	private static final String DUMP_WORKORDER_INSERT = "insert into WORKORDERHIS "
//		+ "select * from WORKORDER where WOSTATE = " + WorkOrder.WOSTATE_END;
//	private static final String DUMP_WORKORDER_DELETE = "delete from WORKORDER "
//		+ "where WOSTATE = " + WorkOrder.WOSTATE_END;
}
