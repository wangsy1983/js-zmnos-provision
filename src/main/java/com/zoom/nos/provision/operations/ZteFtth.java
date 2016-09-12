package com.zoom.nos.provision.operations;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.ServiceVlan;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.message.ZteTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.SystemFlag;
import com.zoom.nos.provision.tl1.session.ZteTl1Session;

public class ZteFtth extends AbstractOperations {

    private static Logger log = LoggerFactory.getLogger(ZteFtth.class);

    private ZteTl1Session session = null;

    private boolean hguFlag = false;
    private boolean sfuFlag = false;

    public ZteFtth(WorkOrder wo) throws ZtlException {
        super(wo);
        session = new ZteTl1Session(wo.getTl1ServerIp(), wo.getTl1ServerPort(), "", 0, wo.getTl1User(), wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
        session.open();
    }

    /**
     * 释放资源
     */
    public void destruction() {
        session.close();
    }

    /**
     *
     */
    public WoResult alterRate() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;

        // 限速
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            // CFG-ONUBW::OLTID=12.0.254.9,PONID=1-1-2-1,ONUID=lycg123:CTAG::UPBW=10M;
            cmd = new StringBuffer();
            cmd.append("CFG-ONUBW::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID,");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            // cmd.append("UPBW=").append(wo.getAtucRate() / 1024).append("M");
            if (wo.getAtucRate() < 1024) {
                cmd.append("UPBW=").append(wo.getAtucRate()).append("K");
            } else {
                cmd.append("UPBW=").append(wo.getAtucRate() / 1024).append("M");
            }
            cmd.append(";");
        } else {
            // CFG-LANPORTBW::OLTID=12.0.254.9,PONID=1-1-2-1,ONUID=lycg123,ONUPORT=1-1-1-3:
            // CTAG::BW=10M;
            cmd = new StringBuffer();
            cmd.append("CFG-LANPORTBW::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(",ONUPORT=1-1-1-1");
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            // cmd.append("BW=").append(wo.getAtucRate() / 1024).append("M");
            if (wo.getAtucRate() < 1024) {
                cmd.append("BW=").append(wo.getAtucRate()).append("K");
            } else {
                cmd.append("BW=").append(wo.getAtucRate() / 1024).append("M");
            }
            cmd.append(";");
        }

        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 关宽带
     */
    public WoResult close() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;

        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            //DEL-ONU::OLTID=192.168.200.2,PONID=1-1-2-1:CTAG::ONUIDTYPE=ONU_NUMBER,ONUID=12;
            cmd = new StringBuffer();
            cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("ONUIDTYPE=ONU_NUMBER");
            cmd.append(",ONUID=").append(wo.getOntId());
            cmd.append(";");
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
        } else {
            // 删除olt宽带业务流
            // DEL-PONVLAN::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021:CTAG::UV=3600;
            cmd = new StringBuffer();
            cmd.append("CHG-ONUUNI-PON::DID=").append(wo.getNeIp());
            cmd.append(",OID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(",PORT=1");
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::STATUS=disable");
            cmd.append(",MODE=1");
            cmd.append(",PVLAN=").append(wo.getVlan());
            cmd.append(",CVLAN=").append(wo.getVlan());
            cmd.append(",ACCESSMODE=oam;");
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                // return new WoResult(rm);
            }
            // DEL-ONU-PON::DID=10.63.204.211,OID=1-1-8-1-1:CTAG::;
            cmd = new StringBuffer();
            cmd.append("DEL-ONU-PON::DID=").append(wo.getNeIp());
            cmd.append(",OID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            // cmd.append(",OID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append(";");
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
        }
        // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
        // return WoResult.rms_not_need_register;
        // }

        return WoResult.SUCCESS;
    }

