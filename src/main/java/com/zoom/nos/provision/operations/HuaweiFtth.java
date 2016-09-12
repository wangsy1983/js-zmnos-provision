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
import com.zoom.nos.provision.tl1.message.HwTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.Ctag;
import com.zoom.nos.provision.tl1.session.HuaweiTl1Session;
import com.zoom.nos.provision.tl1.session.SystemFlag;

public class HuaweiFtth extends AbstractOperations {
    private static Logger log = LoggerFactory.getLogger(HuaweiFtth.class);

    private HuaweiTl1Session session = null;

    private boolean hguFlag = false;

    // 数据业务 vlanid＝10
    private static final String dataUv = "10";

    // 语音业务 vlanid＝20
    private static final String voipUv = "20";

    // 预留业务 vlanid＝30

    // IPTV业务vlanid＝40
    private static final String iptvUv = "40";

    public HuaweiFtth(WorkOrder wo) throws ZtlException {
        super(wo);
        session = new HuaweiTl1Session(wo.getTl1ServerIp(),
                wo.getTl1ServerPort(), "", 0, wo.getTl1User(),
                wo.getTl1Password(), NosEnv.socket_timeout_tl1server);
        // open session
        session.open();
    }

    /**
     * 改宽带速率
     */
    public WoResult alterRate() throws ZtlException {

        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // 取DID
        String did = CoreService.ticketControlService.getDeviceDid(
                wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
        if (StringUtils.isBlank(did)) {
            throw new ZtlException(ErrorConst.noSuchDid);
        }

        // 取速率模板
        String lineProfile = CoreService.ticketControlService
                .getLineProfileName(wo);
        if (StringUtils.isBlank(lineProfile)) {
            throw new ZtlException(ErrorConst.lineProfileNoutFound);
        }

        // 修改速率模板
        cmd = new StringBuffer();
        cmd.append("MOD-SERVICEPORT::DID=").append(did);
        cmd.append(",FN=").append(wo.getFrameId());
        cmd.append(",SN=").append(wo.getSlotId());
        cmd.append(",PN=").append(wo.getPortId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("ONTID=").append(wo.getOntId());
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            cmd.append(",GEMPORTID=1");
        }
        cmd.append(",MTX=").append(lineProfile);
        cmd.append(",MRX=").append(lineProfile);
        cmd.append(";");
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }

        return WoResult.SUCCESS;
    }

