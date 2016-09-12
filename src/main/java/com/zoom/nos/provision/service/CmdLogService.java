package com.zoom.nos.provision.service;

import java.util.Collection;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.DerbyMaxValueIncrementer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.entity.CmdLog;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;
import com.zoom.nos.provision.util.orm.springjdbc.SimpleJdbcSupport;

@Service
@Transactional
public class CmdLogService extends SimpleJdbcSupport {
//	private static Logger log = LoggerFactory.getLogger(CmdLogService.class);
//
//	private static String QUERY_ALL_SQL = "select uuid,workOrderId,originWoId,cmd,woResultId,codeName,descr,exeTime from CmdLog";
//
//	private static String QUERY_SQL = "select uuid,workOrderId,originWoId,cmd,woResultId,codeName,descr,exeTime from CmdLog where   uuid=? ";
//
//	private static String INSERT_SQL = "insert into CmdLog (uuid,workOrderId,originWoId,cmd,woResultId,codeName,descr,exeTime) values (  :uuid  , :workOrderId  , :originWoId  , :cmd  , :woResultId  , :codeName  , :descr  , :exeTime )";
//
//	private static String DELETE_SQL = "delete from CmdLog where   uuid=? ";
//
//	private DerbyMaxValueIncrementer maxValueIncrementer = null;
//
//	@Autowired
//	public void init(DataSource dataSource) {
//		super.init(dataSource);
//		maxValueIncrementer = new DerbyMaxValueIncrementer(dataSource,
//				"CMDLOG_SEQUENCE", "uuid");
//		// 设置取唯一key的缓存大小，程序重启时会浪费一些缓存中的key
//		maxValueIncrementer.setCacheSize(NosEnv.incrementerCacheSize);
//	}
//
//	/**
//	 * get all CmdLog
//	 */
//	@Transactional(readOnly = true)
//	public Collection<CmdLog> getAllCmdLog() {
//		return jdbcTemplate
//				.query(QUERY_ALL_SQL, resultBeanMapper(CmdLog.class));
//	}
//
//	/**
//	 * get a CmdLog
//	 */
//	@Transactional(readOnly = true)
//	public CmdLog getCmdLog(Long uuid) {
//		return jdbcTemplate.queryForObject(QUERY_SQL,
//				resultBeanMapper(CmdLog.class), uuid);
//	}
//
//	/**
//	 * create a CmdLog
//	 */
//	public void createCmdLog(String cmd, WorkOrder wo, TL1ResponseMessage msg) {
//		try {
//			// log cmd
//			CmdLog cmdlog = new CmdLog();
//			cmdlog.setCmd(cmd);
//			if (wo != null) {
//				cmdlog.setWorkOrderId(wo.getUuid());
//				cmdlog.setOriginWoId(wo.getOriginWoId());
//			}
//			cmdlog.setExeTime(new Date());
//			if (msg.isSuccess()) {
//				cmdlog.setWoResultId(0L);
//			} else {
//				cmdlog.setWoResultId(1L);
//				cmdlog.setCodeName(msg.getEn());
//				cmdlog.setDescr(msg.getEnDesc());
//			}
//			// get max uuid
//			Long _uuid = maxValueIncrementer.nextLongValue();
//			cmdlog.setUuid(_uuid);
//			log.debug("createCmdLog:" + cmdlog);
//			jdbcTemplate.update(INSERT_SQL, paramBeanMapper(cmdlog));
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//	}
//
//	/**
//	 * delete a CmdLog
//	 */
//	public void deleteCmdLog(Long uuid) {
//		jdbcTemplate.update(DELETE_SQL, uuid);
//	}
}
