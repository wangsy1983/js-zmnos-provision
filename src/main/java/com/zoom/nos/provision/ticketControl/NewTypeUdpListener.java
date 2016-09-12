package com.zoom.nos.provision.ticketControl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.core.CoreService;

/**
 * 新系统的UDP监听，借用了SNMP的端口
 * @author zm
 *
 */
public class NewTypeUdpListener  implements Runnable{
	private static Logger log = LoggerFactory
	.getLogger(NewTypeUdpListener.class);

	// SNMP工单配置服务器ServerID
	private static final int SERVER_ID = 82;

	// 监听端口号,snmp工单
	private int portSnmpWo = 3082;

	private String area;

	private List salvList;
	
	private CoreService coreService;

	/**
	 * 
	 * @param area
	 * @param salvList
	 *            为空或大小为0，是主采
	 * @param woProducer
	 */
	public NewTypeUdpListener(String area, List salvList,
			CoreService coreService) {
		this.area = area;
		this.salvList = salvList;

		this.coreService = coreService;

		/*
		 * get SNMP工单配置服务器 监听端口
		 */
		portSnmpWo = CoreService.ticketControlService.getServerPort(SERVER_ID);
	}

	//启动线程监听工单到来
	public void run() {
		try {
			log.info("star snmp type ticket process Listener");
			DatagramSocket server_2 = new DatagramSocket(portSnmpWo);
			byte[] buffer = new byte[16];
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			while (true) {
				server_2.receive(dp);

				// snmp开通方式
				processUdpMsgSnmpWo(buffer);
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}

	}

	/**
	 * 处理接受到的udp消息（snmp工单的）
	 * 
	 * @param data
	 * @param remoteIp
	 * @author zhuming
	 */
	private final void processUdpMsgSnmpWo(byte[] data) {
		int len = ((data[8] & 0xff) << 24) | ((data[9] & 0xff) << 16)
				| ((data[10] & 0xff) << 8) | (data[11] & 0xff);
		byte[] temp = new byte[len];
		for (int i = 12; i < 12 + len; i++) {
			temp[i - 12] = data[i];
		}
		String msgBody = new String(temp);
		if (msgBody.equals("1")) {
			if (salvList == null && salvList.size() == 0) {
				// 如果是主采则向各从采发消息
				log.info("主采集机收到客户端消息向各从采集机发送消息(snmp)");
				for (int s = 0; s < salvList.size(); s++) {
					sendMsg((String) salvList.get(s));
				}
			} else {
				// 如果是从采，做snmp工单采则
				try {
					log.info("从采集机接收到snmp/pon开通消息");
					// 从主DB中取工单放入本地DB
					coreService.receiveWo();
				} catch (Exception e) {
					log.error(e.getMessage(),e);
				}
			}
		}
	}
	
	/**
	 * 向从采集机发送消息
	 * @param slaveMachineIP
	 */
	private void sendMsg(String slaveMachineIP) { 
		log.info("发送消息到从采："+slaveMachineIP);
        String msgBody = "1";
        int bodyLen = msgBody.getBytes().length;
        byte[] msg = new byte[bodyLen + 12];
        // head
        msg[0] = 0;
        msg[1] = 0;
        msg[2] = 9;
        msg[3] = 1;
        msg[4] = 84;
        msg[5] = 1;
        msg[6] = 1;
        msg[7] = 0;
        msg[8] = (byte) ((bodyLen >> 24) & 0xff);
        msg[9] = (byte) ((bodyLen >> 16) & 0xff);
        msg[10] = (byte)((bodyLen >> 8) & 0xff);
        msg[11] = (byte)(bodyLen & 0xff);
        // body
        for (int i = 12; i < msg.length; i++) {
            msg[i] = (byte) (msgBody.charAt(i - 12));
        }      
        try {
        	DatagramSocket ds = new DatagramSocket();
        	InetAddress ip = InetAddress.getByName(slaveMachineIP);
        	DatagramPacket dp = new DatagramPacket(msg, msg.length, ip, portSnmpWo);
        	ds.send(dp); 
        } catch (Exception e) {
        	log.error("发送消息到从采："+slaveMachineIP+"失败");
            e.printStackTrace();
        } 
    }

}
