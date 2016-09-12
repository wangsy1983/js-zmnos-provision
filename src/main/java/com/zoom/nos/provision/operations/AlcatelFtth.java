package com.zoom.nos.provision.operations;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.core.WoResult;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.SystemFlag;
import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.AlcatelTl1Session;

public class AlcatelFtth extends AbstractOperations {

    private static Logger log = LoggerFactory.getLogger(AlcatelFtth.class);

    private AlcatelTl1Session session = null;

    boolean hguFlag = false;

    public AlcatelFtth(WorkOrder wo) throws ZtlException {
        super(wo);
        session = new AlcatelTl1Session(wo.getTl1ServerIp(),
                wo.getTl1ServerPort(), "", 0, wo.getTl1User(),
                wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
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
        AlcatelTL1ResponseMessage rm = null;

        // 限速
        // ED-HSI:OLTIP=10.1.159.2:HSIPORT-1-1-2-1-1-1-1:123::BWPROFUP=1M,BWPROFDN=1M;
        cmd = new StringBuffer();
        cmd.append("ED-HSI:OLTIP=").append(wo.getNeIp());
        cmd.append(":HSIPORT-1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append("-").append(wo.getOntId());
        cmd.append("-1-").append(
                CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
                        false));
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        if (wo.getAtucRate() < 1024) {
            cmd.append("BWPROFUP=").append(wo.getAtucRate()).append("K");
            cmd.append(",BWPROFDN=").append(wo.getAtucRate()).append("K");
        } else {
            cmd.append("BWPROFUP=").append(wo.getAtucRate() / 1024).append("M");
            cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024)
                    .append("M");
        }
        // cmd.append("BWPROFUP=").append(wo.getAtucRate() / 1024).append("M");
        // cmd.append(",BWPROFDN=").append(wo.getAtucRate() / 1024).append("M");
        cmd.append(";");

        rm = session.exeAlcatelCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                    + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 关宽带
     */
    public WoResult close() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

        // 江苏联通贝尔FTTH关的时候直接删除整个ONU
        if (SystemFlag.getSystemFlag() != null
                && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
            WoResult result = this.delOnu();
            // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
            // return WoResult.rms_not_need_register;
            // }
            return result;
        }

        // 关闭LAN端口
        // DLT-HSI:OLTIP=10.1.159.2:HSIPORT-1-1-2-1-1-1-1:123::;
        cmd = new StringBuffer();
        cmd.append("DLT-HSI:OLTIP=").append(wo.getNeIp());
        cmd.append(":HSIPORT-1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append("-").append(wo.getOntId());
        cmd.append("-1-").append(
                CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
                        true));
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        rm = session.exeAlcatelCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                    + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        // 如果最后一笔业务，注销
        if (this.hasNotServiceport(wo.getNeIp(), wo.getShelfId(),
                wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId())) {
            // this.delOnu();
        }

        return WoResult.SUCCESS;
    }

    /**
     * 注销ONT
     */
    public WoResult delOnu() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

        // 　DLT-ONT:OLTIP=10.1.159.2:ONT-1-1-2-1-1:123::;

