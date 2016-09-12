package com.zoom.nos.provision;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * ���繤�߳���
 * @author zm
 *
 */
public class NetUtils {

	/**
	 * ���Ŀ��Tcp�˿��Ƿ��ڷ���״̬
	 * 
	 * @param port
	 * @return ���ڷ���״̬true; ��֮false
	 */
	public static boolean testServerPort(String ip, int port) {
		Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(ip), port);
			// Ŀ��˿ڴ��ڷ���״̬
			return true;
		} catch (IOException e) {
			// Ŀ��˿��ѹر�
			return false;
		} finally {
			// socket�ر�
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * test main
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String ip = "192.168.0.201";
		int port = 22;
		System.out.println(ip + " " + port + " : "
				+ (NetUtils.testServerPort(ip, port) ? "open" : "close"));

	}

}
