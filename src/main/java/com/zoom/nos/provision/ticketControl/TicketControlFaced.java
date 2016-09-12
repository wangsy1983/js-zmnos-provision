package com.zoom.nos.provision.ticketControl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
//import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.core.CoreService;

public class TicketControlFaced {
	private static Logger log = LoggerFactory
			.getLogger(TicketControlFaced.class);

	// 工单控制服务器ServerID
	private static final int TICKET_CONTROL_SERVER_ID = 81;

	private static String ticketControlIP = "";

	private TicketControlFaced() {
	}

	/**
	 * 发送工单人工回笼消息
	 * 
	 */
	public static void sendMsgHuilong() {
		//如果ticketControlIP是空的，从数据库里去
		if (StringUtils.isBlank(ticketControlIP)) {
			log.debug("get ticketControlIP");
			ticketControlIP = CoreService.ticketControlService
					.getServerIP(TICKET_CONTROL_SERVER_ID);
		}
		log.debug("发送有可回笼工单消息,To:" + ticketControlIP);
		String msgBody = "2";
		int Port = 3081;
		int bodyLen = msgBody.getBytes().length;
		byte[] msg = new byte[bodyLen + 12];
		// head
		msg[0] = 0;
		msg[1] = 0;
		msg[2] = 84;
		msg[3] = 1;
		msg[4] = 81;
		msg[5] = 1;
		msg[6] = 1;
		msg[7] = 0;
		msg[8] = (byte) ((bodyLen >> 24) & 0xff);
		msg[9] = (byte) ((bodyLen >> 16) & 0xff);
		msg[10] = (byte) ((bodyLen >> 8) & 0xff);
		msg[11] = (byte) (bodyLen & 0xff);
		// body
		for (int i = 12; i < msg.length; i++) {
			msg[i] = (byte) (msgBody.charAt(i - 12));
		}

		// send
		try {
			DatagramSocket ds = new DatagramSocket();
			InetAddress ip = InetAddress.getByName(ticketControlIP);
			DatagramPacket dp = new DatagramPacket(msg, msg.length, ip, Port);
			ds.send(dp);
		} catch (Exception e) {
			// 失败就失败吧，没关系，后面的工单还会发
			log.error("发回笼送消息到：" + ticketControlIP + "失败");
			log.error(e.getMessage(), e);
		}
	}
 	