        // JS　DLT-ONT:OLTIP=172.16.239.46:ONT-1-1-1-3-45:DLTONT::;
        cmd = new StringBuffer();
        cmd.append("DLT-ONT:OLTIP=").append(wo.getNeIp());
        cmd.append(":ONT-1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append("-").append(wo.getOntId());
        cmd.append(":");
        if (SystemFlag.getSystemFlag() != null
                && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
            cmd.append("DLTONT");
        } else {
            cmd.append(Ctag.getCtag());
        }
        cmd.append("::");
        cmd.append(";");

        rm = session.exeAlcatelCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                    + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 开通宽带
     */
    public WoResult open() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

        // CVLANID 不能为空（cvlan）
        if (wo.getCvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.cvlanNotBlank);
        }
        // SVLANID 不能为空（svlan）
        if (wo.getSvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.svlanNotBlank);
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
            // ADD-HGUVLAN:OLTIP=192.168.61.34:ONT-1-1-1-1-128:CTAG::SVLAN=3800,CVLAN=2001;
            cmd = new StringBuffer();
            cmd.append("ADD-HGUVLAN:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=").append(wo.getSvlan());
            cmd.append(",CVLAN=").append(wo.getCvlan());
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }

            // ADD-HGUVLAN:OLTIP=192.168.61.34:ONT-1-1-1-1-128:CTAG::SVLAN=0,CVLAN=45,UV=45;
            cmd = new StringBuffer();
            cmd.append("ADD-HGUVLAN:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=0");
            cmd.append(",CVLAN=45");
            cmd.append(",UV=45");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }

            //ADD-HGUVLAN:OLTIP=oltip:ONT-1-1-slotno-portno-onuno:ctag::SVLAN=IPTV业务svlan,CVLAN=43,UV=43;//iptv新增
            cmd = new StringBuffer();
            cmd.append("ADD-HGUVLAN:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=").append(wo.getIptvvlan());
            cmd.append(",CVLAN=43");
            cmd.append(",UV=43");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }


            //ADD-MULTICAST-PORT:OLTIP=oltip:ONT-1-1-slotno-portno-onuno:CTAG::MAXNUMGRP=8,UV=43;//iptv新增
            cmd = new StringBuffer();
            cmd.append("ADD-MULTICAST-PORT:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("MAXNUMGRP=8");
            cmd.append(",UV=43");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
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

        if(!hguFlag) {
            // JS
            // CRT-HSI:OLTIP=172.16.247.46:HSIPORT-1-1-2-4-34-1-1:CRTHSI::SVLAN=3521,CVLAN=3000,
            // BWRATEUP =100M, BWRATEDN =100M;
            cmd = new StringBuffer();
            cmd.append("CRT-HSI:OLTIP=").append(wo.getNeIp());
            cmd.append(":HSIPORT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append("-1-1:CRTHSI::");
            cmd.append("SVLAN=").append(wo.getSvlan());
            cmd.append(",CVLAN=").append(wo.getCvlan());
            cmd.append(",BWRATEUP=").append(wo.getAtucRate() / 1024)
                    .append("M");
            cmd.append(",BWRATEDN=").append(wo.getAtucRate() / 1024)
                    .append("M");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }
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
     * 注册ONU
     */
    public WoResult registerOnu() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

        String resourceCode = wo.getResourceCode();
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (128 - wo.getOntKey().length()));
            resourceCode = resourceCode.substring(resourceCode.length() - limit);
        } else if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (128 - wo.getOntKey().length()));
            resourceCode = resourceCode.substring(resourceCode.length() - limit);
        }
        resourceCode = wo.getOntKey() + resourceCode;

        // 缺少参数 return
        if (wo.getOntId() == null || wo.getOntId().equals("")
                || wo.getOntId().equals("null")) {
            // ont name不同，失败
            log.debug("缺少参数:OntId不能为空");
            throw new ZtlException(ErrorConst.getOntNameFailed);
        }

        // 取ONT NAME
        String ontName = this.getOntName(wo.getNeIp(), wo.getShelfId(),
                wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId());
        if (ontName == null) {
            // 没取到ont name，注册ont
            cmd = new StringBuffer();

            // if ("192.168.61.34".equals(wo.getNeIp())) {
            if (true) {
                if (StringUtils.isNotBlank(wo.getDeviceType())
                        && wo.getDeviceType().length() > 2
                        && !wo.getDeviceType().startsWith("0")
                        && (wo.getDeviceType().toLowerCase().charAt(2) == 'h')) {
                    // new logic
                    // ADD-ONT:OLTIP=192.168.61.34:ONT-1-1-1-1-128:CTAG::PONTYPE=GPON,LOID=112323111999,ONTTYPE=GS1FE,ONTNAME=南京联通测试1;
                    hguFlag = true;
                    cmd.append("ADD-ONT:OLTIP=").append(wo.getNeIp());
                    cmd.append(":ONT-1-1");
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("PONTYPE=GPON");
                    cmd.append(",LOID=").append(wo.getOntKey());
                    // ONU Model
                    if (StringUtils.isNotBlank(wo.getDeviceType())) {
                        // TODO temp modify by wangsy
                        cmd.append(",ONTTYPE=").append(
                                wo.getDeviceType().substring(1));
                        // cmd.append(",ONTTYPE=I-240E-Q");
                    } else {
                        // cmd.append("ONTTYPE=I-240E-Q");
                        cmd.append(",ONTTYPE=");
                    }
                    cmd.append(",ONTNAME=").append(resourceCode);

                } else {
                    // old logic
                    if (wo.getDeviceType().startsWith("0")) {
                        wo.setDeviceType(wo.getDeviceType().substring(2));
                    } else if (" I-240E-Q".equalsIgnoreCase(wo.getDeviceType())) {
                        // do nothing
                    } else {
                        wo.setDeviceType(wo.getDeviceType().substring(1));
                    }
                    cmd.append("ADD-ONT:OLTIP=").append(wo.getNeIp());
                    cmd.append(":ONT-1-1");
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    if (SystemFlag.getSystemFlag() != null
                            && SystemFlag.getSystemFlag().equals(
                            SystemFlag.JS_UNICOM)) {
                        cmd.append(":");
                        cmd.append(Ctag.getCtag());
                        cmd.append("::");
                        // ONU Model
                        if (StringUtils.isNotBlank(wo.getDeviceType())) {
                            cmd.append("ONTTYPE=").append(wo.getDeviceType());
                            // cmd.append("ONTTYPE=I-240E-Q");
                        } else {
                            // cmd.append("ONTTYPE=I-240E-Q");
                            cmd.append("ONTTYPE=");
                        }
                        cmd.append(",LOID=").append(wo.getOntKey());
                    } else {
                        cmd.append(":");
                        cmd.append(Ctag.getCtag());
                        cmd.append("::");
                        cmd.append("ONTTYPE=I-240E-P");
                        cmd.append(",SLID=").append(wo.getOntKey());
                    }
                    cmd.append(",ONTNAME=").append(resourceCode);
                    cmd.append(",PONTYPE=GPON");
                }
                cmd.append(";");

                rm = session.exeAlcatelCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    // 注册失败，再查一次
                    String ontName2 = this.getOntName(wo.getNeIp(),
                            wo.getShelfId(), wo.getFrameId(), wo.getSlotId(),
                            wo.getPortId(), wo.getOntId());
                    if (ontName2 != null) {
                        if (ontName2.equals(wo.getResourceCode())) {
                            // ont存在，名字相同
                            log.debug("equal ont name:[" + ontName2 + "]");
                            return WoResult.SUCCESS;
                            // return WoResult.not_need_register;
                        } else {
                            // ont name不同，失败
                            log.debug("Different ont name:[" + ontName2 + "]");
                            throw new ZtlException(
                                    ErrorConst.repeatOntIdOnDevAdmin);
                        }
                    }
                    // 再查一次也没有找到
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                            + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }
                return WoResult.SUCCESS;

            } else {

                if (!StringUtils.isBlank(wo.getDeviceType())
                        && wo.getDeviceType().length() > 2
                        && !wo.getDeviceType().startsWith("0")
                        && (wo.getDeviceType().toLowerCase().charAt(2) == 'h')) {
                    // new logic
                    // ADD-ONT:OLTIP=135.251.201.58:ONT-1-1-5-4-2:ADDONT::PONTYPE=GPON,LOID=nj12345678,ONTTYPE=I-240W-Q,ONTNAME=I-240W-Q;
                    hguFlag = true;
                    cmd.append("ADD-ONT:OLTIP=").append(wo.getNeIp());
                    cmd.append(":ONT-1-1");
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("PONTYPE=GPON");
                    cmd.append(",LOID=").append(wo.getOntKey());
                    // ONU Model
                    // if (StringUtils.isNotBlank(wo.getDeviceType())) {
                    // // TODO temp modify by wangsy
                    // cmd.append(",ONTTYPE=").append(wo.getDeviceType());
                    // // cmd.append(",ONTTYPE=I-240E-Q");
                    // } else {
                    // cmd.append("ONTTYPE=I-240E-Q");
                    cmd.append(",ONTTYPE=I-240E-Q");
                    // }
                    cmd.append(",ONTNAME=").append(wo.getResourceCode());

                } else {
                    // old logic
                    if (wo.getDeviceType().startsWith("0")) {
                        wo.setDeviceType(wo.getDeviceType().substring(2));
                    }
                    cmd.append("ADD-ONT:OLTIP=").append(wo.getNeIp());
                    cmd.append(":ONT-1-1");
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append("-").append(wo.getOntId());
                    if (SystemFlag.getSystemFlag() != null
                            && SystemFlag.getSystemFlag().equals(
                            SystemFlag.JS_UNICOM)) {
                        cmd.append(":");
                        cmd.append(Ctag.getCtag());
                        cmd.append("::");
                        // ONU Model
                        cmd.append("ONTTYPE=I-010G-T");
                        cmd.append(",LOID=").append(wo.getOntKey());
                    } else {
                        cmd.append(":");
                        cmd.append(Ctag.getCtag());
                        cmd.append("::");
                        cmd.append("ONTTYPE=I-240E-Q");
                        cmd.append(",SLID=").append(wo.getOntKey());
                    }
                    cmd.append(",ONTNAME=").append(wo.getResourceCode());
                    cmd.append(",PONTYPE=GPON");
                }
                cmd.append(";");

                rm = session.exeAlcatelCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    // 注册失败，再查一次
                    String ontName2 = this.getOntName(wo.getNeIp(),
                            wo.getShelfId(), wo.getFrameId(), wo.getSlotId(),
                            wo.getPortId(), wo.getOntId());
                    if (ontName2 != null) {
                        if (ontName2.equals(wo.getResourceCode())) {
                            // ont存在，名字相同
                            log.debug("equal ont name:[" + ontName2 + "]");
                            return WoResult.SUCCESS;
                            // return WoResult.not_need_register;
                        } else {
                            // ont name不同，失败
                            log.debug("Different ont name:[" + ontName2 + "]");
                            throw new ZtlException(
                                    ErrorConst.repeatOntIdOnDevAdmin);
                        }
                    }
                    // 再查一次也没有找到
                    log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                            + rm.getEn() + rm.getEnDesc());
                    return new WoResult(rm);
                }
                return WoResult.SUCCESS;

            }
        } else {
            // ont 已存在
            if (ontName.equals(wo.getResourceCode())) {
                // ont存在，名字相同
                log.debug("equal ont name:[" + ontName + "]");
                // return WoResult.not_need_register;
                return WoResult.SUCCESS;
            } else {
                // ont name不同，失败
                log.debug("Different ont name:[" + ontName + "]");
                throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
            }
        }
    }

    /**
     * 开语音
     */
    public WoResult openVoip() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

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
        // 如果是新ONU，先注册
        // if (wo.needRegisterOnu()) {
        WoResult _rwo = this.registerOnu();
        if (WoResult.not_need_register.equals(_rwo)) {
            // 接着往下走
            needRegisterOnu = 1;
        } else if (!WoResult.SUCCESS.equals(_rwo)) {
            return _rwo;
        }

        if (hguFlag) {
            // ADD-HGUVLAN:OLTIP=192.168.61.34:ONT-1-1-1-1-128:CTAG::SVLAN=0,CVLAN=42,UV=42;
            cmd = new StringBuffer();
            cmd.append("ADD-HGUVLAN:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=0");
            cmd.append(",CVLAN=42");
            cmd.append(",UV=42");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                return new WoResult(rm);
            }

            // ADD-HGUVLAN:OLTIP=192.168.61.34:ONT-1-1-1-1-128:CTAG::SVLAN=0,CVLAN=45,UV=45;
            cmd = new StringBuffer();
            cmd.append("ADD-HGUVLAN:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=0");
            cmd.append(",CVLAN=45");
            cmd.append(",UV=45");
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
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
        // }

        // SET-VOIP:OLTIP=10.1.159.2:ONT-1-1-2-1-1:12::MODE=H248,IPMODE=STATIC,SVLAN=66,
        // IPADDR=192.3.1.10,NETMASK=255.255.255.0,IPROUTER=192.3.1.1,MGCIP1=192.2.0.1;

        // JS SIP协议
        // SET-VOIP:OLTIP=172.16.239.46:ONT-1-1-1-3-23:2.1::MODE=SIP,SVLAN=200,IPMODE=STATIC,
        // IPADDR=172.17.128.2,NETMASK=255.255.255.0,IPROUTER=172.17.128.1,MGCIP1=0.0.0.0,MGCIP2=0.0.0.0;
        // H248协议
        // SET-VOIP:OLTIP=172.16.239.46:ONT-1-1-1-3-23:setvoip::MODE=H248,SVLAN=300,CVLAN=500,
        // IPMODE=STATIC,IPADDR=172.17.128.2,NETMASK=255.255.255.0,IPROUTER=172.17.128.1,MGCIP1=172.63.0.76,MGCIP2=0.0.0.0;

		/*
         * 工单：1111213000165764
		 * SET-VOIP:OLTIP=198.15.136.34:ONT-1-1-2-6-14:12716:
		 * :MODE=H248,IPMODE=STATIC,SVLAN=3998,IPADDR=10.216.152.30,
		 * NETMASK=255.255.240.0,IPROUTER=10.216.144.1,MGCIP1=116.2.253.34;
		 * 
		 * 修改后命令：
		 * SET-VOIP:OLTIP=198.15.136.34:ONT-1-1-2-6-14:12716::MODE=H248,IPMODE
		 * =STATIC,SVLAN=3998,IPADDR=10.216.152.30,
		 * NETMASK=255.255.240.0,IPROUTER=10.216.144.1,MGCIP1=116.2.253.34,
		 * MGCIP2=116.2.252.34;
		 */

        cmd = new StringBuffer();
        if (CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
                false) != null
                && CoreService.ticketControlService.getONUPort(
                wo.getOriginWoId(), false).equals("1")) {
            cmd.append("SET-VOIP:OLTIP=").append(wo.getNeIp());
            cmd.append(":ONT-1-1");
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append("-").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("MODE=H248,IPMODE=STATIC");
            cmd.append(",SVLAN=").append(wo.getVoiceVLAN());
            cmd.append(",IPADDR=").append(wo.getIadip());
            cmd.append(",NETMASK=").append(wo.getIadipMask());
            cmd.append(",IPROUTER=").append(wo.getIadipGateway());
            cmd.append(",MGCIP1=").append(wo.getSbcIp());
            // 2013.11.23 增加备用sbcip
            cmd.append(",MGCIP2=").append(wo.getSbcIp());
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                        + rm.getEn() + rm.getEnDesc());
                if ("IEAE".equals(rm.getEn())) {
                    // Input, Entity Already Exists - The entity to be created
                    // already exists.
                    // 跳过此错误
                } else {
                    return new WoResult(rm);
                }
            }
        }
        // CRT-POTS:OLTIP=10.1.159.2:POTSPORT-1-1-2-1-1-2-1:12::TERMID=A0;

        // JS
        // CRT-POTS:OLTIP=172.16.239.46:POTSPORT-1-1-1-3-23-2-1:CRTPOTS::TERMID=line1;
        cmd = new StringBuffer();
        cmd.append("CRT-POTS:OLTIP=").append(wo.getNeIp());
        cmd.append(":POTSPORT-1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append("-").append(wo.getOntId());
        cmd.append("-2-").append(
                CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
                        false));
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("TERMID=A").append(wo.getTid());
        cmd.append(";");
        rm = session.exeAlcatelCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                    + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * 关语音
     */
    public WoResult closeVoip() throws ZtlException {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;

        int voicePort = -1;
        try {
            voicePort = Integer.parseInt(wo.getTid()) + 1;
        } catch (Exception e) {
            throw new ZtlException(ErrorConst.tidNeedNumber);
        }

        // DLT-POTS:OLTIP=10.1.159.2:POTSPORT-1-1-2-1-1-2-1:12::;
        cmd = new StringBuffer();
        cmd.append("DLT-POTS:OLTIP=").append(wo.getNeIp());
        cmd.append(":POTSPORT-1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append("-").append(wo.getOntId());
        cmd.append("-2-").append(
                CoreService.ticketControlService.getONUPort(wo.getOriginWoId(),
                        true));
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        rm = session.exeAlcatelCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getOriginWoId() + " cmd:" + cmd + " failed:"
                    + rm.getEn() + rm.getEnDesc());
            return new WoResult(rm);
        }

        // 如果最后一笔业务，注销
        if (this.hasNotServiceport(wo.getNeIp(), wo.getShelfId(),
                wo.getFrameId(), wo.getSlotId(), wo.getPortId(), wo.getOntId())) {
            this.delOnu();
        }

        // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
        // return WoResult.rms_not_need_register;
        // }

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
    private String getOntName(String ip, String rn, String fn, Short sn,
                              Integer pn, String ontId) {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;
        String ontName = "";

        // LST-ONU::OLTID=10.1.162.2,PONID=1-1-1-1,ONUID=32,ONUIDTYPE=ONU_NUMBER::CTAG::;
        cmd = new StringBuffer();
        cmd.append("LST-ONU::OLTID=").append(ip);
        cmd.append(",PONID=1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUID=").append(wo.getOntId());
        cmd.append(",ONUIDTYPE=ONU_NUMBER:");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeAlcatelListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            log.debug(e.toString(), e);
            return null;
        }

        ontName = rm.getResult().get("NAME");
        if ("--".equals(ontName)) {
            // -- onu没有
            return null;
        }
        // String ontKey = rm.getResult().get("LOID");
        // log.info("alcatel ontkey="+ontKey);
        // if (!"--".equals(ontKey)) {
        // ontName += "@@@@" + ontKey;
        // }
        return ontName;
    }

    private boolean hasNotServiceport(String ip, String rn, String fn,
                                      Short sn, Integer pn, String ontId) {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;
        String rs = "";

        // LST-POTS::OLTID=10.1.162.2,PONID=1-1-1-4,ONUIDTYPE=ONU_NUMBER,ONUID=32:1::;
        cmd = new StringBuffer();
        cmd.append("LST-POTS::OLTID=").append(ip);
        cmd.append(",PONID=1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=ONU_NUMBER,ONUID=").append(wo.getOntId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeAlcatelListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("hasNotServiceport failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            log.debug(e.toString(), e);
            return false;
        }

        rs = rm.getResult().get("TID");
        if (StringUtils.isNotBlank(rs) && !"--".equals(rs)) {
            return false;
        }
        // LST-PORTVLAN::OLTID=10.1.162.2,PONID=1-1-2-4,ONUIDTYPE=ONU_NUMBER,ONUID=8:lstPortVlan::;

        // ;LST-LANPORT::OLTID=10.1.162.2,PONID=1-1-1-4,ONUIDTYPE=ONU_NUMBER,ONUID=32:1::;
        cmd = new StringBuffer();
        cmd.append("LST-PORTVLAN::OLTID=").append(ip);
        cmd.append(",PONID=1-1");
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=ONU_NUMBER,ONUID=").append(wo.getOntId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeAlcatelListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("hasNotServiceport failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            log.debug(e.toString(), e);
            return false;
        }

        // rs = rm.getResult().get("ADMINSTATUS");
        rs = rm.getResult().get("OLTID");
        log.debug("--rs--OLTID==" + rs);
        if (StringUtils.isNotBlank(rs) && !"--".equals(rs)) {
            return false;
        }

        return true;
    }
}