    /**
     * close lan
     */
    public WoResult close() throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // INNERVLANID 不能为空（cvlan）
        if (wo.getCvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.cvlanNotBlank);
        }
        // VLANID 不能为空（svlan）
        if (wo.getSvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.svlanNotBlank);
        }

        String did = CoreService.ticketControlService.getDeviceDid(
                wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
        if (StringUtils.isBlank(did)) {
            throw new ZtlException(ErrorConst.noSuchDid);
        }

        cmd = new StringBuffer();
        // js
        // DEL-LANPORTVLAN::OLTID=10.71.62.138,PONID=NA-0-2-1,ONUIDTYPE=LOID,ONUID=sz3108shmi824x,ONUPORT=NA-NA-NA-1:CTAG::;
        if (SystemFlag.getSystemFlag() != null
                && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
            if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
                // DEL-PONVLAN::OLTID=192.168.61.106,PONID=NA-0-3-0,ONUIDTYPE=LOID,ONUID=1234567890:CTAG::UV=2001;
                cmd.append("DEL-PONVLAN::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append("NA");
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",ONUIDTYPE=LOID");
                cmd.append(",ONUID=").append(wo.getOntId());
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append("UV=").append(wo.getCvlan());
                cmd.append(";");
            } else {

                // LST-PORTVLAN::OLTID=10.71.212.190,PONID=NA-0-4-1,ONUIDTYPE=LOID,ONUID=aaaaaaa,ONUPORT=NA-NA-NA-1:CTAG::;
                // DEL-LANPORTVLAN::OLTID=172.16.240.149,PONID=NA-0-5-1,ONUIDTYPE=LOID,ONUID=aaaaa12345678901,ONUPORT=NA-NA-NA-1:CTAG::;
                // 删除业务之前查询一下,是否业务是否存在
                // DEL-LANPORTVLAN::(OLTID=olt-name,PONID=ponport_location,ONUIDTYPE=onuid-type,ONUID=onuindex),
                // ONUPORT=onu-port:CTAG::[UV=UserVlanID];

                // 如果能根据ONTID注销的时候,就不需要去维护LOID
                cmd.append("DEL-LANPORTVLAN::OLTID=").append(wo.getNeIp());
                cmd.append(",PONID=").append("NA");
                cmd.append("-").append(wo.getFrameId());
                cmd.append("-").append(wo.getSlotId());
                cmd.append("-").append(wo.getPortId());
                cmd.append(",ONUIDTYPE=ONU_NUMBER");
                cmd.append(",ONUID=").append(wo.getOntId());
                cmd.append(",ONUPORT=").append("NA-NA-NA-1");
                cmd.append(":");
                cmd.append(Ctag.getCtag());
                cmd.append("::");
                cmd.append(";");
            }
            // JS FTTH 关的时候用LOID
            // cmd.append("DEL-LANPORTVLAN::OLTID=").append(wo.getNeIp());
            // cmd.append(",PONID=").append("NA");
            // cmd.append("-").append(wo.getFrameId());
            // cmd.append("-").append(wo.getSlotId());
            // cmd.append("-").append(wo.getPortId());
            // cmd.append(",ONUIDTYPE=LOID");
            // cmd.append(",ONUID=").append(wo.getOntKey());
            // cmd.append(",ONUPORT=").append("NA-NA-NA-1");
            // cmd.append(":");
            // cmd.append(Ctag.getCtag());
            // cmd.append("::");
            // cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                        + rm.getEnDesc());
                // 删除业务流不成功,直接删除ONU
                // return new WoResult(rm);
            }

            // 如果最后一笔业务，注销 2012-11-29 直接删除ONU
            // if(this.hasNotPORTVLAN(wo.getNeIp(), wo.getFrameId(),
            // wo.getSlotId(), wo.getPortId(), wo.getOntKey())){
            // DEL-ONU::OLTID=172.16.240.149,PONID=NA-0-5-1:CTAG::ONUIDTYPE=LOID,ONUID=aaaaa12345678901;
            this.delJSOnu();
            // }

        } else {
            cmd.append("DEL-SERVICEPORT::DID=").append(did);
            cmd.append(",FN=").append(wo.getFrameId());
            cmd.append(",SN=").append(wo.getSlotId());
            cmd.append(",PN=").append(wo.getPortId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("VLANID=").append(wo.getSvlan());
            cmd.append(",ONTID=").append(wo.getOntId());
            if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
                cmd.append(",GEMPORTID=1");
            }
            cmd.append(",INNERVLANID=").append(wo.getCvlan());
            cmd.append(",UV=").append(dataUv);
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                        + rm.getEnDesc());
                return new WoResult(rm);
            }

            // 如果最后一笔业务，注销
            if (this.hasNotServiceport(did, wo.getFrameId(), wo.getSlotId(),
                    wo.getPortId(), wo.getOntId())) {
                this.delOnu();
            }
        }

        log.warn("hua wei ftth close lan");

        // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
        // return WoResult.rms_not_need_register;
        // }

        return WoResult.SUCCESS;
    }

    /**
     * close voip
     */
    public WoResult closeVoip() throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // VoiceVLANID 不能为空
        if (wo.getVoiceVLAN().intValue() == -1) {
            throw new ZtlException(ErrorConst.voicevlanNotBlank);
        }

        String did = CoreService.ticketControlService.getDeviceDid(
                wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
        if (StringUtils.isBlank(did)) {
            throw new ZtlException(ErrorConst.noSuchDid);
        }
        if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
            // DEL-PONVLAN::OLTID=10.71.62.138,PONID=NA-0-2-1,ONUIDTYPE=LOID,ONUID=3108shmi8240:CTAG::UV=42;
            cmd = new StringBuffer();
            cmd.append("DEL-PONVLAN::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("UV=").append("42");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                        + rm.getEnDesc());
                return new WoResult(rm);
            }

            // 如果最后一笔业务，注销
            if (this.hasNotServiceport(did, wo.getFrameId(), wo.getSlotId(),
                    wo.getPortId(), wo.getOntId())) {
                this.delJSOnu();
            }

        } else {

            cmd = new StringBuffer();
            cmd.append("DEL-SERVICEPORT::DID=").append(did);
            cmd.append(",FN=").append(wo.getFrameId());
            cmd.append(",SN=").append(wo.getSlotId());
            cmd.append(",PN=").append(wo.getPortId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("VLANID=").append(wo.getVoiceVLAN());
            cmd.append(",ONTID=").append(wo.getOntId());
            if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
                cmd.append(",GEMPORTID=2");
            }
            cmd.append(",UV=").append(voipUv);
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                        + rm.getEnDesc());
                return new WoResult(rm);
            }

            // 如果最后一笔业务，注销
            if (this.hasNotServiceport(did, wo.getFrameId(), wo.getSlotId(),
                    wo.getPortId(), wo.getOntId())) {
                this.delOnu();
            }

        }
        log.warn("hua wei ftth close voip");

        // if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
        // return WoResult.rms_not_need_register;
        // }

        return WoResult.SUCCESS;
    }

    /*
     * 开上网业务
     *
     * @see com.zoom.nos.provision.operations.IOperations#open()
     */
    public WoResult open() throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;
        WoResult _rw = null;
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
            // 增加TR069管理通道:
            // ADD-PONVLAN::OLTID=192.168.61.106,PONID=NA-0-3-0,ONUIDTYPE=LOID,ONUID=1234567890:CTAG::CVLAN=45,UV=45,CCOS=7;
            cmd = new StringBuffer();
            cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
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
            cmd.append(",CCOS=7");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                if (!"2689021790".equals(rm.getEn())
                        && !"1613561879".equals(rm.getEn())) {
                    return new WoResult(rm);
                }
            }

            // ADD-PONVLAN::OLTID=192.168.61.106,PONID=NA-0-3-0,ONUIDTYPE=LOID,ONUID=1234567890:CTAG::SVLAN=3800,CVLAN=2037,UV=2037,SCOS=0,CCOS=0

            cmd = new StringBuffer();
            cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=").append(wo.getSvlan());
            cmd.append(",CVLAN=").append(wo.getCvlan());
            cmd.append(",UV=").append(wo.getCvlan());
            cmd.append(",SCOS=0");
            cmd.append(",CCOS=0");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                return new WoResult(rm);
            }

            //iptv预埋: 接下来三条指令
