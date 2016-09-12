package com.zoom.nos.provision.service;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.util.orm.springjdbc.SimpleJdbcSupport;

@Service
@Transactional
public class WorkOrderHisService extends SimpleJdbcSupport {
   
	private static final String ALL_FEILD = " UUID,originWoId,configWoId,woType,woState,acceptTime,startExeTime,endExeTime,cityId,countyId,bureauId,priority,deviceTypeId,neIp,shelfId,frameId,slotId,portId,adslLineType,atucRate,aturRate,snmpReadCommunity,snmpWriteCommunity,tl1ServerIp,tl1ServerPort,tl1User,tl1Password,exeResult,exeReturnMsg,passes,vlan,iptvvlan,voipvlan,videovlan,macAddress,ponLineType,lineType,deviceMaker,originWoUuid,pvc,vci,vpi,nextWorkOrderUuid,ontId,deviceType,ontLanNumber,ontPotsNumber,mgcIp,sbcIp,iadip,iadipMask,iadipGateway,svlan,cvlan,ontKey,tid,useStatus,voiceVLAN,sbcIpReserve,resourceCode,specLineNum ";
    private static final String QUERY_ALL_SQL = "select "+ALL_FEILD+" from WorkOrderHis";

    private static final String QUERY_SQL = "select "+ALL_FEILD+" from WorkOrderHis where  UUID=? ";

    private static final String INSERT_SQL = "insert into WorkOrderHis ("+ALL_FEILD+") values (:uuid,:originWoId,:configWoId,:woType,:woState,:acceptTime,:startExeTime,:endExeTime,:cityId,:countyId,:bureauId,:priority,:deviceTypeId,:neIp,:shelfId,:frameId,:slotId,:portId,:adslLineType,:atucRate,:aturRate,:snmpReadCommunity,:snmpWriteCommunity,:tl1ServerIp,:tl1ServerPort,:tl1User,:tl1Password,:exeResult,:exeReturnMsg,:passes,:vlan,:iptvvlan,:voipvlan,:videovlan,:macAddress,:ponLineType,:lineType,:deviceMaker,:originWoUuid,:pvc,:vci,:vpi,:nextWorkOrderUuid,:ontId,:deviceType,:ontLanNumber,:ontPotsNumber,:mgcIp,:sbcIp,:iadip,:iadipMask,:iadipGateway,:svlan,:cvlan,:ontKey,:tid,:useStatus,:voiceVLAN,:sbcIpReserve,:resourceCode,:specLineNum)";

    private static final String DELETE_SQL = "delete from WorkOrderHis where  UUID=? ";
  
	/**
	 * get all WorkOrderHis
	 */
	@Transactional(readOnly = true)
	public Collection<WorkOrder> getAllWorkOrderHis() {
		return jdbcTemplate.query(QUERY_ALL_SQL,
				resultBeanMapper(WorkOrder.class));
	}

	/**
	 * get a WorkOrderHis
	 */
	@Transactional(readOnly = true)
	public WorkOrder getWorkOrderHis(Long uuid) {
		return jdbcTemplate.queryForObject(QUERY_SQL,
				resultBeanMapper(WorkOrder.class), uuid);
	}

	/**
	 * create a WorkOrderHis
	 */
	public void createWorkOrderHis(WorkOrder entity) {
		jdbcTemplate.update(INSERT_SQL, paramBeanMapper(entity));
	}

	/**
	 * delete a WorkOrderHis
	 */
	public void deleteWorkOrderHis(Long uuid) {
		jdbcTemplate.update(DELETE_SQL, uuid);
	}
}