    /**
     * 注销ONT 不需要改变工单结果状态
     */
    public void deleteOnu() {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        // DEL-ONU::OLTID=136.1.1.100,PONID=1-1-2-1:CTAG::ONUID=12345678;
        cmd = new StringBuffer();
        cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append(wo.getShelfId());
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            cmd.append("ONUIDTYPE=LOID");
        } else {
            cmd.append("ONUIDTYPE=PWD");
        }
        cmd.append(",ONUID=").append(wo.getOntKey());
        cmd.append(";");
        try {
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            }
        } catch (Exception e) {
        }
    }

    /**
     * 注销ONT
     */
    public WoResult delOnu() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        // DEL-ONU::OLTID=136.1.1.100,PONID=1-1-2-1:CTAG::ONUID=12345678;
        cmd = new StringBuffer();
        cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append(wo.getShelfId());
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            cmd.append("ONUIDTYPE=LOID");
        } else {
            cmd.append("ONUIDTYPE=PWD");
        }
        cmd.append(",ONUID=").append(wo.getOntKey());
        cmd.append(";");
        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 开通宽带
     */
    public WoResult open() throws ZtlException {
        if (StringUtils.isNotBlank(wo.getDeviceType()) && wo.getDeviceType().length() > 2 && !wo.getDeviceType().startsWith("0")) {
            if (wo.getDeviceType().toLowerCase().charAt(2) == 'h') {
                hguFlag = true;
                wo.setRmsFlag(1);
            } else {
                sfuFlag = true;
            }
        }
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        int needRegisterOnu = 0;

        // CVLANID 不能为空（cvlan）
        if (wo.getCvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.cvlanNotBlank);
        }
        // SVLANID 不能为空（svlan）
        if (wo.getSvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.svlanNotBlank);
        }

        // 注册
        WoResult _rwo = this.registerOnu();
        if (WoResult.not_need_register.equals(_rwo)) {
            // 接着往下走
            needRegisterOnu = 1;
        } else if (!WoResult.SUCCESS.equals(_rwo)) {
            return _rwo;
        }

        if (hguFlag || sfuFlag) {
            // 区分epon与gpon
            if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
                if (hguFlag) {
                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::SVLAN=2065,CVLAN=3052,UV=3052;
                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::UPBW=G2M,DOWNBW=G10M;
                    // sfu
                    // ACT-LANPORT::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::;
                    // CFG-LANPORTVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::CVLAN=3052,CCOS=6;
                    // hgu
                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789968:CTAG::CVLAN=45,UV=45;

                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::SVLAN=2065,CVLAN=3052,UV=3052;
                    cmd = new StringBuffer();
                    cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=" + wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("SVLAN=").append(wo.getSvlan());
                    cmd.append(",CVLAN=").append(wo.getCvlan());
                    cmd.append(",UV=").append(wo.getCvlan());
                    cmd.append(",SERVICENAME=HSI");//iptv新增
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    //itpv预埋指令
//				ADD-PONVLAN::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid:ctag::SVLAN=IPTVsvlan,CVLAN=43,UV=43,SERVICENAME=IPTV;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=" + wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("SVLAN=").append(wo.getIptvvlan());
                    cmd.append(",CVLAN=43").append(wo.getCvlan());
                    cmd.append(",UV=43").append(wo.getCvlan());
                    cmd.append(",SERVICENAME=IPTV");//iptv新增
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::UPBW=G2M,DOWNBW=G10M;
                    cmd = new StringBuffer();
                    cmd.append("CFG-ONUBW::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    // cmd.append("UPBW=").append(wo.getAtucRate() /
                    // 1024).append("M");
                    // if (wo.getAtucRate() < 1024) {
                    // cmd.append("UPBW=G").append(wo.getAtucRate()).append("K");
                    // } else {
//				cmd.append("UPBW=G").append(wo.getAtucRate() / 1024).append("M");
//				cmd.append(",DOWNBW=G").append(wo.getAtucRate() / 1024).append("M");
                    cmd.append("UPBW=G").append("100").append("M");
//				cmd.append(",DOWNBW=G").append("100").append("M");
                    // }
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789968:CTAG::DOWNBW=G100M,BWTYPE=VPORT;
                    cmd = new StringBuffer();
                    cmd.append("CFG-ONUBW::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("DOWNBW=G").append("100").append("M");
                    cmd.append(",BWTYPE=VPORT");
                    // }
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

//					 ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789968:CTAG::CVLAN=45,UV=45;
                    cmd = new StringBuffer();
                    cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("CVLAN=45");
                    cmd.append(",UV=45");
                    cmd.append(",SERVICENAME=TR069");//iptv新增
                    cmd.append(";");
                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    //以下两条iptv预埋
//                    ADD-PONVLAN::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid:ctag::CVLAN=24,UV=24,,SERVICENAME=multicast;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("CVLAN=24");
                    cmd.append(",UV=24");
                    cmd.append(",SERVICENAME=multicast");//iptv新增
                    cmd.append(";");
                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

//                    ADD-LANIPTVPORT::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid,ONUPORT=onuport:ctag::MVLAN=24,SERVICENAME=IPTV;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("ADD-LANIPTVPORT::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUPORT=").append("1-1-1-1");
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("MVLAN=24");
                    cmd.append(",SERVICENAME=IPTV");
                    cmd.append(";");
                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                } else {

                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::SVLAN=2065,CVLAN=3052,UV=3052;
                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::UPBW=G2M,DOWNBW=G10M;
                    // sfu
                    // ACT-LANPORT::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::;
                    // CFG-LANPORTVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::CVLAN=3052,CCOS=6;
                    // hgu
                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789968:CTAG::CVLAN=45,UV=45;

                    // ADD-PONVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::SVLAN=2065,CVLAN=3052,UV=3052;
                    cmd = new StringBuffer();
                    cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=" + wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("SVLAN=").append(wo.getSvlan());
                    cmd.append(",CVLAN=").append(wo.getCvlan());
                    cmd.append(",UV=").append(wo.getCvlan());
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }


                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999:CTAG::UPBW=G2M,DOWNBW=G10M;
                    cmd = new StringBuffer();
                    cmd.append("CFG-ONUBW::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    // cmd.append("UPBW=").append(wo.getAtucRate() /
                    // 1024).append("M");
                    // if (wo.getAtucRate() < 1024) {
                    // cmd.append("UPBW=G").append(wo.getAtucRate()).append("K");
                    // } else {
//				cmd.append("UPBW=G").append(wo.getAtucRate() / 1024).append("M");
//				cmd.append(",DOWNBW=G").append(wo.getAtucRate() / 1024).append("M");
                    cmd.append("UPBW=G").append("100").append("M");
//				cmd.append(",DOWNBW=G").append("100").append("M");
                    // }
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    // CFG-ONUBW::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789968:CTAG::DOWNBW=G100M,BWTYPE=VPORT;
                    cmd = new StringBuffer();
                    cmd.append("CFG-ONUBW::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("DOWNBW=G").append("100").append("M");
                    cmd.append(",BWTYPE=VPORT");
                    // }
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }


                    // ACT-LANPORT::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::;
                    cmd = new StringBuffer();
                    cmd.append("ACT-LANPORT::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUPORT=1-1-1-1");
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append(";");
                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    // CFG-LANPORTVLAN::OLTID=192.168.200.2,PONID=1-1-2-1,ONUIDTYPE=LOID,ONUID=0516123456789999,ONUPORT=1-1-1-1:CTAG::CVLAN=3052,CCOS=6;
//					cmd = new StringBuffer();
//					cmd.append("CFG-LANPORTVLAN::OLTID=").append(wo.getNeIp());
//					cmd.append(",PONID=").append(wo.getShelfId());
//					cmd.append("-").append(wo.getFrameId());
//					cmd.append("-").append(wo.getSlotId());
//					cmd.append("-").append(wo.getPortId());
//					cmd.append(",ONUIDTYPE=LOID");
//					cmd.append(",ONUID=").append(wo.getOntKey());
//					cmd.append(",ONUPORT=1-1-1-1");
//					cmd.append(":");
//					cmd.append(Ctag.getCtag());
//					cmd.append("::");
////					cmd.append("PT=H.248,EID=eid,IPMODE=static");
//					cmd.append("CVLAN=").append(wo.getCvlan());
//					cmd.append(",CCOS=6");
//					cmd.append(";");
                    cmd = new StringBuffer();
                    cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUPORT=1-1-1-1");
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append(",VLANMOD=tag");
                    cmd.append(",PVID=").append(wo.getCvlan());
                    cmd.append(",PCOS=0");
                    cmd.append(";");
                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                }

            } else {

                // CHG-QINQSTAT-PON::DID=192.168.61.110,PID=1-1-2-1:CTAG::
                // STATUS=enable;
                // CRT-QINQRULE-PON::DID=192.168.61.110:CTAG::PID=1-1-2-1,RULETYPE=cvlan,SVLAN=3800,BEGINCVLAN=2001,ENDCVLAN=2300;
                // CHG-PORTVLAN-PON::DID=192.168.61.110,OID=1-1-2-1-50:CTFG::OPERATION=1,VLANTYPE=1,VLANID=45;

                cmd = new StringBuffer();
                cmd.append("CHG-QINQSTAT-PON::DID=").append(wo.getNeIp());
                cmd.append(",PID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append("STATUS=enable");
                cmd.append(";");

                rm = session.exeZteCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }

                // CRT-QINQRULE-PON::DID=192.168.61.110:CTAG::PID=1-1-2-1,RULETYPE=cvlan,SVLAN=3800,BEGINCVLAN=2001,ENDCVLAN=2300;
                // caculate BEGINCVLAN and ENDCVLAN
                cmd = new StringBuffer();
                cmd.append("CRT-QINQRULE-PON::DID=").append(wo.getNeIp());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append("PID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",RULETYPE=cvlan");
                cmd.append(",SVLAN=").append(wo.getSvlan());
                cmd.append(",BEGINCVLAN=").append(wo.getCvlan());
                cmd.append(",ENDCVLAN=").append(wo.getCvlan());
                cmd.append(";");

                rm = session.exeZteCmd(cmd.toString(), wo);
                if (rm.isFailed() && !"60130716".equals(rm.getEn()) && !"60130721".equals(rm.getEn()) && !"IRE".equalsIgnoreCase(rm.getEn())) {
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }



                if (hguFlag) {
//                    CRT-QINQRULE-PON::DID=oltip:ctag::PID=ponid,RULETYPE=cvlan,SVLAN=IPTV业务svlan,BEGINCVLAN=43,ENDCVLAN=43;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("CRT-QINQRULE-PON::DID=").append(wo.getNeIp());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("PID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",RULETYPE=cvlan");
                    cmd.append(",SVLAN=").append(wo.getIptvvlan());
                    cmd.append(",BEGINCVLAN=43");
                    cmd.append(",ENDCVLAN=43");
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed() && !"60130716".equals(rm.getEn()) && !"60130721".equals(rm.getEn()) && !"IRE".equalsIgnoreCase(rm.getEn())) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }


                    // CHG-PORTVLAN-PON::DID=192.168.61.110,OID=1-1-2-1-50:CTFG::OPERATION=1,VLANTYPE=1,VLANID=45;
                    cmd = new StringBuffer();
                    cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
                    cmd.append(",PID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("OPERATION=1");
                    cmd.append(",VLANTYPE=1");
                    cmd.append(",VLANID=45");
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    //CHG-PORTVLAN-PON::DID=oltip,PID=ponid-onuno:ctag::OPERATION=1,VLANTYPE=1,VLANID=24;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
                    cmd.append(",PID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("OPERATION=1");
                    cmd.append(",VLANTYPE=1");
                    cmd.append(",VLANID=24");
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    cmd = new StringBuffer();
                    cmd.append("CHG-ONU-BWIDTH-PON::DID=").append(wo.getNeIp());
                    cmd.append(",OID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("BWTEMPLATE=FTTH-100").append("M"); // 添加端口限速模板
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    //BND-PORT-MVLAN::DID=oltip,OID=ponid-onuno:ctag::MVLANID=24,BINDTYPE=2;//IPTV新增
                    cmd = new StringBuffer();
                    cmd.append("BND-PORT-MVLAN::DID=").append(wo.getNeIp());
                    cmd.append(",OID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("MVLANID=24"); // 添加端口限速模板
                    cmd.append(",BINDTYPE=2"); // 添加端口限速模板
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                } else {
                    // LAN口配置
                    // CFG-LANPORT::OLTID=192.168.61.110,PONID=1-1-5-8,ONUIDTYPE=LOID,ONUID=54998760001,ONUPORT=1-1-1-1:CTAG::VLANMOD=tag,PVID=2201,PCOS=0;
                    cmd = new StringBuffer();
                    cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(",ONUIDTYPE=LOID,ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUPORT=1-1-1-1");
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append(",VLANMOD=tag");
                    cmd.append(",PVID=").append(wo.getCvlan());
                    cmd.append(",PCOS=0");
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    }

                    cmd = new StringBuffer();
                    cmd.append("CHG-ONU-BWIDTH-PON::DID=").append(wo.getNeIp());
                    cmd.append(",OID=").append(wo.getShelfId());
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("BWTEMPLATE=FTTH-100").append("M"); // 添加端口限速模板
                    cmd.append(";");

                    rm = session.exeZteCmd(cmd.toString(), wo);
                    if (rm.isFailed()) {
                        log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                        return new WoResult(rm);
                    } else {
                        return WoResult.SUCCESS;
                    }
                }
            }

            if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
                if (needRegisterOnu == 1) {
                    return WoResult.rms_not_need_register;
                } else {
                    return WoResult.rms_need_register;
                }
            }
        } else {

            // 宽带的改注册信息，加“专线号”
            // CHG-ONU-PON::DID=198.140.0.6,OID=1-1-4-1-6:Ctag::DESC=01030151;

            cmd = new StringBuffer();
            cmd.append("CHG-ONU-PON::DID=").append(wo.getNeIp());
            cmd.append(",OID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            // 为了防止江苏联通此字段为空
            if (wo.getSpecLineNum() == null || wo.getSpecLineNum().compareTo("") == 0) {
                wo.setSpecLineNum(wo.getOntKey());
            }
            cmd.append("DESC=").append(wo.getSpecLineNum());
            cmd.append(";");

            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }

            // 区分epon与gpon
            if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
                // CFG-ONUBW::OLTID=12.0.254.9,PONID=1-1-2-1,ONUID=lycg123:CTAG::UPBW=10M;
                cmd = new StringBuffer();
                // JS 修改ONU上联口的带宽
                // CHG-ONU-BWIDTH-PON::DID=172.16.243.210,OID=1-1-1-1-25:CTAG::BWTEMPLATE=FTTH-100M;
                cmd.append("CHG-ONU-BWIDTH-PON::DID=").append(wo.getNeIp());
                cmd.append(",OID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append("-").append(wo.getOntId());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append("BWTEMPLATE=FTTH-").append(wo.getAtucRate() / 1024).append("M"); // 添加端口限速模板
                cmd.append(";");

                rm = session.exeZteCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }

                cmd = new StringBuffer();
                // CHG-ONUUNI-PON::DID=172.16.243.210,OID=1-1-1-1-25,PORT=1:3344::STATUS=1,MODE=1,PVLAN=0,CVLAN=3033,ACCESSMODE=oam;
                cmd.append("CHG-ONUUNI-PON::DID=").append(wo.getNeIp());
                cmd.append(",OID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append("-").append(wo.getOntId());
                cmd.append(",PORT=1");
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::STATUS=1");
                cmd.append(",MODE=1");
                cmd.append(",PVLAN=0");
                cmd.append(",CVLAN=").append(wo.getCvlan());
                cmd.append(",ACCESSMODE=oam");
                cmd.append(";");
            } else {

                // 2012-06-26
                // ADD-PONVLAN::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021:CTAG::SVLAN=2401,CVLAN=3600,UV=3600;
                cmd = new StringBuffer();
                cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
                cmd.append(":CTAG::UV=").append(wo.getCvlan());
                cmd.append(",CVLAN=").append(wo.getCvlan());
                cmd.append(",SVLAN=").append(wo.getSvlan());
                cmd.append(";");
                rm = session.exeZteCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }

                // CFG-LANPORTVLAN::OLTID=12.0.254.9,PONID=1-1-2-1,ONUID=lycg123,ONUPORT=1-1-1-1:1::SVLAN=25,CVLAN=1,CCOS=0;
                // CFG-LANPORT::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021,ONUPORT=1-1-1-1:CTAG::BW=4M,VLANMOD=tag,PVID=3600,PCOS=0;
                cmd = new StringBuffer();
                // CFG-LANPORT::OLTID=198.15.244.10,PONID=1-1-6-8,ONUIDTYPE=PWD,ONUID=0608000349,ONUPORT=1-1-1-1|1-1-1-2:CTAG::BW=5M,VLANMOD=tag,PVID=4,PCOS=0;
                cmd.append("CFG-LANPORT::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
                cmd.append(",ONUPORT=1-1-1-1|1-1-1-2");
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                if (wo.getAtucRate() < 1024) {
                    cmd.append("::BW=").append(wo.getAtucRate()).append("K");
                } else {
                    cmd.append("::BW=").append(wo.getAtucRate() / 1024).append("M");
                }
                // cmd.append("::BW=").append(wo.getAtucRate() /
                // 1024).append("M");
                cmd.append(",VLANMOD=tag");
                cmd.append(",PVID=").append(wo.getCvlan());
                cmd.append(",PCOS=0");
                cmd.append(";");
            }
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
        }
        return WoResult.SUCCESS;
    }

    /**
     * 注册ONU
     */
    public WoResult registerOnu() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        String resourceCode = wo.getResourceCode();
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (127 - wo.getOntKey().length()));
            resourceCode = resourceCode.substring(resourceCode.length() - limit);
        } else if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (127 - wo.getOntKey().length()));
            resourceCode = resourceCode.substring(resourceCode.length() - limit);
        }
        resourceCode = wo.getOntKey() + resourceCode;
        // 取ONT NAME
        String ontName = this.getOntName(wo.getNeIp(), wo.getShelfId(), wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId());
        // String ontKey = "";
        if (ontName == null) {
            // 没取到ont name，注册ont
            // ADD-ONU::OLTID=12.0.254.9,PONID=1-1-2-1:1::ONUIDTYPE=LOID,ONUNO=20,ONUID=123,ONUTYPE=ZTE-F420;
            // ADD-ONU::OLTID=12.0.254.1,PONID=1-1-2-1:1::ONUIDTYPE=PWD,ONUNO=20,ONUID=123,ONUTYPE=ZTE-F620;
            cmd = new StringBuffer();

            // if ("192.168.61.110".equals(wo.getNeIp())) {
            if (StringUtils.isNotBlank(wo.getDeviceType()) && wo.getDeviceType().length() > 2 && !wo.getDeviceType().startsWith("0")) {
                if (wo.getDeviceType().toLowerCase().charAt(2) == 'h') {
                    hguFlag = true;
                    wo.setRmsFlag(1);
                } else {
                    sfuFlag = true;
                }
                // new logic
                // ADD-ONU::OLTID=olt-name,PONID=ponport_location:CTAG::
                // [AUTHTYPE=auth-type],ONUID=onu-index[,PWD=onu
                // password][,ONUNO=onu-no][,NAME=name][,DESC=onu
                // description],ONUTYPE=onu
                cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
                    cmd.append("AUTHTYPE=LOID,");
                }
                cmd.append("ONUID=").append(wo.getOntKey());
                cmd.append(",ONUNO=").append(wo.getOntId());
                cmd.append(",NAME=").append(resourceCode);
                if (wo.getDeviceType().equalsIgnoreCase("ZTE-F400")) {
                    cmd.append(",ONUTYPE=").append("F400");
                } else {
                    cmd.append(",ONUTYPE=").append(wo.getDeviceType().substring(1));
                }
                // cmd.append(",ONUTYPE=ZTE-F420");
            } else {
                // old logic
                // JS
                // ADD-ONU-PON::DID=172.16.243.210,PID=1-1-1-1:1.1::OID=1233211231,ONUNO=25,TYPE=ZTE-D420,ONUNAME=test02;
                if (StringUtils.isNotBlank(wo.getDeviceType()) && wo.getDeviceType().startsWith("0")) {
                    wo.setDeviceType(wo.getDeviceType().substring(2));
                } else {
                    wo.setDeviceType(wo.getDeviceType().substring(1));
                }
                cmd.append("ADD-ONU-PON::DID=").append(wo.getNeIp());
                cmd.append(",PID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append("ONUNAME=").append(resourceCode);
                cmd.append(",ONUNO=").append(wo.getOntId());
                cmd.append(",OID=").append(wo.getOntKey());

                if (StringUtils.isNotBlank(wo.getDeviceType())) {
                    cmd.append(",TYPE=").append(wo.getDeviceType());
                    // cmd.append(",TYPE=ZTE-F420");
                } else {
                    cmd.append(",TYPE=");
                }
            }

            cmd.append(";");
            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                // 注册失败，再查一次
                String ontName2 = this.getOntName(wo.getNeIp(), wo.getShelfId(), wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId());
                if (ontName2 != null) {
                    // ontKey =
                    // ontName2.substring(ontName2.indexOf("@@@@")+4,ontName2.length());
                    // ontName2 =
                    // ontName2.substring(0,ontName2.indexOf("@@@@"));
                    if (ontName2.equals(wo.getResourceCode())) {
                        // ont存在，名字相同
                        log.debug("equal ont name:[" + ontName2 + "]");
                        return WoResult.SUCCESS;
                    } else {
                        // ont name不同，失败
                        log.debug("Different ont name:[" + ontName2 + "]");
                        throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
                    }
                }
                // 再查一次也没有找到
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
            return WoResult.SUCCESS;
        } else {
            // ont 已存在
            if (ontName.equals(wo.getResourceCode())) {
                // ont存在，名字相同
                log.debug("equal ont name:[" + ontName + "]");
                return WoResult.SUCCESS;
            } else {
                // ont name或者ont key不同，失败
                log.debug("Different ont name:[" + ontName + "]");
                throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
            }
        }
    }

    /**
     * 开语音
     */
    public WoResult openVoip() throws ZtlException {

        if (StringUtils.isNotBlank(wo.getDeviceType()) && wo.getDeviceType().length() > 2 && !wo.getDeviceType().startsWith("0")) {
            if (wo.getDeviceType().toLowerCase().charAt(2) == 'h') {
                hguFlag = true;
                wo.setRmsFlag(1);
            } else {
                sfuFlag = true;
            }
        }

        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;

        // 验证SbcIp、sbcIpReserve 不为空
        if (StringUtils.isBlank(wo.getSbcIp())) {
            throw new ZtlException(ErrorConst.sbcIpNotBlank);
        }
        if (StringUtils.isBlank(wo.getSbcIpReserve())) {
            throw new ZtlException(ErrorConst.sbcIpReserveNotBlank);
        }

        // VoiceVLANID 不能为空
        if (wo.getVoiceVLAN().intValue() == -1) {
            throw new ZtlException(ErrorConst.voicevlanNotBlank);
        }

        int voicePort = -1;
        try {
            voicePort = Integer.parseInt(wo.getTid()) + 1;
        } catch (Exception e) {
            throw new ZtlException(ErrorConst.tidNeedNumber);
        }

        int needRegisterOnu = 0;
        // 注册
        WoResult _rwo = this.registerOnu();
        if (WoResult.not_need_register.equals(_rwo)) {
            // 接着往下走
            needRegisterOnu = 1;
        } else if (!WoResult.SUCCESS.equals(_rwo)) {
            return _rwo;
        }

        if (hguFlag) {
            // CHG-PORTVLAN-PON::DID=192.168.61.110,OID=1-1-2-1-50:CTFG::OPERATION=1,VLANTYPE=1,VLANID=42;
            cmd = new StringBuffer();
            cmd.append("CHG-PORTVLAN-PON::DID=").append(wo.getNeIp());
            cmd.append(",PID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("OPERATION=1");
            cmd.append(",VLANTYPE=1");
            cmd.append(",VLANID=42");
            cmd.append(";");

            rm = session.exeZteCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
        }

        if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
            if (needRegisterOnu == 1) {
                return WoResult.rms_not_need_register;
            } else {
                return WoResult.rms_need_register;
            }
        }

        // 激活VOIP端口
        // ACT-VOIPPORT::OLTID=136.1.1.100,PONID=1-1-2-1,ONUIDTYPE=LOID,
        // ONUID=12345678,ONUPORT=1-1-1-1:CTAG::;
        cmd = new StringBuffer();
        cmd.append("ACT-VOIPPORT::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append(wo.getShelfId());
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=LOID,ONUID=").append(wo.getOntKey());
        cmd.append(",ONUPORT=1-1-1-" + voicePort);
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            // 配置ONU VOIP信息
            // CFG-VOIPSERVICE::OLTID=172.25.16.34,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021,ONUPORT=1-1-1-1:
            // CTAG::PT=H.248,TID=A0,VOIPVLAN=3603,IPMODE=static,IP=10.20.30.40,IPMASK=255.255.255.0,IPGATEWAY=10.20.30.1,
            // CCOS=5,MGCIP1=192.32.0.8,MGCIP2=192.32.0.6;
            cmd = new StringBuffer();
            cmd.append("CFG-VOIPSERVICE::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(",ONUPORT=1-1-1-" + voicePort);
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("PT=H.248,EID=eid,IPMODE=static");
            cmd.append(",VOIPVLAN=").append(wo.getVoiceVLAN());
            cmd.append(",IP=").append(wo.getIadip());
            cmd.append(",IPMASK=").append(wo.getIadipMask());
            cmd.append(",IPGATEWAY=").append(wo.getIadipGateway());
            cmd.append(",TID=A").append(wo.getTid());
            cmd.append(",CCOS=5");
            cmd.append(",MGCIP1=").append(wo.getSbcIp());
            cmd.append(",MGCIP2=").append(wo.getSbcIpReserve());
            cmd.append(";");
        } else {
            // 配置olt语音业务流 （新加的） 3603是语音vlan
            // ADD-PONVLAN::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021:CTAG::CVLAN=3603,UV=3603;//PON口透传语音VLAN
            cmd = new StringBuffer();
            // 2013-11-23 当语音口大于1时,PONVLAN已经存在
            if (voicePort < 2) {
                cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append(wo.getShelfId());
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::CVLAN=").append(wo.getVoiceVLAN());
                cmd.append(",UV=").append(wo.getVoiceVLAN());
                cmd.append(";");
                rm = session.exeZteCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }
            }
            // 配置ONU VOIP信息
            // CFG-VOIPSERVICE::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021,ONUPORT=1-1-1-1:CTAG::
            // PT=H.248,TID=A0,VOIPVLAN=3603,IPMODE=static,IP=10.20.30.40,IPMASK=255.255.255.0,IPGATEWAY=10.20.30.1,
            // CCOS=5,MGCIP1=192.32.0.5,MGCIP2=192.32.0.7;
            cmd = new StringBuffer();
            cmd.append("CFG-VOIPSERVICE::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append(wo.getShelfId());
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
            cmd.append(",ONUPORT=1-1-1-" + voicePort);
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("PT=H.248,IPMODE=static");
            cmd.append(",VOIPVLAN=").append(wo.getVoiceVLAN());
            cmd.append(",IP=").append(wo.getIadip());
            cmd.append(",IPMASK=").append(wo.getIadipMask());
            cmd.append(",IPGATEWAY=").append(wo.getIadipGateway());
            cmd.append(",TID=A").append(wo.getTid());
            cmd.append(",CCOS=5");
            cmd.append(",MGCIP1=").append(wo.getSbcIp());
            cmd.append(",MGCIP2=").append(wo.getSbcIpReserve());
            cmd.append(";");
        }

        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 关语音
     */
    public WoResult closeVoip() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;

        int voicePort = -1;
        try {
            voicePort = Integer.parseInt(wo.getTid()) + 1;
        } catch (Exception e) {
            throw new ZtlException(ErrorConst.tidNeedNumber);
        }
        // 删除olt语音业务流
        // DEL-PONVLAN::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021:CTAG::CVLAN=3603,UV=3603;
        cmd = new StringBuffer();
        cmd.append("DEL-PONVLAN::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append(wo.getShelfId());
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::CVLAN=").append(wo.getVoiceVLAN());
        cmd.append(",UV=").append(wo.getVoiceVLAN());
        cmd.append(";");
        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }
        // 删除olt语音业务流
        // DEL-VOIPSERVICE::OLTID=172.27.2.2,PONID=1-1-2-4,ONUIDTYPE=PWD,ONUID=14080021,ONUPORT=1-1-1-1:CTAG::;
        cmd = new StringBuffer();
        cmd.append("DEL-VOIPSERVICE::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append(wo.getShelfId());
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=PWD,ONUID=").append(wo.getOntKey());
        cmd.append(",ONUPORT=1-1-1-" + voicePort);
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        rm = session.exeZteCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:" + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }
        // 如果最后一笔业务，注销
        // if (wo.needDelOnu()) {
        // this.delOnu();
        // }
        this.deleteOnu();
        // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
        // return WoResult.rms_not_need_register;
        // }
        return WoResult.SUCCESS;
    }


    /**
     * closeIptv
     */
    public WoResult closeIptv() throws ZtlException {
        WoResult result = close();
        if (!"success".equals(result.getCode())) {
            return result;
        }
        return WoResult.SUCCESS;
    }

    /**
     * 开IPTV
     */
    public WoResult openIptv() throws ZtlException {
        //first del onu
        WoResult result = delOnu();
        if (!"success".equals(result.getCode())) {
            return result;
        }
        //second do open
        result = open();
        if (!"success".equals(result.getCode())) {
            return result;
        }
        return WoResult.SUCCESS;
    }

    /**
     * @param ip
     * @param rn
     * @param fn
     * @param sn
     * @param pn
     * @param ontId
     * @return
     * @throws ZtlException
     */
    private String getOntName(String ip, String rn, String fn, Short sn, Integer pn, String ontId) {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        String ontName = "";
        // String ontKey = "";
        cmd = new StringBuffer();
        cmd.append("LST-ONU-PON::DID=").append(ip);
        cmd.append(",OID=").append(rn);
        cmd.append("-").append(fn);
        cmd.append("-").append(sn);
        cmd.append("-").append(pn);
        cmd.append("-").append(ontId);
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeZteListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + "," + e.getMessage());
            return null;
        }
        // ontKey = rm.getResult().get("ONUID");
        ontName = rm.getResult().get("ONUNAME");
        // if (ontKey != null && ontKey.compareTo("") > 0) {
        // ontName += "@@@@" + ontKey;
        // }
        return ontName;
    }

    private String checkGponOnuExsits() throws ZtlException {
        StringBuffer cmd = null;
        ZteTL1ResponseMessage rm = null;
        String ontName = "";// ontKey = "";
        // LST-ONU::OLTID=172.24.23.38,PONID=1-1-1-8,ONUID=20:CTAG::;
        cmd = new StringBuffer();
        cmd = new StringBuffer();
        cmd.append("LST-ONU::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUID=").append(wo.getOntKey());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeZteListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + "," + e.getMessage());
            return null;
        }

        ontName = rm.getResult().get("NAME");

        return ontName;
    }

}