//			ADD-PONVLAN::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid:ctag::SVLAN=IPTVsvlan,CVLAN=43,UV=43,SCOS=0,CCOS=0;//IPTV新增
            cmd = new StringBuffer();
            cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("SVLAN=").append(wo.getIptvvlan());
            cmd.append(",CVLAN=43");
            cmd.append(",UV=43");
            cmd.append(",SCOS=0");
            cmd.append(",CCOS=0");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                return new WoResult(rm);
            }

//			ADD-LANIPTVPORT::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid:ctag::UV=43,MVLAN=24;//IPTV新增
            cmd = new StringBuffer();
            cmd.append("ADD-LANIPTVPORT::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("UV=43");
            cmd.append(",MVLAN=24");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
                return new WoResult(rm);
            }

//			CFG-LANIPTVPORT::OLTID=oltip,PONID=ponid,ONUIDTYPE=LOID,ONUID=loid:ctag::;//IPTV新增
            cmd = new StringBuffer();
            cmd.append("CFG-LANIPTVPORT::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
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

        cmd = new StringBuffer();
        // 家庭网关连接类型ADD-PONVLAN::OLTID=172.16.240.149,PONID=NA-0-5-1,ONUIDTYPE=LOID,ONUID=aaaaa12345678901:CTAG::CVLAN=3154,UV=2132,SCOS=0,CCOS=0;

        // CFG-LANPORTVLAN::(OLTID=olt-name,PONID=ponport_location,ONUIDTYPE=onuid-type,ONUID=onuindex),
        // ONUPORT=onu-port:CTAG::[SVLAN=outer vlan],CVLAN=Inner
        // vlan[,UV=user vlan]
        // [,SCOS=outer qos][,CCOS=inner qos][,UPBW=up
        // bandwidth][,DOWNBW=down bandwidth]
        // [,DESC=service description];
        // 取速率模板
        wo.setUpOrDown("up");
        String upLineProfile = CoreService.ticketControlService
                .getLineProfileName(wo);
        if (StringUtils.isBlank(upLineProfile)) {
            throw new ZtlException(ErrorConst.lineProfileNoutFound);
        }

        wo.setUpOrDown("down");
        String downLineProfile = CoreService.ticketControlService
                .getLineProfileName(wo);
        if (StringUtils.isBlank(downLineProfile)) {
            throw new ZtlException(ErrorConst.lineProfileNoutFound);
        }
        // CFG-LANPORTVLAN::OLTID=172.16.240.149,PONID=NA-0-5-1,ONUIDTYPE=LOID,
        // ONUID=aaaaa12345678901,ONUPORT=NA-NA-NA-1:CTAG::SVLAN=3154,CVLAN=2132,UV=0,UPBW=0,CCOS=0;
        cmd.append("CFG-LANPORTVLAN::OLTID=").append(wo.getNeIp());
        cmd.append(",PONID=").append("NA");
        cmd.append("-").append(wo.getFrameId());
        cmd.append("-").append(wo.getSlotId());
        cmd.append("-").append(wo.getPortId());
        cmd.append(",ONUIDTYPE=LOID");
        cmd.append(",ONUID=").append(wo.getOntKey());
        cmd.append(",ONUPORT=NA-NA-NA-1");
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("SVLAN=").append(wo.getSvlan());
        cmd.append(",CVLAN=").append(wo.getCvlan());
        cmd.append(",UV=0");// ,SCOS=0,CCOS=0
        cmd.append(",UPBW=").append(upLineProfile);
        cmd.append(",DOWNBW=").append(downLineProfile);
        cmd.append(";");
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            // //1613561879 已经注册过了
            // log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn() +
            // rm.getEnDesc());
            // if ("2686058552".equals(rm.getEn()) ||
            // "IRAE".equals(rm.getEn())) {
            //
            // }
            // 开失败的时候,把ONU清理
            this.delJSOnu();
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;

    }

    /**
     * 建宽带的SERVICEPORT
     */
    private WoResult crtServiceport(String did) throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // INNERVLANID 不能为空（cvlan）
        if (wo.getCvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.cvlanNotBlank);
        }
        // VLANID 不能为空（svlan）
        if (wo.getSvlan().intValue() == -1) {
            throw new ZtlException(ErrorConst.svlanNotBlank);
        }
        // CRT-SERVICEPORT::DID=?,FN=?,SN=?,PN=?:ctag::VLANID=svlan,ONTID=?;
        // 设速率模板
        cmd = new StringBuffer();
        cmd.append("CRT-SERVICEPORT::DID=").append(did);
        cmd.append(",FN=").append(wo.getFrameId());
        cmd.append(",SN=").append(wo.getSlotId());
        cmd.append(",PN=").append(wo.getPortId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("VLANID=").append(wo.getSvlan());
        cmd.append(",ONTID=").append(wo.getOntId());
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            cmd.append(",GEMPORTID=1");
        }
        if (SystemFlag.getSystemFlag() != null
                && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {

        } else {
            // 取速率模板
            String lineProfile = CoreService.ticketControlService
                    .getLineProfileName(wo);
            if (StringUtils.isBlank(lineProfile)) {
                throw new ZtlException(ErrorConst.lineProfileNoutFound);
            }
            cmd.append(",TX=").append(lineProfile);
            cmd.append(",RX=").append(lineProfile);
        }
        cmd.append(",INNERVLANID=").append(wo.getCvlan());
        cmd.append(",UV=").append(dataUv);
        cmd.append(",TAGTRANSFORM=3");
        cmd.append(";");
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;
    }

    /**
     * 建语音的SERVICEPORT
     */
    private WoResult crtServiceportVoip(String did) throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // VoiceVLANID 不能为空
        if (wo.getVoiceVLAN().intValue() == -1) {
            throw new ZtlException(ErrorConst.voicevlanNotBlank);
        }

        cmd = new StringBuffer();
        cmd.append("CRT-SERVICEPORT::DID=").append(did);
        cmd.append(",FN=").append(wo.getFrameId());
        cmd.append(",SN=").append(wo.getSlotId());
        cmd.append(",PN=").append(wo.getPortId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("VLANID=").append(wo.getVoiceVLAN());
        cmd.append(",ONTID=").append(wo.getOntId());
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
            cmd.append(",GEMPORTID=2");
        }
        cmd.append(",TX=").append("HW-2M-VOICE");
        cmd.append(",RX=").append("HW-2M-VOICE");
        cmd.append(",UV=").append(voipUv);
        cmd.append(";");
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;
    }

    /**
     * 释放资源
     */
    public void destruction() {
        session.close();
    }

    // /**
    // *
    // * @param did
    // * @param vlanid
    // * @return
    // * @throws ZtlException
    // */
    // private WoResult createVlan(String did, int vlanid)throws ZtlException{
    // StringBuffer cmd = null;
    // HwTL1ResponseMessage rm = null;
    //
    // // add vlan
    // //ADD-VLAN::DID=123456:ctag::VLANID=100,VLANTYPE=SMART,VLANATTR=COMMON;
    // cmd = new StringBuffer();
    // cmd.append("ADD-VLAN::DID=").append(did);
    // cmd.append(":");
    // cmd.append(Ctag.getCtag());
    // cmd.append("::VLANID=").append(vlanid);
    // cmd.append(",VLANTYPE=SMART,VLANATTR=COMMON;");
    // rm = session.exeHwCmd(cmd.toString(), wo);
    // if (rm.isFailed()) {
    // log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
    // + rm.getEnDesc());
    // return new WoResult(rm);
    // }
    // return WoResult.SUCCESS;
    // }

    /**
     * 注册ONT
     */
    public WoResult registerOnu() throws ZtlException {

        log.debug("registerOnu ...");

        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;
        if (SystemFlag.getSystemFlag() != null
                && SystemFlag.getSystemFlag().equals(SystemFlag.JS_UNICOM)) {
            cmd = new StringBuffer();
            // ADD-ONU::OLTID=172.16.240.149,PONID=NA-0-5-1:1::AUTHTYPE=LOID,ONUID=aaaaa12345678901,ONUNO=33,NAME=test01,ONUTYPE=FE;
            // if ("192.168.61.106".equals(wo.getNeIp())) {
            String resourceCode = wo.getResourceCode();
            if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
                int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (64 - wo.getOntKey().length()));
                resourceCode = resourceCode.substring(resourceCode.length() - limit);
            } else if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_GPON) {
                int limit = com.zoom.nos.provision.util.StringUtils.getLimitLength(resourceCode, (128 - wo.getOntKey().length()));
                resourceCode = resourceCode.substring(resourceCode.length() - limit);
            }
            resourceCode = wo.getOntKey() + resourceCode;

            if (true) {
                if (StringUtils.isNotBlank(wo.getDeviceType())
                        && wo.getDeviceType().length() > 2
                        && !wo.getDeviceType().startsWith("0")
                        && (wo.getDeviceType().toLowerCase().charAt(2) == 'h')) {
                    hguFlag = true;
                    // ADD-ONU::OLTID=192.168.61.106,PONID=NA-0-3-0:100::AUTHTYPE=LOID,ONUID=1234567890,ONUNO=4,NAME=中太测试,ONUTYPE=GH2FE1POTS;
                    // new logic

                    cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append("NA");
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("AUTHTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUNO=").append(wo.getOntId());
                    cmd.append(",NAME=").append(resourceCode);
                    cmd.append(",DESC=").append(resourceCode);
                    cmd.append(",ONUTYPE=").append(
                            wo.getDeviceType().toUpperCase().substring(1));
                    // cmd.append(",ONUTYPE=hw-hg8120");
                } else {
                    // old logic
                    cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append("NA");
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("AUTHTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUNO=").append(wo.getOntId());
                    cmd.append(",NAME=").append(resourceCode);
                    cmd.append(",DESC=").append(resourceCode);
                    if (StringUtils.isNotBlank(wo.getDeviceType())) {
                        if (wo.getDeviceType().startsWith("0")) {
                            cmd.append(",ONUTYPE=").append(
                                    wo.getDeviceType().toUpperCase()
                                            .substring(1));
                        } else {
                            if (wo.getDeviceType().equalsIgnoreCase("HG8240")) {
                                cmd.append(",ONUTYPE=").append(
                                        wo.getDeviceType().toUpperCase());
                            } else {
                                cmd.append(",ONUTYPE=").append(
                                        wo.getDeviceType().toUpperCase()
                                                .substring(1));
                            }
                        }
                    } else {

                    }
                }
                cmd.append(";");
                rm = session.exeHwCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    // 1613561879 已经注册过了
                    log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                            + rm.getEnDesc());
                    if ("1613561879".equals(rm.getEn())
                            || "IRAE".equals(rm.getEn())
                            || rm.getEnDesc().indexOf("1613561879") > 0) {

                    } else {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                                + rm.getEnDesc());
                        return new WoResult(rm);
                    }
                }
                return WoResult.SUCCESS;

            } else {
                if (StringUtils.isNotBlank(wo.getDeviceType())
                        && wo.getDeviceType().length() > 2
                        && !wo.getDeviceType().startsWith("0")
                        && (wo.getDeviceType().toLowerCase().charAt(2) == 'h')) {
                    // new logic

                    cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append("NA");
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("AUTHTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUNO=").append(wo.getOntId());
                    cmd.append(",NAME=").append(wo.getResourceCode());
                    // cmd.append(",ONUTYPE=").append(wo.getDeviceType());
                    cmd.append(",ONUTYPE=hw-hg8120");
                } else {
                    // old logic
                    cmd.append("ADD-ONU::OLTID=").append(wo.getNeIp());
                    cmd.append(",PONID=").append("NA");
                    cmd.append("-").append(wo.getFrameId());
                    cmd.append("-").append(wo.getSlotId());
                    cmd.append("-").append(wo.getPortId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("AUTHTYPE=LOID");
                    cmd.append(",ONUID=").append(wo.getOntKey());
                    cmd.append(",ONUNO=").append(wo.getOntId());
                    cmd.append(",NAME=").append(wo.getResourceCode());
                    cmd.append(",ONUTYPE=").append("hw-hg8120");// wangsy
                    // if (StringUtils.isNotBlank(wo.getDeviceType())) {
                    // if (wo.getDeviceType().startsWith("0")) {
                    // cmd.append(",ONUTYPE=hw-").append(
                    // wo.getDeviceType().substring(1)
                    // .toLowerCase());
                    // } else {
                    // cmd.append(",ONUTYPE=").append(wo.getDeviceType());
                    // }
                    // } else {
                    //
                    // }
                }
                cmd.append(";");
                rm = session.exeHwCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    // 1613561879 已经注册过了
                    log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                            + rm.getEnDesc());
                    if ("1613561879".equals(rm.getEn())
                            || "IRAE".equals(rm.getEn())
                            || rm.getEnDesc().indexOf("1613561879") > 0) {

                    } else {
                        log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                                + rm.getEnDesc());
                        return new WoResult(rm);
                    }
                }
                return WoResult.SUCCESS;
            }
        } else {

            // 取DID
            String did = CoreService.ticketControlService.getDeviceDid(
                    wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
            if (StringUtils.isBlank(did)) {
                throw new ZtlException(ErrorConst.noSuchDid);
            }
            if (SystemFlag.getSystemFlag() != null
                    && SystemFlag.getSystemFlag().equals(SystemFlag.QH_UNICOM)) {
                int key = (int) (Math.random() * 9000 + 1000000000);
                wo.setOntKey(String.valueOf(key));
            }

            if (wo.getOntKey().length() > 10) {
                throw new ZtlException(ErrorConst.oidTooLength);
            }

            // 取ONT NAME
            String ontName = this.getOntName(did, wo.getFrameId(),
                    wo.getSlotId(), wo.getPortId(), wo.getOntId());
            // String ontKey = "";
            if (ontName == null) {
                // 没取到ont name，注册ont
                if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
                    // epon
                    cmd = new StringBuffer();
                    cmd.append("ADD-EPONONT::DID=").append(did);
                    cmd.append(",FN=").append(wo.getFrameId());
                    cmd.append(",SN=").append(wo.getSlotId());
                    cmd.append(",PN=").append(wo.getPortId());
                    cmd.append(",ONTID=").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("NAME=").append(wo.getResourceCode());
                    cmd.append(",LINEPROF=HW-F4P2");
                    cmd.append(",SRVPROF=HW-F4P2");
                    cmd.append(",AUTH=ALWAYS_ON");
                    cmd.append(",ONTKEY=").append(wo.getOntKey());
                    cmd.append(",VENDORID=HWTC");
                    cmd.append(",EQUIPMENTID=38353065");
                    cmd.append(",MAINSOFTVERSION=V100R001C03B069");
                    cmd.append(",BUILDTOPO=TRUE");
                    cmd.append(";");
                } else {
                    // GPON
                    cmd = new StringBuffer();
                    cmd.append("ADD-ONT::DID=").append(did);
                    cmd.append(",FN=").append(wo.getFrameId());
                    cmd.append(",SN=").append(wo.getSlotId());
                    cmd.append(",PN=").append(wo.getPortId());
                    cmd.append(",ONTID=").append(wo.getOntId());
                    cmd.append(":");
                    cmd.append(Ctag.getCtag());
                    cmd.append("::");
                    cmd.append("NAME=").append(wo.getResourceCode());
                    cmd.append(",LINEPROF=HW-F4P2");
                    cmd.append(",SRVPROF=HW-F4P2");
                    cmd.append(",AUTH=ALWAYS_ON");
                    cmd.append(",PWD=").append(wo.getOntKey());
                    cmd.append(",VENDORID=HWTC");
                    cmd.append(",EQUIPMENTID=4563686f4c6966653a484738353061");
                    cmd.append(",MAINSOFTVERSION=V1R1C04SPC003");
                    cmd.append(",BUILDTOPO=TRUE");
                    cmd.append(";");
                }

                rm = session.exeHwCmd(cmd.toString(), wo);
                if (rm.isFailed()) {
                    // 注册失败，再查一次
                    String ontName2 = this.getOntName(did, wo.getFrameId(),
                            wo.getSlotId(), wo.getPortId(), wo.getOntId());
                    if (ontName2 != null) {
                        // ontKey =
                        // ontName2.substring(ontName2.indexOf("@@@@")+4,ontName2.length());
                        // ontName2 =
                        // ontName2.substring(0,ontName2.indexOf("@@@@"));
                        // && ontKey.equals(wo.getOntKey())
                        // equal ont OntKey:[" + ontKey + "]
                        // or Different ont OntKey:[" + ontKey + "]
                        // ont 已存在
                        if (ontName2.equals(wo.getResourceCode())) {
                            // ont存在，名字相同
                            log.debug("equal ont name:[" + ontName2 + "]");
                            return WoResult.SUCCESS;
                        } else {
                            // ont name不同，失败
                            log.debug("Different ont name:[" + ontName2 + "] ");
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
                // ontKey =
                // ontName.substring(ontName.indexOf("@@@@")+4,ontName.length());
                // ontName = ontName.substring(0,ontName.indexOf("@@@@"));
                // && ontKey.equals(wo.getOntKey())
                // equal ont OntKey:[" + ontKey + "]
                // or Different ont OntKey:[" + ontKey + "]
                // ont 已存在
                if (ontName.equals(wo.getResourceCode())) {
                    // ont存在，名字相同
                    log.debug("equal ont name:[" + ontName + "]");
                    return WoResult.SUCCESS;
                } else {
                    // ont name不同，失败
                    log.debug("Different ont name:[" + ontName + "] ");
                    throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
                }
            }
        }
        // if (rm.isFailed()) {
        // log.debug(wo.getConfigWoId()+"-failed:"+rm.getEn()+rm.getEnDesc());
        //
        // if ("2689014743".equals(rm.getEn())) {
        // // 2689014743 ONT ID已经存在
        // //判断ONT NAME是否相等
        // String ontName=this.getOntName(did, wo.getFrameId(), wo.getSlotId(),
        // wo.getPortId(), wo.getOntId());
        // if (ontName == null) {
        // //取ont name失败
        // throw new ZtlException(ErrorConst.getOntNameFailed);
        // }
        // if (ontName.equals(wo.getResourceCode())) {
        // log.debug("equal ont name:"+ontName);
        // return WoResult.SUCCESS;
        // } else {
        // //ont name不同，失败
        // log.debug("Different ont name:"+ontName);
        // throw new ZtlException(ErrorConst.repeatOntIdOnDevAdmin);
        // }
        // }
        // return new WoResult(rm);
        // }
        // return WoResult.SUCCESS;
    }

    /**
     * 注销ONT
     */
    public WoResult delOnu() throws ZtlException {

        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // 取DID
        String did = CoreService.ticketControlService.getDeviceDid(
                wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
        if (StringUtils.isBlank(did)) {
            throw new ZtlException(ErrorConst.noSuchDid);
        }
        // 注销
        // DEL-EPONONT::DEV=10.71.62.38,FN=0,SN=1,PN=0,ONTID=2:1::;
        cmd = new StringBuffer();
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            cmd.append("DEL-EPONONT::DID=").append(did);
        } else {
            cmd.append("DEL-ONT::DID=").append(did);
        }
        cmd.append(",FN=").append(wo.getFrameId());
        cmd.append(",SN=").append(wo.getSlotId());
        cmd.append(",PN=").append(wo.getPortId());
        cmd.append(",ONTID=").append(wo.getOntId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::;");

        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;
    }

    /**
     * JS 注销ONT
     */
    public WoResult delJSOnu() throws ZtlException {

        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;
        // 注销
        // JS
        if (wo.getRmsFlag() != null && wo.getRmsFlag() == 1) {
            // DEL-ONU::OLTID=192.168.61.106,PONID=NA-0-3-0:CTAG::ONUIDTYPE=LOID,ONUID=1234567890;
            cmd = new StringBuffer();
            cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=NA-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntId());
            cmd.append(";");
        } else {
            // DEL-ONU::OLTID=172.16.240.149,PONID=NA-0-5-1:CTAG::ONUIDTYPE=LOID,ONUID=aaaaa12345678901;
            // DEL-ONU::OLTID=172.16.240.149,PONID=NA-0-5-1:CTAG::ONUIDTYPE=ONU_NUMBER,ONUID=3;
            cmd = new StringBuffer();
            cmd.append("DEL-ONU::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=NA-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::ONUIDTYPE=ONU_NUMBER");
            cmd.append(",ONUID=").append(wo.getOntId());
            cmd.append(";");
        }
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;
    }

    /**
     * 开语音
     */
    public WoResult openVoip() throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // 取DID
        String did = CoreService.ticketControlService.getDeviceDid(
                wo.getTl1ServerIp(), wo.getNeIp(), "0" + wo.getCityId());
        if (StringUtils.isBlank(did)) {
            throw new ZtlException(ErrorConst.noSuchDid);
        }

        // 验证SbcIp、sbcIpReserve 不为空
        if (StringUtils.isBlank(wo.getSbcIp())) {
            throw new ZtlException(ErrorConst.sbcIpNotBlank);
        }
        if (StringUtils.isBlank(wo.getSbcIpReserve())) {
            throw new ZtlException(ErrorConst.sbcIpReserveNotBlank);
        }
        String vaprofile = CoreService.ticketControlService.getFtthProfileName(
                wo, WorkOrder.LINETYPE_VAPROFILE);
        if (StringUtils.isBlank(vaprofile)) {
            throw new ZtlException(ErrorConst.lineSrvProfNoutFound);
        }

        // epon修改增值业务模板名 850eV1R1C03_SY0102 //modify by 2012.5.7模板名修改为录入什么就执行什么
        // vaprofile = StringUtils.substringAfter(vaprofile, "_");

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
            // 语音开通：
            // ADD-PONVLAN::OLTID=10.167.223.37,PONID=NA-0-7-1,ONUIDTYPE=LOID,ONUID=1234567890:CTAG::CVLAN=42,UV=42,CCOS=5;
            cmd = new StringBuffer();
            cmd.append("ADD-PONVLAN::OLTID=").append(wo.getNeIp());
            cmd.append(",PONID=").append("NA");
            cmd.append("-").append(wo.getFrameId());
            cmd.append("-").append(wo.getSlotId());
            cmd.append("-").append(wo.getPortId());
            cmd.append(",ONUIDTYPE=LOID");
            cmd.append(",ONUID=").append(wo.getOntKey());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("CVLAN=42");
            cmd.append(",UV=42");
            cmd.append(",CCOS=5");
            cmd.append(";");
            rm = session.exeHwCmd(cmd.toString(), wo);
            if (rm.isFailed()) {
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

        // 语音的改注册信息，加VAPROFILE
        // MOD-ONT::DEV=MA5680T实验设备,FN=0,SN=2,PN=0,ONTID=0:1::VENDORID=HWTC,
        // EQUIPMENTID=323435,MAINSOFTVERSION=V1R002C01S201,VAPROFILE=245V1R2C01_test;
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            // epon
            cmd = new StringBuffer();
            cmd.append("MOD-EPONONT::DID=").append(did);
            cmd.append(",FN=").append(wo.getFrameId());
            cmd.append(",SN=").append(wo.getSlotId());
            cmd.append(",PN=").append(wo.getPortId());
            cmd.append(",ONTID=").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("VENDORID=HWTC");
            cmd.append(",EQUIPMENTID=38353065");
            cmd.append(",MAINSOFTVERSION=V100R001C03B069");
            // cmd.append(",VAPROFILE=850eV1R1C03_").append(vaprofile);
            cmd.append(",VAPROFILE=").append(vaprofile);
            cmd.append(";");
        } else {
            // GPON
            // EQUIPMENTID=323430,MAINSOFTVERSION=V1R002C00,VAPROFILE=240V1R2C00_SY0102
            cmd = new StringBuffer();
            cmd.append("MOD-ONT::DID=").append(did);
            cmd.append(",FN=").append(wo.getFrameId());
            cmd.append(",SN=").append(wo.getSlotId());
            cmd.append(",PN=").append(wo.getPortId());
            cmd.append(",ONTID=").append(wo.getOntId());
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            // cmd.append("VAPROFILE=240V1R2C00_").append(vaprofile); 临时修改
            cmd.append("VAPROFILE=").append(vaprofile);
            cmd.append(",VENDORID=HWTC");
            cmd.append(",EQUIPMENTID=323430");
            cmd.append(",MAINSOFTVERSION=V1R002C00");
            cmd.append(";");
        }
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }

        // Create service port
        WoResult _rw = this.crtServiceportVoip(did);
        // //1616637953 Service Port已存在
        // //2689016452 业务虚端口已经存在
        // if ("1616637953".equals(_rw.getCode()) ||
        // "2689016452".equals(_rw.getCode())) {
        // // 删除serviceport
        // log.debug(wo.getConfigWoId() + "-del voip serviceport.");
        // this.closeVoip();
        //
        // // 删除serviceport后，再执行一次
        // _rw = this.crtServiceportVoip(did);
        // }
        if (!WoResult.SUCCESS.equals(_rw)) {
            return _rw; // 失败，退出
        }

        // CFG-ONTVAINDIV
        int voicePort = -1;
        try {
            voicePort = Integer.parseInt(wo.getTid()) + 1;
        } catch (Exception e) {
            throw new ZtlException(ErrorConst.tidNeedNumber);
        }
        cmd = new StringBuffer();
        cmd.append("CFG-ONTVAINDIV::DID=").append(did);
        cmd.append(",FN=").append(wo.getFrameId());
        cmd.append(",SN=").append(wo.getSlotId());
        cmd.append(",PN=").append(wo.getPortId());
        cmd.append(",ONTID=").append(wo.getOntId());
        cmd.append(",H248TID_").append(voicePort).append("=A")
                .append(wo.getTid());
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            cmd.append(",H248RTPTID_").append(voicePort)
                    .append("=rtp/100000000").append(voicePort);
        }
        cmd.append(",WANIPADDR_1=").append(wo.getIadip());
        cmd.append(",WANSUBMASK_1=").append(wo.getIadipMask());
        cmd.append(",WANGW_1=").append(wo.getIadipGateway());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        rm = session.exeHwCmd(cmd.toString(), wo);
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            return new WoResult(rm);
        }
        return WoResult.SUCCESS;
    }

    /**
     * @param did
     * @param fn
     * @param sn
     * @param pn
     * @param ontId
     * @return
     * @throws ZtlException
     */
    private String getOntName(String did, String fn, Short sn, Integer pn,
                              String ontId) throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;
        String ontName = "";// ontKey = "";

        cmd = new StringBuffer();
        if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
            // epon
            cmd.append("LST-EPONONT::DID=").append(did);
        } else {
            // GPON
            cmd.append("LST-ONTDETAIL::DID=").append(did);
        }
        cmd.append(",FN=").append(fn);
        cmd.append(",SN=").append(sn);
        cmd.append(",PN=").append(pn);
        cmd.append(",ONTID=").append(ontId);
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeHwListCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            return null;
        }

        ontName = rm.getResult().get("NAME");
        // if (wo.getPonLineType().shortValue() == WorkOrder.PONLINETYPE_EPON) {
        // ontKey = rm.getResult().get("ONTKEY");
        // log.info("hw ontkey="+ontKey);
        // } else {
        // ontKey = rm.getResult().get("PWD");
        // }
        //
        // if (ontKey != null && ontKey.compareTo("") > 0) {
        // ontName += "@@@@" + ontKey;
        // }

        return ontName;
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
     * 多业务,同是开宽带和IPTV
     */
    public WoResult openWBIptv() throws ZtlException {
        WoResult open = open();
        if (!"success".equals(open.getCode())) {
            return open;
        }
        WoResult iptv = openIptv();
        if (!"success".equals(iptv.getCode())) {
            return iptv;
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
     * 多业务,同是关宽带和IPTV
     */
    public WoResult closeWBIptv() throws ZtlException {

        WoResult iptv = closeIptv();
        if (!"success".equals(iptv.getCode())) {
            return iptv;
        }
        WoResult close = close();
        if (!"success".equals(close.getCode())) {
            return close;
        }
        return WoResult.SUCCESS;
    }

    /**
     * @param did
     * @param fn
     * @param sn
     * @param pn
     * @param ontId
     * @return
     * @throws ZtlException
     */
    private boolean hasNotServiceport(String did, String fn, Short sn,
                                      Integer pn, String ontId) throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;

        // LST-SERVICEPORT::DID=7369351,FN=0,SN=1,PN=1:CTAG::ONTID=10;
        cmd = new StringBuffer();
        cmd.append("LST-SERVICEPORT::DID=").append(did);
        cmd.append(",FN=").append(fn);
        cmd.append(",SN=").append(sn);
        cmd.append(",PN=").append(pn);
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append("ONTID=").append(ontId);
        cmd.append(";");
        try {
            rm = session.exeHwCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            return false;
        }
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            if ("2686058552".equals(rm.getEn())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @param oltID
     * @param fn
     * @param sn
     * @param pn
     * @param ontkey
     * @return
     * @throws ZtlException
     */
    private boolean hasNotPORTVLAN(String oltID, Short fn, Short sn, Short pn,
                                   String ontkey) throws ZtlException {
        StringBuffer cmd = null;
        HwTL1ResponseMessage rm = null;
        // LST-PORTVLAN::OLTID=10.71.212.190,PONID=NA-0-4-1,ONUIDTYPE=LOID,ONUID=aaaaaaa,ONUPORT=NA-NA-NA-1:CTAG::;
        cmd = new StringBuffer();
        cmd.append("LST-PORTVLAN::OLTID=").append(oltID);
        cmd.append(",PONID=NA");
        cmd.append("-").append(fn);
        cmd.append("-").append(sn);
        cmd.append("-").append(pn);
        cmd.append(",ONUIDTYPE=LOID,ONUID=").append(ontkey);
        cmd.append(",ONUPORT=NA-NA-NA-1");
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        cmd.append(";");
        try {
            rm = session.exeHwCmd(cmd.toString(), wo);
        } catch (ZtlException e) {
            log.debug("get ontName failed:" + e.getErrorcode() + ","
                    + e.getMessage());
            return false;
        }
        if (rm.isFailed()) {
            log.debug(wo.getConfigWoId() + "-failed:" + rm.getEn()
                    + rm.getEnDesc());
            if ("SEOF".equals(rm.getEn())) { // En:SEOF 2686058576
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}