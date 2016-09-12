package com.zoom.nos.provision.entity;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class WorkOrder {
	/**
	 * 1.��ͨ
	 */
	public final static short WOTYPE_OPEN = 1;
	/**
	 * 2.�ر�
	 */
	public final static short WOTYPE_CLOSE = 2;
	/**
	 * 3.��ͣ
	 */
	public final static short WOTYPE_PAUSE = 3;
	/**
	 * 4.�޸�����
	 */
	public final static short WOTYPE_ALTER_RATE = 4;
	/**
	 * 5.��ͨ
	 */
	public final static short WOTYPE_RESUME = 5;
	

	/**
	 * 11.����� 
	 */
	public final static short WOTYPE_ADD_LAN = 11;

	/**
	 * 12.�����
	 */
	public final static short WOTYPE_DEL_LAN = 12;
	
	/**
	 * 19.ע��ONU
	 */
	public final static short WOTYPE_REGISTER_ONU = 19;
	
	/**
	 * 21.������
	 */
	public final static short WOTYPE_ADD_VOIP = 21;

	/**
	 * 22.������
	 */
	public final static short WOTYPE_DEL_VOIP = 22;
	/**
	 * 27.�����ƻ��Ŀ�
	 */
	public final static short WOTYPE_MOVE_ADD_VOIP = 27;
	/**
	 * 28.�����ƻ��Ĺ�
	 */
	public final static short WOTYPE_MOVE_DEL_VOIP = 28;

	/**
	 * �ƻ���
	 * @param wotype
	 * @return
	 */
	public static boolean isMoveClose(short wotype){
		if(wotype == WorkOrder.WOTYPE_MOVE_CLOSE ||wotype == WorkOrder.WOTYPE_MOVE_DEL_VOIP ){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 31.��IPTV
	 */
	public final static short WOTYPE_ADD_IPTV = 31;
	
	/**
	 * 1131.�������IPTV
	 */
	public final static short WOTYPE_ADD_WBIPTV = 1131;
	/**
	 * 1232.�ؿ����IPTV
	 */
	public final static short WOTYPE_DEL_WBIPTV = 1232;
	/**
	 * 32.��IPTV
	 */
	public final static short WOTYPE_DEL_IPTV = 32;
	/**
	 * 41.��VIDEO
	 */
	public final static short WOTYPE_ADD_VIDEO = 41;
	
	/**
	 * 42.��VIDEO
	 */
	public final static short WOTYPE_DEL_VIDEO = 42;
	
	/**
	 * 61.�ƻ��Ŀ�
	 */
	public final static short WOTYPE_MOVE_OPEN = 61;
	
	/**
	 * 62.�ƻ��Ĺ�
	 */
	public final static short WOTYPE_MOVE_CLOSE = 62;

	/**
	 * 99.������Ԫ��ȡSysName(TEST_GETSYSNAME)
	 */
	public final static short WOTYPE_SIMPLETEST = 99;

	/**
	 * adsl��·���� 2.������(fastOnly)
	 */
	public final static short ADSLLINETYPE_FASTONLY = 2;

	/**
	 * adsl��·���� 3.����֯(interleavedOnly)
	 */
	public final static short ADSLLINETYPE_INTERLEAVEDONLY = 3;
	
	//---------------ģ����𣬷��ⲻ̫��-----------------
	//1.LINEPROF 2.SRVPROF 3.VAPROFILE
	public final static int LINETYPE_LINEPROF = 1;
	public final static int LINETYPE_SRVPROF = 2;
	public final static int LINETYPE_VAPROFILE = 3;

	// ------------------------------------------------------
	// ������Ԫ�ͺ�Id (1,dslam/switch,0,�����ͺ�0=ͨ�õ�,�汾)
	// :TODO �Ժ�����Ĳ�Ҫ���ŵ�DB
//	/**
//	 * ������Ԫ�ͺ�Id ����BA1000
//	 */
//	public final static int DEVICETYPE_HUANYUBA1000 = 10050010;


	/**
	 * ��Ԫ�ͺ�Id ZTE F820
	 */
	public final static int DEVICETYPE_ZTE_F820 = 21020110;
	
	/**
	 * ��Ԫ�ͺ�Id ZTE F822
	 */
	public final static int DEVICETYPE_ZTE_F822 = 21020100;
	
	// 0.��pon  1.epon 2.gpon
	public final static int PONLINETYPE_NOT=0;
	public final static int PONLINETYPE_EPON=1;
	public final static int PONLINETYPE_GPON=2;
	
	

	
	/*
	 * ----------------------------------- �־û�����
	 */

	// UUID
	private Long uuid;

	// ԭʼ�������
	private String originWoId;

	// ���ù������
	private Integer configWoId;

	// ������������
	private Short woType;

	// ����״̬
	private Short woState;

	// ����ʱ��
	private Date acceptTime;

	// ��ʼ����ʱ��
	private Date startExeTime;

	// �������ʱ��
	private Date endExeTime;

	// ����id
	private Integer cityId;

	// ����id
	private Integer countyId;

	// ��id
	private Integer bureauId;

	// ִ�����ȼ�
	private Short priority;

	// �豸�ͺ�Id
	private Integer deviceTypeId;

	// ��ԪIP
	private String neIp;

	// ���ܺ�
	private String shelfId;

	// �����
	private String frameId;

	// ���ۺ�
	private Short slotId;

	// �˿ں�
	private Integer portId;

	// adsl��·����
	private Short adslLineType;

	// �����������
	private Integer atucRate;

	// �����������
	private Integer aturRate;

	// snmp��������
	private String snmpReadCommunity;

	// snmpд������
	private String snmpWriteCommunity;

	// tl1����IP
	private String tl1ServerIp;
	
    // tl1����Port
    private Integer tl1ServerPort;

	// tl1�û���
	private String tl1User;

	// tl1����
	private String tl1Password;

	// ִ�н��
	private Long exeResult;

	// ִ�з�����Ϣ
	private String exeReturnMsg;

	// �ڼ�������
	private Short passes;

	////////////////////////////////
    // Ĭ��vlan
    private Integer vlan;

    // iptvҵ��vlan
    private Integer iptvvlan;
    
    // voipҵ��vlan
    private Integer voipvlan;
    
    // videoҵ��vlan
    private Integer videovlan;
    
    // MAC address
    private String macAddress;
        
    // PON��·����
    // (0=��pon,1=epon,2=gpon )
    private Short ponLineType;
    
    // ������ʽ
    // (1=DSL, 2=LAN, 3=FTTH)
    private Short lineType;
    
    // �豸����
    // (HUAWEI,ZTE,ALCATEL,FENGHUO)
    private String deviceMaker;
    
    // ԭʼ����Ψһ�����б��
    private String originWoUuid;    

    // �������·
    private Integer pvc;
    
    // ��ͨ����ʶֵ
    private Integer vci;
    
    // ��·����ʶֵ
    private Integer vpi;
    
    // �¹�������UUID������ԭʼ����Ψһ���б�ţ���֪�м��ʹ�������
    private Long nextWorkOrderUuid;

    // XXX FTTH
    // ONT ID
    private String ontId;

    // �豸�ͺ�
	private String deviceType;
	
	private Integer ontLanNumber;
	private Integer ontPotsNumber;
	private String mgcIp;
	private String sbcIp;
	private String iadip;
	private String iadipMask;
	private String iadipGateway;
    private Integer svlan;
    private Integer cvlan;
    private String ontKey;
    private String tid;
    private Integer useStatus;
	
    private Integer voiceVLAN;
    
    private String sbcIpReserve;
    
    private String resourceCode;
    
    private String specLineNum;
    
    private String vendor;
    
    private String oltPonID;
    
    private String upOrDown;
    
    //RMS��ͥ����
    private Integer rmsFlag;
    public Integer getRmsFlag() {
		return rmsFlag;
	}
    
	public void setRmsFlag(Integer rmsFlag) {
		this.rmsFlag = rmsFlag;
	}
	
	private String buzType;
	
    public String getBuzType() {
		return buzType;
	}
    
	public void setBuzType(String buzType) {
		this.buzType = buzType;
	}
	
	private String gdid;
	
	public String getGdid() {
		return gdid;
	}
	
	public void setGdid(String gdid) {
		this.gdid = gdid;
	}
	
    
//    private Integer accessFlag;
    
//    /**
//     * �Ƿ���Ҫע��onu
//     * @return
//     */
//	public boolean needRegisterOnu() {
//		if (this.getUseStatus().intValue() == 1 || this.getUseStatus().intValue() == 3) {
//			return true;
//		} else {
//			return false;
//		}
//	}
	
//    public Integer getAccessFlag() {
//		return accessFlag;
//	}
//
//
//	public void setAccessFlag(Integer accessFlag) {
//		this.accessFlag = accessFlag;
//	}


	public String getUpOrDown() {
		return upOrDown;
	}


	public void setUpOrDown(String upOrDown) {
		this.upOrDown = upOrDown;
	}


	/**
     * �Ƿ���Ҫע��onu
     * @return
     */
	public boolean needDelOnu() {
		if (this.getUseStatus().intValue() == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	
	////////////////////////////////////////////////


//	public String getVendor() {
//		return vendor;
//	}
//
//
//	public void setVendor(String vendor) {
//		this.vendor = vendor;
//	}


	public String getOltPonID() {
		return oltPonID;
	}


	public void setOltPonID(String oltPonID) {
		this.oltPonID = oltPonID;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WorkOrder [acceptTime=" + acceptTime + ", adslLineType=" + adslLineType
				+ ", atucRate=" + atucRate + ", aturRate=" + aturRate + ", bureauId=" + bureauId
				+ ", cityId=" + cityId + ", configWoId=" + configWoId + ", countyId=" + countyId
				+ ", cvlan=" + cvlan + ", deviceMaker=" + deviceMaker + ", deviceType="
				+ deviceType + ", deviceTypeId=" + deviceTypeId + ", endExeTime=" + endExeTime
				+ ", exeResult=" + exeResult + ", exeReturnMsg=" + exeReturnMsg + ", frameId="
				+ frameId + ", iadip=" + iadip + ", iadipGateway=" + iadipGateway + ", iadipMask="
				+ iadipMask + ", iptvvlan=" + iptvvlan + ", lineType=" + lineType + ", macAddress="
				+ macAddress + ", mgcIp=" + mgcIp + ", neIp=" + neIp + ", nextWorkOrderUuid="
				+ nextWorkOrderUuid + ", ontId=" + ontId + ", ontKey=" + ontKey + ", ontLanNumber="
				+ ontLanNumber + ", ontPotsNumber=" + ontPotsNumber + ", originWoId=" + originWoId
				+ ", originWoUuid=" + originWoUuid + ", passes=" + passes + ", ponLineType="
				+ ponLineType + ", portId=" + portId + ", priority=" + priority + ", pvc=" + pvc
				+ ", resourceCode=" + resourceCode + ", sbcIp=" + sbcIp + ", sbcIpReserve="
				+ sbcIpReserve + ", shelfId=" + shelfId + ", slotId=" + slotId
				+ ", snmpReadCommunity=" + snmpReadCommunity + ", snmpWriteCommunity="
				+ snmpWriteCommunity + ", specLineNum=" + specLineNum + ", startExeTime="
				+ startExeTime + ", svlan=" + svlan + ", tid=" + tid + ", tl1Password="
				+ tl1Password + ", tl1ServerIp=" + tl1ServerIp + ", tl1ServerPort=" + tl1ServerPort
				+ ", tl1User=" + tl1User + ", useStatus=" + useStatus + ", uuid=" + uuid + ", vci="
				+ vci + ", videovlan=" + videovlan + ", vlan=" + vlan + ", voiceVLAN=" + voiceVLAN
				+ ", voipvlan=" + voipvlan + ", vpi=" + vpi + ", woState=" + woState + ", woType="
				+ woType + "]";
	}

    /**
     * get ID
     */    
    public Long getUuid() {
        return uuid;
    }

    /**
     * set ID
     */  
    public void setUuid(Long uuid) {
        this.uuid=uuid;
    }
    /**
     * get originWoId
     */    
    public String getOriginWoId() {
        return originWoId;
    }

    /**
     * set originWoId
     */  
    public void setOriginWoId(String originWoId) {
        this.originWoId=originWoId;
    }
    /**
     * get configWoId
     */    
    public Integer getConfigWoId() {
        return configWoId;
    }

    /**
     * set configWoId
     */  
    public void setConfigWoId(Integer configWoId) {
        this.configWoId=configWoId;
    }
    /**
     * get woType
     */    
    public Short getWoType() {
        return woType;
    }

    /**
     * set woType
     */  
    public void setWoType(Short woType) {
        this.woType=woType;
    }
    /**
     * get woState
     */    
    public Short getWoState() {
        return woState;
    }

    /**
     * set woState
     */  
    public void setWoState(Short woState) {
        this.woState=woState;
    }
    /**
     * get acceptTime
     */    
    public Date getAcceptTime() {
        return acceptTime;
    }

    /**
     * set acceptTime
     */  
    public void setAcceptTime(Date acceptTime) {
        this.acceptTime=acceptTime;
    }
    /**
     * get startExeTime
     */    
    public Date getStartExeTime() {
        return startExeTime;
    }

    /**
     * set startExeTime
     */  
    public void setStartExeTime(Date startExeTime) {
        this.startExeTime=startExeTime;
    }
    /**
     * get endExeTime
     */    
    public Date getEndExeTime() {
        return endExeTime;
    }

    /**
     * set endExeTime
     */  
    public void setEndExeTime(Date endExeTime) {
        this.endExeTime=endExeTime;
    }
    /**
     * get cityId
     */    
    public Integer getCityId() {
        return cityId;
    }

    /**
     * set cityId
     */  
    public void setCityId(Integer cityId) {
        this.cityId=cityId;
    }
    /**
     * get countyId
     */    
    public Integer getCountyId() {
        return countyId;
    }

    /**
     * set countyId
     */  
    public void setCountyId(Integer countyId) {
        this.countyId=countyId;
    }
    /**
     * get bureauId
     */    
    public Integer getBureauId() {
        return bureauId;
    }

    /**
     * set bureauId
     */  
    public void setBureauId(Integer bureauId) {
        this.bureauId=bureauId;
    }
    /**
     * get ִ�����ȼ�
     */    
    public Short getPriority() {
        return priority;
    }

    /**
     * set ִ�����ȼ�
     */  
    public void setPriority(Short priority) {
        this.priority=priority;
    }
    /**
     * get �豸�ͺ�Id
     */    
    public Integer getDeviceTypeId() {
        return deviceTypeId;
    }

    /**
     * set �豸�ͺ�Id
     */  
    public void setDeviceTypeId(Integer deviceTypeId) {
        this.deviceTypeId=deviceTypeId;
    }
    /**
     * get ��ԪIP
     */    
    public String getNeIp() {
        return neIp;
    }

    /**
     * set ��ԪIP
     */  
    public void setNeIp(String neIp) {
        this.neIp=neIp;
    }
    /**
     * get ���ܺ�
     */    
    public String getShelfId() {
        return shelfId;
    }

    /**
     * set ���ܺ�
     */  
    public void setShelfId(String shelfId) {
        this.shelfId=shelfId;
    }
    /**
     * get �����
     */    
    public String getFrameId() {
        return frameId;
    }

    /**
     * set �����
     */  
    public void setFrameId(String frameId) {
        this.frameId=frameId;
    }
    /**
     * get ���ۺ�
     */    
    public Short getSlotId() {
        return slotId;
    }

    /**
     * set ���ۺ�
     */  
    public void setSlotId(Short slotId) {
        this.slotId=slotId;
    }
    /**
     * get �˿ں�
     */    
    public Integer getPortId() {
        return portId;
    }

    /**
     * set �˿ں�
     */  
    public void setPortId(Integer portId) {
        this.portId=portId;
    }
    /**
     * get adsl��·����
     */    
    public Short getAdslLineType() {
        return adslLineType;
    }

    /**
     * set adsl��·����
     */  
    public void setAdslLineType(Short adslLineType) {
        this.adslLineType=adslLineType;
    }
    /**
     * get atucRate
     */    
    public Integer getAtucRate() {
        return atucRate;
    }

    /**
     * set atucRate
     */  
    public void setAtucRate(Integer atucRate) {
        this.atucRate=atucRate;
    }
    /**
     * get aturRate
     */    
    public Integer getAturRate() {
        return aturRate;
    }

    /**
     * set aturRate
     */  
    public void setAturRate(Integer aturRate) {
        this.aturRate=aturRate;
    }
    /**
     * get snmpReadCommunity
     */    
    public String getSnmpReadCommunity() {
        return snmpReadCommunity;
    }

    /**
     * set snmpReadCommunity
     */  
    public void setSnmpReadCommunity(String snmpReadCommunity) {
        this.snmpReadCommunity=snmpReadCommunity;
    }
    /**
     * get snmpWriteCommunity
     */    
    public String getSnmpWriteCommunity() {
        return snmpWriteCommunity;
    }

    /**
     * set snmpWriteCommunity
     */  
    public void setSnmpWriteCommunity(String snmpWriteCommunity) {
        this.snmpWriteCommunity=snmpWriteCommunity;
    }
    /**
     * get tl1ServerIp
     */    
    public String getTl1ServerIp() {
        return tl1ServerIp;
    }

    /**
     * set tl1ServerIp
     */  
    public void setTl1ServerIp(String tl1ServerIp) {
        this.tl1ServerIp=tl1ServerIp;
    }
    /**
     * get tl1ServerPort
     */    
    public Integer getTl1ServerPort() {
        return tl1ServerPort;
    }

    /**
     * set tl1ServerPort
     */  
    public void setTl1ServerPort(Integer tl1ServerPort) {
        this.tl1ServerPort=tl1ServerPort;
    }
    /**
     * get tl1User
     */    
    public String getTl1User() {
        return tl1User;
    }

    /**
     * set tl1User
     */  
    public void setTl1User(String tl1User) {
        this.tl1User=tl1User;
    }
    /**
     * get tl1Password
     */    
    public String getTl1Password() {
        return tl1Password;
    }

    /**
     * set tl1Password
     */  
    public void setTl1Password(String tl1Password) {
        this.tl1Password=tl1Password;
    }
    /**
     * get exeResult
     */    
    public Long getExeResult() {
        return exeResult;
    }

    /**
     * set exeResult
     */  
    public void setExeResult(Long exeResult) {
        this.exeResult=exeResult;
    }
    /**
     * get exeReturnMsg
     */    
    public String getExeReturnMsg() {
        return exeReturnMsg;
    }

    /**
     * set exeReturnMsg
     */  
    public void setExeReturnMsg(String exeReturnMsg) {
        this.exeReturnMsg=exeReturnMsg;
    }
    /**
     * get passes
     */    
    public Short getPasses() {
        return passes;
    }

    /**
     * set passes
     */  
    public void setPasses(Short passes) {
        this.passes=passes;
    }
    /**
     * get Ĭ��vlan
     */    
    public Integer getVlan() {
        return vlan;
    }

    /**
     * set Ĭ��vlan
     */  
    public void setVlan(Integer vlan) {
        this.vlan=vlan;
    }
    /**
     * get iptvҵ��vlan
     */    
    public Integer getIptvvlan() {
        return iptvvlan;
    }

    /**
     * set iptvҵ��vlan
     */  
    public void setIptvvlan(Integer iptvvlan) {
        this.iptvvlan=iptvvlan;
    }
    /**
     * get voipҵ��vlan
     */    
    public Integer getVoipvlan() {
        return voipvlan;
    }

    /**
     * set voipҵ��vlan
     */  
    public void setVoipvlan(Integer voipvlan) {
        this.voipvlan=voipvlan;
    }
    /**
     * get videoҵ��vlan
     */    
    public Integer getVideovlan() {
        return videovlan;
    }

    /**
     * set videoҵ��vlan
     */  
    public void setVideovlan(Integer videovlan) {
        this.videovlan=videovlan;
    }
    /**
     * get MAC address
     */    
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * set MAC address
     */  
    public void setMacAddress(String macAddress) {
        this.macAddress=macAddress;
    }
    /**
     * get PON��·����
     */    
    public Short getPonLineType() {
        return ponLineType;
    }

    /**
     * set PON��·����
     */  
    public void setPonLineType(Short ponLineType) {
        this.ponLineType=ponLineType;
    }
    /**
     * get ������ʽ
     */    
    public Short getLineType() {
        return lineType;
    }

    /**
     * set ������ʽ
     */  
    public void setLineType(Short lineType) {
        this.lineType=lineType;
    }
    /**
     * get �豸���� 
     */    
    public String getDeviceMaker() {
        return deviceMaker;
    }

    /**
     * set �豸���� 
     */  
    public void setDeviceMaker(String deviceMaker) {
        this.deviceMaker=deviceMaker;
    }
    /**
     * get ԭʼ����Ψһ�����б��
     */    
    public String getOriginWoUuid() {
        return originWoUuid;
    }

    /**
     * set ԭʼ����Ψһ�����б��
     */  
    public void setOriginWoUuid(String originWoUuid) {
        this.originWoUuid=originWoUuid;
    }
    /**
     * get �������·
     */    
    public Integer getPvc() {
        return pvc;
    }

    /**
     * set �������·
     */  
    public void setPvc(Integer pvc) {
        this.pvc=pvc;
    }
    /**
     * get ��ͨ����ʶֵ
     */    
    public Integer getVci() {
        return vci;
    }

    /**
     * set ��ͨ����ʶֵ
     */  
    public void setVci(Integer vci) {
        this.vci=vci;
    }
    /**
     * get ��·����ʶֵ
     */    
    public Integer getVpi() {
        return vpi;
    }

    /**
     * set ��·����ʶֵ
     */  
    public void setVpi(Integer vpi) {
        this.vpi=vpi;
    }
    /**
     * get �¹�������UUID
     */    
    public Long getNextWorkOrderUuid() {
        return nextWorkOrderUuid;
    }

    /**
     * set �¹�������UUID
     */  
    public void setNextWorkOrderUuid(Long nextWorkOrderUuid) {
        this.nextWorkOrderUuid=nextWorkOrderUuid;
    }
    /**
     * get ontId
     */    
    public String getOntId() {
        return ontId;
    }

    /**
     * set ontId
     */  
    public void setOntId(String ontId) {
        this.ontId=ontId;
    }
    /**
     * get deviceType
     */    
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * set deviceType
     */  
    public void setDeviceType(String deviceType) {
        this.deviceType=deviceType;
    }
    /**
     * get ontLanNumber
     */    
    public Integer getOntLanNumber() {
        return ontLanNumber;
    }

    /**
     * set ontLanNumber
     */  
    public void setOntLanNumber(Integer ontLanNumber) {
        this.ontLanNumber=ontLanNumber;
    }
    /**
     * get ontPotsNumber
     */    
    public Integer getOntPotsNumber() {
        return ontPotsNumber;
    }

    /**
     * set ontPotsNumber
     */  
    public void setOntPotsNumber(Integer ontPotsNumber) {
        this.ontPotsNumber=ontPotsNumber;
    }
    /**
     * get mgcIp
     */    
    public String getMgcIp() {
        return mgcIp;
    }

    /**
     * set mgcIp
     */  
    public void setMgcIp(String mgcIp) {
        this.mgcIp=mgcIp;
    }
    /**
     * get sbcIp
     */    
    public String getSbcIp() {
        return sbcIp;
    }

    /**
     * set sbcIp
     */  
    public void setSbcIp(String sbcIp) {
        this.sbcIp=sbcIp;
    }
    /**
     * get iadip
     */    
    public String getIadip() {
        return iadip;
    }

    /**
     * set iadip
     */  
    public void setIadip(String iadip) {
        this.iadip=iadip;
    }
    /**
     * get iadipMask
     */    
    public String getIadipMask() {
        return iadipMask;
    }

    /**
     * set iadipMask
     */  
    public void setIadipMask(String iadipMask) {
        this.iadipMask=iadipMask;
    }
    /**
     * get iadipGateway
     */    
    public String getIadipGateway() {
        return iadipGateway;
    }

    /**
     * set iadipGateway
     */  
    public void setIadipGateway(String iadipGateway) {
        this.iadipGateway=iadipGateway;
    }

	/**
	 * @return the svlan
	 */
	public Integer getSvlan() {
		return svlan;
	}

	/**
	 * @param svlan the svlan to set
	 */
	public void setSvlan(Integer svlan) {
		this.svlan = svlan;
	}

	/**
	 * @return the cvlan
	 */
	public Integer getCvlan() {
		return cvlan;
	}

	/**
	 * @param cvlan the cvlan to set
	 */
	public void setCvlan(Integer cvlan) {
		this.cvlan = cvlan;
	}

	/**
	 * @param ontLanNumber the ontLanNumber to set
	 */
	public void setOntLanNumber(int ontLanNumber) {
		this.ontLanNumber = ontLanNumber;
	}

	/**
	 * @param ontPotsNumber the ontPotsNumber to set
	 */
	public void setOntPotsNumber(int ontPotsNumber) {
		this.ontPotsNumber = ontPotsNumber;
	}

	/**
	 * @return the ontKey
	 */
	public String getOntKey() {
		return ontKey;
	}

	/**
	 * @param ontKey the ontKey to set
	 */
	public void setOntKey(String ontKey) {
		this.ontKey = ontKey;
	}

	/**
	 * @return the tid
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @param tid the tid to set
	 */
	public void setTid(String tid) {
		this.tid = tid;
	}

	/**
	 * @return the useStatus
	 */
	public Integer getUseStatus() {
		return useStatus;
	}

	/**
	 * @param useStatus the useStatus to set
	 */
	public void setUseStatus(Integer useStatus) {
		this.useStatus = useStatus;
	}

	/**
	 * @return the voiceVLAN
	 */
	public Integer getVoiceVLAN() {
		return voiceVLAN;
	}

	/**
	 * @param voiceVLAN the voiceVLAN to set
	 */
	public void setVoiceVLAN(Integer voiceVLAN) {
		this.voiceVLAN = voiceVLAN;
	}

	/**
	 * @return the sbcIpReserve
	 */
	public String getSbcIpReserve() {
		return sbcIpReserve;
	}

	/**
	 * @param sbcIpReserve the sbcIpReserve to set
	 */
	public void setSbcIpReserve(String sbcIpReserve) {
		this.sbcIpReserve = sbcIpReserve;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((acceptTime == null) ? 0 : acceptTime.hashCode());
		result = prime * result + ((adslLineType == null) ? 0 : adslLineType.hashCode());
		result = prime * result + ((atucRate == null) ? 0 : atucRate.hashCode());
		result = prime * result + ((aturRate == null) ? 0 : aturRate.hashCode());
		result = prime * result + ((bureauId == null) ? 0 : bureauId.hashCode());
		result = prime * result + ((cityId == null) ? 0 : cityId.hashCode());
		result = prime * result + ((configWoId == null) ? 0 : configWoId.hashCode());
		result = prime * result + ((countyId == null) ? 0 : countyId.hashCode());
		result = prime * result + ((cvlan == null) ? 0 : cvlan.hashCode());
		result = prime * result + ((deviceMaker == null) ? 0 : deviceMaker.hashCode());
		result = prime * result + ((deviceType == null) ? 0 : deviceType.hashCode());
		result = prime * result + ((deviceTypeId == null) ? 0 : deviceTypeId.hashCode());
		result = prime * result + ((endExeTime == null) ? 0 : endExeTime.hashCode());
		result = prime * result + ((exeResult == null) ? 0 : exeResult.hashCode());
		result = prime * result + ((exeReturnMsg == null) ? 0 : exeReturnMsg.hashCode());
		result = prime * result + ((frameId == null) ? 0 : frameId.hashCode());
		result = prime * result + ((iadip == null) ? 0 : iadip.hashCode());
		result = prime * result + ((iadipGateway == null) ? 0 : iadipGateway.hashCode());
		result = prime * result + ((iadipMask == null) ? 0 : iadipMask.hashCode());
		result = prime * result + ((iptvvlan == null) ? 0 : iptvvlan.hashCode());
		result = prime * result + ((lineType == null) ? 0 : lineType.hashCode());
		result = prime * result + ((macAddress == null) ? 0 : macAddress.hashCode());
		result = prime * result + ((mgcIp == null) ? 0 : mgcIp.hashCode());
		result = prime * result + ((neIp == null) ? 0 : neIp.hashCode());
		result = prime * result + ((nextWorkOrderUuid == null) ? 0 : nextWorkOrderUuid.hashCode());
		result = prime * result + ((ontId == null) ? 0 : ontId.hashCode());
		result = prime * result + ((ontKey == null) ? 0 : ontKey.hashCode());
		result = prime * result + ((ontLanNumber == null) ? 0 : ontLanNumber.hashCode());
		result = prime * result + ((ontPotsNumber == null) ? 0 : ontPotsNumber.hashCode());
		result = prime * result + ((originWoId == null) ? 0 : originWoId.hashCode());
		result = prime * result + ((originWoUuid == null) ? 0 : originWoUuid.hashCode());
		result = prime * result + ((passes == null) ? 0 : passes.hashCode());
		result = prime * result + ((ponLineType == null) ? 0 : ponLineType.hashCode());
		result = prime * result + ((portId == null) ? 0 : portId.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((pvc == null) ? 0 : pvc.hashCode());
		result = prime * result + ((resourceCode == null) ? 0 : resourceCode.hashCode());
		result = prime * result + ((sbcIp == null) ? 0 : sbcIp.hashCode());
		result = prime * result + ((sbcIpReserve == null) ? 0 : sbcIpReserve.hashCode());
		result = prime * result + ((shelfId == null) ? 0 : shelfId.hashCode());
		result = prime * result + ((slotId == null) ? 0 : slotId.hashCode());
		result = prime * result + ((snmpReadCommunity == null) ? 0 : snmpReadCommunity.hashCode());
		result = prime * result
				+ ((snmpWriteCommunity == null) ? 0 : snmpWriteCommunity.hashCode());
		result = prime * result + ((specLineNum == null) ? 0 : specLineNum.hashCode());
		result = prime * result + ((startExeTime == null) ? 0 : startExeTime.hashCode());
		result = prime * result + ((svlan == null) ? 0 : svlan.hashCode());
		result = prime * result + ((tid == null) ? 0 : tid.hashCode());
		result = prime * result + ((tl1Password == null) ? 0 : tl1Password.hashCode());
		result = prime * result + ((tl1ServerIp == null) ? 0 : tl1ServerIp.hashCode());
		result = prime * result + ((tl1ServerPort == null) ? 0 : tl1ServerPort.hashCode());
		result = prime * result + ((tl1User == null) ? 0 : tl1User.hashCode());
		result = prime * result + ((useStatus == null) ? 0 : useStatus.hashCode());
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		result = prime * result + ((vci == null) ? 0 : vci.hashCode());
		result = prime * result + ((videovlan == null) ? 0 : videovlan.hashCode());
		result = prime * result + ((vlan == null) ? 0 : vlan.hashCode());
		result = prime * result + ((voiceVLAN == null) ? 0 : voiceVLAN.hashCode());
		result = prime * result + ((voipvlan == null) ? 0 : voipvlan.hashCode());
		result = prime * result + ((vpi == null) ? 0 : vpi.hashCode());
		result = prime * result + ((woState == null) ? 0 : woState.hashCode());
		result = prime * result + ((woType == null) ? 0 : woType.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkOrder other = (WorkOrder) obj;
		if (acceptTime == null) {
			if (other.acceptTime != null)
				return false;
		} else if (!acceptTime.equals(other.acceptTime))
			return false;
		if (adslLineType == null) {
			if (other.adslLineType != null)
				return false;
		} else if (!adslLineType.equals(other.adslLineType))
			return false;
		if (atucRate == null) {
			if (other.atucRate != null)
				return false;
		} else if (!atucRate.equals(other.atucRate))
			return false;
		if (aturRate == null) {
			if (other.aturRate != null)
				return false;
		} else if (!aturRate.equals(other.aturRate))
			return false;
		if (bureauId == null) {
			if (other.bureauId != null)
				return false;
		} else if (!bureauId.equals(other.bureauId))
			return false;
		if (cityId == null) {
			if (other.cityId != null)
				return false;
		} else if (!cityId.equals(other.cityId))
			return false;
		if (configWoId == null) {
			if (other.configWoId != null)
				return false;
		} else if (!configWoId.equals(other.configWoId))
			return false;
		if (countyId == null) {
			if (other.countyId != null)
				return false;
		} else if (!countyId.equals(other.countyId))
			return false;
		if (cvlan == null) {
			if (other.cvlan != null)
				return false;
		} else if (!cvlan.equals(other.cvlan))
			return false;
		if (deviceMaker == null) {
			if (other.deviceMaker != null)
				return false;
		} else if (!deviceMaker.equals(other.deviceMaker))
			return false;
		if (deviceType == null) {
			if (other.deviceType != null)
				return false;
		} else if (!deviceType.equals(other.deviceType))
			return false;
		if (deviceTypeId == null) {
			if (other.deviceTypeId != null)
				return false;
		} else if (!deviceTypeId.equals(other.deviceTypeId))
			return false;
		if (endExeTime == null) {
			if (other.endExeTime != null)
				return false;
		} else if (!endExeTime.equals(other.endExeTime))
			return false;
		if (exeResult == null) {
			if (other.exeResult != null)
				return false;
		} else if (!exeResult.equals(other.exeResult))
			return false;
		if (exeReturnMsg == null) {
			if (other.exeReturnMsg != null)
				return false;
		} else if (!exeReturnMsg.equals(other.exeReturnMsg))
			return false;
		if (frameId == null) {
			if (other.frameId != null)
				return false;
		} else if (!frameId.equals(other.frameId))
			return false;
		if (iadip == null) {
			if (other.iadip != null)
				return false;
		} else if (!iadip.equals(other.iadip))
			return false;
		if (iadipGateway == null) {
			if (other.iadipGateway != null)
				return false;
		} else if (!iadipGateway.equals(other.iadipGateway))
			return false;
		if (iadipMask == null) {
			if (other.iadipMask != null)
				return false;
		} else if (!iadipMask.equals(other.iadipMask))
			return false;
		if (iptvvlan == null) {
			if (other.iptvvlan != null)
				return false;
		} else if (!iptvvlan.equals(other.iptvvlan))
			return false;
		if (lineType == null) {
			if (other.lineType != null)
				return false;
		} else if (!lineType.equals(other.lineType))
			return false;
		if (macAddress == null) {
			if (other.macAddress != null)
				return false;
		} else if (!macAddress.equals(other.macAddress))
			return false;
		if (mgcIp == null) {
			if (other.mgcIp != null)
				return false;
		} else if (!mgcIp.equals(other.mgcIp))
			return false;
		if (neIp == null) {
			if (other.neIp != null)
				return false;
		} else if (!neIp.equals(other.neIp))
			return false;
		if (nextWorkOrderUuid == null) {
			if (other.nextWorkOrderUuid != null)
				return false;
		} else if (!nextWorkOrderUuid.equals(other.nextWorkOrderUuid))
			return false;
		if (ontId == null) {
			if (other.ontId != null)
				return false;
		} else if (!ontId.equals(other.ontId))
			return false;
		if (ontKey == null) {
			if (other.ontKey != null)
				return false;
		} else if (!ontKey.equals(other.ontKey))
			return false;
		if (ontLanNumber == null) {
			if (other.ontLanNumber != null)
				return false;
		} else if (!ontLanNumber.equals(other.ontLanNumber))
			return false;
		if (ontPotsNumber == null) {
			if (other.ontPotsNumber != null)
				return false;
		} else if (!ontPotsNumber.equals(other.ontPotsNumber))
			return false;
		if (originWoId == null) {
			if (other.originWoId != null)
				return false;
		} else if (!originWoId.equals(other.originWoId))
			return false;
		if (originWoUuid == null) {
			if (other.originWoUuid != null)
				return false;
		} else if (!originWoUuid.equals(other.originWoUuid))
			return false;
		if (passes == null) {
			if (other.passes != null)
				return false;
		} else if (!passes.equals(other.passes))
			return false;
		if (ponLineType == null) {
			if (other.ponLineType != null)
				return false;
		} else if (!ponLineType.equals(other.ponLineType))
			return false;
		if (portId == null) {
			if (other.portId != null)
				return false;
		} else if (!portId.equals(other.portId))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (pvc == null) {
			if (other.pvc != null)
				return false;
		} else if (!pvc.equals(other.pvc))
			return false;
		if (resourceCode == null) {
			if (other.resourceCode != null)
				return false;
		} else if (!resourceCode.equals(other.resourceCode))
			return false;
		if (sbcIp == null) {
			if (other.sbcIp != null)
				return false;
		} else if (!sbcIp.equals(other.sbcIp))
			return false;
		if (sbcIpReserve == null) {
			if (other.sbcIpReserve != null)
				return false;
		} else if (!sbcIpReserve.equals(other.sbcIpReserve))
			return false;
		if (shelfId == null) {
			if (other.shelfId != null)
				return false;
		} else if (!shelfId.equals(other.shelfId))
			return false;
		if (slotId == null) {
			if (other.slotId != null)
				return false;
		} else if (!slotId.equals(other.slotId))
			return false;
		if (snmpReadCommunity == null) {
			if (other.snmpReadCommunity != null)
				return false;
		} else if (!snmpReadCommunity.equals(other.snmpReadCommunity))
			return false;
		if (snmpWriteCommunity == null) {
			if (other.snmpWriteCommunity != null)
				return false;
		} else if (!snmpWriteCommunity.equals(other.snmpWriteCommunity))
			return false;
		if (specLineNum == null) {
			if (other.specLineNum != null)
				return false;
		} else if (!specLineNum.equals(other.specLineNum))
			return false;
		if (startExeTime == null) {
			if (other.startExeTime != null)
				return false;
		} else if (!startExeTime.equals(other.startExeTime))
			return false;
		if (svlan == null) {
			if (other.svlan != null)
				return false;
		} else if (!svlan.equals(other.svlan))
			return false;
		if (tid == null) {
			if (other.tid != null)
				return false;
		} else if (!tid.equals(other.tid))
			return false;
		if (tl1Password == null) {
			if (other.tl1Password != null)
				return false;
		} else if (!tl1Password.equals(other.tl1Password))
			return false;
		if (tl1ServerIp == null) {
			if (other.tl1ServerIp != null)
				return false;
		} else if (!tl1ServerIp.equals(other.tl1ServerIp))
			return false;
		if (tl1ServerPort == null) {
			if (other.tl1ServerPort != null)
				return false;
		} else if (!tl1ServerPort.equals(other.tl1ServerPort))
			return false;
		if (tl1User == null) {
			if (other.tl1User != null)
				return false;
		} else if (!tl1User.equals(other.tl1User))
			return false;
		if (useStatus == null) {
			if (other.useStatus != null)
				return false;
		} else if (!useStatus.equals(other.useStatus))
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		if (vci == null) {
			if (other.vci != null)
				return false;
		} else if (!vci.equals(other.vci))
			return false;
		if (videovlan == null) {
			if (other.videovlan != null)
				return false;
		} else if (!videovlan.equals(other.videovlan))
			return false;
		if (vlan == null) {
			if (other.vlan != null)
				return false;
		} else if (!vlan.equals(other.vlan))
			return false;
		if (voiceVLAN == null) {
			if (other.voiceVLAN != null)
				return false;
		} else if (!voiceVLAN.equals(other.voiceVLAN))
			return false;
		if (voipvlan == null) {
			if (other.voipvlan != null)
				return false;
		} else if (!voipvlan.equals(other.voipvlan))
			return false;
		if (vpi == null) {
			if (other.vpi != null)
				return false;
		} else if (!vpi.equals(other.vpi))
			return false;
		if (woState == null) {
			if (other.woState != null)
				return false;
		} else if (!woState.equals(other.woState))
			return false;
		if (woType == null) {
			if (other.woType != null)
				return false;
		} else if (!woType.equals(other.woType))
			return false;
		return true;
	}

	/**
	 * @return the resourceCode
	 */
	public String getResourceCode() {
		return resourceCode;
	}

	/**
	 * @param resourceCode the resourceCode to set
	 */
	public void setResourceCode(String resourceCode) {
		this.resourceCode = resourceCode;
	}

	/**
	 * @return the specLineNum
	 */
	public String getSpecLineNum() {
		return specLineNum;
	}

	/**
	 * @param specLineNum the specLineNum to set
	 */
	public void setSpecLineNum(String specLineNum) {
		this.specLineNum = specLineNum;
	}

    

}