	/**
	 * 发送消息给RMS处理的webservice
	 * 
	 */
	public static void sendMsg2RMSWebService(String woId,String woType,String buzType,String needRegister) {
		
		//如果ticketControlIP是空的，从数据库里去
		if (StringUtils.isBlank(ticketControlIP)) {
			ticketControlIP = CoreService.ticketControlService.getServerIP(TICKET_CONTROL_SERVER_ID);
		}
		int port = 3088;
		Socket socket = null;
		DataInputStream dataIn = null;
		DataOutputStream dataOut = null;
		byte[] buffer = new byte[12];
		System.out.println("ip="+ticketControlIP);
		System.out.println("port="+port);
		byte[] msg =null;
		try {
//			ticketControlIP = "127.0.0.1";
			//如果ticketControlIP是空的，从数据库里去
			if (StringUtils.isBlank(ticketControlIP)) {
				log.debug("get ticketControlIP");
				ticketControlIP = CoreService.ticketControlService.getServerIP(TICKET_CONTROL_SERVER_ID);
			}
//			log.info("----------ticketControlIP="+ticketControlIP);
			socket = new Socket(ticketControlIP, port);
			socket.setSoTimeout(60 * 1000);
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = new DataOutputStream(socket.getOutputStream());
			String msgBody = "5|" + needRegister + "|" + woType + "|" + buzType + "|" + woId;
			log.info("msgBody=" + msgBody);
			int bodyLen = msgBody.getBytes().length;
			msg = new byte[bodyLen + 12];
			
			// head
			msg[0] = 0;
			msg[1] = 0;
			msg[2] = 87;
			msg[3] = 1;
			msg[4] = 14;
			msg[5] = 1;
			msg[6] = 1;
			msg[7] = 0;
			msg[8] = (byte) ((bodyLen >> 24) & 0xff);
			msg[9] = (byte) ((bodyLen >> 16) & 0xff);
			msg[10] = (byte) ((bodyLen >> 8) & 0xff);
			msg[11] = (byte) (bodyLen & 0xff); 
			// body
			byte[] hostIpBody = msgBody.getBytes();
			for (int i = 12; i < hostIpBody.length + 12; i++) {
				msg[i] = hostIpBody[i - 12];
			}
//			打印
//			for(int i = 0;i<msg.length;i++){
//				System.out.println("\n get["+i+"]="+msg[i]);
//			}
			
			int remain = dataIn.available();
			if (remain > 0) {
				byte[] tmp = new byte[remain];
				dataIn.readFully(tmp);
			}
			// 发送
			dataOut.write(msg);
			dataOut.flush();
			// 接收返回值
			while (true) {
				dataIn.read(buffer);
				int len = ((buffer[8] & 0xff) << 24)
						| ((buffer[9] & 0xff) << 16)
						| ((buffer[10] & 0xff) << 8) | (buffer[11] & 0xff);
				byte[] data = new byte[100];
				dataIn.read(data);
				log.info(new String(data, "GB2312"));
				break;
			}
		} catch (Exception e) {
//			// 发送
//			try {
//				dataOut.write(msg);
//				dataOut.flush();
//				// 接收返回值
//				while (true) {
//					dataIn.read(buffer);
//					int len = ((buffer[8] & 0xff) << 24)
//							| ((buffer[9] & 0xff) << 16)
//							| ((buffer[10] & 0xff) << 8) | (buffer[11] & 0xff);
//					byte[] data = new byte[100];
//					dataIn.read(data);
//	//				result = new String(data, "GB2312");
//					log.info(new String(data, "GB2312"));
//					break;
//				}
//			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//			}
		} finally {
			try {
				if (dataIn != null) {
					dataIn.close();
					dataIn = null;
				}
				if (dataOut != null) {
					dataOut.close();
					dataOut = null;
				}
				if (socket != null) {
					socket.close();
					socket = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 * JS
	 * 把处理结束的工单发送给主控程序,由主控程序通过webservice接口发送给BSS系统
	 * @param soId
	 */
	public static void sendMsgToControl(String configWoId,String ontKey,String exeResult) {
		//如果ticketControlIP是空的，从数据库里去
		if (StringUtils.isBlank(ticketControlIP)) {
			log.debug("get ticketControlIP");
			ticketControlIP = CoreService.ticketControlService.getServerIP(TICKET_CONTROL_SERVER_ID);
		}
		//	log.debug("发送处理结束的工单消息给主控程序
        //  由主控程序通过webservice接口发送给BSS厂家,To:" + ticketControlIP); 
		int Port = 3081; 
		String sendStr = "&OI*-9"+configWoId+"---"+exeResult;
        byte[] sendBuf; 
        sendBuf = sendStr.getBytes();
		// send
		try {
			DatagramSocket ds = new DatagramSocket();
			InetAddress ip = InetAddress.getByName(ticketControlIP);
			DatagramPacket dp = new DatagramPacket(sendBuf, sendBuf.length, ip, Port);
			String recvStr = new String(dp.getData() , 0 ,dp.getLength());
//			System.out.println("\n f recvStr=="+recvStr);
			ds.send(dp);
		} catch (Exception e) {
			//如果失败再发一次
			DatagramSocket ds;
			try {
				ds = new DatagramSocket();
				InetAddress ip = InetAddress.getByName(ticketControlIP);
				DatagramPacket dp = new DatagramPacket(sendBuf, sendBuf.length, ip, Port);
				String recvStr = new String(dp.getData() , 0 ,dp.getLength());
				System.out.println("\n 发送消息给:"+ticketControlIP+ ",消息内容是:"+recvStr);
				ds.send(dp);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (UnknownHostException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
		}
	}
}
