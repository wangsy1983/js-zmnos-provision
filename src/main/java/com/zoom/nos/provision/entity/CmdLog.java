package com.zoom.nos.provision.entity;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;

public class CmdLog {

    // ID
    private Long uuid;
    
    // workOrderUuid
    private Long workOrderId;
    
    // ԭʼ����ID
    private String originWoId;
    
    // ����
    private String cmd;
    
    // ���
    private Long woResultId;
    
    // ������
    private String codeName;
    
    // ��������
    private String descr;
    
    // ִ��ʱ��
    private Date exeTime;
    

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
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
     * get workOrderUuid
     */    
    public Long getWorkOrderId() {
        return workOrderId;
    }

    /**
     * set workOrderUuid
     */  
    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId=workOrderId;
    }
    /**
     * get ԭʼ����ID
     */    
    public String getOriginWoId() {
        return originWoId;
    }

    /**
     * set ԭʼ����ID
     */  
    public void setOriginWoId(String originWoId) {
        this.originWoId=originWoId;
    }
    /**
     * get ����
     */    
    public String getCmd() {
        return cmd;
    }

    /**
     * set ����
     */  
    public void setCmd(String cmd) {
        this.cmd=cmd;
    }
    /**
     * get ���
     */    
    public Long getWoResultId() {
        return woResultId;
    }

    /**
     * set ���
     */  
    public void setWoResultId(Long woResultId) {
        this.woResultId=woResultId;
    }
    /**
     * get ������
     */    
    public String getCodeName() {
        return codeName;
    }

    /**
     * set ������
     */  
    public void setCodeName(String codeName) {
        this.codeName=codeName;
    }
    /**
     * get ��������
     */    
    public String getDescr() {
        return descr;
    }

    /**
     * set ��������
     */  
    public void setDescr(String descr) {
        this.descr=descr;
    }
    /**
     * get ִ��ʱ��
     */    
    public Date getExeTime() {
        return exeTime;
    }

    /**
     * set ִ��ʱ��
     */  
    public void setExeTime(Date exeTime) {
        this.exeTime=exeTime;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
		result = prime * result
				+ ((codeName == null) ? 0 : codeName.hashCode());
		result = prime * result + ((descr == null) ? 0 : descr.hashCode());
		result = prime * result + ((exeTime == null) ? 0 : exeTime.hashCode());
		result = prime * result
				+ ((originWoId == null) ? 0 : originWoId.hashCode());
		result = prime * result
				+ ((woResultId == null) ? 0 : woResultId.hashCode());
		result = prime * result
				+ ((workOrderId == null) ? 0 : workOrderId.hashCode());
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
		CmdLog other = (CmdLog) obj;
		if (cmd == null) {
			if (other.cmd != null)
				return false;
		} else if (!cmd.equals(other.cmd))
			return false;
		if (codeName == null) {
			if (other.codeName != null)
				return false;
		} else if (!codeName.equals(other.codeName))
			return false;
		if (descr == null) {
			if (other.descr != null)
				return false;
		} else if (!descr.equals(other.descr))
			return false;
		if (exeTime == null) {
			if (other.exeTime != null)
				return false;
		} else if (!exeTime.equals(other.exeTime))
			return false;
		if (originWoId == null) {
			if (other.originWoId != null)
				return false;
		} else if (!originWoId.equals(other.originWoId))
			return false;
		if (woResultId == null) {
			if (other.woResultId != null)
				return false;
		} else if (!woResultId.equals(other.woResultId))
			return false;
		if (workOrderId == null) {
			if (other.workOrderId != null)
				return false;
		} else if (!workOrderId.equals(other.workOrderId))
			return false;
		return true;
	}

}