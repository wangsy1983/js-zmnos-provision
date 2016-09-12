package com.zoom.nos.provision.entity;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ServiceVlan {

    // ID
    private Integer uuid;
    
    // 业务编号
    private Short serviceCode;
    
    // 业务名称
    private String serviceName;
    
    // VLAN号
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

	// 永久虚电路
    private Short pvc;
    
    // 虚通道标识值
    private Short vci;
    
    // 虚路径标识值
    private Short vpi;
    
    // 地市区号
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
     * get 业务编号
     */    
    public Short getServiceCode() {
        return serviceCode;
    }

    /**
     * set 业务编号
     */  
    public void setServiceCode(Short serviceCode) {
        this.serviceCode=serviceCode;
    }
    /**
     * get 业务名称
     */    
    public String getServiceName() {
        return serviceName;
    }

    /**
     * set 业务名称
     */  
    public void setServiceName(String serviceName) {
        this.serviceName=serviceName;
    }
    /**
     * get VLAN号
     */    
    public Short getVlan() {
        return vlan;
    }

    /**
     * set VLAN号
     */  
    public void setVlan(Short vlan) {
        this.vlan=vlan;
    }
    /**
     * get 永久虚电路
     */    
    public Short getPvc() {
        return pvc;
    }

    /**
     * set 永久虚电路
     */  
    public void setPvc(Short pvc) {
        this.pvc=pvc;
    }
    /**
     * get 虚通道标识值
     */    
    public Short getVci() {
        return vci;
    }

    /**
     * set 虚通道标识值
     */  
    public void setVci(Short vci) {
        this.vci=vci;
    }
    /**
     * get 虚路径标识值
     */    
    public Short getVpi() {
        return vpi;
    }

    /**
     * set 虚路径标识值
     */  
    public void setVpi(Short vpi) {
        this.vpi=vpi;
    }
    /**
     * get 地市区号
     */    
    public String getCityCode() {
        return cityCode;
    }

    /**
     * set 地市区号
     */  
    public void setCityCode(String cityCode) {
        this.cityCode=cityCode;
    }

}