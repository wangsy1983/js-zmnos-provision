package com.zoom.nos.provision.tl1.session;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zoom.nos.provision.ErrorConst;
import com.zoom.nos.provision.core.CoreService;
import com.zoom.nos.provision.exception.ZtlException;
import com.zoom.nos.provision.tl1.message.TL1ResponseMessage;

public abstract class Tl1Session {

	private static Logger log = LoggerFactory.getLogger(Tl1Session.class);

	// tl1 server ip
	private String serverIp = "";

	// tl1 server port
	private int serverPort = 0;

	// 超时时间(毫秒)：默认120秒
	private int timeout = 120000;

	// local IP address
	private String localIp = "";
	// local port
	private int localPort = 20001;

	// user
	private String user = "";
	// password
	private String password = "";

	// client socket
	private Socket socket = null;

	// <cr><lf>;
	public final String endString = TL1ResponseMessage.Terminator;

	/**
	 * 
	 * @param serverIp
	 * @param serverPort
	 * @param localIp
	 * @param localPort
	 * @param user
	 * @param password
	 * @param timeout
	 *            毫秒为单位
	 */
	public Tl1Session(String serverIp, int serverPort, String localIp,
			int localPort, String user, String password, int timeout) {
		this.serverIp = serverIp;
		this.serverPort = serverPort;
		this.localIp = localIp;
		this.localPort = localPort;
		this.user = user;
		this.password = password;
		this.timeout = timeout;
	}

	/**
	 * open session
	 * 
	 * @throws ZtlExeException
	 */
	public void open() throws ZtlException {
		if (socket != null) {
			try {
				this.close();
				socket = null;
			} catch (Exception e) {
			}
		}
		try {
			// 连接 tl1Server socket
			socket = new Socket();
			if (StringUtils.isNotBlank(localIp)) {
				// 绑定本地地址
				socket.bind(new InetSocketAddress(localIp, localPort));
			}
			// set 超时时间
			socket.setSoTimeout(timeout);
			// 连接
			socket.connect(new InetSocketAddress(serverIp, serverPort));
			log.debug("connect to "+serverIp+":"+serverPort+" t="+timeout);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.TCPErr);
		}
		try {
			// 登录网管
			login();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZtlException(ErrorConst.Tl1ServerLoginErr);
		}
	}

	/**
	 * close session
	 * 
	 */
	public void close() {
		// logout
		try {
			logout();
		} catch (Exception e) {
			log.error(serverIp + " session logout failed", e);
		}
		// close socket
		if (socket != null) {
			try {
				socket.close();
				log.debug(serverIp + " session socket closed");
			} catch (Exception e) {
				log.error(serverIp + " session socket close failed", e);
			}
			socket = null;
		}
	}

	/**
	 * login
	 * 
	 * @throws ZtlException
	 */
	public abstract void login() throws Exception;

	/**
	 * logou
	 * 
	 * @throws ZtlException
	 */
	public abstract void logout() throws Exception;

	/**
	 * 执行TL1命令，返回TL1ResponseMessage
	 * 
	 * @param cmd
	 * @return
	 * @throws IOException
	 */
	public synchronized TL1ResponseMessage exeCmd(String cmd)
			throws ZtlException {
		try {
			log.debug("cmd=" + cmd);
			String remsgText = "";
			// 发送命令
			Writer write = new OutputStreamWriter(socket.getOutputStream());
			write.write(cmd);
			write.flush();
			// 返回消息
			remsgText = this.read(endString);

			while (TL1ResponseMessage.isAutonomousMessage(remsgText)) {
				// 如果是通知消息，取下一个返回消息
				// 读取下一个返回消息块
				remsgText = this.read(endString);
				// 通知消息的处理，目前没有
			}
			// 不是通知消息,解析为TL1ResponseMessage
			TL1ResponseMessage msg = TL1ResponseMessage.parse(remsgText);
			return msg;
		}catch (SocketTimeoutException e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.TCPIOErr);
		} catch (ZtlException e) {
			log.error(e.toString(), e);
			throw e;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.TCPIOErr);
		} catch (Exception e) {
			log.error(e.toString(), e);
			throw new ZtlException(ErrorConst.UnknowError);
		}
	}

	/**
	 * 从设备读入命令串
	 * 
	 * @param endFlag
	 *            终止标志字符
	 * @return 已经读入的字符串
	 * @throws IOException 遇到IO错误由上级方法捕捉
	 */
	private String read(String endFlag) {
		try{
			int length = 0;
			int DATA_SEG_LEN = 2 * 1024;
			byte byteBuffer[] = new byte[DATA_SEG_LEN];
			StringWriter swr = new StringWriter();
			String tmps = null;
	
			while ((length = socket.getInputStream().read(byteBuffer, 0,
					DATA_SEG_LEN)) != -1) {
				//debug
	//			debugBytes(byteBuffer);
				//
				tmps = new String(byteBuffer, 0, length);
				// log.debug(tmps);
				swr.write(tmps.toCharArray(), 0, tmps.toCharArray().length);
				if (tmps.indexOf(endFlag) != -1) {
					break;
				}
			}

			log.debug("reMsg=" + swr.toString());
			return swr.toString();
		} catch (IOException e) {
			log.error(e.toString(), e);
			try {
				throw new ZtlException(ErrorConst.TCPIOErr);
			} catch (ZtlException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return e.toString();
		}
	}
	
	/**
	 * 打印byte数组
	 * @param byteBuffer
	 */
	private void debugBytes(byte [] byteBuffer){
		StringBuffer _byte=new StringBuffer();
		for (byte b : byteBuffer) {
			if(b != 0){
				_byte.append("["+Integer.toHexString(b)+"]");
			}
		}
		log.debug("Bytes="+_byte);
	}

	/**
	 * session is open?
	 * 
	 * @return
	 */
	public boolean isOpen() {
		// OReilly Java Network Programming 3rd Edition 提供的方法
		boolean connected = socket.isConnected() && !socket.isClosed();
		return connected;
	}

	/**
	 * get ServerIp
	 * 
	 * @return
	 */
	public String getServerIp() {
		return serverIp;
	}

	/**
	 * get ServerPort
	 * 
	 * @return
	 */
	public int getServerPort() {
		return serverPort;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

}