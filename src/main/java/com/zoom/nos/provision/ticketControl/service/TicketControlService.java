package com.zoom.nos.provision.ticketControl.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.nos.provision.EncryptData;
import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.ServiceVlan;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.SystemFlag;
import com.zoom.nos.provision.util.orm.springjdbc.SimpleJdbcSupport;

@Service
@Transactional
public class TicketControlService extends SimpleJdbcSupport {
	private static Logger log = LoggerFactory.getLogger(TicketControlService.class);

	@Autowired
	public void init(DataSource dataSource) {
		super.init(dataSource);
	}


	/**
	 * 取速率模板
	 * @param rate
	 * @param serverIp
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getLineProfileName(WorkOrder wo) {
		int rate = wo.getAtucRate();
		String serverIp = wo.getTl1ServerIp();
		String areacode = "0" + wo.getCityId();

		String sql = "";
		if (wo.getLineType().shortValue() == 1) {
			sql = "select a.ProfileName from AdslTL1ProfileInfo a, TL1ServerInfo b where"
				+ " a.TL1ServerIP=b.TL1ServerIP and a.AreaCode=b.AreaCode "
				+ " and a.LineMode=b.LineMode and "
				+ " a.AdslDnMaxRate=? and a.TL1ServerIP=? and a.AreaCode=?";

		} else if (wo.getLineType().shortValue() == 2) {
			sql = "select a.ProfileName from AdslTL1ProfileInfo a where a.LineMode='lan' and"
				+ " a.AdslDnMaxRate=? and a.TL1ServerIP=? and a.AreaCode=?";
		} else {
			sql = "select a.ProfileName from AdslTL1ProfileInfo a where a.LineMode='ftth' and"
				+ " a.AdslDnMaxRate=? and a.TL1ServerIP=? and a.AreaCode=?";
			if(wo.getUpOrDown()!=null && wo.getUpOrDown().compareTo("")>0){
				sql += " and UpOrDown='"+wo.getUpOrDown()+"'";
			}
		}

		String rs = "";
		try {
//			log.debug("getLineProfileName sql=" + sql);
			log.debug("getLineProfileName:rate=" + rate + ", serverIp=" + serverIp + ", Areacode="
					+ areacode+ ", LineType=" + wo.getLineType());
			rs = jdbcTemplate.queryForObject(sql, String.class,
					new Integer(rate), serverIp, areacode);
//			log.debug("getLineProfileName ProfileNamer=" + rs);
		} catch (EmptyResultDataAccessException e) {
			log.warn("没有找到模板");
			rs = "";
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("有多个模板");
			rs = "";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rs = "";
		}

		return rs;
	}
	/**
	 * 取速率模板
	 * @param rate
	 * @param serverIp
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getLineProfileNameForHuaWeiFTTH(WorkOrder wo,String upOrDown) {
		int rate = wo.getAtucRate();
		String serverIp = wo.getTl1ServerIp();
		String areacode = "0" + wo.getCityId();

		String sql = "";
 
		sql = "select a.ProfileName from AdslTL1ProfileInfo a where a.LineMode='ftth' and"
			+ " a.AdslDnMaxRate=? and a.TL1ServerIP=? and a.AreaCode=? and UpOrDown=?";
		String rs = "";
		try {
//			log.debug("getLineProfileName sql=" + sql);
			log.debug("getLineProfileName:rate=" + rate + ", serverIp=" + serverIp + ", Areacode="
					+ areacode+ ", LineType=" + wo.getLineType());
			rs = jdbcTemplate.queryForObject(sql, String.class,
					new Integer(rate), serverIp, areacode, upOrDown);
//			log.debug("getLineProfileName ProfileNamer=" + rs);
		} catch (EmptyResultDataAccessException e) {
			log.warn("没有找到模板");
			rs = "";
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("有多个模板");
			rs = "";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rs = "";
		}
		return rs;
	}
	
	/**
	 * @param rate
	 * @param serverIp
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getFtthProfileName(WorkOrder wo, int ProfileType) {
		int areaId = wo.getCityId();

		String sql = "";
		String rs = "";

		if (ProfileType == WorkOrder.LINETYPE_VAPROFILE) {
			sql = "select profileName from FtthProfile where "
					+ " type=? and areaId=? and ontLanNumber=? and ontPotsNumber=? and mgcIp=?";
			try {
				rs = jdbcTemplate.queryForObject(sql, String.class, new Integer(ProfileType),
						new Integer(areaId), new Integer(wo.getOntLanNumber()), new Integer(wo
								.getOntPotsNumber()), wo.getSbcIp()+"/"+ wo.getSbcIpReserve());
			} catch (EmptyResultDataAccessException e) {
				log.warn("Empty Result Data: ProfileType=" + ProfileType + " areaId=" + areaId
						+ " OntLanNumber=" + wo.getOntLanNumber() + " OntPotsNumber="
						+ wo.getOntPotsNumber() + " mgcIp=" + wo.getSbcIp()+"/"+ wo.getSbcIpReserve());
				rs = "";
			} catch (IncorrectResultSizeDataAccessException e) {
				log.error("Incorrect Result Size Data: ProfileType=" + ProfileType + " areaId="
						+ areaId + " OntLanNumber=" + wo.getOntLanNumber() + " OntPotsNumber="
						+ wo.getOntPotsNumber() + " mgcIp=" + wo.getSbcIp()+"/"+ wo.getSbcIpReserve());
				rs = "";
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				rs = "";
			}
		} else {
			sql = "select profileName from FtthProfile where "
					+ " type=? and areaId=? and ontLanNumber=? and ontPotsNumber=?";
			try {
				rs = jdbcTemplate.queryForObject(sql, String.class, new Integer(ProfileType),
						new Integer(areaId), new Integer(wo.getOntLanNumber()), new Integer(wo
								.getOntPotsNumber()));
			} catch (EmptyResultDataAccessException e) {
				log.warn("Empty Result Data: ProfileType=" + ProfileType + " areaId=" + areaId
						+ " OntLanNumber=" + wo.getOntLanNumber() + " OntPotsNumber="
						+ wo.getOntPotsNumber() );
				rs = "";
			} catch (IncorrectResultSizeDataAccessException e) {
				log.error("Incorrect Result Size Data: ProfileType=" + ProfileType + " areaId="
						+ areaId + " OntLanNumber=" + wo.getOntLanNumber() + " OntPotsNumber="
						+ wo.getOntPotsNumber() );
				rs = "";
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				rs = "";
			}
		}
		return rs;
	}
	
	/**
	 * 取UV
	 * @param rate
	 * @param serverIp
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getUserVlan(WorkOrder wo) {
		String areacode = "0" + wo.getCityId();
		int serviceCode = 1;

		String sql = "";

		if (wo.getWoType().shortValue() == WorkOrder.WOTYPE_ADD_VOIP) {
			serviceCode = 5;
		} else if (wo.getWoType().shortValue() == WorkOrder.WOTYPE_ADD_IPTV) {
			serviceCode = 4;
		} else if (wo.getWoType().shortValue() == WorkOrder.WOTYPE_ADD_VIDEO) {
			serviceCode = 3;
		} else {
			serviceCode = 1;
		}

		sql = "select vlan from ServiceVlan where type=1 and serviceCode=? and cityCode=?";

		String rs = "";
		try {
			log.debug("getServiceVlan: serviceCode=" + serviceCode + ", cityCode=" + areacode);
			rs = jdbcTemplate.queryForObject(sql, String.class, new Integer(serviceCode), areacode);
		} catch (EmptyResultDataAccessException e) {
			log.warn("Empty Result Data");
			rs = "";
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("Incorrect Result Size Data");
			rs = "";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			rs = "";
		}

		return rs;
	}
	

	/**
	 * 取DID
	 * @param serverIp
	 * @param deviceIp
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getDeviceDid(String serverIp,String deviceIp, String Areacode) {
		String sql = "select DeviceID from AdslTL1DevInfoMap where"
			+" TL1ServerIP = ? and DeviceIP = ? and AreaCode = ?";
		String rs="";
		try{
			rs = jdbcTemplate.queryForObject(sql, String.class,serverIp,deviceIp,Areacode);
			
			log.debug("getDeviceDid->serverIp="+serverIp+", deviceIp="+deviceIp+
					", Areacode="+ Areacode+", DID="+rs);
		}catch(EmptyResultDataAccessException e){
			//没有找到模板 
			log.error("没有找到DID, deviceIp="+deviceIp+", Areacode="+ Areacode);
		}catch(IncorrectResultSizeDataAccessException e){
			log.error("有多个DID, deviceIp="+deviceIp+", Areacode="+ Areacode);
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}

		return rs;
	}
	
	/**
	 * 取某tl1server上的全部DID
	 * @param serverIp
	 * @param areacode
	 * @return ip,did
	 */
	@Transactional(readOnly = true)
	public Map<String, String> getDeviceDidMap(String serverIp, String areacode) {
		String sql = "select DeviceIP,DeviceID from AdslTL1DevInfoMap where"
				+ " TL1ServerIP = ? and AreaCode = ?";
		try {
			List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,
					serverIp, areacode);
			
			Map<String, String> map = new Hashtable<String, String>();
			for (Iterator<Map<String, Object>> iter = list.iterator(); iter
					.hasNext();) {
				Map<String, Object> _rsmap = (Map<String, Object>) iter.next();
				map.put((String) _rsmap.get("DeviceIP"), (String) _rsmap
						.get("DeviceID"));
			}
			return map;
		} catch (EmptyResultDataAccessException e) {
			// 没有找到模板 
			log.error("没有DID, serverIp=" + serverIp+", Areacode=" + areacode);
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 取AdslConfigDevice表中 全部IP
	 * @param serverIp
	 * @param Areacode
	 * @return ip,did
	 */
	@Transactional(readOnly = true)
	public List<String> getDeviceIpList(String adminIp, String areacode) {
		String sql = "select DeviceIP from AdslConfigDevice where"
				+ " TL1Server = ? and AreaCode = ?";
		try {
			 ParameterizedRowMapper<String> mapper = new ParameterizedRowMapper<String>() {
				    
			        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			            return rs.getString("DeviceIP");
			        }
			    };
			    
			List<String> list = jdbcTemplate.query(sql,mapper,
					adminIp, areacode);
			
			return list;
		} catch (EmptyResultDataAccessException e) {
			// 没有找到模板，忽略。
			log.error("没有记录, serverIp=" + adminIp+", Areacode=" + areacode);
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 主控数据库中，修改DID
	 * @param tl1ServerIP
	 * @param areaCode
	 * @param deviceIP
	 * @param deviceID
	 * @param deviceName
	 * @return
	 */
	@Transactional(readOnly = false)
	public int updateDid(String tl1ServerIP, String areaCode, String deviceIP,
			String deviceID, String deviceName) {
		log.debug("修改DID:tl1ServerIP=" + tl1ServerIP + ", areaCode=" + areaCode
				+ ", deviceIP=" + deviceIP + ", deviceID=" + deviceID
				+ ", deviceName=" + deviceName);

		String sql = "update AdslTL1DevInfoMap set DeviceID=?,DeviceName=? "
				+ "where TL1ServerIP=? and AreaCode=? and DeviceIP=?";

		return jdbcTemplate.update(sql, deviceID, deviceName, tl1ServerIP,
				areaCode, deviceIP);
	}
	
	/**
	 * 主控数据库中，新增DID
	 * @param tl1ServerIP
	 * @param areaCode
	 * @param deviceIP
	 * @param deviceID
	 * @param deviceName
	 * @return
	 */
	@Transactional(readOnly = false)
	public int addDid(String tl1ServerIP, String areaCode, String deviceIP,
			String deviceID, String deviceName) {
		log.debug("新增DID:tl1ServerIP=" + tl1ServerIP + ", areaCode=" + areaCode
				+ ", deviceIP=" + deviceIP + ", deviceID=" + deviceID
				+ ", deviceName=" + deviceName);

		String sql = "insert into AdslTL1DevInfoMap (TL1ServerIP,AreaCode,DeviceIP,DeviceID,DeviceName) "
				+ "values (?,?,?,?,?)";

		return jdbcTemplate.update(sql, tl1ServerIP, areaCode, deviceIP,
				deviceID, deviceName);
	}
	
	/**
	 *  删除DID
	 * @param tl1ServerIP
	 * @param areaCode
	 * @param deviceIP
	 * @return
	 */
	@Transactional(readOnly = false)
	public int delDid(String tl1ServerIP, String areaCode, String deviceIP) {
		log.debug("删除DID:tl1ServerIP="+tl1ServerIP+", areaCode="+ areaCode+", deviceIP="+ deviceIP);
		
		String sql = "delete from AdslTL1DevInfoMap where TL1ServerIP=? and AreaCode=? and DeviceIP=?";

		return jdbcTemplate.update(sql, tl1ServerIP, areaCode, deviceIP);
	}
	
	/**
	 *  
	 * 
	 * @param codeName
	 * @param descr
	 * @return
	 */
	@Transactional(readOnly = true)
	public long getErrorIdForCodeName(WoResult woResult) {
		String sql = "select ErrCode from AdslRunErrDescr where CodeName=? and type=? ";
		try {
			return jdbcTemplate.queryForLong(sql, woResult.getCode(),
					woResult.getType());
		} catch (EmptyResultDataAccessException e) {
			System.out.println("\n query sql=select ErrCode from AdslRunErrDescr where CodeName="+woResult.getCode()+"=== and type="+woResult.getType()+"");
			log.error("错误在AdslRunErrDescr表里没定义;CodeName=" + woResult.getCode()
					+ ",descr=" + woResult.getDescr()+ ",type=" + woResult.getType());
			return ErrorConst.UnknowError;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ErrorConst.UnknowError;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getRedoExpression(long errCode) {
		String sql = "select redoExpression from AdslRunErrDescr where ErrCode = ?";
		try{
//			System.out.println("\n sql:"+sql.toString()+" errCode::"+errCode);
			return jdbcTemplate.queryForObject(sql, String.class, new Long(errCode));
		}catch(EmptyResultDataAccessException e){
			log.error("not found RedoExpression-"+errCode);
			return "";
		}catch(Exception e){
			log.error(e.getMessage(),e);
			return "";
		}
	}
	
	@Transactional(readOnly = true)
	public String getLocalState() {
		String sql = "select ParamValue from ParamTable where ParamTableName='PhoneInterzone' and ParamName='LocalState'";
		try{ 
			return jdbcTemplate.queryForObject(sql, String.class);
		}catch(EmptyResultDataAccessException e){
			return "";
		}catch(Exception e){
			log.error(e.getMessage(),e);
			return "";
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public String getServerIP(int serverId) {
		String sql = "select ServerIP from ServerInfo where ServerID = ?";

		return jdbcTemplate.queryForObject(sql, String.class, new Integer(serverId));
	}

	/**
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public int getServerPort(int serverId) {
		String sql = "select ServerPort from ServerInfo where ServerID = ?";

		return jdbcTemplate.queryForInt(sql, new Integer(serverId));
	}

	/**
	 * 工单执行结束
	 */
	@Transactional(readOnly = false)
	public boolean workOrderEnd(int configTicketID, long exeResult) {

		String q = "select TicketType from AdslConfigTicket where ConfigTicketID=?";
		Integer ticketType = 1;
		try {
			ticketType = jdbcTemplate.queryForObject(q, Integer.class, new Integer(
					configTicketID));
		} catch (EmptyResultDataAccessException e) {
			log.error("configTicketID=" + configTicketID + ",exits TicketControl Database record");
			return false;
		}

		Map<String, Object> namedParameters = new HashMap<String, Object>();
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
			namedParameters.put("TicketStatus", exeResult == 0 ? 2 : 3);
		} else {
			if (ticketType == 0) {
				namedParameters.put("TicketStatus", 6);
			} else {
				namedParameters.put("TicketStatus", exeResult == 0 ? 2 : 3);
			}
		}
		
		namedParameters.put("ExecTime", new Date());
		namedParameters.put("FailedCode", exeResult);
		namedParameters.put("ConfigTicketID", configTicketID);
		
		String sql = "update AdslConfigTicket "
				+ " set TicketStatus=:TicketStatus, ExecTime=:ExecTime, FailedCode=:FailedCode"
				+ " where ConfigTicketID=:ConfigTicketID";

		log.debug("set AdslConfigTicket-> "+namedParameters);
		try{
			jdbcTemplate.update(sql, namedParameters);
		}catch(Exception e){
			try{
				log.info("jdbcTemplate连接数据库失败,判定工单程序再执行一遍SQL语句");
				jdbcTemplate.update(sql, namedParameters);
			}catch(Exception ee){
				return false;
			}
		}
		return ticketType == 0 ? true : false;
	}

	/**
	 * @param configTicketID
	 * @param exeResult
	 * @return
	 */
	@Transactional(readOnly = false)
	public int setTicketFailed(int configTicketID, long exeResult) {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		// ExecTime=当前时间
		namedParameters.put("ExecTime", new Date());
		namedParameters.put("FailedCode", exeResult);
		namedParameters.put("ConfigTicketID", configTicketID);

		String sql = "update AdslConfigTicket "
				+ " set TicketStatus=3, ExecTime=:ExecTime, FailedCode=:FailedCode"
				+ " where ConfigTicketID=:ConfigTicketID";

		log.debug("set AdslConfigTicket-> "+namedParameters);
		return jdbcTemplate.update(sql, namedParameters);
	}
	
	/**
	 * 
	 * @param configTicketID
	 * @param state
	 * @return 更新笔数
	 */
	@Transactional(readOnly = false)
	public int setTicketStatus(int configTicketID, int state) {

		String sql = "update AdslConfigTicket " + " set TicketStatus=?" + " where ConfigTicketID=?";

		return jdbcTemplate.update(sql, new Integer(state), new Integer(configTicketID));
	}

	/**
	 * 
	 * @return 更新笔数
	 */
	@Transactional(readOnly = false)
	public int updateTicketStatusRunToWait(String areacode) {
		String sql = "update AdslConfigTicket " + " set TicketStatus=0"
				+ " where TicketStatus=1 and AreaCode=? ";
		return jdbcTemplate.update(sql, areacode);
	}

	class TicketParameterizedRowMapper implements ParameterizedRowMapper<WorkOrder>{
		private boolean isNew;
		public WorkOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
			WorkOrder wo = new WorkOrder();
			wo.setOriginWoId(rs.getString("OriginTicketID"));
			wo.setConfigWoId(rs.getInt("ConfigTicketID"));
			short _ServiceID = rs.getShort("ServiceID");
			if (_ServiceID == 6) {
				if(isNew){
					wo.setWoType(WorkOrder.WOTYPE_MOVE_OPEN);
				}else{
					wo.setWoType(WorkOrder.WOTYPE_MOVE_CLOSE);
				}
			}else if (_ServiceID == 26) {
				if(isNew){
					wo.setWoType(WorkOrder.WOTYPE_MOVE_ADD_VOIP);
				}else{
					wo.setWoType(WorkOrder.WOTYPE_MOVE_DEL_VOIP);
				}
				
			}
//			宽带和IPTV同时移机
			else if (_ServiceID == 36) {
				if(isNew){
					wo.setWoType(WorkOrder.WOTYPE_ADD_WBIPTV);
				}else{
					wo.setWoType(WorkOrder.WOTYPE_DEL_WBIPTV);
				}
			}
			else{
				//其他不变
				wo.setWoType(_ServiceID);
			}	
			
			wo.setAcceptTime(new Date());
			wo.setStartExeTime(new Date());
			wo.setEndExeTime(new Date());
			String _areaCode = rs.getString("AreaCode");
			wo.setCityId(Integer.parseInt(_areaCode));
//			
//			//在开IPTV时如果是dslam设备走的是单播,pon设备走的是组播,由于 dslam和pon+dsl在代码中没有区分,所以需要在些加上标记
//			//解析程序中 dslam是单独处理的,AccessFlag = 8888 标识为dslam(AD)设备工单,其它情况均不区分
//			String AccessFlag = rs.getString("AccessFlag");
//			if (AccessFlag != null && !AccessFlag.equals("null") && AccessFlag.compareTo("") > 0) {
//				wo.setAccessFlag(Integer.parseInt(AccessFlag));
//			} else {
//				wo.setAccessFlag(0);
//			}
//			
			String countyCode = rs.getString("CountyCode");
			wo.setCountyId(StringUtils.isNumeric(countyCode) ? Integer
							.parseInt(countyCode) : 0);
			wo.setPriority(rs.getShort("PriLevel"));
			wo.setDeviceType(rs.getString("DeviceModel"));
			if ("F820".equalsIgnoreCase(wo.getDeviceType())) {
				wo.setDeviceTypeId(WorkOrder.DEVICETYPE_ZTE_F820);
			} else {
				// 通用
				wo.setDeviceTypeId(0);
			}
			wo.setNeIp(rs.getString("DeviceIP"));
			//JS 机架可能为NA
			String shelfId = rs.getString("ShelfID");
			if (shelfId != null && shelfId.compareTo("") > 0) {
				if (shelfId.equals("NA") || shelfId.equals("N") || shelfId.equals("A")) {
					wo.setShelfId("NA");
				} else {
					try {
						int tmpShelfId = Integer.parseInt(shelfId);
						if (tmpShelfId < 32000) {
							wo.setShelfId(shelfId);
						} else {
							wo.setShelfId("-1");
						}
					} catch (Exception e) {
						wo.setShelfId("-1");
					}
				}
			}
			
			String frameId = rs.getString("FrameID");
			if (frameId != null && frameId.compareTo("") > 0) {
				if (frameId.equals("NA") || frameId.equals("N") || frameId.equals("A")) {
					wo.setFrameId("NA");
				} else {
					try {
						int tmpFrameId = Integer.parseInt(frameId);
						if (tmpFrameId < 32000) {
							wo.setFrameId(frameId);
						} else {
							wo.setFrameId("-1");
						}
					} catch (Exception e) {
						wo.setFrameId("-1");
					}
				}
			}
			
			//槽口防止出现错误的数据，int 变 short
//			int shelfId = rs.getInt("ShelfID") < 32000 ? rs.getInt("ShelfID") : -1;
//			int frameId = rs.getInt("FrameID") < 32000 ? rs.getInt("FrameID") : -1;
			int slotId = rs.getInt("SlotID") < 32000 ? rs.getInt("SlotID") : -1;
//			int portId = rs.getInt("PortID") < 32000 ? rs.getInt("PortID") : -1;
			Integer portId = rs.getInt("PortID") < 999999999 ? rs.getInt("PortID") : -1;
			
//			wo.setFrameId((short)frameId);
			wo.setSlotId((short)slotId);
//			wo.setPortId((short)portId);
			wo.setPortId(portId);
			
			wo.setOntId(rs.getString("ontId"));
			wo.setAtucRate(rs.getInt("Rate"));
			wo.setAturRate(NosEnv.default_AturRate);
			wo.setPasses((short) 1);
			//
			wo.setTl1ServerIp(rs.getString("TL1ServerIP"));
			
			wo.setVlan(rs.getInt("Vlan"));
			wo.setIptvvlan(rs.getInt("iptvvlan"));
			wo.setVoipvlan(rs.getInt("voipvlan"));
			wo.setVideovlan(rs.getInt("videovlan"));
			wo.setMacAddress(rs.getString("MacAddress"));
			wo.setLineType(rs.getShort("LineType"));
			wo.setPonLineType(rs.getShort("PonLineType"));
			wo.setOriginWoUuid(rs.getString("OriginWoUuid"));
			wo.setPvc(rs.getInt("Pvc"));
			wo.setVci(rs.getInt("Vci"));
			wo.setVpi(rs.getInt("Vpi"));
			
			
			wo.setOntKey(rs.getString("ontKey"));
			wo.setTid(rs.getString("tid"));
			wo.setOntLanNumber(rs.getInt("ontLanNumber"));
			wo.setOntPotsNumber(rs.getInt("ontPotsNumber"));
			wo.setUseStatus(rs.getInt("useStatus"));
			wo.setSbcIp(rs.getString("sbcIp"));
			wo.setIadip(rs.getString("iadip"));
			wo.setIadipMask(rs.getString("iadipMask"));
			wo.setIadipGateway(rs.getString("iadipGateway"));
			wo.setVlan(rs.getInt("vlan"));
			wo.setSvlan(rs.getInt("svlan"));
			wo.setCvlan(rs.getInt("cvlan"));
			wo.setVoiceVLAN(rs.getInt("voiceVLAN"));
			wo.setSbcIpReserve(rs.getString("sbcIpReserve"));
			wo.setResourceCode(rs.getString("resourceCode"));
			wo.setSpecLineNum(rs.getString("specLineNum"));
			//JS联通添加厂商和OLT信息
//			wo.setVendor(rs.getString("Vendor"));
			wo.setOltPonID(rs.getString("OltPonID"));
			
			// RMS
			String rmsFlagString = rs.getString("RMSFlag");
			int rmsFlag = 0;
			if (StringUtils.isEmpty(rmsFlagString) || "null".equalsIgnoreCase(rmsFlagString)){
				rmsFlag = 0;
			} else {
				try {
					rmsFlag = Integer.parseInt(rmsFlagString);
				} catch (NumberFormatException e) {
					rmsFlag = 0;
				}
			}
			wo.setRmsFlag(rmsFlag);
			wo.setBuzType(rs.getString("BuzType"));
			return wo;
		}
		public boolean isNew() {
			return isNew;
		}
		public void setNew(boolean isNew) {
			this.isNew = isNew;
		}
	}
	
	/**
	 * 组sql
	 * @param isNew
	 * @return
	 */
	private String getTicketQuerySql(boolean isNew){
		StringBuffer sql = new StringBuffer();

		sql.append(" select ");
		sql.append(" a.OriginTicketID as OriginTicketID "); //
		sql.append(" ,a.ConfigTicketID as ConfigTicketID "); //
		sql.append(" ,a.ServiceID as ServiceID "); // --
		sql.append(" ,b.DeviceIP as DeviceIP "); //
		if(isNew){
			sql.append(" ,a.NewRate as Rate "); //
		}else{
			sql.append(" ,a.OldRate as Rate "); // 
		}		
		sql.append(" ,b.ShelfID as ShelfID "); // 
		sql.append(" ,b.FrameID as FrameID "); // 
		sql.append(" ,b.SlotID as SlotID "); // 
		sql.append(" ,b.PortID as PortID "); // 
		sql.append(" ,b.ontId as ontId "); // 
		sql.append(" ,a.PriLevel as PriLevel "); //
		sql.append(" ,a.AcceptTime as AcceptTime "); // 
		sql.append(" ,a.ExecTime as ExecTime "); // 
		sql.append(" ,a.TicketType as TicketType "); // 
		sql.append(" ,a.AreaCode as AreaCode "); // 
//		sql.append(" ,a.AccessFlag as AccessFlag ");
		sql.append(" ,a.CountyCode as CountyCode "); // 
//		rms
		sql.append(" ,a.BuzType as BuzType");
		sql.append(" ,a.RMSFlag as RMSFlag");
		sql.append(" ,b.deviceType as DeviceModel "); // 
		sql.append(" ,b.tl1ServerIP as TL1ServerIP ");
		sql.append(" ,b.vlan as Vlan "); // 
		sql.append(" ,b.IPTVVLAN as iptvvlan "); // 
		sql.append(" ,b.VOIPVLAN as voipvlan "); // 
		sql.append(" ,b.VideoVLAN as videovlan "); // 
		sql.append(" ,b.MacAddress as MacAddress "); //
		sql.append(" ,b.LineType as LineType "); // 
		sql.append(" ,b.PonLineType as PonLineType "); // 
		sql.append(" ,a.TicketUID as OriginWoUuid "); // 
		sql.append(" ,b.pvc as Pvc "); //
		sql.append(" ,b.vci as Vci "); //
		sql.append(" ,b.vpi as Vpi "); //
		sql.append(" ,b.ontKey,b.tid,b.ontLanNumber,b.ontPotsNumber,b.useStatus");
		sql.append(" ,b.sbcIp,b.iadip,b.iadipMask,b.iadipGateway,b.vlan,b.svlan,b.cvlan");
		sql.append(" ,b.voiceVLAN,b.sbcIpReserve,b.resourceCode,b.specLineNum,b.Vendor,b.OltPonID ");
		sql.append(" from ");
		sql.append(" AdslConfigTicket a, ");
		sql.append(" DslamResourceInfo b ");
		sql.append(" where ");
		if(isNew){
			sql.append(" a.NewDslamID = b.DslamResourceID "); // 
		}else{
			sql.append(" a.OldDslamID = b.DslamResourceID "); // 
		}
		sql.append(" and a.AbnormalCode = NULL "); //
		sql.append(" and a.TicketStatus = 0 "); // 
		sql.append(" and a.LineType is not null "); //
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			
		} else {
			sql.append(" and a.AreaCode=? ");
		}
		
		return sql.toString();
	}
	
	/**
	 * 获取需要处理的工单
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<WorkOrder> getProcessTicket(String areacode) {
		
		// 取new
		TicketParameterizedRowMapper newMapper = new TicketParameterizedRowMapper();
		newMapper.setNew(true);
		String newsql=getTicketQuerySql(true);
//		 log.debug("z get new ProcessTicket areacode:" + areacode + " SQL:\n"
//		 + newsql.toString());
		 List<WorkOrder> processWoList = null;
		 if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			  processWoList = jdbcTemplate.query(newsql, newMapper);
		 }else{
			  processWoList = jdbcTemplate.query(newsql, newMapper, areacode);
		 }
		 
 
		//取old
		TicketParameterizedRowMapper oldMapper = new TicketParameterizedRowMapper();
		oldMapper.setNew(false);
		String oldsql=getTicketQuerySql(false);
//		 log.debug("2 get old ProcessTicket areacode:" + areacode + " SQL:"
//		 + oldsql.toString());
		List<WorkOrder> processWoList_old = null; 
		if (SystemFlag.getSystemFlag() != null && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
			processWoList_old = jdbcTemplate.query(oldsql, oldMapper);
		} else {
			processWoList_old = jdbcTemplate.query(oldsql, oldMapper, areacode);
		}
//		List<WorkOrder> processWoList_old = jdbcTemplate.query(oldsql,
//				oldMapper, areacode);
		for (WorkOrder orderOld : processWoList_old) {
			processWoList.add(orderOld);
		}

		return processWoList;
	}
	
	/**
	 * 将某种状态的工单，更改为待处理工单
	 * 
	 * @param status
	 * @throws Exception
	 */ 
	@Transactional(readOnly = true)
	public void modifyTicketStatusToWait(int status,String areacode)
			throws Exception {
		log.info("JS pon工单失败定时重做,need update TicketStatusToWait:" + status);
		StringBuffer sql = new StringBuffer(); 
		sql.append(" select ConfigTicketID from AdslConfigTicket ");
		sql.append(" where TicketStatus =");
		sql.append(status);
		sql.append(" and AreaCode='");
		sql.append(areacode);
		sql.append("' ");  
		try {
			StringBuffer ctids = new StringBuffer();
			int _rscount = 0;
			List<?> result = jdbcTemplate.queryForList(sql.toString());
			for(Object ConfigTicketID : result){
				ctids.append(ConfigTicketID.toString()).append(",");
				_rscount++;
			}
			log.info("need update ConfigTicketIDs:" + ctids);
			log.info("need update ConfigTicketID count:" + _rscount); 
			sql.setLength(0);
			sql.append(" update AdslConfigTicket set TicketStatus = 0 ");
			sql.append(" where TicketStatus =");
			sql.append(status);
			sql.append(" and AreaCode='");
			sql.append(areacode);
			sql.append("' ");

			log.debug("update:" + sql.toString());
			int _updateCount = jdbcTemplate.update(sql.toString());
			log.info("update to wait status count:" + _updateCount);

		} catch (Exception e) {
			log.error("JS 定时重做PON类型失败工单---失败", e);
			throw new Exception("更改正在处理工单为待处理工单失败!");
		} 
	}
	
	@Transactional(readOnly = true)
	public String getContiunTime(String ParamID, String AreaCode) {
		String str = "10000";
		StringBuffer sql = new StringBuffer();
		sql.append("select ParamValue from AdslParamDef where AreaCode='"
				+ AreaCode + "' and ParamID =" + ParamID);
		try {
			str = jdbcTemplate.queryForObject(sql.toString(), String.class).toString();
		} catch (EmptyResultDataAccessException e) {
			try {
				sql = new StringBuffer();
				sql.append("select ParamValue from AdslParamDef where AreaCode='default' and ParamID ="+ ParamID);
				str = jdbcTemplate.queryForObject(sql.toString(), String.class).toString();
			} catch (EmptyResultDataAccessException ee) {
				log.error("查找配置信息错误！");
				log.debug("sql:"+sql.toString());
			}catch (Exception ee) {
				log.error("查找配置信息错误！");
				ee.printStackTrace();
			}
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("查找配置信息错误,出现多条!");
		} catch (Exception e) {
			log.error("查找配置信息错误！");
		}
		return str;
	}

	
	/**
	 * @param serverIp
	 * @param areacode
	 * @return
	 */
	public int getThreadPoolMaxSize(String serverIp, String areacode) {
		if(StringUtils.isBlank(serverIp)){
			return NosEnv.snmp_max_connection;
		}
		
		String sql = "select PoolMaxSize from TL1ServerInfo where"
				+ " TL1ServerIP = ? and AreaCode = ?";
		int rs = 10;
		try {
			Integer i = jdbcTemplate.queryForObject(sql, Integer.class,
					serverIp, areacode);
			if (i != null) {
				rs = i.intValue();
			} else {
				log.warn("PoolMaxSize 为空");
			}
		} catch (EmptyResultDataAccessException e) {
			log.error("没找到, serverIp=" + serverIp + ", Areacode=" + areacode);
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("有多相同的, serverIp=" + serverIp + ", Areacode=" + areacode);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		log.debug("getThreadPoolMaxSize->ServerIp=" + serverIp + ", Areacode="
				+ areacode + ", PoolMaxSize=" + rs);
		return rs;
	}
	
	/**
	 * @param wo
	 * @param areacode
	 * @throws ZtlException
	 */
	public void getDeviceLoginInfo(WorkOrder wo, String areacode)
			throws ZtlException {
		try {
			if (StringUtils.isBlank(wo.getTl1ServerIp())) {
				wo.setTl1ServerIp("");
				String sql = "select ReadCommunity,WriteCommunity,DeviceModel,LineType"
						+ " from AdslConfigDevice where DeviceIP = ? and AreaCode = ?";
				try{
					Map<String, Object> rsmap = jdbcTemplate.queryForMap(sql,wo.getNeIp(), areacode);
					wo.setSnmpReadCommunity((String) rsmap.get("ReadCommunity"));
					wo.setSnmpWriteCommunity((String) rsmap.get("WriteCommunity"));
					String model = (String) rsmap.get("DeviceModel");
					String deviceMaker = "";
					if (model != null && model.startsWith("switch")) {
						deviceMaker = "switch";
					} else if (model != null && model.startsWith("HUANYU")) {
						deviceMaker = "HUANYU";
						wo.setDeviceType(model);
					} else {
						deviceMaker = "";
					}
					wo.setDeviceMaker(deviceMaker);
					Integer adslLineType = (Integer) rsmap.get("LineType");
					if (adslLineType != null) {
						wo.setAdslLineType(adslLineType.shortValue());
					}
				} catch (IncorrectResultSizeDataAccessException e) {
					log.error("snmp device info not found!");
					wo.setSnmpReadCommunity("");
					wo.setSnmpWriteCommunity("");
					wo.setDeviceMaker("");
					short adslLineType = 1;
					wo.setAdslLineType(adslLineType);
				}
			} else {
				// TL1
				String sql = "select TL1ServerPort,UserName,Password,Manufacturer"
						+ " from TL1ServerInfo where TL1ServerIP = ? and AreaCode = ?";
				Map<String, Object> rsmap = jdbcTemplate.queryForMap(sql,
						wo.getTl1ServerIp(), areacode);
				wo.setTl1ServerPort(((Integer) rsmap.get("TL1ServerPort"))
						.intValue());
				wo.setTl1User((String) rsmap.get("UserName"));
				String dpwd = (String) rsmap.get("Password");
				EncryptData encryptData = new EncryptData();
				wo.setTl1Password(encryptData.decrypt(dpwd));
//				System.out.println("\n (String) rsmap.get(Manufacturer)==="+(String) rsmap.get("Manufacturer"));
				//
				wo.setDeviceMaker((String) rsmap.get("Manufacturer"));
			}
		} catch (EmptyResultDataAccessException e) {
			log.error("没找到"+wo.getTl1ServerIp()+" "+areacode);
			throw new ZtlException(ErrorConst.incompleteInfo);
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("有多相同的"+wo.getTl1ServerIp()+" "+areacode);
			throw new ZtlException(ErrorConst.incompleteInfo);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.incompleteInfo);
		}
	}
	
	public ServiceVlan getServiceVlan(short woType, String areacode) {
		Short serviceCode = -1;

		if (woType == WorkOrder.WOTYPE_ADD_IPTV || woType == WorkOrder.WOTYPE_DEL_IPTV) {
			serviceCode = 4;
		} else if (woType == WorkOrder.WOTYPE_ADD_VOIP || woType == WorkOrder.WOTYPE_DEL_VOIP) {
			serviceCode = 5;
		} else if (woType == WorkOrder.WOTYPE_ADD_VIDEO || woType == WorkOrder.WOTYPE_DEL_VIDEO) {
			serviceCode = 3;
		}		
		StringBuffer sql = new StringBuffer();
		sql.append("select UUID,ServiceCode,ServiceName,Vlan,PVC,VCI,VPI,CityCode ");
		sql.append(" from ServiceVlan where ServiceCode=? and CityCode=? ");

		try {
			return jdbcTemplate.queryForObject(sql.toString(),
					resultBeanMapper(ServiceVlan.class), serviceCode, areacode);
		} catch (EmptyResultDataAccessException e) {
			log.error("没找到");
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			log.error("有多相同的");
			return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 取ServiceVlan
	 * @param type 1单播(dslam) 2组播(pon+dsl)
	 * @param woType
	 * @param Areacode
	 * @return
	 */
	@Transactional(readOnly = true)
	public ServiceVlan getServiceVlan(int type,Short woType,String areacode) {
		Short serviceCode = -1; 
		if (woType == WorkOrder.WOTYPE_ADD_IPTV || woType == WorkOrder.WOTYPE_ADD_WBIPTV 
				|| woType == WorkOrder.WOTYPE_DEL_IPTV || woType == WorkOrder.WOTYPE_DEL_WBIPTV) {
			serviceCode = 4;
		} else if (woType == WorkOrder.WOTYPE_ADD_VOIP || woType == WorkOrder.WOTYPE_DEL_VOIP) {
			serviceCode = 5;
		} else if (woType == WorkOrder.WOTYPE_ADD_VIDEO || woType == WorkOrder.WOTYPE_DEL_VIDEO) {
			serviceCode = 3;
		}
		
		StringBuffer sql = new StringBuffer();
//		//如果PVC值为一套,只认为是普通开宽带
//		sql.append("select count(*) as num from ServiceVlan where ServiceCode=? and CityCode=? ");
//		try {
//			log.debug("getServiceVlan: serviceCode=" + serviceCode + ", cityCode=" + areacode);
//			int pvcCount = jdbcTemplate.queryForObject(sql.toString(), int.class, new Integer(serviceCode), areacode);
//			if(pvcCount < 2){
//				return null;
//			}
//		} catch (Exception e) {
//			log.error(e.getMessage(), e); 
//		}
//		
//		//type>0代表dslam设备,走单播,等于0时Pon走组播
//		if (type > 0) {
//			type = 1;
//		} else {
//			type = 2;
//		}
		sql = new StringBuffer();
		sql.append("select UUID,ServiceCode,ServiceName,Vlan,IGMPVLAN,PVCNO,PVC,VCI,VPI,CityCode ");
		sql.append(" from ServiceVlan where Type=? and ServiceCode=? and CityCode=? ");
		ServiceVlan rs = null;
		try {
			log.debug("getServiceVlan: serviceCode=" + serviceCode + ", cityCode=" + areacode);
			rs = jdbcTemplate.queryForObject(sql.toString(), resultBeanMapper(ServiceVlan.class), 
					new Integer(type), new Integer(serviceCode), areacode);
			log.info("rs.getVlan()="+rs.getVlan());
			log.info("rs.getIgmpVlan()="+rs.getIgmpVlan());
		} catch (Exception e) {
			log.error(e.getMessage(), e); 
		}
		return rs;
	}
	
//	public ServiceVlan getIptvParam(String cityID,String manufacturer) {
//		StringBuffer sql = new StringBuffer();
//		ServiceVlanRowMapper serviceVlan = new ServiceVlanRowMapper();
//		List<ServiceVlan> serviceVlanList = null;
//		sql.append(" select count(*) from ServiceVlan where ServiceName='IPTV-dslam' and PVC=2 and CityCode='"+cityID +"' Manufacturer='"+manufacturer+"'");
//		try {
//			serviceVlanList = jdbcTemplate.query(sql.toString(),serviceVlan);
//			if(serviceVlanList.size()==0){
//				//多条PVC时=2
//				sql.append(" select count(*) from ServiceVlan where ServiceName='IPTV-dslam' and CityCode="+cityID +" and  PVC=2");
//				serviceVlanList = jdbcTemplate.query(sql.toString(),serviceVlan);
//				if (serviceVlanList.size() == 1) {
//					return serviceVlanList.get(0);
//				} else {
//					return null;
////					log.error("IPTV PVC parameter configure error!");
//				}
//			}
//		} catch (EmptyResultDataAccessException e) {
//			 
//		} catch (IncorrectResultSizeDataAccessException e) {
//			 
//		} catch (Exception e) {
//			log.error(e,e);
//		} 
//		return serviceVlanList.get(0);
//	}
	
	/**
	 * 插入命令执行日志
	 * 
	 * @return 更新笔数
	 */
	@Transactional(readOnly=false,noRollbackFor=RuntimeException.class)
	public int insertCmdlog(String cmd, WorkOrder wo, TL1ResponseMessage msg) {
		try {
			Long workOrderId = null;
			String originWoId = "";
			if (wo != null) {
				workOrderId = wo.getUuid();
				originWoId = wo.getOriginWoId();
			}
			Integer woResultId;
			String codeName = "";
			String descr = "";
			if (msg.isSuccess()) {
				woResultId = 0;
			} else {
				woResultId = 1;
				codeName = msg.getEn();
				descr = msg.getEnDesc();
			}
			String areacode = "0" + wo.getCityId();
			
//			if (cmd.length()>190) {
//				cmd=cmd.substring(0, 190)+"[略]";
//			}
			log.debug("createCmdLog:" + originWoId + " [" + cmd + "] " + " [" + codeName
					+ "] " + descr);

			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO CmdLog(uuid, workOrderId, originWoId, cmd, woResultId,");
			sql.append(" codeName, descr, exeTime, areaCode)");
			sql.append("VALUES(0, ?, ?, ?, ?, ?, ?, GETDATE(), ?)");
			return jdbcTemplate.update(sql.toString(), workOrderId, originWoId, cmd, woResultId,
					codeName, descr, areacode);
		} catch (Exception e) {
			log.error("insert cmdlogerror:" + e.getMessage(), e);
			return 0;
		}
	}
	 
	/**
	 * 贝尔命令中，语音和宽带业务ONU PORT 要单独取出来 
	 */
	@Transactional(readOnly = true)
	public String getONUPort(String TicketUID,boolean isdetele){
		String onuPort = "";
		String sql = "";
		try{
			if(isdetele){
				 sql = "set ROWCOUNT 1 select OnuPort from AdslSingleOriginTicket adsl," +
							"PonResourceOriginInfo pon where adsl.OldPonID = pon.UUID and OriginTicketID='"+TicketUID+"'  set ROWCOUNT 0";
			} else {
				 sql = "set ROWCOUNT 1 select OnuPort from AdslSingleOriginTicket adsl," +
							"PonResourceOriginInfo pon where adsl.NewPonID = pon.UUID and OriginTicketID='"+TicketUID+"' set ROWCOUNT 0"; 
			}
			 onuPort = jdbcTemplate.queryForObject(sql, String.class); 
		}catch(Exception e){
//			try{
//				
//				 onuPort = jdbcTemplate.queryForObject(sql, String.class); 
//			}catch(Exception e1){
//				//如果查不到值默认为1
				return "1";
//			}
		}
		if (onuPort != null && onuPort.compareTo("") > 0) {
			onuPort = onuPort.substring(onuPort.length() - 1, onuPort.length());
		} else {
			//如果查不到值默认为1
			return "1";
		}
		return onuPort;
	}
	
	@Transactional(readOnly = true)
	public List<WorkOrder> getAlcatelShouZhouTest(){ 
		String sql = "";
		List<WorkOrder> testWorkOrder = null;
		try{
			sql = "select OLTIP,Slot,Port,OntId,ONUName,MACADDR,SERNUM,SLID,CVLANID,SVLANID from AlcateShuZhouTest ";  
			TestListParameterizedRowMapper testMapper = new TestListParameterizedRowMapper();
			testWorkOrder = jdbcTemplate.query(sql, testMapper); 
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("\n 取数据出错");
		}
		return testWorkOrder;
	}
	
	class TestListParameterizedRowMapper implements ParameterizedRowMapper<WorkOrder>{ 
		public WorkOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
			WorkOrder wo = new WorkOrder(); 
			wo.setNeIp(rs.getString("OLTIP"));
			wo.setSlotId(rs.getShort("Slot"));
			wo.setPortId(rs.getInt("Port"));
			wo.setOntId(rs.getString("OntId"));
			wo.setResourceCode(rs.getString("ONUName"));
			wo.setIadipGateway(rs.getString("MACADDR"));
			wo.setIadipMask(rs.getString("SERNUM"));
			wo.setOriginWoUuid(rs.getString("SLID"));
			wo.setCvlan(rs.getInt("CVLANID"));
			wo.setSvlan(rs.getInt("SVLANID"));
			return wo;
		} 
	}
//	@Transactional(readOnly = true)
//	public String getServerIP(int serverId) {
//		String sql = "select ServerIP from ServerInfo where ServerID = ?";
//
//		return jdbcTemplate.queryForObject(sql, String.class, new Integer(serverId));
//	}

}
