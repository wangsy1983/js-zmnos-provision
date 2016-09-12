package com.zoom.nos.provision.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ServiceVlan {

    // ID
    private Integer uuid;
    
    // ҵ����
    private Short serviceCode;
    
    // ҵ������
    private String serviceName;
    
    // VLAN��
    private Short vlan;
    
    private Short igmpVlan;
    
    private Short pvcNo;
    
    public Short getIgmpVlan() {
		return igmpVlan;
	}

	public void setIgmpVlan(Short igmpVlan) {
		this.igmpVlan = igmpVlan;
	}

	public Short getPvcNo() {
		return pvcNo;
	}

	public void setPvcNo(Short pvcNo) {
		this.pvcNo = pvcNo;
	}

	// �������·
    private Short pvc;
    
    // ��ͨ����ʶֵ
    private Short vci;
    
    // ��·����ʶֵ
    private Short vpi;
    
    // ��������
    private String cityCode;
    

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

    /**
     * get ID
     */    
    public Integer getUuid() {
        return uuid;
    }

    /**
     * set ID
     */  
    public void setUuid(Integer uuid) {
        this.uuid=uuid;
    }
    /**
     * get ҵ����
     */    
    public Short getServiceCode() {
        return serviceCode;
    }

    /**
     * set ҵ����
     */  
    public void setServiceCode(Short serviceCode) {
        this.serviceCode=serviceCode;
    }
    /**
     * get ҵ������
     */    
    public String getServiceName() {
        return serviceName;
    }

    /**
     * set ҵ������
     */  
    public void setServiceName(String serviceName) {
        this.serviceName=serviceName;
    }
    /**
     * get VLAN��
     */    
    public Short getVlan() {
        return vlan;
    }

    /**
     * set VLAN��
     */  
    public void setVlan(Short vlan) {
        this.vlan=vlan;
    }
    /**
     * get �������·
     */    
    public Short getPvc() {
        return pvc;
    }

    /**
     * set �������·
     */  
    public void setPvc(Short pvc) {
        this.pvc=pvc;
    }
    /**
     * get ��ͨ����ʶֵ
     */    
    public Short getVci() {
        return vci;
    }

    /**
     * set ��ͨ����ʶֵ
     */  
    public void setVci(Short vci) {
        this.vci=vci;
    }
    /**
     * get ��·����ʶֵ
     */    
    public Short getVpi() {
        return vpi;
    }

    /**
     * set ��·����ʶֵ
     */  
    public void setVpi(Short vpi) {
        this.vpi=vpi;
    }
    /**
     * get ��������
     */    
    public String getCityCode() {
        return cityCode;
    }

    /**
     * set ��������
     */  
    public void setCityCode(String cityCode) {
        this.cityCode=cityCode;
    }

}