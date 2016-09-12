// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 2012/9/24 14:35:09
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   AlcatelTest.java

package com.zoom.nos.provision.operations;

import com.zoom.nos.provision.NosEnv;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.entity.WorkOrder;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.ticketControl.service.TicketControlService;
import com.zoom.nos.provision.tl1.message.AlcatelTL1ResponseMessage;
import com.zoom.nos.provision.tl1.session.AlcatelTl1Session;
import com.zoom.nos.provision.tl1.session.Ctag;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlcatelTest
{

    private AlcatelTl1Session session;
    private static Logger log = LoggerFactory.getLogger(AlcatelTest.class);
    int success;
    int failed;
    public AlcatelTest()
        throws ZtlException
    {
        session = null;
        success = 0;
        failed = 0;
        session = new AlcatelTl1Session("221.6.70.197", 12015, "", 0, "manager", "123456", NosEnv.socket_timeout_tl1server);
        session.open();
    }

    public void open()
        throws ZtlException
    {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;
        List workOrderList = CoreService.ticketControlService.getAlcatelShouZhouTest();
        long start = System.currentTimeMillis();
        for(Iterator iterator = workOrderList.iterator(); iterator.hasNext(); success++)
        {
            WorkOrder workorder = (WorkOrder)iterator.next();
            if(workorder.getCvlan() == null)
                throw new ZtlException(2016L);
            if(workorder.getSvlan() == null)
                throw new ZtlException(2015L);
            registerOnu(workorder);
            cmd = new StringBuffer();
            cmd.append("CRT-HSI:OLTIP=").append(workorder.getNeIp());
            cmd.append(":HSIPORT-1-1");
            cmd.append("-").append(workorder.getSlotId());
            cmd.append("-").append(workorder.getPortId());
            cmd.append("-").append(workorder.getOntId());
            cmd.append("-1-1");
            cmd.append(":");
            cmd.append(Ctag.getCtag());
            cmd.append("::");
            cmd.append("FLOW=1");
            cmd.append(",BWPROFUP=").append("2M");
            cmd.append(",BWPROFDN=").append("2M");
            cmd.append(",SVLAN=").append(workorder.getSvlan());
            cmd.append(",CVLAN=").append(workorder.getCvlan());
            cmd.append(";");
            rm = session.exeAlcatelCmd(cmd.toString(), null);
            if(!rm.isFailed())
                continue;
            failed++;
            log.debug((new StringBuilder(" cmd:")).append(cmd).append(" failed:").append(rm.getEn()).append(rm.getEnDesc()).toString());
            break;
        }

        long end = System.currentTimeMillis();
        System.out.println((new StringBuilder("\n \u5DE5\u5355\u4E00\u5171:")).append(workOrderList.size()).append(" \u6761,\u5E73\u5747\u6BCF\u6761\u8017\u65F6:").append((end - start) / (long)workOrderList.size() / 1000L).append("\u79D2").toString());
    }

    public boolean registerOnu(WorkOrder workorder)
        throws ZtlException
    {
        StringBuffer cmd = null;
        AlcatelTL1ResponseMessage rm = null;
        if(workorder.getIadipGateway() == null && workorder.getIadipMask() == null && workorder.getOriginWoUuid() == null)
        {
            System.out.println("\n \u672A\u586B\u5199\u4EFB\u4F55\u4E00\u79CD\u6CE8\u518C\u65B9\u5F0F\u6216\u7F3A\u5C11\u5FC5\u8981\u53C2\u6570!");
            return false;
        }
        cmd = new StringBuffer();
        cmd.append("ADD-ONT:OLTIP=").append(workorder.getNeIp());
        cmd.append(":ONT-1-1");
        cmd.append("-").append(workorder.getSlotId());
        cmd.append("-").append(workorder.getPortId());
        cmd.append("-").append(workorder.getOntId());
        cmd.append(":");
        cmd.append(Ctag.getCtag());
        cmd.append("::");
        if(workorder.getOriginWoUuid() != null && workorder.getOriginWoUuid().compareTo(" ") > 0)
            cmd.append("ONTTYPE=I240E-Q");
        else
            cmd.append("ONTTYPE=I-240E-P");
        cmd.append(",ONTNAME=").append(workorder.getResourceCode());
        cmd.append(",PONTYPE=GPON");
        if(workorder.getOriginWoUuid() != null && workorder.getOriginWoUuid().compareTo(" ") > 0)
            cmd.append(",LOID=").append(workorder.getOriginWoUuid());
        else
        if(workorder.getIadipGateway() != null && workorder.getIadipGateway().compareTo(" ") > 0)
            cmd.append(",MACADDR=").append(workorder.getIadipGateway());
        else
        if(workorder.getIadipMask() != null && workorder.getIadipMask().compareTo(" ") > 0)
            cmd.append(",SERNUM=").append(workorder.getIadipMask());
        cmd.append(";");
        rm = session.exeAlcatelCmd(cmd.toString(), null);
        if(rm.isFailed())
        {
            log.debug((new StringBuilder(" cmd:")).append(cmd).append(" failed:").append(rm.getEn()).append(rm.getEnDesc()).toString());
            return false;
        } else
        {
            return true;
        }
    }

    public static void main(String args[])
    {
        try
        {
            AlcatelTest alcatel = new AlcatelTest();
            alcatel.open();
        }
        catch(ZtlException e)
        {
            e.printStackTrace();
        }
    }


}