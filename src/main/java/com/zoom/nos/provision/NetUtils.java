package com.zoom.nos.provision;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 网络工具程序
 * @author zm
 *
 */
public class NetUtils {

	/**
	 * 检测目标Tcp端口是否处于服务状态
	 * 
	 * @param port
	 * @return 处于服务状态true; 反之false
	 */
	public static boolean testServerPort(String ip, int port) {
		Socket socket = null;
		try {
			socket = new Socket(InetAddress.getByName(ip), port);
			// 目标端口处于服务状态
			return true;
		} catch (IOException e) {
			// 目标端口已关闭
			return false;
		} finally {
			// socket关闭
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
